package edu.ucsd.idea;

import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import edu.ucsd.ClassMethod;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class CaretPositionToMethod {
    private PsiFile lastPsiFile=null;

    public ClassMethod caretPositionChanged(CaretEvent e) {
      try {
          log.debug("position changed from {} to {}", e.getOldPosition(), e.getNewPosition());

          Editor currEditor = e.getEditor();
          Project project = currEditor.getProject();

          if (project == null) {
              log.error("project was null");
              return null;
          }

          PsiFile currPsiFile = PsiDocumentManager.getInstance(currEditor.getProject()).getPsiFile(currEditor.getDocument());
          if (currPsiFile == null) {
              log.error("currPsiFile was null");
              return null;
          }

          if(currPsiFile!=lastPsiFile)
            lastPsiFile = currPsiFile ;

          Caret caret = e.getCaret();
          if (caret == null) {
              log.error("caret was null");
              return null;
          }

          PsiElement element = currPsiFile.findElementAt(caret.getOffset());

          PsiClass currClass = PsiTreeUtil.getParentOfType(element, PsiClass.class);
          if (currClass == null) {
              log.error("currClass was null");
              return null;
          }

          PsiMethod currMethod = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
          if (currMethod == null) {
              log.error("currMethod was null");
              return null;
          }
          //If indexes are being built fail early;
          currMethod.getReturnType().getCanonicalText();

          List<String> parameterTypes = extractParameterTypes(currMethod);

          log.info("currClass {} currMethod {} parameterTypes {}", currClass.getName(), currMethod.getName(), parameterTypes);

//        TODO: what to do if there are multiple classes with the same name?
          String retType = currMethod.getReturnType().getCanonicalText();
          if (retType==null)
              retType="void";
          else
              switch (retType) {
                  case "byte":
                  case "short":
                  case "int":
                  case "long":
                  case "float":
                  case "double":
                  case "char":
                  case "boolean":
                      retType="java.lang."+retType;
              }


          ClassMethod classMethod =
                  new ClassMethod(
                          currClass.getQualifiedName(),
                          currClass.getName(),
                          currMethod.getName(),
                          parameterTypes,
                          retType,
                          currPsiFile);
          return classMethod;
      } catch(Exception ex) {
          log.debug(ex.getMessage(), ex);
      }
      return null;
    }

    private List<String> extractParameterTypes(PsiMethod method) {
        List<String> result = new ArrayList<>();
        PsiParameter[] parameters = method.getParameterList().getParameters();

        for (int i = 0; i < parameters.length; i++) {
            result.add(parameters[i].getType().getCanonicalText());//.getPresentableText());
        }

        return result;
    }
}
