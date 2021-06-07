package edu.ucsd.reinfer;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import edu.ucsd.GettyRunNotifier;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KillInferAction extends AnAction {

    public KillInferAction() {
        super("killInfer");
    }

    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        if (project != null) {
            project.getMessageBus().syncPublisher(GettyRunNotifier.GETTY_RUN_NOTIFIER_TOPIC).forceStop();
        }
    }
}