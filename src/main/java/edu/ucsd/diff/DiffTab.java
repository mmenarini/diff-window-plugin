package edu.ucsd.diff;

import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffRequestPanel;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.diff.util.DiffUserDataKeys;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.JBUI;
import edu.ucsd.factory.PanelFactory;

import javax.swing.*;
import java.awt.*;

public class DiffTab {
    private String title;
    private Content content;

    private PanelFactory panelFactory;

    public DiffTab(String title, String diffOld, String diffNew, PanelFactory panelFactory) {
        this.title = title;
        this.panelFactory = panelFactory;
        this.updateTab(diffOld, diffNew);
    }

    public void updateTab(String diffOld, String diffNew) {
//        create diff
        SimpleDiffRequest diffRequest = createDiffRequest(diffOld, diffNew);
        Disposable disposable = Disposer.newDisposable();
        DiffRequestPanel diffPanel = createDiffPanel(diffRequest, disposable);
        JPanel panel = createPanel(diffPanel);

//        add actions
        JPanel toolWindowActionsPanel = createToolWindowActionsPanel(getActionGroun());
        panel.add(toolWindowActionsPanel, BorderLayout.WEST);

//        create new content for tab
        if (this.content != null) {
            this.content.dispose();
        }
        this.content = ContentFactory.SERVICE.getInstance().
                createContent(panel, this.title, false);
        content.setDisposer(disposable);
    }

    private DefaultActionGroup getActionGroun() {
        final AnAction action = ActionManager.getInstance().getAction("DiffWindow.ReInfer");
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(action);
        return actionGroup;
    }


    private JPanel createToolWindowActionsPanel(DefaultActionGroup actionGroup) {
        JPanel toolbarPanel = new JPanel();
        ActionManager actionManager = ActionManager.getInstance();
        ActionToolbar leftToolbar = actionManager.createActionToolbar(ActionPlaces.DIFF_TOOLBAR, actionGroup, false);
        toolbarPanel.add(leftToolbar.getComponent(), BorderLayout.WEST);
        return toolbarPanel;
    }

    private DiffRequestPanel createDiffPanel(SimpleDiffRequest diffRequest, Disposable disposable) {
        DiffRequestPanel diffPanel = panelFactory.createDiffRequestPanel(disposable);
        diffPanel.putContextHints(DiffUserDataKeys.PLACE, "ExtractSignature");
        diffPanel.setRequest(diffRequest);
        return diffPanel;
    }

    private JPanel createPanel(DiffRequestPanel diffPanel) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(diffPanel.getComponent(), BorderLayout.CENTER);
        panel.setBorder(IdeBorderFactory.createEmptyBorder(JBUI.insetsTop(5)));
        return panel;
    }

    private SimpleDiffRequest createDiffRequest(String diffOld, String diffNew) {
        DiffContentFactory contentFactory = DiffContentFactory.getInstance();

        DocumentContent oldContent = contentFactory.create(diffOld);
        DocumentContent newContent = contentFactory.create(diffNew);

        return new SimpleDiffRequest(title, oldContent, newContent, "Before", "After");
    }

    public Content getContent() {
        return this.content;
    }

    public String getTitle() {
        return title;
    }
}
