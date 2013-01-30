package org.objectledege.coral.tools.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.objectledge.coral.tools.sql.SqlRunnerComponent;
import org.objectledge.filesystem.FileSystem;

/**
 * Executes a set of SQL scripts on a specified database.
 * 
 * @author rafal
 * @goal run-sql
 */
public class SqlRunnerMojo
    extends AbstractDbMojo
{
    /**
     * Base directory for looking up sources lists and source files.
     * 
     * @parameter default-value="${project.basedir.canonicalPath}"
     */
    private String baseDir;

    /**
     * Location of sources list file.
     * 
     * @parameter expression="${sqlSourcesList}" default-value="src/main/resources/sql/sources.list"
     */
    private String sqlSourcesList;

    /**
     * Character encoding for loading the source files.
     * 
     * @parameter default-value="UTF-8"
     */
    private String fileEncoding;

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        FileSystem fileSystem = FileSystem.getStandardFileSystem(baseDir);
        initDataSource();
        try
        {
            SqlRunnerComponent runner = new SqlRunnerComponent(fileSystem, dataSource,
                new MavenDNALogger(getLog()));
            runner.run(sqlSourcesList, fileEncoding);
        }
        catch(Exception e)
        {
            throw new MojoExecutionException("SQL scripts execution failed", e);
        }
        finally
        {
            shutdownDataSource();
        }
    }
}
