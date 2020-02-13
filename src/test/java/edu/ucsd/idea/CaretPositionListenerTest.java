package edu.ucsd.idea;

import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import edu.ucsd.AppState;
import edu.ucsd.ClassMethod;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

@RunWith(JMockit.class)
public class CaretPositionListenerTest {

//    @Tested
//    CaretPositionListener caretPositionListener = new CaretPositionListener();

    @Mocked
    AppState appState;
    @Mocked
    CaretEvent caretEvent;
    @Mocked
    PsiTreeUtil psiTreeUtil;
    @Mocked
    PsiMethod psiMethod;
    @Mocked
    PsiClass psiClass;


    @Test
    public void caretPositionChangedTest() throws InterruptedException {
        //ClassMethod classMethod = new ClassMethod("edu.ucsd.TestClass","TestClass", "testMethod", new ArrayList<>(), null);

        new Expectations() {{
            PsiTreeUtil.getParentOfType((PsiElement) any, PsiMethod.class);
            result = psiMethod;

            PsiTreeUtil.getParentOfType((PsiElement) any, PsiClass.class);
            result = psiClass;

            psiMethod.getName();
            result = "testMethod";

            psiClass.getQualifiedName();
            result = "edu.ucsd.TestClass";

            psiClass.getName();
            result = "TestClass";

            // These two are recorded as expected:
            //AppState.setCurrentClassMethod(classMethod);
            times = 1;
        }};

        //caretPositionListener.caretPositionChanged(caretEvent);

//        TODO: test classmethod qualified method runWithCorrectPythonVersion

// TODO: test the observable
//        TestObserver<ClassMethod> observer = AppState.getCurrentClassMethodObservable()
//                .test()
//                .awaitCount(1)
//                .assertSubscribed()
//                .assertNoErrors()
//                .assertValue(new ClassMethod("sdfsdf", "sdfdsf"));
    }

}