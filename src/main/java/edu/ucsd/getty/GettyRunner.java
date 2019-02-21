package edu.ucsd.getty;

import edu.ucsd.AppState;
import edu.ucsd.mmenarini.getty.GettyMainKt;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.SystemUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class GettyRunner {
    private ExecutorService execService = Executors.newFixedThreadPool(2);

    private String gettyPath;
    private String pythonPath;
    private String projectBasePath;

    public GettyRunner(String projectBasePath/*, String gettyPath, String pythonPath*/) {
        //log.warn("getty runner gettyPath {}, pythonPath {}", gettyPath, pythonPath);
        this.projectBasePath = projectBasePath;
//        this.gettyPath = gettyPath;
//        this.pythonPath = pythonPath;
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

    public void run(String methodSignature) throws IOException {
        Path gitPath = Paths.get(projectBasePath);
        AppState.headRepoDir = GettyMainKt.cloneGitHead(gitPath);

        runGradleInvariants(methodSignature, AppState.headRepoDir);
        runGradleInvariants(methodSignature, gitPath);

    }

    private void runGradleInvariants(String methodSignature, Path repoDir) throws IOException {
        ProcessBuilder builder = new ProcessBuilder();
//        builder.command(
//                "./gradlew","cleanTest", "cleanCallgraph", "cleanDaikon", "cleanInvariants","invariants",
//                "-PmethodSignature="+methodSignature, "--info", "--stacktrace");
        builder.command(
                "./gradlew","cleanTest", "invariants",
                "-PmethodSignature="+methodSignature);
        builder.directory(repoDir.toFile());
        builder.redirectErrorStream(true);

        Process p = builder.start();
        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(p.getInputStream()));

//        TODO: show logs in DiffWindow
        execService.submit(new Logger(stdError));

        waitForProcessToComplete(p);

        if (p.exitValue() != 0) {
            String cmd = String.format("./gradlew cleanDaikon invariants");
            logProcessErrorOutput(p, cmd, stdError);
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
}
