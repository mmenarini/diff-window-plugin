package edu.ucsd.properties;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class PropertiesForm {
    private JPanel panel;
    private JTextField gettyPathField;
    private JTextField pythonPathField;
    private JCheckBox debugLogCheckBox;
    private JCheckBox stackTraceCheckBox;
    private JCheckBox cleanBeforeRunningCheckBox;
    private JCheckBox removeWorkBeforeRunningCheckBox;
    private JCheckBox doNotAutoRunCheckBox;

    public PropertiesForm(@Nullable Project project) {
    }

    public void setProperties(Properties properties) {
        this.debugLogCheckBox.setSelected(properties.isDebugLog());
        this.stackTraceCheckBox.setSelected(properties.isStackTrace());
        this.cleanBeforeRunningCheckBox.setSelected(properties.isCleanBeforeRunning());
        this.removeWorkBeforeRunningCheckBox.setSelected(properties.isRemoveWorkBeforeRunning());
        this.doNotAutoRunCheckBox.setSelected(properties.isDoNotAutorun());
    }

//    public String getGettyPath() {
//        return "";//gettyPathField.getText();
//    }
//
//    public String getPythonPath() {
//        return "";//pythonPathField.getText();
//    }

    public boolean getDebugLog() { return debugLogCheckBox.isSelected(); }

    public boolean getStackTrace() {
        return stackTraceCheckBox.isSelected();
    }

    public boolean getCleanBeforeRunningCheckBox() {
        return cleanBeforeRunningCheckBox.isSelected();
    }

    public boolean getRemoveWorkBeforeRunningCheckBox() {
        return removeWorkBeforeRunningCheckBox.isSelected();
    }

    public boolean getDoNotAutorunCheckBox() {
        return doNotAutoRunCheckBox.isSelected();
    }

    public JPanel getPanel() {
        return panel;
    }
}
