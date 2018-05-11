package edu.ucsd.properties;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class PropertiesForm {
    private JPanel panel;
    private JLabel gettyPathLabel;
    private JTextField gettyPathField;
    private JLabel pythonPathLabel;
    private JTextField pythonPathField;

    public PropertiesForm(@Nullable Project project) {
    }

    public void setProperties(Properties properties) {
        this.gettyPathField.setText(properties.getGettyPath());
        this.pythonPathField.setText(properties.getPythonPath());

    }

    public String getGettyPath() {
        return gettyPathField.getText();
    }

    public String getPythonPath() {
        return pythonPathField.getText();
    }

    public JPanel getPanel() {
        return panel;
    }
}
