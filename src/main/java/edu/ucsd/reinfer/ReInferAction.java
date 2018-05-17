package edu.ucsd.reinfer;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import edu.ucsd.getty.GettyConstants;
import edu.ucsd.getty.GettyRunner;
import edu.ucsd.git.GitAdapter;
import edu.ucsd.properties.Properties;
import edu.ucsd.properties.PropertiesService;
import edu.ucsd.rsync.RsyncAdapter;
import io.reactivex.disposables.Disposable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static edu.ucsd.getty.GettyConstants.GETTY_COMMIT_MESSAGE;

@Slf4j
public class ReInferAction extends AnAction {
    private ReInferPriority reInferPriority = ReInferPriority.getInstance();
    private PropertiesService propertiesService = PropertiesService.getInstance();
    private ExecutorService execService = Executors.newFixedThreadPool(1);


    private Disposable propertiesSubscription;
    private Properties properties;
    private GettyRunner gettyRunner;
    private RsyncAdapter rsyncAdapter;
    private GitAdapter gitAdapter;

    public ReInferAction() {
        super("reinfer");
        propertiesSubscription = propertiesService.getPropertiesObservable()
                .subscribe(p -> this.properties = p);
    }

    public void actionPerformed(AnActionEvent event) {
        log.warn("Reinferring {} size {}", reInferPriority.getPriorityList().toArray(), reInferPriority.getPriorityList().size());

        if (properties == null || properties.isRequiredFieldEmpty()) {
            propertiesService.showSetPropertiesDialog();
        }

        Project project = event.getProject();
        if (project != null) {
            String basePath = StringUtils.defaultIfEmpty(project.getBasePath(), "");
            Optional<File> priorityFileOptional = ReInferPriorityFileWriter.write(basePath, reInferPriority);


//            if file present run Getty
            priorityFileOptional.ifPresent(f -> {
                log.warn("file path {}", f.getAbsolutePath());
                reInfer(project, f);
            });

        }

    }

    private void reInfer(Project project, File priorityFile) {
        execService.submit(() -> {
            String projectPath = project.getBasePath();

            GettyConstants gettyConstants = new GettyConstants(project);


//            RSYNC

            rsyncAdapter = new RsyncAdapter(projectPath + "/", gettyConstants.SOURCE_DIR);

            try {
                rsyncAdapter.sync();
            } catch (Exception e) {
                log.error("Rsync failed:", e);
                return;
            }


//            GIT

            try {
                gitAdapter = new GitAdapter(gettyConstants.SOURCE_DIR);
            } catch (IOException e) {
                log.error("Failed to initialize git repo", e);
                return;
            }

            gitAdapter.commitAllChanges(GETTY_COMMIT_MESSAGE);

            String hashOfHead = "";
            String hashOfParent = "";
            try {
                hashOfHead = gitAdapter.getHashOfHead();
                hashOfParent = gitAdapter.getHashOfFirstParent();
            } catch (IOException e) {
                log.error("Failed to get commit hash", e);
                return;
            }


//            GETTY

            gettyRunner = new GettyRunner(gettyConstants.SOURCE_DIR, properties.getGettyPath(), properties.getPythonPath());
            try {
//                TODO: move priority file to gettyConstants?
                gettyRunner.run(hashOfParent, hashOfHead, priorityFile.getAbsolutePath());
            } catch (IOException e) {
                log.error("Getty failed:", e);
            }

            //        todo: set app state to not currently re-inferring
        });

//        todo: set app state to re-inferring

    }
}