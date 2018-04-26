package edu.ucsd.getty;

import edu.ucsd.util.LogUtils;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;


public class GettyInvariantsFilesRetrieverTest {

    private static final String GStack_isFull = "_getty_inv__GStack_isFull----";
    private static final String GStack_isEmpty = "_getty_inv__GStack_isEmpty----";
    private static final String _19f4281 = "__19f4281_.inv.out";
    private static final String _a562db1 = "__a562db1_.inv.out";
    private static final String html = ".html";


    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    GettyInvariantsFilesRetriever filesRetriever;

    @Before
    public void setUp() throws Exception {
        LogUtils.removeLog();
        PropertyConfigurator.configure(this.getClass().getClassLoader().getResource("log4j-test.properties"));

        folder.newFile(GStack_isEmpty + _19f4281);
        folder.newFile(GStack_isEmpty + _19f4281 + html);
        folder.newFile(GStack_isFull + _19f4281);
        folder.newFile(GStack_isFull + _19f4281 + html);
        folder.newFile(GStack_isFull + _a562db1 + html);
        folder.newFile(GStack_isFull + _a562db1);

        filesRetriever = new GettyInvariantsFilesRetriever(folder.getRoot());
    }

    @Test
    public void getFilesByClassname() {
        Optional<List<File>> optionalFiles = filesRetriever.getFiles("GStack");
        assertNotEquals(Optional.empty(), optionalFiles);

        List<File> files = optionalFiles.get();
        assertEquals(3, files.size());
        assertEquals(GStack_isEmpty + _19f4281, files.get(0).getName());
        assertEquals(GStack_isFull + _19f4281, files.get(1).getName());
        assertEquals(GStack_isFull + _a562db1, files.get(2).getName());

        files.forEach(file -> {
//            System.out.println(file.getName() + "   matches: "+file.getName().matches(".+_\\.inv\\.out$"));
            assertTrue(file.getName().matches(".+_\\.inv\\.out$"));
        });
    }

    @Test
    public void getFilesByClassnameAndMethodName() {
        Optional<List<File>> optionalFiles = filesRetriever.getFiles("GStack", "isFull");
        assertNotEquals(Optional.empty(), optionalFiles);

        List<File> files = optionalFiles.get();
        assertEquals(2, files.size());
        assertEquals(GStack_isFull + _19f4281, files.get(0).getName());
        assertEquals(GStack_isFull + _a562db1, files.get(1).getName());
    }

    @Test
    public void getFilesByClassnameMethodNameAndHashCode() {
        Optional<List<File>> optionalFiles = filesRetriever.getFiles("GStack", "isFull", "19f4281");
        assertNotEquals(Optional.empty(), optionalFiles);

        List<File> files = optionalFiles.get();
        assertEquals(1, files.size());
        assertEquals(GStack_isFull + _19f4281, files.get(0).getName());
    }

    @Test
    public void getFilesByNonExistingClassName() {
        Optional<List<File>> optionalFiles = filesRetriever.getFiles("whatever");
        assertNotEquals(Optional.empty(), optionalFiles);

        List<File> files = optionalFiles.get();
        assertEquals(0, files.size());
    }

    @Test
    public void getFilesByNonExistingMethodName() {
        Optional<List<File>> optionalFiles = filesRetriever.getFiles("GStack", "whatever");
        assertNotEquals(Optional.empty(), optionalFiles);

        List<File> files = optionalFiles.get();
        assertEquals(0, files.size());
    }

    @Test
    public void getFilesFromNonExistingFolder() {
        folder.delete();
        Optional<List<File>> optionalFiles = filesRetriever.getFiles("whatever");
        assertEquals(Optional.empty(), optionalFiles);
        assertTrue(LogUtils.getLogAsString().get().contains("was empty or rendered an I/O error"));
    }

}