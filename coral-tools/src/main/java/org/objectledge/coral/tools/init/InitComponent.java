package org.objectledge.coral.tools.init;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.jcontainer.dna.Logger;
import org.objectledge.database.DatabaseType;
import org.objectledge.database.DatabaseUtils;
import org.objectledge.filesystem.FileSystem;

/**
 * A component that initializes a database schema for use as Coral store.
 * 
 * @author rafal
 */
public class InitComponent
{
    /** DataSource for accessing the database. */
    private final DataSource dataSource;

    /** Database type. */
    private final DatabaseType dbType;

    /** Ledge file system for accessing Coral sql scripts on the classpath. */
    private final FileSystem fileSystem;

    /** Should existing data be dropped? */
    private final boolean force;

    /** The logger. */
    private final Logger logger;

    /**
     * Creates a new instance of the component.
     * 
     * @param dataSource DataSource for accessing the database.
     * @param fileSystem Ledge file system for accessing Coral sql scripts on the classpath.
     * @param force should existing data be dropped?
     * @param logger a Logger
     * @throws SQLException
     */
    public InitComponent(DataSource dataSource, FileSystem fileSystem, boolean force, Logger logger)
        throws SQLException
    {
        this.dataSource = dataSource;
        this.logger = logger;
        this.dbType = DatabaseType.detect(dataSource);
        this.fileSystem = fileSystem;
        this.force = force;
    }

    /**
     * Initializes the database for coral use.
     * 
     * @throws Exception if the initialization failed.
     */
    public void run()
        throws Exception
    {
        if((hasTable("ledge_id_table") || hasTable("ledge_parameters")
            || hasTable("ledge_scheduler") || hasTable("ledge_naming_attribute") || hasTable("coral_resource_class")))
        {
            if(force)
            {
                cleanup();
            }
            else
            {
                throw new IllegalStateException(
                    "data exists in the database, but force parameter not set");
            }
        }

        runScript("sql/parameters/DBParametersTables.sql");
        runScript("sql/coral/CoralRITables.sql");
        runScript("sql/coral/CoralRIConstraints.sql");
        runScript("sql/coral/CoralDatatypesTables.sql");
        runScript("sql/coral/CoralDatatypesConstraints.sql");
        runScript("sql/coral/CoralRIInitial.sql");
        runScript("sql/coral/CoralDatatypesInitial.sql");
    }

    private boolean hasTable(String table)
        throws Exception
    {
        return DatabaseUtils.hasTable(dataSource, table);
    }

    private void cleanup()
        throws Exception
    {
        if(hasTable("ledge_id_table"))
        {
            runScript("sql/database/IdGeneratorDropTables.sql");
        }

        if(hasTable("ledge_parameters"))
        {
            runScript("sql/parameters/DBParametersDropTables.sql");
        }

        if(hasTable("ledge_scheduler"))
        {
            runScript("sql/scheduler/DBSchedulerDropTables.sql");
        }

        if(hasTable("ledge_naming_attribute"))
        {
            runScript("sql/naming/DBNamingDropTables.sql");
        }

        if(hasTable("coral_resource_class"))
        {
            cleanupPersistentResourceTables();
            runScript("sql/coral/CoralDatatypesDropTables.sql");
            runScript("sql/coral/CoralRIDropTables.sql");
        }
    }

    private void cleanupPersistentResourceTables()
        throws SQLException
    {
        Connection conn = dataSource.getConnection();
        try
        {
            Statement stmt = conn.createStatement();
            try
            {
                List<String> tables = new ArrayList<String>();
                ResultSet rset = stmt
                    .executeQuery("SELECT db_table_name FROM coral_resource_class WHERE db_table_name IS NOT NULL");
                try
                {
                    while(rset.next())
                    {
                        if(DatabaseUtils.hasTable(dataSource, rset.getString(1)))
                        {
                            tables.add(rset.getString(1));
                        }
                    }
                }
                finally
                {
                    rset.close();
                }
                if(tables.size() > 0)
                {
                    logger.info("dropping PersitentResource tables");
                    for(String table : tables)
                    {
                        stmt.execute("DROP TABLE " + table);
                    }
                }
            }
            finally
            {
                stmt.close();
            }
        }
        finally
        {
            conn.close();
        }
    }

    private void runScript(String path)
        throws Exception
    {
        logger.info("running " + adapt(path));
        Reader scriptReader = fileSystem.getReader(adapt(path), "UTF-8");
        if(scriptReader == null)
        {
            throw new IOException("script " + path + " missing from classpath");
        }
        DatabaseUtils.runScript(dataSource, scriptReader);
    }

    private String adapt(String path)
    {
        String raw = path.substring(0, path.length() - 3); // strip sql suffix
        final String adapted = raw + dbType.getSuffix() + ".sql";
        if(fileSystem.exists(adapted))
        {
            return adapted;
        }
        else
        {
            return path;
        }
    }
}
