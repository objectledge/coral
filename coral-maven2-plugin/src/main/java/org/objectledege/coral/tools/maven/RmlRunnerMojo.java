package org.objectledege.coral.tools.maven;

import java.io.Reader;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.jcontainer.dna.Logger;
import org.jcontainer.dna.impl.Log4JLogger;
import org.objectledge.authentication.DefaultPrincipal;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.tools.BatchLoader;
import org.objectledge.coral.tools.LedgeContainerFactory;
import org.objectledge.filesystem.FileSystem;
import org.objectledge.utils.StackTrace;
import org.picocontainer.MutablePicoContainer;

public class RmlRunnerMojo
    extends AbstractDbMojo
{
    /**
     * Base directory for looking up sources lists and source files.
     * 
     * @parameter expression="${project.basedir.canonicalPath}"
     */
    private String baseDir;

    /**
     * Configuration directory for looking up container composition file and component configuration
     * files.
     * 
     * @parameter default-value="config"
     */
    private String configDir;
    
    /**
     * Coral subject to execute scripts as. When not defined scripts will be executed as root subject.
     * 
     * @parameter expression="${subject}"
     */
    private String subjectName;
    
    /**
     * Location of sources list file.
     * 
     * @parameter expression="${rmlSourcesList}" default-value="src/main/resources/rml/sources.list"
     */
    private String sourcesList;
    
    /**
     * Character encoding for loading the source files.
     * 
     * @parameter default-value="UTF-8"
     */
    private String fileEncoding;

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        initDataSource();
        FileSystem fileSystem;
        CoralSession coralSession;
        try
        {
            MutablePicoContainer container = LedgeContainerFactory.newLedgeContainer(baseDir, configDir);
            container.registerComponentInstance("dataSource", dataSource);
            fileSystem = (FileSystem)container.getComponentInstance(FileSystem.class);
            CoralSessionFactory factory = (CoralSessionFactory)container.getComponentInstance(
                CoralSessionFactory.class);
            if(subjectName != null)
            {
                coralSession = factory.getSession(new DefaultPrincipal(subjectName));
            }
            else
            {
                coralSession = factory.getRootSession();
            }
        }
        catch(Exception e)
        {
            throw new MojoExecutionException("failed to intitialize Coral session", e);
        }
        
        final Logger logger = new Log4JLogger(org.apache.log4j.Logger.getLogger(RmlRunnerMojo.class));
        final CoralSession session = coralSession;
        BatchLoader loader = new BatchLoader(fileSystem, logger, fileEncoding)
        {
            public void load(Reader in)
                throws Exception
            {
                String result = session.getScript().runScript(in);
                if(result != null && result.length() > 0)
                {
                    logger.info(result);
                }
            }
        };
        try
        {
            loader.loadBatch(sourcesList);
        }
        catch(Exception e)
        {
            throw new MojoFailureException(sourcesList, "script execution failed", new StackTrace(e).toString());
        }
    }
}
