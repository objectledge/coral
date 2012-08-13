package org.objectledge.coral.tools;

import java.util.Arrays;

import junit.framework.TestCase;

import org.objectledge.filesystem.FileSystem;

public class ScriptLoaderTest
    extends TestCase
{
    private String one;

    private byte[] two;

    public void testLoader()
        throws Exception
    {
        ScriptLoader.loadScripts(this, null, FileSystem.getClasspathFileSystem());
        assertEquals("SELECT 1 = 1\n", one);
        assertTrue(Arrays.equals(new byte[] { 32, 32, 32, 32, 32 }, two));
    }
}
