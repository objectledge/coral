package org.objectledege.coral.tools.maven;

import java.sql.SQLException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.objectledge.coral.tools.refcheck.IntegrityChecker;

/**
 * Validates integrity constraints that are not enforced by the backed database.
 * 
 * @goal integrity-check
 * @author rafal.krzewski@caltha.pl
 */
public class IntegrityCheckerMojo
    extends AbstractDbMojo
{
    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        try
        {
            initDataSource();
            IntegrityChecker checker = new IntegrityChecker(dataSource.getConnection(), fileSystem,
                new MavenDNALogger(getLog()));
            checker.run();
        }
        catch(SQLException e)
        {
            SQLException se = (SQLException)e;
            while(se.getNextException() != null)
            {
                getLog().error(se);
                se = se.getNextException();
            }
            throw new MojoExecutionException("SQL Exception", se);
        }
        catch(Exception e)
        {
            throw new MojoExecutionException("internal error", e);
        }
        finally
        {
            shutdownDataSource();
        }
    }
}
