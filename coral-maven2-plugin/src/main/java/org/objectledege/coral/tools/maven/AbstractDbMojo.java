package org.objectledege.coral.tools.maven;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.objectledge.coral.tools.DataSourceFactory;

/**
 * Abstract base class for Mojos that access a database.
 * 
 * @author rafal
 */
public abstract class AbstractDbMojo
    extends AbstractMojo
{
    /**
     * Additional classpath elements to be used for loading database the driver.
     * 
     * @parameter
     */
    protected String driverClasspath;

    /**
     * Class name of the database driver.
     * 
     * @parameter
     */
    protected String dbDriver;

    /**
     * JDBC URL of the target database.
     * 
     * @parameter
     */
    protected String dbURL;

    /**
     * User name for database connection.
     * 
     * @parameter
     */

    protected String dbUser;

    /**
     * Password for database connection.
     * 
     * @parameter
     */
    protected String dbPassword;

    protected DataSource dataSource;

    /**
     * Initializes the datasource according to configured parameters.
     * 
     * @throws MojoExecutionException when datasource initialization fails.
     */
    protected void initDataSource()
        throws MojoExecutionException
    {
        try
        {
            ClassLoader cl = DataSourceFactory.getDriverClassLoader(driverClasspath);
            Thread.currentThread().setContextClassLoader(cl);
        }
        catch(Exception e)
        {
            throw new MojoExecutionException("failed to initialize database driver classloader", e);
        }
        try
        {
            dataSource = DataSourceFactory.newDataSource(dbDriver, dbURL, dbUser, dbPassword);
        }
        catch(SQLException e)
        {
            throw new MojoExecutionException("failed to initialized datasource", e);
        }
    }
}
