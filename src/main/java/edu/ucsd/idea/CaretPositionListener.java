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
import edu.ucsd.properties.Properties;
import edu.ucsd.properties.PropertiesForm;
import edu.ucsd.properties.PropertiesService;
import edu.ucsd.reinfer.ReInferPriority;
import io.reactivex.disposables.Disposable;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class CaretPositionListener implements CaretListener {
    private ExecutorService execService = Executors.newFixedThreadPool(1);
    private PsiFile lastPsiFile=null;

    public CaretPositionListener(){
    }
    @Override
    public void caretPositionChanged(CaretEvent e) {
      try {
          log.debug("position changed from {} to {}", e.getOldPosition(), e.getNewPosition());

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
            String projectPath = project.getBasePath();
//            gettyRunner = new GettyRunner(
//                  project,
//                  projectPath);
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

    private List<String> extractParameterTypes(PsiMethod method) {
        List<String> result = new ArrayList<>();

        PsiParameter[] parameters = method.getParameterList().getParameters();

        for (int i = 0; i < parameters.length; i++) {
            result.add(parameters[i].getType().getCanonicalText());//.getPresentableText());
        }

        return result;
    }
}
