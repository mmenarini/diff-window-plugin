package edu.ucsd;

import com.intellij.AppTopics;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.ui.GuiUtils;
import edu.ucsd.getty.GettyRunner;
import edu.ucsd.properties.Properties;
import edu.ucsd.properties.PropertiesService;
import io.reactivex.disposables.Disposable;
import javafx.application.Application;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

@Slf4j
public class AutoRunInvariantsComponent implements ProjectComponent, PsiTreeChangeListener {
    private PropertiesService propertiesService = PropertiesService.getInstance();
    private ScheduledExecutorService execService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture scheduledFuture;
    private GettyRunner gettyRunner;
    private Project project;
    private PsiDocumentManager psiDocumentManager;
    private PsiManager psiManager;
    private Disposable propertiesSubscription;
    private Properties properties;
//    private Timer scheduledInference;
//    private TimerTask scheduledInferenceTask;
    public AutoRunInvariantsComponent(Project project){
        //        scheduledInference = new Timer("AutomaticInference");
        propertiesSubscription = propertiesService.getPropertiesObservable()
                .subscribe(p -> this.properties = p);
        this.project=project;
        this.psiDocumentManager = PsiDocumentManager.getInstance(project);
        this.psiManager = PsiManager.getInstance(project);
        String projectPath = project.getBasePath();
        gettyRunner = new GettyRunner(project);
    }

    @Override
    public void projectOpened() {
        GettyRunner.cloneRepository(project.getBasePath());
        psiManager.addPsiTreeChangeListener(this);
    }

    @Override
    public void projectClosed() {
        execService.shutdown();
//        scheduledInference.cancel();
        propertiesSubscription.dispose();
        psiManager.removePsiTreeChangeListener(this);
        gettyRunner.dispose();
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
                                    psiDocumentManager.performWhenAllCommitted(() -> AppState.runGetty(gettyRunner, AppState.method));
                                } catch (Exception ex) {
                                    log.error("Error while committing docs",ex);
                                }
                            }
                        }, ModalityState.NON_MODAL);
                }
            },3L, TimeUnit.SECONDS);
        }
    }

    @Override
    public void childMoved(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void propertyChanged(@NotNull PsiTreeChangeEvent event) {

    }
}
