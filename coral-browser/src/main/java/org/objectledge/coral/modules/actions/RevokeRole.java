package org.objectledge.coral.modules.actions;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.CoralSessionFactory;
import org.objectledge.coral.security.Role;
import org.objectledge.coral.security.Subject;
import org.objectledge.pipeline.ProcessingException;

/**
 * Revoke role action.
 * 
 * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: RevokeRole.java,v 1.1 2004-03-22 20:21:35 pablo Exp $
 */
public class RevokeRole
    extends BaseBrowserAction
{
    /**
     * Action constructor.
     * 
     * @param logger the logger.
     * @param coralSessionFactory the coral session factory.
     */
    public RevokeRole(Logger logger, CoralSessionFactory coralSessionFactory)
    {
        super(logger, coralSessionFactory);
    }    
    
    /**
     * Performs the action.
     */
    public void process(Context context)
            throws ProcessingException    
    {
        prepare(context);
        long roleId = parameters.getLong("role_id",-1);
        long subjectId = parameters.getLong("sub_id",-1);
        try
        {
            Subject subject = coralSession.getSecurity().getSubject(subjectId);
            Role role = coralSession.getSecurity().getRole(roleId);
            coralSession.getSecurity().revoke(role, subject);
        }
        catch(Exception e)
        {
            logger.error("ARLException: ",e);
            //templatingContext.put("trace",StringUtils.stackTrace(e));
            templatingContext.put("result","exception");
            return;
        }
        finally
        {
            coralSession.close();
        }
        templatingContext.put("result","revoked_successfully");
    }
}




