package org.objectledge.coral.tools.rml;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.jcontainer.dna.Logger;
import org.objectledge.authentication.DefaultPrincipal;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.tools.BatchLoader;
import org.objectledge.coral.tools.LedgeContainerFactory;
import org.objectledge.database.DatabaseUtils;
import org.objectledge.database.Transaction;
import org.objectledge.filesystem.FileSystem;
import org.picocontainer.MutablePicoContainer;

public class RmlRunnerComponent
{
    /**
     * Base directory for looking up sources lists and source files.
     */
    private String baseDir;

    /**
     * Configuration directory for looking up container composition file and component configuration
     * files.
     */
    private String configDir;

    /**
     * Coral subject to execute scripts as. When not defined scripts will be executed as root
     * subject.
     */
    private String subjectName;

    /**
     * Location of sources list file.
     */
    private String sourcesList;

    /**
     * Character encoding for loading the source files.
     */
    private String fileEncoding;

    /**
     * DataSource
     */
    private DataSource dataSource;

    /**
     * The file system.
     */
    private FileSystem fileSystem;

    /**
     * The logger.
     */
    private Logger log;

    private final Transaction transaction;

    /**
     * Create RmlRunnerComponent
     * 
     * @param baseDir base directory for loading container configuration from external filesystem.
     * @param configDir configuration directory relative to baseDir / classpath root.
     * @param subjectName name of the subject for creating Coral session.
     * @param sourcesList path of the sources list file.
     * @param fileEncoding encoding of the source files.
     * @param dataSource the DataSource for accessing the DB.
     * @param transaction Transaction manager facade
     * @param fileSystem filesystem component.
     * @param log the logger.
     */
    public RmlRunnerComponent(String baseDir, String configDir, String subjectName,
        String sourcesList, String fileEncoding, DataSource dataSource, Transaction transaction,
        FileSystem fileSystem, Logger log)
    {
        this.baseDir = baseDir;
        this.configDir = configDir;
        this.subjectName = subjectName;
        this.sourcesList = sourcesList;
        this.fileEncoding = fileEncoding;

        this.dataSource = dataSource;
        this.transaction = transaction;
        this.fileSystem = fileSystem;
        this.fileEncoding = fileEncoding;
        this.log = log;
    }

    public void run()
        throws Exception
    {
        CoralSession coralSession;

        Map<Object, Object> componentInstances = new HashMap<Object, Object>();
        componentInstances.put(DataSource.class, dataSource);
        componentInstances.put(Transaction.class, transaction);
        MutablePicoContainer container = LedgeContainerFactory.newLedgeContainer(baseDir,
            configDir, componentInstances);
        fileSystem = (FileSystem)container.getComponentInstance(FileSystem.class);
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

        final CoralSession session = coralSession;
        BatchLoader loader = new BatchLoader(fileSystem, log, fileEncoding)
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

        loader.loadBatch(sourcesList);

        log.info("disconnecting from the db");
        DatabaseUtils.shutdown(dataSource);
    }
}
