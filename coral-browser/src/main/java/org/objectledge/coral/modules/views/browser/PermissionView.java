package org.objectledge.coral.modules.views.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.security.Permission;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.table.TableStateManager;

/**
 * The permission view screen.
 */
public class PermissionView
    extends BaseBrowserView
{
    public PermissionView(Context context, Logger logger, CoralSessionFactory sessionFactory,
                    TableStateManager tableStateManager)
    {
        super(context, logger, sessionFactory, tableStateManager);
    }
    
    public void process(Context context) throws ProcessingException
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
