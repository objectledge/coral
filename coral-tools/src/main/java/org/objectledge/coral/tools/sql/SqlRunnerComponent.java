package org.objectledge.coral.tools.sql;

import java.io.Reader;

import javax.sql.DataSource;

import org.jcontainer.dna.Logger;
import org.objectledge.coral.tools.BatchLoader;
import org.objectledge.database.DatabaseUtils;
import org.objectledge.filesystem.FileSystem;

public class SqlRunnerComponent
{
    private final FileSystem fileSystem;

    private final Logger logger;

    private final DataSource dataSource;

    public SqlRunnerComponent(FileSystem fileSystem, DataSource dataSource, Logger logger)
    {
        this.fileSystem = fileSystem;
        this.dataSource = dataSource;
        this.logger = logger;
    }

    public void run(String sqlSourcesList, String fileEncoding)
        throws Exception
    {
        BatchLoader loader = new BatchLoader(fileSystem, logger, fileEncoding)
            {
                public void load(Reader in)
                    throws Exception
                {
                    DatabaseUtils.runScript(dataSource, in);
                }
            };
        loader.loadBatch(sqlSourcesList);
    }
}
