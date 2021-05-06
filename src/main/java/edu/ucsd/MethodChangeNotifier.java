package edu.ucsd;

import com.intellij.util.messages.Topic;

public interface MethodChangeNotifier {
        Topic<MethodChangeNotifier> METHOD_CHANGE_NOTIFIER_TOPIC = Topic.create("Change Method", MethodChangeNotifier.class);
        void newMethod(ClassMethod method);
}
