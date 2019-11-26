package edu.ucsd.getty;

import edu.ucsd.util.LogUtils;
import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.*;


public class GettyInvariantsFilesRetrieverTest {

    private static final String GStack_isFull = "_getty_inv__GStack_isFull----";
    private static final String GStack_isEmpty = "_getty_inv__GStack_isEmpty----";
    private static final String GStack_Constructor = "_getty_inv__GStack_--init----";
    private static final String GStack_push = "_getty_inv__GStack_push--Object--";
    private static final String GStack_fakePush = "_getty_inv__GStack_fakePush--Object----int----Object--";
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
        folder.newFile(GStack_push + _a562db1);
        folder.newFile(GStack_Constructor +"--" + _a562db1);
        folder.newFile(GStack_Constructor +"int--" + _a562db1);
        folder.newFile(GStack_fakePush + _a562db1);
        //TODO: Fix test
        //filesRetriever = new GettyInvariantsFilesRetriever(folder.getRoot());
    }

    @Test
    public void getFilesByClassname() {
        Optional<List<File>> optionalFiles = filesRetriever.getFiles("GStack");
        assertNotEquals(Optional.empty(), optionalFiles);

        List<File> files = optionalFiles.get();
        List<String> fileNames = files.stream().map(File::getName).collect(Collectors.toList());

        assertEquals(7, files.size());
        assertTrue(fileNames.contains(GStack_push + _a562db1));
        assertTrue(fileNames.contains(GStack_isEmpty + _19f4281));
        assertTrue(fileNames.contains(GStack_Constructor +"--" + _a562db1));
        assertTrue(fileNames.contains(GStack_fakePush + _a562db1));
        assertTrue(fileNames.contains(GStack_isFull + _19f4281));
        assertTrue(fileNames.contains(GStack_Constructor + "int--" + _a562db1));
        assertTrue(fileNames.contains(GStack_isFull + _a562db1));

        files.forEach(file -> {
//            System.out.println(file.getName() + "   matches: "+file.getName().matches(".+_\\.inv\\.out$"));
            assertTrue(file.getName().matches(".+_\\.inv\\.out$"));
        });
    }

    @Test
    public void getFilesByClassnameAndMethodName() {
        Optional<List<File>> optionalFiles = filesRetriever.getFiles("GStack", "isFull", new ArrayList<>());
        assertNotEquals(Optional.empty(), optionalFiles);

        List<File> files = optionalFiles.get();
        List<String> fileNames = files.stream().map(File::getName).collect(Collectors.toList());

        assertEquals(2, files.size());
        assertTrue(fileNames.contains(GStack_isFull + _19f4281));
        assertTrue(fileNames.contains(GStack_isFull + _a562db1));
    }

    @Test
    public void getFilesByClassnameMethodNameAndHashCode() {
        Optional<List<File>> optionalFiles = filesRetriever.getFiles("GStack", "isFull", new ArrayList<>(), "19f4281");
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

    @Test
    public void testConstructor() {
        Optional<List<File>> optionalFiles = filesRetriever.getFiles("GStack", "GStack", new ArrayList<>());
        assertNotEquals(Optional.empty(), optionalFiles);

        List<File> files = optionalFiles.get();
        assertEquals(1, files.size());
        assertEquals(GStack_Constructor + "--" + _a562db1, files.get(0).getName());
    }

    @Test
    public void testConstructorWithParameter() {
        ArrayList<String> parameterTypes = new ArrayList<>();
        parameterTypes.add("int");
        Optional<List<File>> optionalFiles = filesRetriever.getFiles("GStack", "GStack", parameterTypes);
        assertNotEquals(Optional.empty(), optionalFiles);

        List<File> files = optionalFiles.get();
        assertEquals(1, files.size());
        assertEquals(GStack_Constructor + "int--" + _a562db1, files.get(0).getName());
    }

    @Test
    public void testMethodWithParameter() {
        ArrayList<String> parameterTypes = new ArrayList<>();
        parameterTypes.add("Object");
        Optional<List<File>> optionalFiles = filesRetriever.getFiles("GStack", "push", parameterTypes);
        assertNotEquals(Optional.empty(), optionalFiles);

        List<File> files = optionalFiles.get();
        assertEquals(1, files.size());
        assertEquals(GStack_push + _a562db1, files.get(0).getName());
    }

    @Test
    public void testMethodWithMultipleParameters() {
        ArrayList<String> parameterTypes = new ArrayList<>();
        parameterTypes.add("Object");
        parameterTypes.add("int");
        parameterTypes.add("Object");
        Optional<List<File>> optionalFiles = filesRetriever.getFiles("GStack", "fakePush", parameterTypes);
        assertNotEquals(Optional.empty(), optionalFiles);

        List<File> files = optionalFiles.get();
        assertEquals(1, files.size());
        assertEquals(GStack_fakePush + _a562db1, files.get(0).getName());
    }

}