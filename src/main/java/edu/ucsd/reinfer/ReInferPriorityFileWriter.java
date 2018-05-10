package edu.ucsd.reinfer;

import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Slf4j
class ReInferPriorityFileWriter {
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static String fileName = "re-infer-priority.json";

    static Optional<File> write(String basePath, ReInferPriority reInferPriority) {
        try {
            File f = new File(basePath + "/" + fileName);
            objectMapper.writeValue(f, reInferPriority);
            return Optional.of(f);
        } catch (IOException e) {
            log.error("error converting to JSON {}", e);
            return Optional.empty();
        }
    }
}
