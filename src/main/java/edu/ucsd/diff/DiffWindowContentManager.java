package edu.ucsd.diff;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.ui.UIUtil;
import edu.ucsd.*;
import edu.ucsd.factory.PanelFactory;
import edu.ucsd.getty.GettyConstants;
import edu.ucsd.getty.GettyInvariantsFilesRetriever;
import edu.ucsd.mmenarini.getty.GettyMainKt;
import edu.ucsd.properties.Properties;
import edu.ucsd.properties.PropertiesService;
import io.reactivex.disposables.Disposable;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class DiffWindowContentManager {
    private final GettyRunNotifier gettyRunPublisher;
    private Path repoHeadDir=null;
    private PropertiesService propertiesService = PropertiesService.getInstance();
    private ExecutorService execService = Executors.newFixedThreadPool(1);
    //private GettyRunner gettyRunner;
    private Project currentProject = null;
    private ToolWindow toolWindow;
    private GettyInvariantsFilesRetriever gettyInvariantsFilesRetriever;
    private PanelFactory panelFactory;
    private DiffWindow diffWindow;
    //private Disposable classMethodObservable;
    private ClassMethod shownClassMethod;
    private String HeadPanel ="";
    private String CurrentPanel ="";
    private Disposable propertiesSubscription;
    private Properties properties;

    //private GettyRunner gettyRunner;
    private AutoRunInvariantsComponent autoRunInvariantsComponent;

    //Notice that project can change (either open a new project in the same window or a multiproject config
    //We can manage to get the correct project form the ClassMethod passed to the classmethod changed event
    //So we should save the project we get and make sure that we call the event on the right tool window for the project
    public DiffWindowContentManager(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        propertiesSubscription = propertiesService.getPropertiesObservable()
                .subscribe(p -> this.properties = p);
        //gettyRunner = new GettyRunner(project);
        this.toolWindow = toolWindow;

        this.currentProject = project;
        GettyConstants gettyConstants = new GettyConstants(project);
        this.gettyInvariantsFilesRetriever = new GettyInvariantsFilesRetriever(new File(gettyConstants.OUTPUT_DIR), project);
        this.panelFactory = new PanelFactory(project);

        MessageBus messageBus = project.getMessageBus();
        gettyRunPublisher = messageBus.syncPublisher(GettyRunNotifier.GETTY_RUN_NOTIFIER_TOPIC);
        messageBus.connect().subscribe(MethodChangeNotifier.METHOD_CHANGE_NOTIFIER_TOPIC, new MethodChangeNotifier() {
            @Override
            public void newMethod(ClassMethod method) {
                handleClassMethodChanged(method);
            }
        });
        messageBus.connect().subscribe(GettyRunNotifier.GETTY_RUN_NOTIFIER_TOPIC, new GettyRunNotifier() {
            @Override
            public void run() {

            }

            @Override
            public void run(ClassMethod method, boolean stopIfRunning) {

            }

            @Override
            public void stop() {

            }

            @Override
            public void started(ClassMethod method) {

            }

            @Override
            public void cloning(String repo) {

            }

            @Override
            public void cloned(Path repoHead) {
                repoHeadDir=repoHead;
            }

            @Override
            public void error(String message) {
                messageDiffWindow(shownClassMethod, message,null);
            }

            @Override
            public void headInferenceStared(ClassMethod method) {
                HeadPanel="Inferring Invariants";
                updateUI();
            }

            @Override
            public void currentInferenceStarted(ClassMethod method) {
                CurrentPanel="Inferring Invariants";
                updateUI();
            }

            @Override
            public void headInferenceError(ClassMethod method, String message) {
                HeadPanel=message;
                updateUI();
            }

            @Override
            public void currentInferenceError(ClassMethod method, String message) {
                CurrentPanel=message;
                updateUI();
            }

            @Override
            public void headInferenceDone(ClassMethod method) {
                handleClassMethodChangedHead(method);
            }

            @Override
            public void currentInferenceDone(ClassMethod method) {
                handleClassMethodChangedCurrent(method);
            }

            @Override
            public void done(ClassMethod method) {

            }

            @Override
            public void stopped(ClassMethod method) {
                if(HeadPanel=="Inferring Invariants")
                    HeadPanel="Sopped while  inferring";
                if(CurrentPanel=="Inferring Invariants")
                    CurrentPanel="Sopped while  inferring";
            }
        });

        //this.classMethodObservable = initClassMethodSubscription();
        ProjectManager.getInstance().addProjectManagerListener(project, new ProjectManagerListener() {
            @Override
            public void projectClosing(@NotNull Project project) {
                //classMethodObservable.dispose();
                propertiesSubscription.dispose();
            }
        });
        Path gitPath = Paths.get(project.getBasePath());
        repoHeadDir = GettyMainKt.cloneGitHead(gitPath);
    }

    private void messageDiffWindow(ClassMethod method, String message, String message2){
        UIUtil.invokeLaterIfNeeded(() -> {
            List<DiffTab> tabsList = new ArrayList<>();
            tabsList.add(new DiffTab(String.format("%s - %s", method.getClassName(),
                    method.getMethodName()), message, message2, panelFactory));
            if (this.diffWindow != null)
                this.diffWindow.removeSelfFromToolWindow();
            this.diffWindow = new DiffWindow(toolWindow, tabsList);
        });
    }

    private void handleClassMethodChangedHead(ClassMethod newClassMethod) {
        if(repoHeadDir==null)
            return;
        File file = gettyInvariantsFilesRetriever.getFileHead(newClassMethod, repoHeadDir);
        Optional<String> str = FileReader.readFileAsString(file);
        if(str.isPresent()) {
            HeadPanel= str.get();
        } else {
            HeadPanel = "Computing...";
            gettyRunPublisher.run(newClassMethod, false);
        }
        updateUI();
    }

    private void handleClassMethodChangedCurrent(ClassMethod newClassMethod) {
        File file = gettyInvariantsFilesRetriever.getFileCurrent(newClassMethod);
        Optional<String> str = FileReader.readFileAsString(file);
        if(str.isPresent()) {
            CurrentPanel= str.get();
        } else {
            CurrentPanel = "Computing...";
            gettyRunPublisher.run(newClassMethod, false);
        }
        updateUI();
    }

    private void updateUI() {
        messageDiffWindow(shownClassMethod, HeadPanel, CurrentPanel);
    }

    private void handleClassMethodChanged(ClassMethod newClassMethod) {
        try {
            if (currentProject != newClassMethod.getDeclaringFile().getProject()) {
                log.warn("Class method received but on different project");
                return;
            }
            if (newClassMethod.getDeclaringFile().getProject().isDisposed()) {
                log.warn("Class method received but corresponding project is disposed");
                return;
            }
            log.info("class method observed: class {} method {} parameterTypes {}",
                    newClassMethod.getClassName(), newClassMethod.getMethodName(), newClassMethod.getParameterTypes());
            shownClassMethod = newClassMethod;
            handleClassMethodChangedHead(newClassMethod);
            handleClassMethodChangedCurrent(newClassMethod);
        } catch (Throwable ex) {
            log.error("Exception while processing handleClassMethodChanged", ex);
        }
    }
}
