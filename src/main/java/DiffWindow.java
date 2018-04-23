import com.intellij.openapi.wm.ToolWindow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiffWindow {
    private ToolWindow toolWindow;
    private Map<String, DiffTab> tabsMap;

    public DiffWindow(ToolWindow toolWindow, List<DiffTab> tabsList) {
        this.toolWindow = toolWindow;
        this.tabsMap = new HashMap<>();
        tabsList.forEach(t -> tabsMap.put(t.getTitle(), t));
    }

    public void updateTab(String title, String diffOld, String diffNew) {
        DiffTab diffTab = this.tabsMap.get(title);
        if (diffTab != null) {
            diffTab.updateTab(diffOld, diffNew);
            this.toolWindow.getContentManager().addContent(diffTab.getContent());
        } else {
            System.out.println(String.format("No tab found with title \"%s\".", title));
        }
    }
}
