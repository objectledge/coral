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
 * Add role action.
 * 
 * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: GrantPermission.java,v 1.2 2005-02-06 22:30:48 pablo Exp $
 */
public class GrantPermission
    extends BaseBrowserAction
{
    public GrantPermission(Logger logger)
    {
        super(logger);
    }
    /**
     * Performs the action.
     */
    public void execute(Context context, Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, CoralSession coralSession)
    throws ProcessingException    
    {
        String roleName = parameters.get("role_name","");
        long resourceId = parameters.getLong("res_id",-1);
        String permissionName = parameters.get("perm_name","");
        boolean recursive = parameters.getBoolean("recursive",false);
        try
        {
            Resource resource = coralSession.getStore().getResource(resourceId);
            Role role = coralSession.getSecurity().getUniqueRole(roleName);
            Permission permission = coralSession.getSecurity().getUniquePermission(permissionName);
            coralSession.getSecurity().grant(resource, role, permission, recursive);
        }
        catch(Exception e)
        {
            logger.error("ARLException: ",e);
            //templatingContext.put("trace",StringUtils.stackTrace(e));
            templatingContext.put("result","exception");
            return;
        }
        templatingContext.put("result","granted_successfully");
    }
}




