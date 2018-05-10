package edu.ucsd.reinfer;

import edu.ucsd.ClassMethod;

import java.util.LinkedList;
import java.util.List;

public class ReInferPriority {
    private List<String> priorityList = new LinkedList<>();

    private static ReInferPriority singleton;

    public static ReInferPriority getInstance() {
        if (singleton == null) {
            singleton = new ReInferPriority();
        }
        return singleton;
    }

    public void addClassMethod(ClassMethod classMethod) {
        priorityList.remove(classMethod.getQualifiedMethodName());
        priorityList.add(0, classMethod.getQualifiedMethodName());
    }

    public List<String> getPriorityList() {
        return priorityList;
    }

    public void reset() {
        priorityList.clear();
    }

}
