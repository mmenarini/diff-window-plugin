package edu.ucsd.diff;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import edu.ucsd.AppState;
import edu.ucsd.ClassMethod;
import edu.ucsd.FileReader;
import edu.ucsd.factory.PanelFactory;
import edu.ucsd.getty.GettyConstants;
import edu.ucsd.getty.GettyInvariantsFilesRetriever;
import edu.ucsd.getty.GettyRunner;
import edu.ucsd.properties.Properties;
import edu.ucsd.properties.PropertiesService;
import io.reactivex.disposables.Disposable;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class DiffWindowContentManager {
    private PropertiesService propertiesService = PropertiesService.getInstance();
    private ExecutorService execService = Executors.newFixedThreadPool(1);
    private GettyRunner gettyRunner;
    private ToolWindow toolWindow;
    private GettyInvariantsFilesRetriever gettyInvariantsFilesRetriever;
    private PanelFactory panelFactory;
    private DiffWindow diffWindow;
    private Disposable classMethodObservable;
    private ClassMethod previousClassMethod;
    private Disposable propertiesSubscription;
    private Properties properties;

    public DiffWindowContentManager(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        propertiesSubscription = propertiesService.getPropertiesObservable()
                .subscribe(p -> this.properties = p);
        String projectPath = project.getBasePath();
        gettyRunner = new GettyRunner(
                project,
                projectPath,
                properties.isDebugLog(), properties.isStackTrace(), properties.isCleanBeforeRunning());

        this.toolWindow = toolWindow;
        GettyConstants gettyConstants = new GettyConstants(project);

        this.gettyInvariantsFilesRetriever = new GettyInvariantsFilesRetriever(new File(gettyConstants.OUTPUT_DIR), project);

        this.panelFactory = new PanelFactory(project);

        this.classMethodObservable = initClassMethodSubscription();

    }

    private Disposable initClassMethodSubscription() {
        return AppState.getCurrentClassMethodObservable()
                .subscribe(this::handleClassMethodChanged);
    }

    private void handleClassMethodChangedOnDispatch(ClassMethod newClassMethod) {
        log.warn("class method observed: class {} method {} parameterTypes {}",
                newClassMethod.getClassName(), newClassMethod.getMethodName(), newClassMethod.getParameterTypes());

            //if (previousClassMethod != null && previousClassMethod.equals(newClassMethod)) return;

            final List<DiffTab> tabsList = new ArrayList<>();


            Optional<List<File>> filesOptional = gettyInvariantsFilesRetriever
                    .getFiles(newClassMethod);//.getClassName(), newClassMethod.getMethodName(), newClassMethod.getParameterTypes());

            if (filesOptional.isPresent()) {
                tabsList.addAll(initTabsList(filesOptional.get(), newClassMethod));
            } else {
//            TODO: no invariant files, display message that user should execute re-infer action
                log.warn("filesOptional was empty");
            }

            if (tabsList.size() == 0) {
                tabsList.add(new DiffTab("", "", "", panelFactory));
            }

            this.previousClassMethod = newClassMethod;

            ApplicationManager.getApplication().invokeLater(() -> {
                if (this.diffWindow != null) this.diffWindow.removeSelfFromToolWindow();
                this.diffWindow = new DiffWindow(toolWindow, tabsList);
            });
    }

        private void handleClassMethodChanged(ClassMethod newClassMethod) {
        try {
            ApplicationManager.getApplication().invokeLater(new Runnable() {
                @Override
                public void run() {
                    handleClassMethodChangedOnDispatch(newClassMethod);
                }
            });
        } catch(Throwable ex) {
            log.error("Exception while processing handleClassMethodChanged", ex);
        }
    }

    private List<DiffTab> initTabsList(List<File> files, ClassMethod newClassMethod) {
        List<DiffTab> tabsList = new ArrayList<>();

        if (files.size() != 2) {
            files.forEach(f -> log.warn("File: {}", f.getName()));
            log.error("Total number of files was {} instead of 2", files.size());
        } else {
//            TODO: base pre/post on hash instead of index
            if (!(files.get(0).exists() && files.get(1).exists()))
                execService.submit(() -> {
                   // try {
                        //TODO: Reenable when performance issue on MAC solved
                        //gettyRunner.run(newClassMethod);
                   // } catch (IOException e) {
                   //     log.error("Getty failed:", e);
                   // }
                });

            Optional<String> pre = FileReader.readFileAsString(files.get(0));
            Optional<String> post = FileReader.readFileAsString(files.get(1));

            if (pre.isPresent() && post.isPresent()) {
                tabsList.add(new DiffTab(String.format("%s - %s", newClassMethod.getClassName(),
                        newClassMethod.getMethodName()), pre.get(), post.get(), panelFactory));
            } else {
                log.warn("Pre or post was empty");
            }
        }

        return tabsList;
    }
}
