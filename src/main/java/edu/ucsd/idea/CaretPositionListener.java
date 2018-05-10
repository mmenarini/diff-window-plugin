package edu.ucsd.idea;

import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import edu.ucsd.AppState;
import edu.ucsd.ClassMethod;
import edu.ucsd.reinfer.ReInferPriority;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CaretPositionListener implements CaretListener {

    @Override
    public void caretPositionChanged(CaretEvent e) {
        log.info("position changed from {} to {}", e.getOldPosition(), e.getNewPosition());

//        Find current class and method and update the AppState

        Editor currEditor = e.getEditor();
        Project project = currEditor.getProject();
        if (project == null) {
            log.error("project was null");
            return;
        }

        PsiFile currPsiFile = PsiDocumentManager.getInstance(currEditor.getProject()).getPsiFile(currEditor.getDocument());
        if (currPsiFile == null) {
            log.error("currPsiFile was null");
            return;
        }

        Caret caret = e.getCaret();
        if (caret == null) {
            log.error("caret was null");
            return;
        }
        PsiElement element = currPsiFile.findElementAt(caret.getOffset());

        PsiClass currClass = PsiTreeUtil.getParentOfType(element, PsiClass.class);
        if (currClass == null) {
            log.error("currClass was null");
            return;
        }

        PsiMethod currMethod = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        if (currMethod == null) {
            log.error("currMethod was null");
            return;
        }

        List<String> parameterTypes = extractParameterTypes(currMethod);

        log.info("currClass {} currMethod {} parameterTypes {}", currClass.getName(), currMethod.getName(), parameterTypes);

//        TODO: what to do if there are multiple classes with the same name?
        ClassMethod classMethod = new ClassMethod(currClass.getQualifiedName(), currClass.getName(), currMethod.getName(), parameterTypes);

        AppState.setCurrentClassMethod(classMethod);

        FileStatusManager fileStatusManager = FileStatusManager.getInstance(project);
        FileStatus status = fileStatusManager.getStatus(currPsiFile.getVirtualFile());

        if (!FileStatus.NOT_CHANGED.equals(status)) {
            log.warn("adding re-infer for {}", classMethod.getQualifiedMethodName());
            ReInferPriority.getInstance().addClassMethod(classMethod);
        }

        log.warn("file status: {}", status);

    }

    private List<String> extractParameterTypes(PsiMethod method) {
        List<String> result = new ArrayList<>();

        PsiParameter[] parameters = method.getParameterList().getParameters();

        for (int i = 0; i < parameters.length; i++) {
            result.add(parameters[i].getType().getPresentableText());
        }

        return result;
    }
}
