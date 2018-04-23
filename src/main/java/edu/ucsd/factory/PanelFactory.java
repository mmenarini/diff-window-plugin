package edu.ucsd.factory;

import com.intellij.diff.DiffManager;
import com.intellij.diff.DiffRequestPanel;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;

public class PanelFactory {
    private Project project;

    public PanelFactory(Project project) {
        this.project = project;
    }

    public DiffRequestPanel createDiffRequestPanel(Disposable disposable) {
        return DiffManager.getInstance().createRequestPanel(project, disposable, null);
    }
}
