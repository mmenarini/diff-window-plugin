package edu.ucsd;

import edu.ucsd.getty.GettyRunner;
import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
@Slf4j
public class AppState {
    private static ExecutorService execService = Executors.newFixedThreadPool(1);
    private static boolean isGettyRunning = false;
    public static ClassMethod method;
    public static Path headRepoDir;
//    public static boolean debugGradlePlugin = true;
//    public static boolean stacktraceGradlePlugin = true;
    private static BehaviorSubject<ClassMethod> currentClassMethod = BehaviorSubject.create();
    public static Observable<ClassMethod> getCurrentClassMethodObservable() {
        return currentClassMethod;
    }
    public static Path getHeadRepoInvariantsCache() {
        return headRepoDir.resolve("_invariants_cache");
    }


    public static void triggerObservables() {
        if (method!=null)
            currentClassMethod.onNext(method);
    }
    public static void setCurrentClassMethod(ClassMethod classMethod) {
        method = classMethod;
        currentClassMethod.onNext(classMethod);
    }

    public static boolean runGetty(GettyRunner gettyRunner, ClassMethod method) {
        return runGetty(gettyRunner, method, false);
    }

    public static boolean runGetty(GettyRunner gettyRunner, ClassMethod method, boolean doDisposeGettyRunner) {
        //We rerun the inference if the cared is in one method
        synchronized (execService){
            if (isGettyRunning) return false;
            isGettyRunning=true;
        }
        execService.submit(() -> {
            try {
                if (method!=null && method.getMethodSignature()!=null)
                    gettyRunner.run(method);
            } catch (IOException e) {
                log.error("Getty failed:", e);
            } finally {
                synchronized (execService){
                    isGettyRunning=false;
                }
                if (doDisposeGettyRunner)
                    gettyRunner.dispose();
            }
        });
        return true;
    }
}
