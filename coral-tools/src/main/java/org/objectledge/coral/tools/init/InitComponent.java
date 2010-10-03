package org.objectledge.coral.tools.init;

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
    
    /**
     * Creates a new instance of the component.
     * 
     * @param dataSource DataSource for accessing the database.
     * @param fileSystem Ledge file system for accessing Coral sql scripts on the classpath.
     */
    public InitComponent(DataSource dataSource, FileSystem fileSystem)
    {
        this.dataSource = dataSource;
        this.fileSystem = fileSystem;
    }
    
    /**
     * Initializes the database for coral use.
     * 
     * @throws Exception if the initialization failed.
     */
    public void run()
        throws Exception
    {
        if(!hasTable("ledge_id_table"))
        {
            runScript("sql/database/IdGeneratorTables.sql");
        }
        else
        {
            runScript("sql/database/IdGeneratorCleanup.sql");
        }
        
        if(!hasTable("ledge_parameters"))
        {
            runScript("sql/parameters/db/DBParametersTables.sql");
        }
        else
        {
            runScript("sql/parameters/db/DBParametersCleanup.sql");
        }
        
        if(!hasTable("ledge_scheduler"))
        {
            runScript("sql/scheduler/db/DBSchedulerTables.sql");
        }
        else
        {
            runScript("sql/scheduler/db/DBSchedulerCleanup.sql");
        }
        
        if(!hasTable("ledge_naming_attribute"))
        {
            runScript("sql/naming/db/DBNamingTables.sql");
        }
        else
        {
            runScript("sql/naming/db/DBNamingCleanup.sql");
        }
        
        if(!hasTable("coral_resource_class"))
        {
            runScript("sql/coral/CoralRITables.sql");
            runScript("sql/coral/CoralDatatypesTables.sql");
        }
        else
        {
            runScript("sql/coral/CoralDatatypesCleanup.sql");
            runScript("sql/coral/CoralRICleanup.sql");
        }
        runScript("sql/coral/CoralRIInitial.sql");
        runScript("sql/coral/CoralDatatypesInitial.sql");
    }
    
    private boolean hasTable(String table)
        throws Exception
    {
        return DatabaseUtils.hasTable(dataSource, table);
    }
    
    private void runScript(String path)
        throws Exception
    {
        DatabaseUtils.runScript(dataSource, fileSystem.getReader(path, "UTF-8"));        
    }
}
