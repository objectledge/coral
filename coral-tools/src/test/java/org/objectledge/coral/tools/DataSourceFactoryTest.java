package org.objectledge.coral.tools;

import java.io.File;
import java.net.MalformedURLException;
import java.sql.SQLException;

import javax.sql.DataSource;

import junit.framework.TestCase;

public class DataSourceFactoryTest
    extends TestCase
{
    private static final String POSTGRES_DRIVER = "/usr/share/java/postgresql-jdbc3.jar";

    public void testHSQLDB()
        throws SQLException
    {
        DataSource dataSource = DataSourceFactory.newDataSource("org.hsqldb.jdbcDriver",
            "jdbc:hsqldb:.", "sa", "");
        assertNotNull(dataSource.getConnection());
    }

    public void testPostgres()
        throws SQLException, MalformedURLException
    {
        if(new File(POSTGRES_DRIVER).exists())
        {
            DataSource dataSource = DataSourceFactory.newDataSource("org.postgresql.Driver",
                "jdbc:postgresql://localhost/template1", "postgres", "");
            Thread.currentThread().setContextClassLoader(
                DataSourceFactory.getDriverClassLoader(POSTGRES_DRIVER));
            try
            {
                dataSource.getConnection();
            }
            catch(SQLException e)
            {
                assertTrue(e.getMessage().contains("no password was provided") || e.getMessage().contains("authentication failed"));
            }
        }
    }
}
