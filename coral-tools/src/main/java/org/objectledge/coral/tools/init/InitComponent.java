package org.objectledge.coral.tools.init;

import java.io.IOException;
import java.io.Reader;

import javax.sql.DataSource;

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

    /** Ledge file system for accessing Coral sql scripts on the classpath. */
    private final FileSystem fileSystem;

    /** Should existing data be dropped? */
    private final boolean force;

    /**
     * Creates a new instance of the component.
     * 
     * @param dataSource DataSource for accessing the database.
     * @param fileSystem Ledge file system for accessing Coral sql scripts on the classpath.
     * @param force should existing data be dropped?
     */
    public InitComponent(DataSource dataSource, FileSystem fileSystem, boolean force)
    {
        this.dataSource = dataSource;
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

        runScript("sql/database/IdGeneratorTables.sql");
        runScript("sql/parameters/db/DBParametersTables.sql");
        runScript("sql/scheduler/db/DBSchedulerTables.sql");
        runScript("sql/naming/db/DBNamingTables.sql");
        runScript("sql/coral/CoralRITables.sql");
        runScript("sql/coral/CoralDatatypesTables.sql");
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
            runScript("sql/parameters/db/DBParametersDropTables.sql");
        }

        if(hasTable("ledge_scheduler"))
        {
            runScript("sql/scheduler/db/DBSchedulerDropTables.sql");
        }

        if(hasTable("ledge_naming_attribute"))
        {
            runScript("sql/naming/db/DBNamingDropTables.sql");
        }

        if(hasTable("coral_resource_class"))
        {
            runScript("sql/coral/CoralDatatypesDropTables.sql");
            runScript("sql/coral/CoralRIDropTables.sql");
        }
    }

    private void runScript(String path)
        throws Exception
    {
        Reader scriptReader = fileSystem.getReader(path, "UTF-8");
        if(scriptReader == null)
        {
            throw new IOException("script " + path + " missing from classpath");
        }
        DatabaseUtils.runScript(dataSource, scriptReader);
    }
}
