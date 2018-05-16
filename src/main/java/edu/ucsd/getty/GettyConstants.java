package edu.ucsd.getty;

import com.intellij.openapi.project.Project;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GettyConstants {
    public final String SOURCE_DIR;
    public final String OUTPUT_DIR;

    private static final String SOURCE_DIR_POSTFIX = "_getty";
    private static final String OUTPUT_DIR_POSTFIX = ".__getty_output__";

    public GettyConstants(Project project) {
        String projectPath = getProjectPath(project);

        SOURCE_DIR = projectPath + SOURCE_DIR_POSTFIX;
        OUTPUT_DIR = SOURCE_DIR + OUTPUT_DIR_POSTFIX;
    }

    private static String getProjectPath(Project project) {
        String projectPath = project.getBasePath();
        if (projectPath == null) {
            throw new IllegalArgumentException("Project base path was null");
        }
        return projectPath;
    }

}
