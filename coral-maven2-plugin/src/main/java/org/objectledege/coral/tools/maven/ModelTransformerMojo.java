package org.objectledege.coral.tools.maven;

import static org.objectledege.coral.tools.maven.ModelTransformerMojo.TransformationType.genericToTabluar;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.objectledge.coral.tools.DataSourceFactory;
import org.objectledge.coral.tools.transform.GenericToTabular;
import org.objectledge.coral.tools.transform.TransformationComponent;
import org.objectledge.filesystem.FileSystem;

/**
 * A Mojo that transforms between supported Coral database models.
 * 
 * @author rafal.krzewski@objectledge.org
 * @goal transform-model
 */
public class ModelTransformerMojo
    extends AbstractMojo
{
    public enum TransformationType
    {
        genericToTabluar
    }

    /**
     * Additional classpath elements to be used for loading database the driver.
     * 
     * @parameter expression="${driverClasspath}"
     */
    protected String driverClasspath;

    /**
     * Class name of the database driver.
     * 
     * @parameter expression="${dbDriver}"
     */
    protected String dbDriver;

    /**
     * User name for database connections.
     * 
     * @parameter expression="${dbUser}"
     */

    protected String dbUser;

    /**
     * Password for database connections.
     * 
     * @parameter expression="${dbPassword}"
     */
    protected String dbPassword;

    /**
     * JDBC URL of the source database.
     * 
     * @parameter expression="${sourceURL}"
     */
    protected String sourceURL;

    /**
     * User name for source database connection, when not defined {@code dbUser} value will be used.
     * 
     * @parameter expression="${targetUser}"
     */

    protected String sourceUser;

    /**
     * Password for source database connection, when not defined {@code dbPassword} value will be
     * used.
     * 
     * @parameter expression="${targetPassword}"
     */
    protected String sourcePassword;

    /**
     * JDBC URL of the target database.
     * 
     * @parameter expression="${targetURL}"
     */
    protected String targetURL;

    /**
     * User name for target database connection, when not defined {@code dbUser} value will be used.
     * 
     * @parameter expression="${targetUser}"
     */
    protected String targetUser;

    /**
     * Password for target database connection, when not defined {@code dbPassword} value will be
     * used.
     * 
     * @parameter expression="${targetPassword}"
     */
    protected String targetPassword;

    /**
     * Requested transformation type. Supported types are: {@code genericToTabular}.
     * 
     * @parameter expression=${transformation}
     */
    protected TransformationType transformation = genericToTabluar;

    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        MavenDNALogger log = new MavenDNALogger(getLog());
        TransformationComponent transformationComponent;
        switch(transformation)
        {
        case genericToTabluar:
            transformationComponent = new GenericToTabular();
            break;
        default:
            throw new MojoExecutionException("unsupported transformation " + transformation);
        }

        try
        {
            ClassLoader cl = DataSourceFactory.getDriverClassLoader(driverClasspath);
            Thread.currentThread().setContextClassLoader(cl);
        }
        catch(Exception e)
        {
            throw new MojoExecutionException("failed to initialize database driver classloader", e);
        }
        try
        {
            DataSource source = DataSourceFactory.newDataSource(dbDriver, sourceURL,
                sourceUser != null ? sourceUser : dbUser, sourcePassword != null ? sourcePassword
                    : dbPassword);
            Connection sourceConn = source.getConnection();
            try
            {
                DataSource target = DataSourceFactory.newDataSource(dbDriver, targetURL,
                    targetUser != null ? targetUser : dbUser,
                    targetPassword != null ? targetPassword : dbPassword);
                Connection targetConn = target.getConnection();
                try
                {
                    transformationComponent.run(sourceConn, targetConn,
                        FileSystem.getClasspathFileSystem(), log);
                }
                catch(SQLException e)
                {
                    throw new MojoExecutionException("transformation failed", e);
                }
                finally
                {
                    targetConn.close();
                }
            }
            catch(SQLException e)
            {
                throw new MojoExecutionException("failed to initialize target database connection",
                    e);
            }
            finally
            {
                sourceConn.close();
            }
        }
        catch(SQLException e)
        {
            throw new MojoExecutionException("failed to initialize source database connection", e);
        }
    }
}
