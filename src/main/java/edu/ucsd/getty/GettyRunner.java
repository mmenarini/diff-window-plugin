package edu.ucsd.getty;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
public class GettyRunner {

    private String gettyPath;
    private String pythonPath;
    private String projectBasePath;

    public GettyRunner(String projectBasePath, String gettyPath, String pythonPath) {
        log.warn("getty runner gettyPath {}, pythonPath {}", gettyPath, pythonPath);
        this.projectBasePath = projectBasePath;
        this.gettyPath = gettyPath;
        this.pythonPath = pythonPath;
    }

    public void run(String commitHashPre, String commitHashPost, String priorityFilePath) throws IOException {
        if (!isCorrectPythonVersion()) {
            throw new IllegalStateException("Wrong python version");
        }


        ProcessBuilder builder = new ProcessBuilder();
//        builder.command(pythonPath, gettyPath, "-h");
        builder.command(pythonPath, gettyPath, commitHashPre, commitHashPost, priorityFilePath);
        builder.directory(new File(projectBasePath).getAbsoluteFile());
        builder.redirectErrorStream(true);
        Process p = builder.start();

        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(p.getInputStream()));

        waitForProcessToComplete(p);

        if (p.exitValue() != 0) {
            String cmd = String.format("%s %s %s %s %s", pythonPath, gettyPath, commitHashPre, commitHashPost, priorityFilePath);
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

//        String cmd = String.format("%s --version", pythonPath);
//        Process p = Runtime.getRuntime().exec(cmd);
//        BufferedReader stdError = new BufferedReader(new
//                InputStreamReader(p.getErrorStream()));
        waitForProcessToComplete(p);

        if (p.exitValue() != 0) {
            logProcessErrorOutput(p, pythonPath + " --version", stdError);
            return false;
        }

        String s = stdError.readLine();
        log.warn("s {}", s);
        int version = Integer.parseInt(s.split(" ")[1].split("\\.")[0]);
        if (version != 2) {
            log.error("Python version 2.7 required but major version was {}: {}", version, s);
            return false;
        }

        return true;
    }
//
//    private String getCsiPath(String gettyHome) {
//        if (SystemUtils.IS_OS_WINDOWS) {
//            gettyHome += "\\";
//        } else {
//            gettyHome += "/";
//        }
//        return gettyHome + "csi.py";
//    }

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
