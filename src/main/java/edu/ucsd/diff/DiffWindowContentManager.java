package edu.ucsd.diff;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import edu.ucsd.AppState;
import edu.ucsd.ClassMethod;
import edu.ucsd.FileReader;
import edu.ucsd.factory.PanelFactory;
import edu.ucsd.getty.GettyInvariantsFilesRetriever;
import io.reactivex.disposables.Disposable;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class DiffWindowContentManager {
    private static final String GETTY_OUTPUT_DIRECTORY_POST_FIX = ".__getty_output__";

    private ToolWindow toolWindow;
    private GettyInvariantsFilesRetriever gettyInvariantsFilesRetriever;
    private PanelFactory panelFactory;
    private DiffWindow diffWindow;
    private Disposable classMethodObservable;
    private ClassMethod previousClassMethod;

    public DiffWindowContentManager(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        this.toolWindow = toolWindow;

        this.gettyInvariantsFilesRetriever = new GettyInvariantsFilesRetriever(
                new File(String.format("%s%s", project.getBasePath(), GETTY_OUTPUT_DIRECTORY_POST_FIX))
        );

        this.panelFactory = new PanelFactory(project);

        this.classMethodObservable = initClassMethodSubscription();

    }

    private Disposable initClassMethodSubscription() {
        return AppState.getCurrentClassMethodObservable()
                .subscribe(this::handleClassMethodChanged);
    }

    private void handleClassMethodChanged(ClassMethod newClassMethod) {
        log.warn("class method observed: class {} method {} parameterTypes {}",
                newClassMethod.getClassName(), newClassMethod.getMethodName(), newClassMethod.getParameterTypes());

        if (previousClassMethod != null && previousClassMethod.equals(newClassMethod)) return;

        if (this.diffWindow != null) this.diffWindow.removeSelfFromToolWindow();

        List<DiffTab> tabsList = new ArrayList<>();

        Optional<List<File>> filesOptional = gettyInvariantsFilesRetriever
                .getFiles(newClassMethod.getClassName(), newClassMethod.getMethodName(), newClassMethod.getParameterTypes());

        if (filesOptional.isPresent()) {
            tabsList = initTabsList(filesOptional.get(), newClassMethod);
        } else {
            log.warn("filesOptional was empty");
        }

        this.diffWindow = new DiffWindow(toolWindow, tabsList);

        this.previousClassMethod = newClassMethod;
    }

    private List<DiffTab> initTabsList(List<File> files, ClassMethod newClassMethod) {
        List<DiffTab> tabsList = new ArrayList<>();

        if (files.size() != 2) {
            files.forEach(f -> log.warn("File: {}", f.getName()));
            log.error("Total number of files was {} instead of 2", files.size());
        } else {
//            TODO: base pre/post on hash instead of index
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
