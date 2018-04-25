package edu.ucsd.diff;

import com.intellij.mock.MockProject;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.impl.ToolWindowHeadlessManagerImpl;
import com.intellij.util.pico.DefaultPicoContainer;
import edu.ucsd.factory.PanelFactory;
import org.junit.Before;
import org.junit.Test;
import org.picocontainer.PicoContainer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DiffWindowTest {

    @Before
    public void before() {
        System.out.println("before");
    }


    @Test
    public void updateTab() {
//        PicoContainer picoContainer = new DefaultPicoContainer();
//        Disposable disposable = Disposer.newDisposable();
//        Project project = new MockProject(picoContainer, disposable);
//        ToolWindow toolWindow = new ToolWindowHeadlessManagerImpl.MockToolWindow(project);
//        PanelFactory factory = new PanelFactory(project);
//
//        List<DiffTab> tabsList = new ArrayList<>();
//        DiffTab tab1 = new DiffTab("Test tab", "pre\ncontent", "post\ncontent\nnew line", factory);
//        tabsList.add(tab1);
//        DiffTab tab2 = new DiffTab("Test tab 2", "", "", factory);
//        tabsList.add(tab2);
//
//
//        DiffWindow diffWindow = new DiffWindow(toolWindow, tabsList);
//        DiffTab tab = diffWindow.getTab("Test tab").orElseThrow(null);
//        assertEquals(tab, tab1);
    }
}