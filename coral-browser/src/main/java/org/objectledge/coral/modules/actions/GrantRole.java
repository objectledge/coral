package org.objectledge.coral.modules.actions;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.security.Role;
import org.objectledge.coral.security.Subject;
import org.objectledge.pipeline.ProcessingException;

/**
 * Grant role action.
 * 
 * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: GrantRole.java,v 1.3 2004-03-25 23:35:27 pablo Exp $
 */
public class GrantRole
    extends BaseBrowserAction
{
    /**
     * Action constructor.
     * 
     * @param logger the logger.
     * @param coralSessionFactory the coral session factory.
     */
    public GrantRole(Logger logger, CoralSessionFactory coralSessionFactory)
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
        String subjectName = parameters.get("sub_name","");
        boolean allowGranting = parameters.getBoolean("granting_allowed",false);
        try
        {
            Subject subject = coralSession.getSecurity().getSubject(subjectName);
            Role role = coralSession.getSecurity().getRole(roleId);
            coralSession.getSecurity().grant(role, subject, allowGranting);
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
        templatingContext.put("result","granted_successfully");
    }
}




