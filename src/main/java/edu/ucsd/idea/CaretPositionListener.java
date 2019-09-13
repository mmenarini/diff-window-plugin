package edu.ucsd.idea;

import com.intellij.lang.PsiBuilderUtil;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.openapi.vcs.FileStatusManager;
import com.intellij.psi.*;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import edu.ucsd.AppState;
import edu.ucsd.ClassMethod;
import edu.ucsd.getty.GettyRunner;
import edu.ucsd.mmenarini.getty.GettyMainKt;
import edu.ucsd.properties.PropertiesForm;
import edu.ucsd.reinfer.ReInferPriority;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class CaretPositionListener implements CaretListener {
    private long modCount=-1;
    private ExecutorService execService = Executors.newFixedThreadPool(1);
    private GettyRunner gettyRunner;
    private PsiFile lastPsiFile=null;
    //private boolean hadError = true;


    public String getFullReturnType(String input) {
        if (input==null)
            input="void";
        else
            switch (input) {
                case "byte":
                case "short":
                case "int":
                case "long":
                case "float":
                case "double":
                case "char":
                case "boolean":
                    input="java.lang."+input;
            }
        return input;
    }

    @Override
    public void caretPositionChanged(CaretEvent e) {
      try {
          log.debug("position changed from {} to {}", e.getOldPosition(), e.getNewPosition());
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

          if(currPsiFile!=lastPsiFile) {
            lastPsiFile = currPsiFile ;
            //hadError=true;
            modCount = -1;
            String projectPath = project.getBasePath();
            gettyRunner = new GettyRunner(
                  project,
                  projectPath);
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
          //If indexes are being built fail early;
          currMethod.getReturnType().getCanonicalText();

          ArrayList<String> parameterTypes = extractParameterTypes(currMethod);
          for (int index = 0; index < parameterTypes.size(); index++) {
              String paramType = parameterTypes.get(index);
              parameterTypes.set(index, getFullReturnType(paramType));
          }

          log.info("currClass {} currMethod {} parameterTypes {}", currClass.getName(), currMethod.getName(), parameterTypes);

//        TODO: what to do if there are multiple classes with the same name?
          String retType = currMethod.getReturnType().getCanonicalText();
          String fullRetType = getFullReturnType(retType);

          ClassMethod classMethod =
                  new ClassMethod(
                          currClass.getQualifiedName(),
                          currClass.getName(),
                          currMethod.getName(),
                          parameterTypes,
                          fullRetType,
                          currPsiFile);

//          long curModCount = currPsiFile.getManager().getModificationTracker().getModificationCount();
//          boolean curError = PsiTreeUtil.hasErrorElements(currPsiFile);
//          if(!curError && curModCount>modCount){
//              modCount = curModCount;
//              execService.submit(() -> {
//                  try {
//                      if (classMethod.getMethodSignature()!=null) {
//                          gettyRunner.run(classMethod);
//                          AppState.setCurrentClassMethod(classMethod);
//                      }
//                  } catch (IOException ex) {
//                      log.error("Getty failed:", ex);
//                  }
//              });
//          }

          if (AppState.method==null || !classMethod.getMethodSignature().equals(AppState.method.getMethodSignature())) {
              AppState.setCurrentClassMethod(classMethod);
              log.warn("classMethod: {}", classMethod.getMethodSignature());

/*
              FileStatusManager fileStatusManager = FileStatusManager.getInstance(project);
              FileStatus status = fileStatusManager.getStatus(currPsiFile.getVirtualFile());

              if (!FileStatus.NOT_CHANGED.equals(status)) {
                  log.warn("adding re-infer for {}", classMethod.getQualifiedMethodName());
                  ReInferPriority.getInstance().addClassMethod(classMethod);
              }

              log.warn("file status: {}", status);
*/
          }
      } catch(Exception ex) {
          log.debug(ex.getMessage(), ex);
      }
    }

    private ArrayList<String> extractParameterTypes(PsiMethod method) {
        ArrayList<String> result = new ArrayList<>();

        PsiParameter[] parameters = method.getParameterList().getParameters();

        for (int i = 0; i < parameters.length; i++) {
            result.add(parameters[i].getType().getCanonicalText());//.getPresentableText());
        }

        return result;
    }
}
