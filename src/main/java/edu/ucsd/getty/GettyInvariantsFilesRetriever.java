package edu.ucsd.getty;

import com.intellij.openapi.project.Project;
import edu.ucsd.AppState;
import edu.ucsd.ClassMethod;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class GettyInvariantsFilesRetriever {
    private static final String FILE_PREFIX = "_getty_inv_";

    private File gettyOutputDir;
    private Project project;

    public GettyInvariantsFilesRetriever(File gettyOutputDir,Project project) {
        this.gettyOutputDir = gettyOutputDir;
        this.project=project;
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
                if (isFileNameContainsClassName(fileName, className)) {
                    result.add(file);
                }
            }
        }

        return Optional.of(result);
    }

    public Optional<List<File>> getFiles(String className, String methodName) {
        String m = transformMethodNameForConstructor(className, methodName);
        return getFiles(className).map(files -> files.stream()
                .filter(file -> isFileNameContainsMethodName(file.getName(), m))
                .collect(Collectors.toList())
        );
    }

    public Optional<List<File>> getFiles(String className, String methodName, List<String> parameterTypes) {
        return getFiles(className, methodName).map(files -> files.stream()
                .filter(file -> isFileNameContainsAllParameterTypes(file.getName(), parameterTypes))
                .collect(Collectors.toList())
        );
    }

    public Optional<List<File>> getFiles(String className, String methodName, List<String> parameterTypes, String hashCode) {
        return getFiles(className, methodName, parameterTypes).map(files -> files.stream()
                .filter(file -> isFileNameContainsHashCode(file.getName(), hashCode))
                .collect(Collectors.toList())
        );
    }

    public Optional<File> getFile(String className, String methodName, List<String> parameterTypes, String hashCode) {
        return getFiles(className, methodName, parameterTypes, hashCode).map(files -> files.stream()
                .reduce((file, otherFile) -> {
                    log.warn("Multiple files matched for className {}, methodName {}, and hashCode {}");
                    return file;
                })
                .orElse(null)
        );
    }

    private String transformMethodNameForConstructor(String className, String methodName) {
        if (className != null && StringUtils.equals(className, methodName)) {
            return "--init";
        } else {
            return methodName;
        }
    }

    private static boolean isGettyInvariantsOutputFile(File file) {
        return file != null && file.isFile() && "out".equals(FilenameUtils.getExtension(file.getName()));
    }

    private static boolean isFileNameContainsClassName(String fileName, String className) {
        return fileName.contains(String.format("%s_%s_", FILE_PREFIX, className));
    }

    private static boolean isFileNameContainsMethodName(String fileName, String methodName) {
        return fileName.contains(String.format("_%s--", methodName));
    }

    private static boolean isFileNameContainsAllParameterTypes(String fileName, List<String> parameterTypes) {
        String[] split = fileName.split("--");
        log.info("filename {} parameters {} split {}", fileName, parameterTypes, split);
        int i = 1;
        String firstParamType = split[i];

        if (StringUtils.equals(firstParamType, "init")) {
            i = 3;
            firstParamType = split[i];
        }

        if (StringUtils.equals(firstParamType, "") && parameterTypes.size() == 0) {
            return true;
        }
        String paramString = "";
        for (String paramType : parameterTypes) {
            paramString = paramString + paramType  + "-";
        }
        if (paramString.length()>0)
            paramString = paramString.substring(0,paramString.length()-1);

        StringBuilder fileParamString = new StringBuilder(split[i++]);
        while (i<split.length){
            if(split[i++].equals(""))
                fileParamString.append("-").append(split[i++]);
            else
                break;
        }

        if (!StringUtils.equals(fileParamString.toString(), paramString)) {
            return false;
        }

        return true;
    }

    private static boolean isFileNameContainsHashCode(String fileName, String hashCode) {
        return fileName.contains(String.format("_%s_", hashCode));
    }

    public Optional<List<File>> getFiles(ClassMethod newClassMethod) {
        if (AppState.headRepoDir==null)
            return Optional.empty();
        Path invBase = Paths.get(project.getBasePath()).resolve("build").resolve("invariants");
        Path invHead = AppState.headRepoDir.resolve("build").resolve("invariants");

        String[] lst = newClassMethod.getMethodSignature().split(" ");
        if (lst.length==3) {
            String filename = lst[2].substring(0,lst[2].length()-1)+".inv";
            Path filePre = invHead
                    .resolve(newClassMethod.qualifiedClassName.replace(".","/"))
                    .resolve(filename);
            Path filePost = invBase
                    .resolve(newClassMethod.qualifiedClassName.replace(".","/"))
                    .resolve(filename);

            List<File> result = Arrays.asList(filePre.toFile(), filePost.toFile());
            return Optional.of(result);
        }
        return Optional.empty();
    }
}
