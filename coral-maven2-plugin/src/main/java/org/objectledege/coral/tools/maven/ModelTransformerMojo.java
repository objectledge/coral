package org.objectledege.coral.tools.maven;

import static org.objectledege.coral.tools.maven.ModelTransformerMojo.TransformationType.genericToTabluar;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.objectledge.coral.tools.transform.GenericToTabular;
import org.objectledge.coral.tools.transform.TransformationComponent;
import org.objectledge.database.DatabaseUtils;
import org.objectledge.database.JDBCDataSource;
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
     * @parameter
     */
    private String driverClasspath;

    /**
     * @parameter
     */
    private DataSourceInfo source;

    /**
     * @parameter
     */
    private DataSourceInfo target;

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
            ClassLoader cl = DatabaseUtils.getDriverClassLoader(driverClasspath);
            Thread.currentThread().setContextClassLoader(cl);
        }
        catch(Exception e)
        {
            throw new MojoExecutionException("failed to initialize database driver classloader", e);
        }
        try
        {
            DataSource sourceDs = new JDBCDataSource(driverClasspath, source.getDataSourceClass(),
                source.getDataSourceProperties());
            Connection sourceConn = sourceDs.getConnection();
            try
            {
                DataSource targetDs = new JDBCDataSource(driverClasspath,
                    target.getDataSourceClass(), target.getDataSourceProperties());
                Connection targetConn = targetDs.getConnection();
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
                    DatabaseUtils.shutdown(targetDs);
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
                DatabaseUtils.shutdown(sourceDs);
            }
        }
        catch(SQLException e)
        {
            throw new MojoExecutionException("failed to initialize source database connection", e);
        }
    }

    public static class DataSourceInfo
    {
        /**
         * @parameter
         */
        private String dataSourceClass;

        /**
         * @parameter
         */
        private Properties dataSourceProperties;

        public String getDataSourceClass()
        {
            return dataSourceClass;
        }

        public void setDataSourceClass(String dataSourceClass)
        {
            this.dataSourceClass = dataSourceClass;
        }

        public Properties getDataSourceProperties()
        {
            return dataSourceProperties;
        }

        public void setDataSourceProperties(Properties dataSourceProperties)
        {
            this.dataSourceProperties = dataSourceProperties;
        }
    }
}
