package org.objectledge.coral.modules.views.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.security.Role;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.table.TableStateManager;

/**
 * The role view screen.
 */
public class RoleView extends BaseBrowserView
{
    public RoleView(Context context, Logger logger, CoralSessionFactory sessionFactory,
                    TableStateManager tableStateManager)
    {
        super(context, logger, sessionFactory, tableStateManager);
    }

    public void process(Context context) throws ProcessingException
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
