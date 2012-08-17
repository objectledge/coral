package org.objectledege.coral.tools.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.objectledge.coral.tools.init.InitComponent;
import org.objectledge.database.DatabaseUtils;
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
    /**
     * Should existing data be dropped?
     * 
     * @parameter expression="${force}" default-value="false"
     */
    private boolean force;
    
    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        FileSystem fileSystem = FileSystem.getClasspathFileSystem();
        initDataSource();
        try 
        {
            InitComponent init = new InitComponent(dataSource, fileSystem, force,
                new MavenDNALogger(getLog()));
            init.run();

            getLog().info("disconnecting from the db");
            DatabaseUtils.shutdown(dataSource);
        }
        catch(Exception e)
        {
            throw new MojoExecutionException("database initialization failed", e);
        }
    }
}
