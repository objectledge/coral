package org.objectledge.coral.modules.actions.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.security.Permission;
import org.objectledge.coral.security.Role;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.store.Resource;
import org.objectledge.parameters.Parameters;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.web.mvc.MVCContext;

/**
 * Revoke permission action.
 * 
 * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: RevokePermission.java,v 1.2 2005-02-06 22:30:48 pablo Exp $
 */
public class RevokePermission
    extends BaseBrowserAction
{
 
    
    public RevokePermission(Logger logger)
    {
        super(logger);
    }
    /**
     * Performs the action.
     */
    public void execute(Context context, Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, CoralSession coralSession)
    throws ProcessingException
    {
        long roleId = parameters.getLong("role_id",-1);
        long resourceId = parameters.getLong("res_id",-1);
        long permissionId = parameters.getLong("perm_id",-1);
        try
        {
            Resource resource = coralSession.getStore().getResource(resourceId);
            Role role = coralSession.getSecurity().getRole(roleId);
            Permission permission = coralSession.getSecurity().getPermission(permissionId);
            coralSession.getSecurity().revoke(resource, role, permission);
        }
        catch(Exception e)
        {
            logger.error("ARLException: ",e);
            //context.put("trace",StringUtils.stackTrace(e));
            templatingContext.put("result","exception");
            return;
        }
        templatingContext.put("result","revoked_successfully");
    }
}




