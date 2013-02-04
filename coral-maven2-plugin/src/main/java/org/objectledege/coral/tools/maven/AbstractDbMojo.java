package org.objectledege.coral.tools.maven;

import java.net.MalformedURLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.objectledge.coral.tools.DataSourceFactory;
import org.objectledge.database.Transaction;

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
    private String driverClasspath;

    /**
     * Class name of the {@code javax.sql.DataSource} implementation.
     * 
     * @parameter
     */
    private String dataSourceClass;

    /**
     * JavaBean properties of the {@code javax.sql.DataSource}.
     * 
     * @parameter
     */
    private Properties dataSourceProperties;

    protected Transaction transaction;

    /**
     * {@link DataSource} instance.
     */
    protected DataSource dataSource;

    /**
     * The data source factory.
     */
    private DataSourceFactory dataSourceFactory;

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
            dataSourceFactory = new DataSourceFactory(driverClasspath, dataSourceClass, dataSourceProperties,
                new MavenDNALogger(getLog()));
        }
        catch(MalformedURLException e)
        {
            throw new MojoExecutionException("invalid driver classpath", e);
        }
        dataSource = dataSourceFactory.getDataSource();
        transaction = dataSourceFactory.getTransaction();
    }

    /**
     * Shuts down the datasource and transaction manager.
     */
    protected void shutdownDataSource()
    {
        getLog().info("disconnecting from the db");
        dataSourceFactory.close();
    }
}
