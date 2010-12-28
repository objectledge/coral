package org.objectledge.coral.tools;

import org.objectledge.container.LedgeContainer;
import org.objectledge.filesystem.FileSystem;
import org.objectledge.filesystem.FileSystemProvider;
import org.picocontainer.MutablePicoContainer;

public class LedgeContainerHelper
{
    /**
     * Returns a session factory instance.
     * 
     * @param ledgeBaseDir the ledge base directory.
     * @param ledgeConfig TODO
     * @return container instance.
     * @throws Exception if the factory could not be initialized.
     */
    public static MutablePicoContainer getLedgeContainer(String ledgeBaseDir, String ledgeConfig)
        throws Exception
    {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if(cl == null)
        {
            cl = LedgeContainerHelper.class.getClassLoader();
        }
        FileSystemProvider lfs = new org.objectledge.filesystem.
            LocalFileSystemProvider("local", ledgeBaseDir);
        FileSystemProvider cfs = new org.objectledge.filesystem.
            ClasspathFileSystemProvider("classpath", cl);
        FileSystem fs = new FileSystem(new FileSystemProvider[] { lfs, cfs }, 4096, 65536);
        LedgeContainer ledgeContainer = 
            new LedgeContainer(fs, ledgeConfig, cl);
        MutablePicoContainer container = ledgeContainer.getContainer();
        container.registerComponentInstance(ClassLoader.class, cl);
        return container;
    }

}
