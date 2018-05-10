package edu.ucsd.reinfer;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import edu.ucsd.ClassMethod;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class ReInferAction extends AnAction {
    private ReInferPriority reInferPriority = ReInferPriority.getInstance();

    public ReInferAction() {
        super("reinfer");
    }

    public void actionPerformed(AnActionEvent event) {
        log.warn("Reinferring {} size {}", reInferPriority.getPriorityList().toArray(), reInferPriority.getPriorityList().size());
        Optional<File> file = ReInferPriorityFileWriter.write(reInferPriority);

//        if file present run Getty
    }
}