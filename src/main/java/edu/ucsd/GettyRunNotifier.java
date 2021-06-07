package edu.ucsd;

import com.intellij.util.messages.Topic;

import java.nio.file.Path;

public interface GettyRunNotifier {
        Topic<GettyRunNotifier> GETTY_RUN_NOTIFIER_TOPIC = Topic.create("Running getty", GettyRunNotifier.class);
        void run();
        void run(ClassMethod method, boolean stopIfRunning);
        void stop();
        void forceStop();
        void started(ClassMethod method);
        void cloning(String repo);
        void cloned(Path repoHead);
        void error(String message);
        void headInferenceStared(ClassMethod method);
        void currentInferenceStarted(ClassMethod method);
        void headInferenceError(ClassMethod method,String message);
        void currentInferenceError(ClassMethod method,String message);
        void headInferenceDone(ClassMethod method);
        void currentInferenceDone(ClassMethod method);
        void done(ClassMethod method);
        void stopped(ClassMethod method);
}
