package edu.ucsd.diff;

import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.event.EditorEventMulticaster;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import edu.ucsd.idea.CaretPositionListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.PropertyConfigurator;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class DiffToolWindowFactory implements ToolWindowFactory {
    DiffWindowContentManager diffWindowContentManager;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
//        TODO: move to config class
        PropertyConfigurator.configure(this.getClass().getClassLoader().getResource("log4j.properties"));

        diffWindowContentManager = new DiffWindowContentManager(project, toolWindow);

//        listener
        CaretListener caretListener = new CaretPositionListener();

        EditorEventMulticaster eventMulticaster = EditorFactory.getInstance().getEventMulticaster();
        eventMulticaster.addCaretListener(caretListener);

        log.warn("created caret listener");

        log.warn("Project base path {}", project.getBasePath());


    }
}
