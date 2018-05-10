package edu.ucsd;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class ClassMethod {
    private String qualifiedClassName;
    private String className;
    private String methodName;
    private List<String> parameterTypes;

    public String getQualifiedMethodName() {
        return qualifiedClassName +
                ":" +
                methodName;
    }
}
