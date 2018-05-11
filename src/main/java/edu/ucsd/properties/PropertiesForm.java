package edu.ucsd.properties;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class PropertiesForm {
    private JTextField gettyPathField;
    private JPanel panel;
    private JLabel gettyPathLabel;

    public PropertiesForm(@Nullable Project project) {
    }

    public void setGettyPath(String path) {
        this.gettyPathField.setText(path);
    }

    public String getGettyPath() {
        return gettyPathField.getText();
    }

    public JPanel getPanel() {
        return panel;
    }
}
