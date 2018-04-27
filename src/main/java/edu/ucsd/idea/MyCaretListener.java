package edu.ucsd.idea;

import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import edu.ucsd.AppState;
import edu.ucsd.ClassMethod;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MyCaretListener implements CaretListener {

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

        log.info("currClass {} currMethod {}", currClass.getName(), currMethod.getName());

//        TODO: what to do if there are multiple classes with the same name?
        ClassMethod classMethod = new ClassMethod(currClass.getName(), currMethod.getName());

        AppState.setCurrentClassMethod(classMethod);
    }
}
