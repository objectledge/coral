package org.objectledge.coral.tools;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public enum DatabaseSystem
{
    HSQL, POSTGRES, MYSQL, UNKNOWN;

    public static DatabaseSystem detectDatabase(Connection conn)
        throws SQLException
    {
        DatabaseMetaData md = conn.getMetaData();
        if(md.getDatabaseProductName().startsWith("HSQL"))
        {
            return HSQL;
        }
        if(md.getDatabaseProductName().startsWith("Postgres"))
        {
            return POSTGRES;
        }
        if(md.getDatabaseProductName().startsWith("MySQL"))
        {
            return MYSQL;
        }
        return UNKNOWN;
    }

    public static String getSuffix(DatabaseSystem dbSystem)
    {
        switch(dbSystem)
        {
        case HSQL:
            return "hsql";
        case POSTGRES:
            return "pg";
        case MYSQL:
            return "mysql";
        default:
            return null;
        }
    }
}
