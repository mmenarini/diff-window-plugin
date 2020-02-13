package edu.ucsd;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

@Slf4j
public class FileReader {
    public static Optional<String> readFileAsString(File file) {
        if (file == null) {
            log.warn("File param was null");
            return Optional.empty();
        } else if (!file.exists()) {
            log.warn("File "+file.getAbsolutePath()+" does not exist");
            return Optional.empty();
        }
        //Read it as a string
        StringBuilder stringBuilder = new StringBuilder();

        try {
            for (String s : Files.readAllLines(file.toPath())) {
                stringBuilder.append(s);
                stringBuilder.append('\n');
            }
        } catch (IOException e1) {
            log.error("Error: " + ExceptionUtils.getMessage(e1) +
                    ", Root cause: " + ExceptionUtils.getRootCauseMessage(e1));
        }

        return Optional.of(stringBuilder.toString());
    }
}
