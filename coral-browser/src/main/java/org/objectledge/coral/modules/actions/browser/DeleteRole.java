package org.objectledge.coral.modules.actions.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.entity.EntityInUseException;
import org.objectledge.coral.security.Role;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.parameters.Parameters;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.web.mvc.MVCContext;

/**
 * Delete role action.
 * 
 * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: DeleteRole.java,v 1.2 2005-02-06 22:30:48 pablo Exp $
 */
public class DeleteRole
    extends BaseBrowserAction
{
    
    public DeleteRole(Logger logger)
    {
        super(logger);
    }
    
    /**
     * Performs the action.
     */
    public void execute(Context context, Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, CoralSession coralSession)
        throws ProcessingException    
    {
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
        templatingContext.put("result","deleted_successfully");
    }
}




