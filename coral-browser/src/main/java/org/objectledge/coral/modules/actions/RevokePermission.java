package org.objectledge.coral.modules.actions;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.CoralSessionFactory;
import org.objectledge.coral.security.Permission;
import org.objectledge.coral.security.Role;
import org.objectledge.coral.store.Resource;
import org.objectledge.pipeline.ProcessingException;

/**
 * Revoke permission action.
 * 
 * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: RevokePermission.java,v 1.1 2004-03-22 20:21:35 pablo Exp $
 */
public class RevokePermission
    extends BaseBrowserAction
{
    /**
     * Action constructor.
     * 
     * @param logger the logger.
     * @param coralSessionFactory the coral session factory.
     */
    public RevokePermission(Logger logger, CoralSessionFactory coralSessionFactory)
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
        finally
        {
            coralSession.close();
        }
        templatingContext.put("result","revoked_successfully");
    }
}




