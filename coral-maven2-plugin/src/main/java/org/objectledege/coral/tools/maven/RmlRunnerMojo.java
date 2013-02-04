package org.objectledege.coral.tools.maven;

import java.util.Collections;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.objectledge.coral.tools.rml.RmlRunnerComponent;
import org.objectledge.utils.StackTrace;

/**
 * A Mojo that executes RML scripts.
 * 
 * @author rafal.krzewski@objectledge.org
 * @goal run-rml
 */
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
     * Coral subject to execute scripts as. When not defined scripts will be executed as root
     * subject.
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
        try
        {
            RmlRunnerComponent runner = new RmlRunnerComponent(dataSource, transaction,
                new MavenDNALogger(getLog()));
            runner.run(baseDir, configDir, subjectName, sourcesList, fileEncoding,
                Collections.<String, Object> emptyMap(), Collections.<String> emptyList());
        }
        catch(Exception e)
        {
            throw new MojoFailureException(sourcesList, "script execution failed: "
                + e.getMessage(), new StackTrace(e).toString());
        }
        finally
        {
            shutdownDataSource();
        }
    }
}
