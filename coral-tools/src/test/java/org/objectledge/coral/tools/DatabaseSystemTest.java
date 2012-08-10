package org.objectledge.coral.tools;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import junit.framework.TestCase;

public class DatabaseSystemTest
    extends TestCase
{

    public void testHsql()
        throws SQLException
    {
        DataSource dataSource = DataSourceFactory.newDataSource("org.hsqldb.jdbcDriver",
            "jdbc:hsqldb:.", "sa", "");
        final Connection connection = dataSource.getConnection();
        assertNotNull(connection);
        DatabaseSystem system = DatabaseSystem.detectDatabase(connection);
        assertEquals(DatabaseSystem.HSQL, system);
        assertEquals("hsql", DatabaseSystem.getSuffix(system));
    }
}
