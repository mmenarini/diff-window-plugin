import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffManager;
import com.intellij.diff.DiffRequestPanel;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.diff.util.DiffUserDataKeys;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

public class DiffTab {
    //    has panel
//    has content
//    panel and content have a disposable
    private String title;
    private Content content;

    public DiffTab(String title, Content content) {
        this.title = title;
        this.content = content;
    }

    public void updateTab(String diffOld, String diffNew) {
//        creating panel for diff
        JPanel panel = new JPanel(new BorderLayout());

//        creating a diff
        DiffContentFactory contentFactory = DiffContentFactory.getInstance();

        DocumentContent oldContent = contentFactory.create("pre\ncontent");
        DocumentContent newContent = contentFactory.create("post\ncontent\nnew line");

        SimpleDiffRequest diffRequest = new SimpleDiffRequest("Diff", oldContent, newContent, "Before", "After");


//        need to manage the disposables creation and destroy
        Disposable myDis = Disposer.newDisposable();
        DiffRequestPanel diffPanel = DiffManager.getInstance().createRequestPanel(project, myDis,null);

        diffPanel.putContextHints(DiffUserDataKeys.PLACE, "ExtractSignature");
        diffPanel.setRequest(diffRequest);

//        add diff to panel
        panel.add(diffPanel.getComponent(), BorderLayout.CENTER);
        panel.setBorder(IdeBorderFactory.createEmptyBorder(JBUI.insetsTop(5)));


//        create new content
        this.content = ContentFactory.SERVICE.getInstance().
                createContent(panel, this.title, false);
    }

    public Content getContent() {
        return this.content;
    }

    public String getTitle() {
        return title;
    }
}
