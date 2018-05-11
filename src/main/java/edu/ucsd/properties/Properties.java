package edu.ucsd.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
@AllArgsConstructor
public class Properties {
    public static final String GETTY_PATH = "gettyPath";
    public static final String PYTHON_PATH = "pythonPath";
    private String gettyPath;
    private String pythonPath;

    public boolean isRequiredFieldEmpty() {
        return StringUtils.isEmpty(gettyPath) ||
                StringUtils.isEmpty(pythonPath);
    }
}
