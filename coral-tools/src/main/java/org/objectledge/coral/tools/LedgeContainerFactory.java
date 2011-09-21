package org.objectledge.coral.tools;

import java.io.IOException;
import java.util.Map;

import org.nanocontainer.integrationkit.PicoCompositionException;
import org.objectledge.container.LedgeContainer;
import org.objectledge.filesystem.FileSystem;
import org.objectledge.filesystem.FileSystemProvider;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoRegistrationException;

public class LedgeContainerFactory
{
    /**
     * Returns a session factory instance.
     * 
     * @param ledgeBaseDir the ledge base directory.
     * @param ledgeConfig TODO
     * @param componentInstances TODO
     * @return container instance.
     * @throws Exception if the factory could not be initialized.
     */
    public static MutablePicoContainer newLedgeContainer(String ledgeBaseDir, String ledgeConfig,
        Map<Object, Object> componentInstances)
        throws IOException, ClassNotFoundException, PicoCompositionException,
        PicoRegistrationException
    {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if(cl == null)
        {
            cl = LedgeContainerFactory.class.getClassLoader();
            Thread.currentThread().setContextClassLoader(cl);
        }
        FileSystemProvider lfs = new org.objectledge.filesystem.LocalFileSystemProvider("local",
            ledgeBaseDir);
        FileSystemProvider cfs = new org.objectledge.filesystem.ClasspathFileSystemProvider(
            "classpath", cl);
        FileSystem fs = new FileSystem(new FileSystemProvider[] { lfs, cfs }, 4096, 65536);
        LedgeContainer ledgeContainer = new LedgeContainer(fs, ledgeConfig, cl, componentInstances);
        MutablePicoContainer container = ledgeContainer.getContainer();
        container.registerComponentInstance(ClassLoader.class, cl);
        return container;
    }

}
