package org.objectledge.coral.tools.rml;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Level;
import org.jcontainer.dna.Logger;
import org.jcontainer.dna.impl.Log4JLogger;
import org.objectledge.authentication.DefaultPrincipal;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.tools.LedgeContainerFactory;
import org.objectledge.coral.tools.TemplateProcessingBatchLoader;
import org.objectledge.database.Transaction;
import org.objectledge.filesystem.FileSystem;
import org.objectledge.templating.Templating;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.templating.velocity.VelocityTemplating;
import org.picocontainer.MutablePicoContainer;

public class RmlRunnerComponent
{
    /**
     * DataSource.
     */
    private final DataSource dataSource;

    /**
     * The logger.
     */
    private final Logger log;

    /**
     * Transaction manager facade.
     */
    private final Transaction transaction;

    /**
     * Create RmlRunnerComponent
     * 
     * @param dataSource the DataSource for accessing the DB.
     * @param transaction Transaction manager facade
     * @param log the logger.
     */
    public RmlRunnerComponent(DataSource dataSource, Transaction transaction, Logger log)
    {
        this.dataSource = dataSource;
        this.transaction = transaction;
        this.log = log;
    }

    /**
     * Run RML scripts enumarated in sourcesList file.
     * 
     * @param baseDir base directory for loading container configuration from external filesystem.
     * @param configDir configuration directory relative to baseDir / classpath root.
     * @param subjectName name of the subject for creating Coral session.
     * @param sourcesList path of the sources list file.
     * @param fileEncoding encoding of the source files.
     * @throws Exception
     */
    public void run(String baseDir, String configDir, String subjectName, String sourcesList,
        String fileEncoding, Map<String, Object> templateVars, List<String> templateMacroLibraries)
        throws Exception
    {
        CoralSession coralSession;

        Map<Object, Object> componentInstances = new HashMap<Object, Object>();
        componentInstances.put(DataSource.class, dataSource);
        componentInstances.put(Transaction.class, transaction);
        MutablePicoContainer container = LedgeContainerFactory.newLedgeContainer(baseDir,
            configDir, componentInstances);
        FileSystem fileSystem = (FileSystem)container.getComponentInstance(FileSystem.class);
        CoralSessionFactory factory = (CoralSessionFactory)container
            .getComponentInstance(CoralSessionFactory.class);
        if(subjectName != null)
        {
            coralSession = factory.getSession(new DefaultPrincipal(subjectName));
        }
        else
        {
            coralSession = factory.getRootSession();
        }

        org.apache.log4j.Logger templatingLog = org.apache.log4j.Logger
            .getLogger("org.apache.velocity");
        templatingLog.setLevel(Level.ERROR);
        Templating templating = new VelocityTemplating(new VelocityTemplating.Config()
            .withEncoding(fileEncoding).withLibraries(templateMacroLibraries), new Log4JLogger(
            templatingLog), fileSystem);
        TemplatingContext context = templating.createContext(templateVars);

        final CoralSession session = coralSession;
        TemplateProcessingBatchLoader loader = new TemplateProcessingBatchLoader(fileSystem,
                        templating, log, fileEncoding)
            {
                public void load(Reader in)
                    throws Exception
                {
                    String result = session.getScript().runScript(in);
                    if(result != null && result.length() > 0)
                    {
                        log.info(result);
                    }
                }
            };

        loader.loadBatch(sourcesList, context);
    }
}
