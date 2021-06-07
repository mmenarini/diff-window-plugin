package edu.ucsd;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.messages.MessageBus;
import edu.ucsd.getty.GettyRunner;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;

@Slf4j
public class AppState implements Disposable {
    private final MethodChangeNotifier methodChangePublisher;
    private boolean isGettyRunning = false;
    public ClassMethod method;
    private final GettyRunner gettyRunner;


    public AppState(Project project){
        MessageBus messageBus = project.getMessageBus();
        methodChangePublisher = messageBus.syncPublisher(MethodChangeNotifier.METHOD_CHANGE_NOTIFIER_TOPIC);
        GettyRunNotifier gettyRunPublisher = messageBus.syncPublisher(GettyRunNotifier.GETTY_RUN_NOTIFIER_TOPIC);
        gettyRunner = new GettyRunner(project);
        Disposer.register(this, gettyRunner);
        messageBus.connect().subscribe(GettyRunNotifier.GETTY_RUN_NOTIFIER_TOPIC, new GettyRunNotifier() {
            @Override
            public void run() {
                if (method!=null)
                    runGetty(method,false);
            }

            @Override
            public void run(ClassMethod method, boolean stopIfRunning) {
                runGetty(method,stopIfRunning);
            }

            @Override
            public void stop() {
                stopGetty(false);
            }

            @Override
            public void forceStop() {
                stopGetty(true);
            }

            @Override
            public void started(ClassMethod method) {

            }

            @Override
            public void cloning(String repo) {

            }

            @Override
            public void cloned(Path repoHead) {

            }

            @Override
            public void error(String message) {

            }

            @Override
            public void headInferenceStared(ClassMethod method) {

            }

            @Override
            public void currentInferenceStarted(ClassMethod method) {

            }

            @Override
            public void headInferenceError(ClassMethod method, String message) {

            }

            @Override
            public void currentInferenceError(ClassMethod method, String message) {

            }

            @Override
            public void headInferenceDone(ClassMethod method) {

            }

            @Override
            public void currentInferenceDone(ClassMethod method) {

            }

            @Override
            public void done(ClassMethod method) {
                synchronized (gettyRunner) {
                    isGettyRunning = false;
                }
            }

            @Override
            public void stopped(ClassMethod method) {

            }
        });
    }

    public void setCurrentClassMethod(ClassMethod classMethod) {
        if(classMethod.equals(method)) return;
        method = classMethod;
        methodChangePublisher.newMethod(method);
    }

    public boolean runGetty(ClassMethod method, boolean breakIfRunning) {
        //We rerun the inference if the cared is in one method
        boolean doRestart = false;
        synchronized (gettyRunner){
            if (isGettyRunning && !breakIfRunning) return false;
            if (isGettyRunning)
                doRestart=true;
            isGettyRunning=true;
        }
        if (method!=null && method.getMethodSignature()!=null)
            if(doRestart)
                gettyRunner.stopAndRun(method);
            else
                gettyRunner.run(method);

        return true;
    }

    public void stopGetty(boolean force) {
        synchronized (gettyRunner){
            if (force || isGettyRunning) {
                gettyRunner.stop();
                isGettyRunning = false;
            }
        }
    }

    @Override
    public void dispose() {
        stopGetty(true);
    }
}
