package edu.ucsd.properties;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class PropertiesAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        PropertiesService.getInstance().showSetPropertiesDialog();
    }
}
