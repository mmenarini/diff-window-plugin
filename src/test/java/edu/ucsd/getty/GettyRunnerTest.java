package edu.ucsd.getty;

import edu.ucsd.util.LogUtils;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.*;

@RunWith(JMockit.class)
public class GettyRunnerTest {

    @Before
    public void setUp() {
        LogUtils.removeLog();
        PropertyConfigurator.configure(this.getClass().getClassLoader().getResource("log4j-test.properties"));
    }

    @Test
    public void runWithCorrectPythonVersion() throws IOException {
        GettyRunner gettyRunner = new GettyRunner(System.getProperty("user.dir"),"src/test/resources/testScript", "python2.7");
        gettyRunner.run("pre", "post", "file");
    }
    @Test

    public void runWithExceptionInPythonCode() throws IOException {
        GettyRunner gettyRunner = new GettyRunner(System.getProperty("user.dir"),"src/test/resources/testScriptWithException", "python2.7");

        try {
            gettyRunner.run("pre", "post", "file");
            fail("No IllegalStateException thrown");
        } catch (IllegalStateException e) {
            assertEquals("The csi script exited with value 1", e.getMessage());
        }
        assertTrue(LogUtils.getLogAsString().get().contains("Hello World!"));
        assertTrue(LogUtils.getLogAsString().get().contains("Exception: I know Python!"));
    }

    @Test
    public void runWithInCorrectPythonVersion() throws IOException {
        GettyRunner gettyRunner = new GettyRunner(System.getProperty("user.dir"),"src/test/resources/testScript", "python3");
        try {
            gettyRunner.run("pre", "post", "file");
            fail("No IllegalStateException thrown");
        } catch (IllegalStateException e) {
            assertEquals("Wrong python version", e.getMessage());
        }
        assertTrue(LogUtils.getLogAsString().get().contains("Python version 2.7 required but major version was 3"));
    }

}