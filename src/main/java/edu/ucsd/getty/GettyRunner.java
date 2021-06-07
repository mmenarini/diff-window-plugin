package edu.ucsd.getty;

import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import edu.ucsd.ClassMethod;
import edu.ucsd.GettyRunNotifier;
import edu.ucsd.mmenarini.getty.GettyMainKt;
import edu.ucsd.properties.Properties;
import edu.ucsd.properties.PropertiesService;
import io.reactivex.disposables.Disposable;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Slf4j
public class GettyRunner implements com.intellij.openapi.Disposable {
    private final ExecutorService execService = Executors.newFixedThreadPool(4);
    private final GettyRunNotifier gettyRunPublisher;
    private final PropertiesService propertiesService = PropertiesService.getInstance();
    private final Disposable propertiesSubscription;
    private Properties properties;
    private final String projectBasePath;
    private final Project project;
    private Process gradleProcess=null;
    private Future<?> gettyFuture=null;
    private Path headRepoDir=null;
    private boolean runningGradle=false;
    private ClassMethod method;

    public GettyRunner(Project project) {
        this.projectBasePath = project.getBasePath();
        this.project = project;
        propertiesSubscription = propertiesService.getPropertiesObservable()
                .subscribe(p -> this.properties = p);

        MessageBus messageBus = project.getMessageBus();
        gettyRunPublisher = messageBus.syncPublisher(GettyRunNotifier.GETTY_RUN_NOTIFIER_TOPIC);
        cloneRepository(projectBasePath);
    }

    public void run(ClassMethod method) {
        synchronized (execService) {
            this.method = method;
            gettyFuture = execService.submit(() -> do_run(method));
        }
    }

    public Path cloneRepository(String projectBasePath){
        Path gitPath = Paths.get(projectBasePath);
        gettyRunPublisher.cloning(gitPath.toString());
        headRepoDir = GettyMainKt.cloneGitHead(gitPath);
        gettyRunPublisher.cloned(headRepoDir);
        return headRepoDir;
    }

