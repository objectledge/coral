package org.objectledge.coral.modules.actions.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.entity.EntityInUseException;
import org.objectledge.coral.security.Permission;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.parameters.Parameters;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.web.mvc.MVCContext;

/**
 * Delete permission action.
 * 
 * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: DeletePermission.java,v 1.2 2005-02-06 22:30:48 pablo Exp $
 */
public class DeletePermission
    extends BaseBrowserAction
{
    public DeletePermission(Logger logger)
    {
        super(logger);
    }
    
    /**
     * Performs the action.
     */
    public void execute(Context context, Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, CoralSession coralSession)
    throws ProcessingException    
    {
        long permId = parameters.getLong("perm_id",-1L);
        try
        {
            Permission permission = coralSession.getSecurity().getPermission(permId);
            coralSession.getSecurity().deletePermission(permission);
            parameters.remove("perm_id");
        }
        catch(EntityDoesNotExistException e)
        {
            logger.error("ARLException: ",e);
            templatingContext.put("result","exception");
            //context.put("trace",StringUtils.stackTrace(e));
            return;
        }
        catch(EntityInUseException e)
        {
            logger.error("ARLException: ",e);
            templatingContext.put("result","exception");
            //context.put("trace",StringUtils.stackTrace(e));
            return;
        }
        templatingContext.put("result","deleted_successfully");
    }
}
