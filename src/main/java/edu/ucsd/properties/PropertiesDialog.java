package edu.ucsd.properties;

import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class PropertiesDialog {

    private final PropertiesForm propertiesForm;
    private DialogBuilder builder;

    public PropertiesDialog(PropertiesForm propertiesForm) {
        this.propertiesForm = propertiesForm;

        builder = new DialogBuilder();
        builder.setTitle("Set properties");
        builder.setCenterPanel(propertiesForm.getPanel());
    }

    public Optional<Properties> showAndGetProperties() {
        boolean isOk = builder.show() == DialogWrapper.OK_EXIT_CODE;
        if (isOk) {
            return Optional.of(new Properties(
                    propertiesForm.getDebugLog(),
                    propertiesForm.getStackTrace(),
                    propertiesForm.getCleanBeforeRunningCheckBox(),
                    propertiesForm.getRemoveWorkBeforeRunningCheckBox(),
                    propertiesForm.getDoNotAutorunCheckBox()));
        }
        return Optional.empty();
    }
}
