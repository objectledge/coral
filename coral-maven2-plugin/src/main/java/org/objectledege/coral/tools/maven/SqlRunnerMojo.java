package org.objectledege.coral.tools.maven;

import java.io.Reader;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.objectledge.coral.tools.BatchLoader;
import org.objectledge.coral.tools.DataSourceFactory;
import org.objectledge.database.DatabaseUtils;
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
     * Basedir for looking up sources list and source files.
     * 
     * @parameter expression="${project.basedir.canonicalPath}
     */
    private String baseDir;

    /**
     * Location of sources list file.
     * 
     * @parameter default-value="src/main/resources/sql/sources.list"
     */
    private String sqlSourcesList;

    /**
     * Character encoding for loading the source files.
     * 
     * @parameter expression="${coral.sql.file.encodnig}" default-value="UTF-8"
     */
    private String fileEncoding;

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        Log log = getLog();
        initDataSource();
        try
        {
            FileSystem fileSystem = FileSystem.getStandardFileSystem(baseDir);
            BatchLoader loader = new BatchLoader(fileSystem, new MavenDNALogger(log), fileEncoding)
                {
                    public void load(Reader in)
                        throws Exception
                    {
                        DatabaseUtils.runScript(dataSource, in);
                    }
                };
            log.info("dbURL " + dbURL);
            log.info("dbUser " + dbUser);
            log.info("sqlSourcesList " + sqlSourcesList);
            loader.loadBatch(sqlSourcesList);
        }
        catch(Exception e)
        {
            throw new MojoExecutionException("SQL scripts execution failed", e);
        }

    }
}
