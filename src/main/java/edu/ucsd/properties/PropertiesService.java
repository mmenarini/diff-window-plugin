package edu.ucsd.properties;

import com.intellij.ide.DataManager;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import static edu.ucsd.properties.Properties.*;

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
        DataManager.getInstance(). getDataContextFromFocusAsync().onSuccess(dataContext ->
                {
                    Project project = dataContext.getData(PlatformDataKeys.PROJECT);
                    PropertiesForm propertiesForm = new PropertiesForm(project);
                    propertiesForm.setProperties(properties.getValue());
                    PropertiesDialog dialog = new PropertiesDialog(propertiesForm);
                    Optional<Properties> props = dialog.showAndGetProperties();
                    props.ifPresent(
                            properties -> this.setProperties(properties));
                }
        );
    }

    public Observable<Properties> getPropertiesObservable() {
        return properties;
    }

    private void initProperties() {
        properties.onNext(new Properties(
            Boolean.valueOf(propertiesComponent.getValue(DEBUG_LOG_PATH)),
            Boolean.valueOf(propertiesComponent.getValue(STACK_TRACE_PATH)),
            Boolean.valueOf(propertiesComponent.getValue(CLEAN_BEFORE_RUNNING_PATH)),
            Boolean.valueOf(propertiesComponent.getValue(REMOVE_WORK_BEFORE_RUNNING_PATH)),
            Boolean.valueOf(propertiesComponent.getValue(DO_NOT_AUTORUN_PATH))
        ));
    }

    public void setProperties(Properties properties) {
//        propertiesComponent.setValue(GETTY_PATH, properties.getGettyPath());
//        propertiesComponent.setValue(PYTHON_PATH, properties.getPythonPath());
        propertiesComponent.setValue(DEBUG_LOG_PATH, properties.isDebugLog());
        propertiesComponent.setValue(STACK_TRACE_PATH, properties.isStackTrace());
        propertiesComponent.setValue(CLEAN_BEFORE_RUNNING_PATH, properties.isCleanBeforeRunning());
        propertiesComponent.setValue(REMOVE_WORK_BEFORE_RUNNING_PATH, properties.isRemoveWorkBeforeRunning());
        propertiesComponent.setValue(DO_NOT_AUTORUN_PATH, properties.isDoNotAutorun());
        this.properties.onNext(properties);
    }
}
