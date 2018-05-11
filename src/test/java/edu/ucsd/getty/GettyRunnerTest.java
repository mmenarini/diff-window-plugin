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
    @Tested
    GettyRunner gettyRunner = new GettyRunner("bla/csi.py", "python2.7");

    @Before
    public void setUp() {
        LogUtils.removeLog();
        PropertyConfigurator.configure(this.getClass().getClassLoader().getResource("log4j-test.properties"));
    }

    @Test
    public void name() throws IOException {
        try {
            gettyRunner.run("pre", "post", "file");
            fail("No IllegalStateException thrown");
        } catch (IllegalStateException e) {
            assertEquals("The csi script exited with value 2", e.getMessage());
        }
        assertTrue(LogUtils.getLogAsString().get().contains("can't open file 'bla/csi.py': [Errno 2] No such file or directory"));
    }
}