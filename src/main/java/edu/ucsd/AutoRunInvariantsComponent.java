package edu.ucsd;

import com.intellij.AppTopics;
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
import edu.ucsd.getty.GettyRunner;
import edu.ucsd.properties.Properties;
import edu.ucsd.properties.PropertiesService;
import io.reactivex.disposables.Disposable;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
@Slf4j
public class AutoRunInvariantsComponent implements ProjectComponent, PsiTreeChangeListener {
    private PropertiesService propertiesService = PropertiesService.getInstance();
    private ExecutorService execService = Executors.newFixedThreadPool(1);
    private GettyRunner gettyRunner;
    private Project project;
    private PsiDocumentManager psiDocumentManager;
    private PsiManager psiManager;
    private Disposable propertiesSubscription;
    private Properties properties;
    public AutoRunInvariantsComponent(Project project){
        propertiesSubscription = propertiesService.getPropertiesObservable()
                .subscribe(p -> this.properties = p);
        this.project=project;
        this.psiDocumentManager = PsiDocumentManager.getInstance(project);
        this.psiManager = PsiManager.getInstance(project);
        String projectPath = project.getBasePath();
        gettyRunner = new GettyRunner(
                project,
                projectPath,
                properties.isDebugLog(), properties.isStackTrace(), properties.isCleanBeforeRunning());
    }

    public void runGetty() {
        //We rerun the inference if the cared is in one method
        execService.submit(() -> {
            try {
                if (AppState.method!=null && AppState.method.getMethodSignature()!=null)
                    gettyRunner.run(AppState.method);
            } catch (IOException e) {
                log.error("Getty failed:", e);
            }
        });
    }
    @Override
    public void projectOpened() {
        GettyRunner.cloneRepository(project.getBasePath());
        psiManager.addPsiTreeChangeListener(this);
        //project.getMessageBus().connect().subscribe(AppTopics.FILE_DOCUMENT_SYNC,this);
//        CompilerManager.getInstance(project).addAfterTask(context -> {
//            if(context.getMessageCount(CompilerMessageCategory.ERROR)==0){
//                //We rerun the inference if the cared is in one method
//                runGetty();
//            }
//            return true;
//        });
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
        PsiFile file = event.getFile();
        if (!PsiTreeUtil.hasErrorElements(file)) {
            if (AppState.method!=null && AppState.method.declaringFile==file) {
                Document doc = psiDocumentManager.getDocument(file);
                psiDocumentManager.commitAllDocuments();
                psiDocumentManager.performWhenAllCommitted(() -> runGetty());
            }
        }
    }

    @Override
    public void childMoved(@NotNull PsiTreeChangeEvent event) {

    }

    @Override
    public void propertyChanged(@NotNull PsiTreeChangeEvent event) {

    }
}
