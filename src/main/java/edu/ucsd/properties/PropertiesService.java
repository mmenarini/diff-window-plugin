package edu.ucsd.properties;

import com.intellij.ide.DataManager;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import lombok.extern.slf4j.Slf4j;

import static edu.ucsd.properties.Properties.GETTY_PATH;

@Slf4j
public class PropertiesService {
    private static PropertiesService singleton;

    private PropertiesComponent propertiesComponent;

    public PropertiesService() {
        this(PropertiesComponent.getInstance());
    }

    public PropertiesService(PropertiesComponent propertiesComponent) {
        this.propertiesComponent = propertiesComponent;
    }

    public static PropertiesService getInstance() {
        if (singleton == null) {
            singleton = new PropertiesService();
        }
        return singleton;
    }

    public void showSetPropertiesDialog() {
        DataContext dataContext = DataManager.getInstance().getDataContextFromFocus().getResultSync();
        Project project = dataContext.getData(PlatformDataKeys.PROJECT);
        PropertiesForm propertiesForm = new PropertiesForm(project);
        propertiesForm.setGettyPath(getGettyPath());
        PropertiesDialog dialog = new PropertiesDialog(propertiesForm);
        dialog.showAndGetProperties().ifPresent(p -> setGettyPath(p.getGettyPath()));
    }

    public void setGettyPath(String path) {
        propertiesComponent.setValue(GETTY_PATH, path);
    }

    public String getGettyPath() {
        return propertiesComponent.getValue(GETTY_PATH);
    }
}
