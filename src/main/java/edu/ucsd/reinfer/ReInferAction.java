package edu.ucsd.reinfer;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Optional;

@Slf4j
public class ReInferAction extends AnAction {
    private ReInferPriority reInferPriority = ReInferPriority.getInstance();

    public ReInferAction() {
        super("reinfer");
    }

    public void actionPerformed(AnActionEvent event) {
        log.warn("Reinferring {} size {}", reInferPriority.getPriorityList().toArray(), reInferPriority.getPriorityList().size());
        Project project = event.getProject();
        if (project != null) {
            String basePath = StringUtils.defaultIfEmpty(project.getBasePath(), "");
            Optional<File> file = ReInferPriorityFileWriter.write(basePath, reInferPriority);
            file.ifPresent(f -> log.warn("file path {}", f.getAbsolutePath()));

            //        if file present run Getty
        }

    }
}