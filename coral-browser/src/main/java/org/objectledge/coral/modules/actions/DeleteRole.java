package org.objectledge.coral.modules.actions;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.entity.EntityInUseException;
import org.objectledge.coral.security.Role;
import org.objectledge.pipeline.ProcessingException;

/**
 * Delete role action.
 * 
 * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: DeleteRole.java,v 1.2 2004-03-25 23:35:27 pablo Exp $
 */
public class DeleteRole
    extends BaseBrowserAction
{
    /**
     * Action constructor.
     * 
     * @param logger the logger.
     * @param coralSessionFactory the coral session factory.
     */
    public DeleteRole(Logger logger, CoralSessionFactory coralSessionFactory)
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
        long roleId = parameters.getLong("role_id",-1L);
        try
        {
            Role role = coralSession.getSecurity().getRole(roleId);
            coralSession.getSecurity().deleteRole(role);
            parameters.remove("role_id");
        }
        catch(EntityDoesNotExistException e)
        {
            logger.error("ARLException: ",e);
            templatingContext.put("result","exception");
            //templatingContext.put("trace",StringUtils.stackTrace(e));
            return;
        }
        catch(EntityInUseException e)
        {
            logger.error("ARLException: ",e);
            templatingContext.put("result","exception");
            //templatingContext.put("trace",StringUtils.stackTrace(e));
            return;
        }
        finally
        {
            coralSession.close();
        }
        templatingContext.put("result","deleted_successfully");
    }
}




