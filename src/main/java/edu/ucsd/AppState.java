package edu.ucsd;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

import java.nio.file.Path;

public class AppState {
    public static ClassMethod method;
    public static Path headRepoDir;
    private static BehaviorSubject<ClassMethod> currentClassMethod = BehaviorSubject.create();

    public static Observable<ClassMethod> getCurrentClassMethodObservable() {
        return currentClassMethod;
    }
    public static void triggerObservables() {
        if (method!=null)
            currentClassMethod.onNext(method);
    }
    public static void setCurrentClassMethod(ClassMethod classMethod) {
        method = classMethod;
        currentClassMethod.onNext(classMethod);
    }
}