    private void do_run(ClassMethod method) {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Running invariants inference...", false, PerformInBackgroundOption.ALWAYS_BACKGROUND) {
            @Override
            public void onCancel() {
                if (project != null) {
                        project.getMessageBus().syncPublisher(GettyRunNotifier.GETTY_RUN_NOTIFIER_TOPIC).forceStop();
                }
            }
            //boolean running;
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
/*                running = true;
                execService.submit(() -> {
                    while(!indicator.isCanceled()) {
                        indicator.checkCanceled();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });*/
                indicator.setIndeterminate(false);
                indicator.setFraction(0);
                indicator.setText("Started");
                gettyRunPublisher.started(method);
                if (properties.isRemoveWorkBeforeRunning() && Files.exists(headRepoDir)){
                    indicator.setText("Removing repo files");
                    try {
                        Files.walkFileTree(headRepoDir,
                                new SimpleFileVisitor<Path>() {
                                    @Override
                                    public FileVisitResult postVisitDirectory(
                                            Path dir, IOException exc) throws IOException {
                                        try {
                                            Files.delete(dir);
                                        } catch(Throwable ignored) {}
                                        return FileVisitResult.CONTINUE;
                                    }

                                    @Override
                                    public FileVisitResult visitFile(
                                            Path file, BasicFileAttributes attrs)
                                            throws IOException {
                                        try {
                                            Files.delete(file);
                                        } catch(Throwable t) {}
                                        return FileVisitResult.CONTINUE;
                                    }
                                });
                    } catch (IOException e) {
                        gettyRunPublisher.error(e.getMessage());
                    }
                }
                indicator.setFraction(0.1);
                indicator.setText("Cloning Repository");

                cloneRepository(projectBasePath);

                //TODO change to a config option
                String daikonDir = System.getenv("DAIKONDIR");
                Path daikonJar=null;
                if (daikonDir!=null)
                    daikonJar = Paths.get(daikonDir).resolve("daikon.jar");
                String daikonJarPath = null;
                if (daikonJar!=null && Files.exists(daikonJar))
                    daikonJarPath = daikonJar.toAbsolutePath().toString();
                Path invCacheFile=GettyInvariantsFilesRetriever.getHeadRepoCachedInvariantFilePath(method, headRepoDir);
                if (Files.notExists(invCacheFile)) {
                    try {
                        indicator.setFraction(0.4);
                        indicator.setText("Running inference on head repository");

                        gettyRunPublisher.headInferenceStared(method);
                        runGradleInvariants(method.getMethodSignature(), headRepoDir, daikonJarPath);
                        Path invFile = GettyInvariantsFilesRetriever.getHeadRepoInvariantFilePath(method,headRepoDir);
                        assert invFile != null;
                        if (Files.notExists(invFile)) {
                            String msg = String.format("Invariant file %s for method %s does not exist.", invFile, method);
                            log.error(msg);
                            throw new IllegalStateException(msg);
                        } else {
                            //Cache the file
                            invCacheFile.getParent().toFile().mkdirs();
                            Files.copy(invFile, invCacheFile);
                        }
                        gettyRunPublisher.headInferenceDone(method);
                    } catch(IllegalStateException | IOException e) {
                        log.error("Did not complete GETTY successfully on the HEAD repo.");
                        gettyRunPublisher.headInferenceError(method, e.getMessage());
                    }
                }
                try {
                    indicator.setFraction(0.6);
                    indicator.setText("Running inference on current program");
                    gettyRunPublisher.currentInferenceStarted(method);
                    runGradleInvariants(method.getMethodSignature(), Paths.get(projectBasePath), daikonJarPath);
                    gettyRunPublisher.currentInferenceDone(method);
                    indicator.setText("Done");
                } catch(IllegalStateException e) {
                    log.error("Did not complete GETTY successfully on the current code.");
                    gettyRunPublisher.currentInferenceError(method, e.getMessage());
                    indicator.setText("Error: "+e.getMessage());
                }
                indicator.setFraction(1);
                //running=false;
            }
        });
    }

    public Future<?> stopAndRun(ClassMethod method) {
        return execService.submit(() -> {
            do_stop();
            do_run(method);
            gettyRunPublisher.done(method);
        });
    }

    private void do_stop() {
        synchronized (execService) {
            if ((gettyFuture != null) && !gettyFuture.isDone()) {
                gettyFuture.cancel(true);
            }
            if (runningGradle) {
                gradleProcess.destroy();
                try {
                    gradleProcess.waitFor(3, TimeUnit.SECONDS);
                } catch (InterruptedException e) {

                } finally {
                    if (gradleProcess.isAlive())
                        gradleProcess.destroyForcibly();
                }
                //Can't do this because it would kill my intellij while debugging
                /*try {
                    ProcessBuilder builder = new ProcessBuilder();
                    List<String> commandsList = getSystemGradlew();
                    commandsList.add("--stop");
                    builder.command(commandsList);
                    builder.directory(Paths.get(projectBasePath).toFile());
                    builder.redirectErrorStream(true);
                    Process gradleKillProcess = builder.start();
                    BufferedReader stdError = new BufferedReader(new
                            InputStreamReader(gradleKillProcess.getInputStream()));
                    execService.submit(new Logger(stdError));
                    gradleKillProcess.waitFor();
                } catch (IOException e) {
                    log.error("IOExcepion {0}", e);
                    throw new IllegalStateException("IO Excepion " + e.getLocalizedMessage());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/

            }
            // Kill test process if running with
            // taskkill /F /IM "gettyJava.exe" /T
            try {
                ProcessBuilder builder = new ProcessBuilder();
                List<String> commandsList = new LinkedList<>();
                commandsList.add("taskkill");
                commandsList.add("/F");
                commandsList.add("/IM");
                commandsList.add("\"gettyJava.exe\"");
                commandsList.add("/T");
                builder.command(commandsList);
                Process gettyJavaKillProcess = builder.start();
                gettyJavaKillProcess.waitFor();
            } catch (IOException e) {
                log.error("IOExcepion {0}", e);
                throw new IllegalStateException("IO Excepion " + e.getLocalizedMessage());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            runningGradle = false;
        }
    }

    public Future<?> stop() {
        return execService.submit(() -> {
            do_stop();
            gettyRunPublisher.stopped(method);
            gettyRunPublisher.done(method);
        });
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

    private void runGradleInvariants(String methodSignature, Path repoDir, String daikonJarPath) {
        ProcessBuilder builder = new ProcessBuilder();
        List<String> commandsList = getSystemGradlew();
        commandsList.add("--no-daemon");
        if (properties.isCleanBeforeRunning())
            commandsList.add("clean");
        else {
            commandsList.add("cleanDaikon");
            //commandsList.add("cleanInvariants");
        }
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

        String cmdLog = "["+repoDir+"]"+"\"" + String.join("\" \"", builder.command()) + "\"";

        log.warn(cmdLog);

        synchronized (execService){
            runningGradle=true;
        }
        try {
            gradleProcess = builder.start();
            BufferedReader stdError = new BufferedReader(new
                InputStreamReader(gradleProcess.getInputStream()));

//        TODO: show logs in DiffWindow
            execService.submit(new Logger(stdError));
            gradleProcess.waitFor();
            if (gradleProcess.exitValue() != 0) {
                logProcessErrorOutput(gradleProcess, cmdLog, stdError);
                log.error("The gradle build exited with value " + gradleProcess.exitValue());
                throw new IllegalStateException("The csi script exited with value " + gradleProcess.exitValue());
            }
        } catch (InterruptedException e) {
            log.error("Interrupted {0}", e);
            //throw new IllegalStateException("Interrupted Excepion " + e.getLocalizedMessage());
        } catch (IOException e) {
            log.error("IOExcepion {0}", e);
            throw new IllegalStateException("IO Excepion " + e.getLocalizedMessage());
        } finally {
            synchronized (execService){
                runningGradle=false;
            }
        }

    }

    private void logProcessErrorOutput(Process p, String cmd, BufferedReader stdError) {
        log.error("Error running {}", cmd);
        String s;
        try{
        while ((s = stdError.readLine()) != null)
            log.error(s);
        }catch(IOException e){
            log.error("Could not read gradle error log.");
            log.error(e.getLocalizedMessage());
        }
    }


    @Override
    public void dispose() {
        propertiesSubscription.dispose();
    }
}
