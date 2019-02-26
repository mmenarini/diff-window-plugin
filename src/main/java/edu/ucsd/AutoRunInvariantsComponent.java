package edu.ucsd;

import com.intellij.openapi.compiler.CompilerManager;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import edu.ucsd.getty.GettyRunner;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
@Slf4j
public class AutoRunInvariantsComponent implements ProjectComponent {
    private ExecutorService execService = Executors.newFixedThreadPool(1);
    private GettyRunner gettyRunner;
    private Project project;
    public AutoRunInvariantsComponent(Project project){
        this.project=project;
        String projectPath = project.getBasePath();
        gettyRunner = new GettyRunner(
                project,
                projectPath);
    }

    @Override
    public void projectOpened() {
        GettyRunner.cloneRepository(project.getBasePath());
        CompilerManager.getInstance(project).addAfterTask(context -> {
            if(context.getMessageCount(CompilerMessageCategory.ERROR)==0){
                //We rerun the inference if the cared is in one method
                execService.submit(() -> {
                    try {
                        if (AppState.method!=null && AppState.method.getMethodSignature()!=null)
                        gettyRunner.run(AppState.method);
                    } catch (IOException e) {
                        log.error("Getty failed:", e);
                    }
                });
            }
            return true;
        });
    }
}
