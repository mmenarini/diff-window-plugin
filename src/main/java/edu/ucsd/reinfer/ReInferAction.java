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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
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
    }

    public void actionPerformed(AnActionEvent event) {
        Project project = event.getProject();
        if (project != null) {
            reInfer(project, AppState.method);
        }
    }

    private void reInfer(Project project, ClassMethod method) {
        AppState.runGetty(gettyRunner, method, true);

    }
}