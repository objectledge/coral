package org.objectledge.coral.modules.views.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.security.Role;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.i18n.I18nContext;
import org.objectledge.parameters.Parameters;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.table.TableStateManager;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.web.mvc.MVCContext;

/**
 * The role view screen.
 */
public class RoleView extends BaseBrowserView
{
    public RoleView(Context context, Logger logger, TableStateManager tableStateManager)
    {
        super(context, logger, tableStateManager);
    }
    
    public void process(Parameters parameters, TemplatingContext templatingContext, 
        MVCContext mvcContext, I18nContext i18nContext, CoralSession coralSession)
        throws ProcessingException
    {
        try
        {
            long roleId = parameters.getLong("role_id",-1);
            if (roleId != -1)
            {
                Role role = coralSession.getSecurity().getRole(roleId);
                templatingContext.put("role", role);
            }
        }
        catch (EntityDoesNotExistException e)
        {
            throw new ProcessingException("Role not found", e);
        }
    }
}
