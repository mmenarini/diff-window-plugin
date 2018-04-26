package edu.ucsd.util;

import edu.ucsd.FileReader;

import java.io.File;
import java.util.Optional;

public class LogUtils {
    public static Optional<String> getLogAsString() {
        return FileReader.readFileAsString(new File("test.log"));
    }

    public static void removeLog() {
        new File("test.log").delete();
    }
}
