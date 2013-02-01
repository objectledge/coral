package org.objectledge.coral.tools.extract;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.objectledge.filesystem.FileSystem;

public class FileExtractionComponentTest
    extends TestCase
{
    public void testExtratction()
        throws Exception
    {
        FileSystem cfs = FileSystem.getClasspathFileSystem();
        FileExtractionComponent extractor = new FileExtractionComponent(cfs);

        final File outputBase = new File("target/test/extract");
        outputBase.mkdirs();
        extractor.run("extract", outputBase, "UTF-8",
            Collections.<String, Object> singletonMap("variable", "value"),
            Collections.<String> emptyList());

        assertContents(new File(outputBase, "file1.txt"), "file1");
        assertContents(new File(outputBase, "file2.txt"), "file2 value");
        assertContents(new File(new File(outputBase, "nested"), "file3.txt"), "file3");
    }

    private void assertContents(File file, String contents)
        throws IOException
    {
        assertTrue(file.exists());
        assertEquals(contents, FileUtils.readFileToString(file, "UTF-8"));
    }
}
