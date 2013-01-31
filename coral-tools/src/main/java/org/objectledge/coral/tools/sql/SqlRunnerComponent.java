package org.objectledge.coral.tools.sql;

import java.io.Reader;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Level;
import org.jcontainer.dna.Logger;
import org.jcontainer.dna.impl.Log4JLogger;
import org.objectledge.coral.tools.TemplateProcessingBatchLoader;
import org.objectledge.database.DatabaseUtils;
import org.objectledge.filesystem.FileSystem;
import org.objectledge.templating.Templating;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.templating.velocity.VelocityTemplating;

public class SqlRunnerComponent
{
    private final FileSystem fileSystem;

    private final Logger log;

    private final DataSource dataSource;

    public SqlRunnerComponent(FileSystem fileSystem, DataSource dataSource, Logger logger)
    {
        this.fileSystem = fileSystem;
        this.dataSource = dataSource;
        this.log = logger;
    }

    public void run(String sqlSourcesList, String fileEncoding, Map<String, Object> templateVars,
        List<String> templateMacroLibraries)
        throws Exception
    {
        org.apache.log4j.Logger templatingLog = org.apache.log4j.Logger
            .getLogger("org.apache.velocity");
        templatingLog.setLevel(Level.ERROR);
        Templating templating = new VelocityTemplating(new VelocityTemplating.Config()
            .withEncoding(fileEncoding).withLibraries(templateMacroLibraries), new Log4JLogger(
            templatingLog), fileSystem);
        TemplatingContext context = templating.createContext(templateVars);

        TemplateProcessingBatchLoader loader = new TemplateProcessingBatchLoader(fileSystem,
                        templating, log, fileEncoding)
            {
                public void load(Reader in)
                    throws Exception
                {
                    DatabaseUtils.runScript(dataSource, in);
                }
            };
        loader.loadBatch(sqlSourcesList, context);
    }
}
