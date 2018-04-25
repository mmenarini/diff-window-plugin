package edu.ucsd.getty;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class GettyInvariantsOutputFilesRetriever {
    private static final String FILE_PREFIX = "_getty_inv_";

    private File gettyOutputDir;

    public GettyInvariantsOutputFilesRetriever(File gettyOutputDir) {
        this.gettyOutputDir = gettyOutputDir;
    }

    public Optional<List<File>> getFiles(String className) {
        File[] files = gettyOutputDir.listFiles();

        if (files == null) {
            log.warn("Directory {} was empty or rendered an I/O error", gettyOutputDir.getAbsolutePath());
            return Optional.empty();
        }

        List<File> result = new ArrayList<>();
        for (File file : files) {
            if (isGettyInvariantsOutputFile(file)) {
                String fileName = file.getName();
                if (fileNameContainsClassName(fileName, className)) {
                    result.add(file);
                }
            }
        }

        return Optional.of(result);
    }

    private boolean isGettyInvariantsOutputFile(File file) {
        return file != null && file.isFile() && ".out".equals(FilenameUtils.getExtension(file.getName()));
    }

    public Optional<List<File>> getFiles(String className, String methodName) {
        return getFiles(className).map(files -> files.stream()
                .filter(file -> fileNameContainsMethodName(file.getName(), methodName))
                .collect(Collectors.toList())
        );
    }

    public Optional<List<File>> getFiles(String className, String methodName, String hashCode) {
        return getFiles(className, methodName).map(files -> files.stream()
                .filter(file -> fileNameContainsHashCode(file.getName(), hashCode))
                .collect(Collectors.toList())
        );
    }

    public Optional<File> getFile(String className, String methodName, String hashCode) {
        return getFiles(className, methodName, hashCode).map(files -> files.stream()
                .reduce((file, otherFile) -> {
                    log.warn("Multiple files matched for className {}, methodName {}, and hashCode {}");
                    return file;
                })
                .orElse(null)
        );
    }

    private boolean fileNameContainsClassName(String fileName, String className) {
        return fileName.contains(String.format("%s_%s_", FILE_PREFIX, className));
    }

    private boolean fileNameContainsMethodName(String fileName, String methodName) {
//        TODO: how to deal with the constructor method? It is called "init"
        return fileName.contains(String.format("_%s--", methodName));
    }

    private boolean fileNameContainsHashCode(String fileName, String hashCode) {
        return fileName.contains(String.format("_%s_", hashCode));
    }
}
