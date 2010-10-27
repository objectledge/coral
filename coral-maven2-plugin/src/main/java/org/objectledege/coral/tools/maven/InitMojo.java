package org.objectledege.coral.tools.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.objectledge.coral.tools.init.InitComponent;
import org.objectledge.filesystem.FileSystem;

/**
 * Initializes a database schema for use as Coral store.
 * 
 * @author rafal
 * @goal init
 */
public class InitMojo
    extends AbstractDbMojo
{

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        FileSystem fileSystem = FileSystem.getClasspathFileSystem();
        initDataSource();
        InitComponent init = new InitComponent(dataSource, fileSystem);
        try 
        {
            init.run();
        }
        catch(Exception e)
        {
            throw new MojoExecutionException("database initialization failed", e);
        }
    }
}
