package edu.ucsd.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
@AllArgsConstructor
public class Properties {
//    public static final String GETTY_PATH = "gettyPath";
//    public static final String PYTHON_PATH = "pythonPath";
    public static final String DEBUG_LOG_PATH = "debugLogPath";
    public static final String STACK_TRACE_PATH = "stackTracePath";
    public static final String CLEAN_BEFORE_RUNNING_PATH = "cleanBeforeRunningPath";
    public static final String REMOVE_WORK_BEFORE_RUNNING_PATH = "removeWorkBeforeRunningPath";
    public static final String DO_NOT_AUTORUN_PATH = "doNotAutorunPath";

//    private String gettyPath;
//    private String pythonPath;
    private boolean debugLog;
    private boolean stackTrace;
    private boolean cleanBeforeRunning;
    private boolean removeWorkBeforeRunning;
    private boolean doNotAutorun;
//    public boolean isRequiredFieldEmpty() {
//        return StringUtils.isEmpty(gettyPath) ||
//                StringUtils.isEmpty(pythonPath);
//    }
}
