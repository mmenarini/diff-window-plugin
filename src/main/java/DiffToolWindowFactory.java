import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DiffToolWindowFactory implements ToolWindowFactory {
    private DiffWindow diffWindow;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        List<DiffTab> tabsList = new ArrayList<>();
        tabsList.add(new DiffTab("Test tab", "pre\ncontent", "post\ncontent\nnew line", new PanelFactory(project)));
        tabsList.add(new DiffTab("Test tab 2", "", "", new PanelFactory(project)));
        this.diffWindow = new DiffWindow(toolWindow, tabsList);
    }
}
