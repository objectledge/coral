package org.objectledge.coral.tools;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.jcontainer.dna.Logger;
import org.jcontainer.dna.impl.DefaultConfiguration;
import org.jcontainer.dna.impl.Log4JLogger;
import org.objectledge.context.Context;
import org.objectledge.database.JotmTransaction;
import org.objectledge.database.Transaction;
import org.objectledge.database.XaPoolDataSource;

/**
 * Helper class for creating database connections suitable for using in a maven plugin or standalone
 * installer
 * 
 * @author rafal
 */
public class DataSourceFactory
{
    /**
     * Creates a new DataSource according supplied parameters.
     * 
     * @param databaseDriver name of the JDBC driver to use.
     * @param databaseUrl JDBC database url.
     * @param databaseUrl username to be used for creating the connections.
     * @param databasePassword password to be used for creating the connections.
     * @return
     * @throws SQLException when datasource creation fails
     */
    public static DataSource newDataSource(String databaseDriver, String databaseUrl,
        String databaseUser, String databasePassword)
        throws SQLException
    {
        try
        {
            Context context = new Context();
            Logger jotmLogger = new Log4JLogger(org.apache.log4j.Logger
                .getLogger(JotmTransaction.class));
            Transaction transaction = new JotmTransaction(0, 0, context, jotmLogger);

            DefaultConfiguration conf = new DefaultConfiguration("config", "", "");
            DefaultConfiguration connConf = new DefaultConfiguration("connection", "", "config");
            conf.addChild(connConf);
            DefaultConfiguration driver = new DefaultConfiguration("driver", "",
                "config/connection");
            driver.setValue(databaseDriver);
            connConf.addChild(driver);
            DefaultConfiguration url = new DefaultConfiguration("url", "", "config/connection");
            url.setValue(databaseUrl);
            connConf.addChild(url);
            if(databaseUser != null && databaseUser.length() > 0)
            {
                DefaultConfiguration user = new DefaultConfiguration("user", "",
                    "config/connection");
                user.setValue(databaseUser);
                connConf.addChild(user);
            }
            if(databasePassword != null && databasePassword.length() > 0)
            {
                DefaultConfiguration password = new DefaultConfiguration("password", "",
                    "config/connection");
                password.setValue(databasePassword);
                connConf.addChild(password);
            }
            return new XaPoolDataSource(transaction, conf, null);
        }
        catch(Exception e)
        {
            throw new SQLException("failed to initialize the data source", e);
        }
    }

    /**
     * Provides a ClassLoader suitable for loading SQL driver class. You need to call
     * <code>Thread.currentThread().setContextClassLoader(DataSourceFactory.getDriverClassLoader(DRIVER_CLASSPATH));</code>
     * before requesting the connection.
     * 
     * @param driverClasspath additional classpath entries that will be used for instantiating
     *        driver class. Entries should be separated with path.separator suitable for the
     *        platform. When null or empty, driver class is expected to be already available in the
     *        ClassLoader used to load this class.
     * @return a ClassLoader suitable for loading SQL driver class.
     * @throws MalformedURLException when provided classpath is malformed.
     */
    public static ClassLoader getDriverClassLoader(String driverClasspath)
        throws MalformedURLException
    {
        if(driverClasspath == null || driverClasspath.length() == 0)
        {
            return Thread.currentThread().getContextClassLoader();
        }
        else
        {
            String pathSeparator = System.getProperty("path.separator");
            String[] elements = driverClasspath.split(pathSeparator);
            URL[] urls = new URL[elements.length];
            for(int i = 0; i < elements.length; i++)
            {
                urls[i] = new URL("file", "", elements[i].trim());
            }
            return new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
        }
    }
}
