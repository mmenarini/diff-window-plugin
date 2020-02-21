package edu.ucsd.getty;

import com.intellij.execution.RunManagerEx;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import edu.ucsd.AppState;
import edu.ucsd.ClassMethod;
import edu.ucsd.mmenarini.getty.GettyMainKt;
import edu.ucsd.properties.Properties;
import edu.ucsd.properties.PropertiesService;
import io.reactivex.disposables.Disposable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.SystemUtils;
import org.jetbrains.plugins.gradle.action.GradleExecuteTaskAction;
import org.jetbrains.plugins.gradle.tooling.ModelBuilderService;
import org.jetbrains.plugins.gradle.util.GradleUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class GettyRunner implements com.intellij.openapi.Disposable {
    private ExecutorService execService = Executors.newFixedThreadPool(2);
    private PropertiesService propertiesService = PropertiesService.getInstance();
    private Disposable propertiesSubscription;
    private Properties properties;
    private String gettyPath;
    private String pythonPath;
    private String projectBasePath;
    private Project project;
    //private boolean enableDebug, enableStackTrace, cleanBeforeRunning;
    public GettyRunner(Project project) {
        this.projectBasePath = project.getBasePath();
        this.project = project;
        propertiesSubscription = propertiesService.getPropertiesObservable()
                .subscribe(p -> this.properties = p);

    }


/*    public void run(String commitHashPre, String commitHashPost, String priorityFilePath) throws IOException {
        if (!isCorrectPythonVersion()) {
            throw new IllegalStateException("Wrong python version");
        }


        ProcessBuilder builder = new ProcessBuilder();
//        builder.command(pythonPath, gettyPath, "-h");
//        builder.command(pythonPath, gettyPath, commitHashPre, commitHashPost);

        if (SystemUtils.IS_OS_WINDOWS) {
//            TODO: not tested on Windows yet
            System.out.println("OS is Windows, attempting to run getty through WSL bash");
            builder.command("bash.exe -c", '"' + pythonPath, gettyPath, commitHashPre, commitHashPost, priorityFilePath + '"');
        } else {
//    TODO: getty crashes when there are no changes in the working copy: It cannot find .inv files and all_to_consider will be an empty set.
            builder.command(pythonPath, gettyPath, commitHashPre, commitHashPost, priorityFilePath);
        }

        builder.directory(new File(projectBasePath).getAbsoluteFile());
        builder.redirectErrorStream(true);

        Process p = builder.start();
        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(p.getInputStream()));

//        TODO: show logs in DiffWindow
        execService.submit(new Logger(stdError));

        waitForProcessToComplete(p);

        if (p.exitValue() != 0) {
            String cmd = String.format("%s %s %s %s %s", pythonPath, gettyPath, commitHashPre, commitHashPost, priorityFilePath);
            logProcessErrorOutput(p, cmd, stdError);
            throw new IllegalStateException("The csi script exited with value " + p.exitValue());
        }
    }*/

    public static void cloneRepository(String projectBasePath){
        Path gitPath = Paths.get(projectBasePath);
        AppState.headRepoDir = GettyMainKt.cloneGitHead(gitPath);
    }

    public void run(ClassMethod /*String*/ method) throws IOException {
        if (properties.isRemoveWorkBeforeRunning()){
            Files.walkFileTree(AppState.headRepoDir,
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult postVisitDirectory(
                            Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(
                            Path file, BasicFileAttributes attrs)
                            throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }
                });
        }

        cloneRepository(projectBasePath);
        //TODO change to a config option
        String daikonDir = System.getenv("DAIKONDIR");
        Path daikonJar=null;
        if (daikonDir!=null)
            daikonJar = Paths.get(daikonDir).resolve("daikon.jar");
        String daikonJarPath = null;
        if (daikonJar!=null && Files.exists(daikonJar))
            daikonJarPath = daikonJar.toAbsolutePath().toString();
        if (Files.notExists(GettyInvariantsFilesRetriever.getHeadRepoInvarinatFilePath(method)))
            runGradleInvariants(method.getMethodSignature(), AppState.headRepoDir, daikonJarPath);
        runGradleInvariants(method.getMethodSignature(), Paths.get(projectBasePath), daikonJarPath);
        AppState.triggerObservables();
        //runGradleInvariantsOnProject(method);

    }

    //Not using this because it would change the focus to the run window
    private void runGradleInvariantsOnProject(String methodSignature) throws IOException {
        GradleExecuteTaskAction.runGradle(
                project,
                null,
                Paths.get(projectBasePath).toAbsolutePath().toString(),
                "invariants -PmethodSignature=\""+methodSignature + "\""
        );
    }

    private List<String> getSystemGradlew() {
        List<String> commandsList = new LinkedList<>();
        if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
            commandsList.add("cmd.exe");
            commandsList.add("/C");
            commandsList.add("gradlew.bat");
        } else
            commandsList.add("./gradlew");
        return commandsList;
    }

    private void runGradleInvariants(String methodSignature, Path repoDir, String daikonJarPath) throws IOException {
        ProcessBuilder builder = new ProcessBuilder();
        List<String> commandsList = getSystemGradlew();
        if (properties.isCleanBeforeRunning())
            commandsList.add("clean");
        else
            commandsList.add("cleanDaikon");
        commandsList.addAll(Arrays.asList("invariants",
                "-PmethodSignature=" + methodSignature));
        if (daikonJarPath!=null) {
            commandsList.add("-PdaikonJarFile=" + daikonJarPath);
        }
//        if (enableDebug && enableStackTrace)
//            commandsList.add("--scan");
//        else {
        if (properties.isDebugLog()) commandsList.add("--debug");
        if (properties.isStackTrace()) commandsList.add("--stacktrace");
//        }
        builder.command(commandsList);
//        builder.command(
//                "./gradlew","cleanTest", "cleanCallgraph", "cleanDaikon", "cleanInvariants","invariants",
//                "-PmethodSignature="+methodSignature, "--info", "--stacktrace");
//        builder.command(
//                "./gradlew", "invariants",
//                "-PmethodSignature="+methodSignature, "--info", "--stacktrace");
        builder.directory(repoDir.toFile());
        builder.redirectErrorStream(true);

        String cmdLog = "\"" + String.join("\" \"", builder.command()) + "\"";

        log.warn(cmdLog);

        Process p = builder.start();
        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(p.getInputStream()));

