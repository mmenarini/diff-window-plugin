package edu.ucsd.diff;

import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.event.EditorEventMulticaster;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import edu.ucsd.AppState;
import edu.ucsd.FileReader;
import edu.ucsd.factory.PanelFactory;
import edu.ucsd.getty.GettyInvariantsFilesRetriever;
import edu.ucsd.idea.MyCaretListener;
import io.reactivex.disposables.Disposable;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.PropertyConfigurator;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class DiffToolWindowFactory implements ToolWindowFactory {
    private DiffWindow diffWindow;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

//        TODO: move to file that will handle retrieving invariant files
        Disposable classMethodObservable = AppState.getCurrentClassMethodObservable()
                .subscribe(classMethod -> log.warn("class method observed: class {} method {}", classMethod.getClassName(), classMethod.getMethodName()));


//        TODO: move to config class
        PropertyConfigurator.configure(this.getClass().getClassLoader().getResource("log4j.properties"));


        List<DiffTab> tabsList = new ArrayList<>();
        GettyInvariantsFilesRetriever gettyInvariantsFilesRetriever = new GettyInvariantsFilesRetriever(
                new File("/Users/sander/Git/research/se/dsproj.__getty_output__"));

        Optional<List<File>> filesOptional = gettyInvariantsFilesRetriever.getFiles("GStack", "pop");

        PanelFactory panelFactory = new PanelFactory(project);

        if (filesOptional.isPresent()) {
            List<File> files = filesOptional.get();
            files.forEach(f -> log.info("File: {}", f.getName()));
            log.info("Total number of files is {}", files.size());

            Optional<String> pre = FileReader.readFileAsString(files.get(0));
            Optional<String> post = FileReader.readFileAsString(files.get(1));

            if (pre.isPresent() && post.isPresent()) {
                tabsList.add(new DiffTab("GStack", pre.get(), post.get(), panelFactory));
            } else {
                log.warn("Pre or post was empty");
            }
        }

        tabsList.add(new DiffTab("Test tab", "pre\ncontent", "post\ncontent\nnew line", panelFactory));
        tabsList.add(new DiffTab("Test tab 2", "", "", panelFactory));
        this.diffWindow = new DiffWindow(toolWindow, tabsList);


//        listener
        CaretListener caretListener = new MyCaretListener();

        EditorEventMulticaster eventMulticaster = EditorFactory.getInstance().getEventMulticaster();
        eventMulticaster.addCaretListener(caretListener);

        log.warn("created caret listener");


    }
}
