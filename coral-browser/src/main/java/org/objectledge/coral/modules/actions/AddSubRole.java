package org.objectledge.coral.modules.actions;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.CoralSessionFactory;
import org.objectledge.coral.security.Role;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.utils.StringUtils;

/**
 * Grant role action.
 * 
 * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: AddSubRole.java,v 1.1 2004-03-22 20:21:35 pablo Exp $
 */
public class AddSubRole
    extends BaseBrowserAction
{
    /**
     * Action constructor.
     * 
     * @param logger the logger.
     * @param coralSessionFactory the coral session factory.
     */
    public AddSubRole(Logger logger, CoralSessionFactory coralSessionFactory)
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
        String roleName = parameters.get("role_name","");
        try
        {
            Role role = coralSession.getSecurity().getRole(roleId);
            Role subRole = coralSession.getSecurity().getUniqueRole(roleName);
            coralSession.getSecurity().addSubRole(role, subRole);
        }
        catch(Exception e)
        {
            logger.error("ARLException: ",e);
            //context.put("trace",StringUtils.stackTrace(e));
            templatingContext.put("result","exception");
            return;
        }
        finally
        {
            coralSession.close();
        }
        templatingContext.put("result","added_successfully");
    }
}




