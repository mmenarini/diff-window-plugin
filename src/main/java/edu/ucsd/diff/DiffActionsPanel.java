package edu.ucsd.diff;

import com.intellij.openapi.actionSystem.*;

import javax.swing.*;
import java.awt.*;

public class DiffActionsPanel {
    public static final JPanel ACTIONS_PANEL = createToolWindowActionsPanel(getActionGroup());

    private static DefaultActionGroup getActionGroup() {
        final AnAction action = ActionManager.getInstance().getAction("DiffWindow.ReInfer");
        final AnAction actionKill = ActionManager.getInstance().getAction("DiffWindow.KillInfer");

        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(action);
        actionGroup.add(actionKill);
        return actionGroup;
    }


    private static JPanel createToolWindowActionsPanel(DefaultActionGroup actionGroup) {
        JPanel toolbarPanel = new JPanel();
        ActionManager actionManager = ActionManager.getInstance();
        ActionToolbar leftToolbar = actionManager.createActionToolbar(ActionPlaces.DIFF_TOOLBAR, actionGroup, false);
        toolbarPanel.add(leftToolbar.getComponent(), BorderLayout.WEST);
        return toolbarPanel;
    }
}
