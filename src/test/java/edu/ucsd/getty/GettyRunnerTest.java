package edu.ucsd.getty;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import edu.ucsd.util.LogUtils;
import mockit.integration.junit4.JMockit;
import org.apache.log4j.PropertyConfigurator;
import org.jdom.JDOMException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static java.lang.Thread.sleep;
import static org.junit.Assert.*;

@RunWith(JMockit.class)
public class GettyRunnerTest {
public Project proj;
    @Before
    public void setUp() throws IOException, JDOMException {
        LogUtils.removeLog();
        PropertyConfigurator.configure(this.getClass().getClassLoader().getResource("log4j-test.properties"));
        proj = ProjectManager.getInstance().loadAndOpenProject(System.getProperty("user.dir"));
    }

    @Test
    public void runWithCorrectPythonVersion() throws IOException {
        GettyRunner gettyRunner = new GettyRunner(proj, "src/test/resources/testScript", true, true);
        //gettyRunner.run("pre", "post", "file");
    }

    @Test
    public void runWithExceptionInPythonCode() throws IOException, InterruptedException {
        GettyRunner gettyRunner = new GettyRunner(proj, "src/test/resources/testScriptWithException", true, true);

        try {
            //gettyRunner.run("pre", "post", "file");
            sleep(100);
//            fail("No IllegalStateException thrown");
        } catch (IllegalStateException e) {
            assertEquals("The csi script exited with value 1", e.getMessage());
        }
        assertTrue(LogUtils.getLogAsString().get().contains("Hello World!"));
        assertTrue(LogUtils.getLogAsString().get().contains("Exception: I know Python!"));
    }

    @Test
    public void runWithInCorrectPythonVersion() throws IOException {
        GettyRunner gettyRunner = new GettyRunner(proj, "src/test/resources/testScript", true, true);
        try {
            //gettyRunner.run("pre", "post", "file");
            fail("No IllegalStateException thrown");
        } catch (IllegalStateException e) {
            assertEquals("Wrong python version", e.getMessage());
        }
        assertTrue(LogUtils.getLogAsString().get().contains("Python version 2.7 required but major version was 3"));
    }

}