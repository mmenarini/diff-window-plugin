package edu.ucsd;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.EditorEventMulticaster;
import edu.ucsd.idea.CaretPositionToMethod;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.PropertyConfigurator;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class InitializePlugin implements ApplicationComponent {
    @Override
    public void initComponent() {
        System.out.println(".....Initializing log4j.....");

        PropertyConfigurator.configure(this.getClass().getClassLoader().getResource("log4j.properties"));

        log.warn("initializing caret listener...");

//        listener
//        CaretPositionToMethod caretListener = new CaretPositionToMethod();
//
//        EditorEventMulticaster eventMulticaster = EditorFactory.getInstance().getEventMulticaster();
//        eventMulticaster.addCaretListener(caretListener);

        log.warn("created caret listener");
    }

    @Override
    public void disposeComponent() {

    }

    @NotNull
    @Override
    public String getComponentName() {
        return "Invariants Diff Tool Window";
    }
}
