package edu.ucsd;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

public class AppState {
    private static BehaviorSubject<ClassMethod> currentClassMethod = BehaviorSubject.create();

    public static Observable<ClassMethod> getCurrentClassMethodObservable() {
        return currentClassMethod;
    }

    public static void setCurrentClassMethod(ClassMethod classMethod) {
        currentClassMethod.onNext(classMethod);
    }
}
