package org.objectledge.coral.tools;

import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.log4j.Level;
import org.jcontainer.dna.Logger;
import org.objectledge.btm.BitronixDataSource;
import org.objectledge.btm.BitronixTransaction;
import org.objectledge.btm.BitronixTransactionManager;
import org.objectledge.context.Context;
import org.objectledge.database.DatabaseUtils;
import org.objectledge.database.Transaction;

public class DataSourceFactory
    implements AutoCloseable
{
    /**
     * {@link BitronixTransactionManager} instance.
     */
    private final BitronixTransactionManager btm;

    /**
     * {@Transaction} helper.
     */
    private final Transaction transaction;

    /**
     * {@link DataSource} instance.
     */
    private final DataSource dataSource;

    public DataSourceFactory(String driverClasspath, String dataSourceClass,
        Properties dataSourceProperties, Logger log)
        throws MalformedURLException
    {
        ClassLoader cl = DatabaseUtils.getDriverClassLoader(driverClasspath);
        Thread.currentThread().setContextClassLoader(cl);

        log.info("dataSourceClass: " + dataSourceClass);
        if(dataSourceProperties != null)
        {
            Enumeration<String> pe = (Enumeration<String>)dataSourceProperties.propertyNames();
            while(pe.hasMoreElements())
            {
                String p = pe.nextElement();
                log.info("dataSourceProperty: " + p + " = " + dataSourceProperties.getProperty(p));
            }
        }
        else
        {
            log.warn("null dataSourceProperties");
        }

        org.apache.log4j.Logger.getLogger("bitronix.tm").setLevel(Level.INFO);

        btm = new BitronixTransactionManager("coral", dataSourceClass, dataSourceProperties, null, log);
        dataSource = new BitronixDataSource("coral", btm);
        transaction = new BitronixTransaction(btm, new Context(), log, null);
    }

    public void close()
    {
        btm.close();
    }

    public Transaction getTransaction()
    {
        return transaction;
    }

    public DataSource getDataSource()
    {
        return dataSource;
    }

}
