package edu.ucsd;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

public class ReInferAction extends AnAction {
    public ReInferAction() {
        super("reinfer");
    }

    public void actionPerformed(AnActionEvent event) {
        System.out.println("Re-inferring!!");
        Project project = event.getProject();
        Messages.showMessageDialog(project, "Re-inferring!", "Reinferr", Messages.getErrorIcon());
    }
}