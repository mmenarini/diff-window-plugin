package edu.ucsd.getty;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
public class GettyRunner {

    private String gettyHome;

    public GettyRunner(String gettyHome) {
        this.gettyHome = gettyHome;
    }

    public void run(String commitHashPre, String commitHashPost, String priorityFilePath) throws IOException {
        if (!isCorrectPythonVersion()) {
            throw new IllegalStateException("Wrong python version");
        }

        String cmd = String.format("python %s/csi.py %s %s %s", gettyHome, commitHashPre, commitHashPost, priorityFilePath);
        Process p = Runtime.getRuntime().exec(cmd);

        if (p.exitValue() != 0) {
            logProcessErrorOutput(p, cmd);
            throw new IllegalStateException("The csi script exited with value " + p.exitValue());
        }
    }

    private boolean isCorrectPythonVersion() throws IOException {
        String cmd = "python --version";
        Process p = Runtime.getRuntime().exec(cmd);

        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(p.getErrorStream()));

        if (p.exitValue() != 0) {
            logProcessErrorOutput(p, cmd);
            return false;
        }

        String s = stdError.readLine();
        int version = Integer.parseInt(s.split(" ")[1].split("\\.")[0]);
        if (version != 2) {
            log.error("Python version 2.7 required but major version was {}: {}", version, s);
            return false;
        }

        return true;
    }

    private void logProcessErrorOutput(Process p, String cmd) throws IOException {
        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(p.getErrorStream()));

        log.error("Error running {}", cmd);
        String s = null;
        while ((s = stdError.readLine()) != null) {
            log.error(s);
        }
    }
}
