import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffManager;
import com.intellij.diff.DiffRequestPanel;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.diff.util.DiffUserDataKeys;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class MyToolWindowFactory implements ToolWindowFactory {
    private ToolWindow myToolWindow;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        myToolWindow = toolWindow;

        JPanel panel = new JPanel(new BorderLayout());

        DiffContentFactory contentFactory = DiffContentFactory.getInstance();

        DocumentContent oldContent = contentFactory.create("pre\ncontent");
        DocumentContent newContent = contentFactory.create("post\ncontent\nnew line");

        SimpleDiffRequest request = new SimpleDiffRequest("Diff", oldContent, newContent, "Before", "After");


//        need to manage the disposables creation and destroy
        Disposable myDis = Disposer.newDisposable();
        DiffRequestPanel diffPanel = DiffManager.getInstance().createRequestPanel(project, myDis,null);

        diffPanel.putContextHints(DiffUserDataKeys.PLACE, "ExtractSignature");
        diffPanel.setRequest(request);

        panel.add(diffPanel.getComponent(), BorderLayout.CENTER);
        panel.setBorder(IdeBorderFactory.createEmptyBorder(JBUI.insetsTop(5)));


        Content content = ContentFactory.SERVICE.getInstance().
                createContent(panel, "My Title", false);
        myToolWindow.getContentManager().addContent(content);
    }
}
