package edu.ucsd;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.GuiUtils;
import com.intellij.util.messages.MessageBus;
import edu.ucsd.getty.GettyRunner;
import edu.ucsd.idea.CaretPositionToMethod;
import edu.ucsd.properties.Properties;
import edu.ucsd.properties.PropertiesService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.xml.soap.Text;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


@Slf4j
public class AutoRunInvariantsComponent implements ProjectComponent, PsiTreeChangeListener, Disposable {
    private final CaretListener myCaretMethodListener;
    private final CaretPositionToMethod caretToMethod;
    private PropertiesService propertiesService = PropertiesService.getInstance();
    private ScheduledExecutorService execService;
    private ScheduledFuture scheduledFuture;
    public AppState appState;
    private Project project;
    private PsiDocumentManager psiDocumentManager;
    private PsiManager psiManager;
    private io.reactivex.disposables.Disposable propertiesSubscription;
    private Properties properties;
    private MyEditorManagerListener myEditorManagerListener;
    private boolean _isDisposed = false;
    private GettyRunNotifier gettyRunPublisher;

    private final class MyEditorManagerListener implements FileEditorManagerListener {
        @Override
        public void selectionChanged(@NotNull FileEditorManagerEvent event) {
            FileEditor newEditor = event.getNewEditor();
            FileEditor oldEditor = event.getOldEditor();
            if (oldEditor instanceof TextEditor)
                ((TextEditor) oldEditor).getEditor().getCaretModel().removeCaretListener(myCaretMethodListener);
            if (newEditor instanceof TextEditor)
                ((TextEditor) newEditor).getEditor().getCaretModel().addCaretListener(myCaretMethodListener);
        }
    }
    public AutoRunInvariantsComponent(Project project){
        propertiesSubscription = propertiesService.getPropertiesObservable()
                .subscribe(p -> this.properties = p);
        myEditorManagerListener= new MyEditorManagerListener();
        this.project=project;
        this.psiDocumentManager = PsiDocumentManager.getInstance(project);
        this.psiManager = PsiManager.getInstance(project);
        String projectPath = project.getBasePath();
        appState = new AppState(project);
        caretToMethod = new CaretPositionToMethod();

        MessageBus messageBus = project.getMessageBus();
        messageBus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, myEditorManagerListener);
        gettyRunPublisher = messageBus.syncPublisher(GettyRunNotifier.GETTY_RUN_NOTIFIER_TOPIC);

        myCaretMethodListener= new CaretListener() {
            @Override
            public void caretPositionChanged(@NotNull CaretEvent e) {
                ClassMethod method = caretToMethod.caretPositionChanged(e);
                if ((method!=null) && !method.equals(appState.method)){
                    appState.setCurrentClassMethod(method);
                }
            }
        };
    }

    @Override
    public void projectOpened() {
        execService = Executors.newSingleThreadScheduledExecutor();
        psiManager.addPsiTreeChangeListener(this);

    }

    @Override
    public void projectClosed() {
        execService.shutdown();
        psiManager.removePsiTreeChangeListener(this);
    }


    @Override
    public void beforeChildAddition(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void beforeChildRemoval(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void beforeChildReplacement(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void beforeChildMovement(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void beforeChildrenChange(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void beforePropertyChange(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void childAdded(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void childRemoved(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void childReplaced(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void childrenChanged(@NotNull PsiTreeChangeEvent event) {
//        if (scheduledInferenceTask!=null) scheduledInferenceTask.cancel();
        if (scheduledFuture!=null) scheduledFuture.cancel(false);
        if (properties.isDoNotAutorun()) return;
        PsiFile file = event.getFile();
        if (!PsiTreeUtil.hasErrorElements(file)) {
            scheduledFuture = execService.schedule(new Runnable() {
                @Override
                public void run() {
                        GuiUtils.invokeLaterIfNeeded(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    psiDocumentManager.commitAllDocuments();
                                    psiDocumentManager.performWhenAllCommitted(() -> gettyRunPublisher.run(appState.method, true));
                                } catch (Exception ex) {
                                    log.error("Error while committing docs",ex);
                                }
                            }
                        }, ModalityState.NON_MODAL);
                }
            },5L, TimeUnit.SECONDS);
        }
    }

    @Override
    public void childMoved(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void propertyChanged(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void dispose() {
        _isDisposed = true;
        propertiesSubscription.dispose();
    }


}
