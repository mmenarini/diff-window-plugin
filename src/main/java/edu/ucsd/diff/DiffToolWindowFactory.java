package edu.ucsd.diff;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class DiffToolWindowFactory implements ToolWindowFactory {
    DiffWindowContentManager diffWindowContentManager;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        diffWindowContentManager = new DiffWindowContentManager(project, toolWindow);
        log.warn("Project base path {}", project.getBasePath());
    }
}
