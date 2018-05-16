package edu.ucsd.getty;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;

@Slf4j
public class Logger implements Runnable {
    private BufferedReader stdError;

    public Logger(BufferedReader stdError) {
        this.stdError = stdError;
    }

    @Override
    public void run() {
        try {
            writeLogs();
        } catch (IOException e) {
            log.error("Logging failed: {}", e);
        }
    }

    private void writeLogs() throws IOException {
        String s = null;
        while ((s = stdError.readLine()) != null) {
            log.warn(s);
        }
    }
}