//        TODO: show logs in DiffWindow
        execService.submit(new Logger(stdError));

        waitForProcessToComplete(p);

        if (p.exitValue() != 0) {
            logProcessErrorOutput(p, cmdLog, stdError);
            throw new IllegalStateException("The csi script exited with value " + p.exitValue());
        }
    }

    private boolean isCorrectPythonVersion() throws IOException {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(pythonPath, "--version");
        builder.directory(new File(projectBasePath).getAbsoluteFile());
        builder.redirectErrorStream(true);
        Process p = builder.start();

        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(p.getInputStream()));

        waitForProcessToComplete(p);

        if (p.exitValue() != 0) {
            logProcessErrorOutput(p, pythonPath + " --version", stdError);
            return false;
        }

        String s = stdError.readLine();
        log.warn("Version: {}", s);
        int version = Integer.parseInt(s.split(" ")[1].split("\\.")[0]);
        if (version != 2) {
            log.error("Python version 2.7 required but major version was {}: {}", version, s);
            return false;
        }

        return true;
    }

    private void waitForProcessToComplete(Process p) {
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            log.error("Interrupted {}", e);
        }
    }

    private void logProcessErrorOutput(Process p, String cmd, BufferedReader stdError) throws IOException {
        log.error("Error running {}", cmd);
        String s = null;
        while ((s = stdError.readLine()) != null) {
            log.error(s);
        }
    }

    @Override
    public void dispose() {
        propertiesSubscription.dispose();
    }
}
