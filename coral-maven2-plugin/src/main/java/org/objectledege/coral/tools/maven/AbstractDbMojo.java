package org.objectledege.coral.tools.maven;

import java.util.Enumeration;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.objectledge.btm.BitronixDataSource;
import org.objectledge.btm.BitronixTransaction;
import org.objectledge.btm.BitronixTransactionManager;
import org.objectledge.context.Context;
import org.objectledge.database.DatabaseUtils;
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

    /**
     * {@link BitronixTransactionManager} instance.
     */
    private BitronixTransactionManager btm;

    protected Transaction tm;

    /**
     * {@link DataSource} instance.
     */
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
            ClassLoader cl = DatabaseUtils.getDriverClassLoader(driverClasspath);
            Thread.currentThread().setContextClassLoader(cl);
        }
        catch(Exception e)
        {
            throw new MojoExecutionException("failed to initialize database driver classloader", e);
        }
        getLog().info("dataSourceClass: " + dataSourceClass);
        if(dataSourceProperties != null)
        {
            Enumeration<String> pe = (Enumeration<String>)dataSourceProperties.propertyNames();
            while(pe.hasMoreElements())
            {
                String p = pe.nextElement();
                getLog().info(
                    "dataSourceProperty: " + p + " = " + dataSourceProperties.getProperty(p));
            }
        }
        else
        {
            getLog().warn("null dataSourceProperties");
        }

        BasicConfigurator.configure();
        Logger.getLogger("bitronix.tm").setLevel(Level.INFO);

        btm = new BitronixTransactionManager("coral", dataSourceClass, dataSourceProperties);
        dataSource = new BitronixDataSource("coral", btm);
        tm = new BitronixTransaction(btm, new Context(), new MavenDNALogger(getLog()), null);
    }

    /**
     * Shuts down the datasource and transaction manager.
     */
    protected void shutdownDataSource()
    {
        btm.stop();
    }
}
