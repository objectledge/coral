package org.objectledge.coral.modules.executable;

import javax.sql.DataSource;

import org.objectledge.container.LedgeContainer;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.database.DatabaseUtils;
import org.objectledge.database.ThreadDataSource;
import org.objectledge.filesystem.FileSystem;

/**
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class DBReset
{
    private LedgeContainer container;

    private FileSystem fs;

    public DBReset() throws Exception
    {
        fs = FileSystem.getStandardFileSystem("src/webapp");
        container = new LedgeContainer(fs, "/config", getClass().getClassLoader());
    }

    public void execute() throws Exception
    {
        CoralSessionFactory coralSessionFactory = (CoralSessionFactory)container.getContainer().getComponentInstance(CoralSessionFactory.class);
        DataSource ds = (DataSource)container.getContainer().getComponentInstanceOfType(ThreadDataSource.class);
        if (!DatabaseUtils.hasTable(ds, "ledge_id_table"))
        {
            DatabaseUtils.runScript(ds, fs.getReader("sql/database/IdGenerator.sql", "UTF-8"));
        }
        if (!DatabaseUtils.hasTable(ds, "ledge_parameters"))
        {
            DatabaseUtils.runScript(ds, fs.getReader("sql/parameters/db/DBParameters.sql", "UTF-8"));
        }
        if (!DatabaseUtils.hasTable(ds, "coral_resource_class"))
        {
            DatabaseUtils.runScript(ds, fs.getReader("sql/coral/CoralRITables.sql", "UTF-8"));
            DatabaseUtils.runScript(ds, fs.getReader("sql/coral/CoralDatatypesTables.sql", "UTF-8"));
        }
        else
        {
            DatabaseUtils.runScript(ds, fs.getReader("sql/coral/CoralDatatypesCleanup.sql", "UTF-8"));
            DatabaseUtils.runScript(ds, fs.getReader("sql/coral/CoralRICleanup.sql", "UTF-8"));
        }
        DatabaseUtils.runScript(ds, fs.getReader("sql/coral/CoralRIInitial.sql", "UTF-8"));
        DatabaseUtils.runScript(ds, fs.getReader("sql/coral/CoralDatatypesInitial.sql", "UTF-8"));
        container.killContainer();

    }

    public static void main(String[] args) throws Exception
    {
        DBReset dbReset = new DBReset();
        dbReset.execute();
    }
}
