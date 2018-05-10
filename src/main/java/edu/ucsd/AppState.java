package edu.ucsd;

import com.intellij.openapi.vfs.VirtualFile;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

import java.util.ArrayList;
import java.util.List;

public class AppState {
    private static BehaviorSubject<ClassMethod> currentClassMethod = BehaviorSubject.create();

    public static Observable<ClassMethod> getCurrentClassMethodObservable() {
        return currentClassMethod;
    }

    public static void setCurrentClassMethod(ClassMethod classMethod) {
        currentClassMethod.onNext(classMethod);
    }
}
