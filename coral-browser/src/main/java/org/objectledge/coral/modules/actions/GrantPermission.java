package org.objectledge.coral.modules.actions;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.security.Permission;
import org.objectledge.coral.security.Role;
import org.objectledge.coral.store.Resource;
import org.objectledge.pipeline.ProcessingException;

/**
 * Add role action.
 * 
 * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: GrantPermission.java,v 1.2 2004-03-25 23:35:27 pablo Exp $
 */
public class GrantPermission
    extends BaseBrowserAction
{
    /**
     * Action constructor.
     * 
     * @param logger the logger.
     * @param coralSessionFactory the coral session factory.
     */
    public GrantPermission(Logger logger, CoralSessionFactory coralSessionFactory)
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
        finally
        {
            coralSession.close();
        }
        templatingContext.put("result","granted_successfully");
    }
}




