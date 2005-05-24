package org.objectledge.coral.modules.views.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.security.Permission;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.i18n.I18nContext;
import org.objectledge.parameters.Parameters;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.table.TableStateManager;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.web.mvc.MVCContext;
import org.objectledge.web.mvc.security.PolicySystem;

/**
 * The permission view screen.
 */
public class PermissionView
    extends BaseBrowserView
{
    public PermissionView(Context context, PolicySystem policySystemArg, Logger logger, TableStateManager tableStateManager)
    {
        super(context, policySystemArg, logger, tableStateManager);
    }
    
    public void process(Parameters parameters, TemplatingContext templatingContext, 
        MVCContext mvcContext, I18nContext i18nContext, CoralSession coralSession)
        throws ProcessingException
    {
        try
        {
            long permissionId = parameters.getLong("perm_id",-1);
            if(permissionId != -1)
            {
                Permission permission = coralSession.getSecurity().getPermission(permissionId);
                templatingContext.put("permission",permission);
            }
        }
        catch(EntityDoesNotExistException e)
        {
            throw new ProcessingException("Permission not found",e);
        }
    }
}
