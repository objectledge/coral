package org.objectledege.coral.tools.maven;

import java.util.Collections;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.objectledge.coral.tools.sql.SqlRunnerComponent;

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
        initDataSource();
        try
        {
            SqlRunnerComponent runner = new SqlRunnerComponent(fileSystem, dataSource,
                new MavenDNALogger(getLog()));
            runner.run(sqlSourcesList, fileEncoding, Collections.<String, Object> emptyMap(),
                Collections.<String> emptyList());
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
