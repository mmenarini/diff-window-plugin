package edu.ucsd.reinfer;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import edu.ucsd.getty.GettyRunner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Slf4j
public class ReInferAction extends AnAction {
    private ReInferPriority reInferPriority = ReInferPriority.getInstance();
    private GettyRunner gettyRunner = new GettyRunner("test");

    public ReInferAction() {
        super("reinfer");
    }

    public void actionPerformed(AnActionEvent event) {
        log.warn("Reinferring {} size {}", reInferPriority.getPriorityList().toArray(), reInferPriority.getPriorityList().size());
        Project project = event.getProject();
        if (project != null) {
            String basePath = StringUtils.defaultIfEmpty(project.getBasePath(), "");
            Optional<File> file = ReInferPriorityFileWriter.write(basePath, reInferPriority);


//            if file present run Getty
            file.ifPresent(f -> {
                log.warn("file path {}", f.getAbsolutePath());

                try {
//                    TODO: commit hashes
                    gettyRunner.run("19f4281", "a562db1", f.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });

        }

    }
}