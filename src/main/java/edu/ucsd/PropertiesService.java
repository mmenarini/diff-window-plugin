package edu.ucsd;

import com.intellij.ide.util.PropertiesComponent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PropertiesService {
    private static final String GETTY_PATH = "gettyPath";

    private PropertiesComponent propertiesComponent;

    public PropertiesService(PropertiesComponent propertiesComponent) {
        this.propertiesComponent = propertiesComponent;

        if (getGettyPath() == null) {
//todo: open dialog for setting gettypath
        }
    }

    public void setGettyPath(String path) {
        propertiesComponent.setValue(GETTY_PATH, path);
    }

    public String getGettyPath() {
        return propertiesComponent.getValue(GETTY_PATH);
    }
}
