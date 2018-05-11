package edu.ucsd.properties;

import com.intellij.ide.DataManager;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import lombok.extern.slf4j.Slf4j;

import static edu.ucsd.properties.Properties.GETTY_PATH;
import static edu.ucsd.properties.Properties.PYTHON_PATH;

@Slf4j
public class PropertiesService {
    private static PropertiesService singleton;
    private PropertiesComponent propertiesComponent;
    private BehaviorSubject<Properties> properties = BehaviorSubject.create();

    public PropertiesService() {
        this(PropertiesComponent.getInstance());
    }

    public PropertiesService(PropertiesComponent propertiesComponent) {
        this.propertiesComponent = propertiesComponent;
        initProperties();
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
        propertiesForm.setProperties(properties.getValue());
        PropertiesDialog dialog = new PropertiesDialog(propertiesForm);
        dialog.showAndGetProperties().ifPresent(this::setProperties);
    }

    public Observable<Properties> getPropertiesObservable() {
        return properties;
    }

    private void initProperties() {
        properties.onNext(new Properties(
                propertiesComponent.getValue(GETTY_PATH),
                propertiesComponent.getValue(PYTHON_PATH)
        ));
    }

    public void setProperties(Properties properties) {
        propertiesComponent.setValue(GETTY_PATH, properties.getGettyPath());
        propertiesComponent.setValue(PYTHON_PATH, properties.getPythonPath());
        this.properties.onNext(properties);
    }
}
