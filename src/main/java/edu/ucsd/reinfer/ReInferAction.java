package edu.ucsd.reinfer;

import com.intellij.ide.actions.runAnything.RunAnythingUtil;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.playback.commands.ActionCommand;
import com.intellij.openapi.util.ActionCallback;
import edu.ucsd.AppState;
import edu.ucsd.ClassMethod;
import edu.ucsd.getty.GettyConstants;
import edu.ucsd.getty.GettyRunner;
import edu.ucsd.git.GitAdapter;
import edu.ucsd.properties.Properties;
import edu.ucsd.properties.PropertiesService;
import edu.ucsd.rsync.RsyncAdapter;
import io.reactivex.disposables.Disposable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.plugins.gradle.tooling.ModelBuilderService;

import java.awt.event.InputEvent;
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
/*
        if (properties == null || properties.isRequiredFieldEmpty()) {
            propertiesService.showSetPropertiesDialog();
        }
*/

        Project project = event.getProject();
        if (project != null) {
//            String basePath = StringUtils.defaultIfEmpty(project.getBasePath(), "");
//            Optional<File> priorityFileOptional = ReInferPriorityFileWriter.write(basePath, reInferPriority);


            reInfer(project, AppState.method);

//            if file present run Getty
//            priorityFileOptional.ifPresent(f -> {
//                log.warn("file path {}", f.getAbsolutePath());
//                reInfer(project, f);
//            });

        }

    }

/*    private void reInfer(Project project, File priorityFile) {
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
                //gettyRunner.run(hashOfParent, hashOfHead, priorityFile.getAbsolutePath());
                gettyRunner.run();
            } catch (IOException e) {
                log.error("Getty failed:", e);
            }

            //        todo: set app state to not currently re-inferring
        });

//        todo: set app state to re-inferring

    }*/

/*private void runInvariantsTask() {
    AnAction gradleAction = ActionManager.getInstance().getAction("Gradle.ExecuteTask");
    InputEvent event = ActionCommand.getInputEvent("invariants");
    ActionCallback callback = ActionManager.getInstance().tryToExecute(gradleAction, event, null, null, true);

}*/

    private void reInfer(Project project, ClassMethod /*String*/ method) {
        execService.submit(() -> {
            String projectPath = project.getBasePath();

            GettyConstants gettyConstants = new GettyConstants(project);
            gettyRunner = new GettyRunner(
                    project,
                    projectPath,
                    properties.isDebugLog(), properties.isStackTrace(), properties.isCleanBeforeRunning());//(gettyConstants.SOURCE_DIR, properties.getGettyPath(), properties.getPythonPath());
            try {
//                TODO: move priority file to gettyConstants?
                //gettyRunner.run(hashOfParent, hashOfHead, priorityFile.getAbsolutePath());
                gettyRunner.run(method);

                //runInvariantsTask();
            } catch (IOException e) {
                log.error("Getty failed:", e);
            }

            //        todo: set app state to not currently re-inferring

        });
//        todo: set app state to re-inferring

    }
}