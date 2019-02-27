package edu.ucsd;

import com.intellij.psi.PsiFile;
import edu.ucsd.mmenarini.getty.GettyMainKt;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class ClassMethod {
    public String qualifiedClassName;
    public String className;
    public String methodName;
    public List<String> parameterTypes;
    public String returnType;
    public PsiFile declaringFile;

    public String getQualifiedMethodName() {
        return qualifiedClassName +
                ":" +
                methodName;
    }

    public String getMethodSignature() {
        return GettyMainKt.createSignature(this);
    }

}
