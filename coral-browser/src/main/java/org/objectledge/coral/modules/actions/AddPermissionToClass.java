package org.objectledge.coral.modules.actions;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.CoralSessionFactory;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.security.Permission;
import org.objectledge.pipeline.ProcessingException;

/**
 * Add role action.
 * 
 * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: AddPermissionToClass.java,v 1.1 2004-03-22 20:21:35 pablo Exp $
 */
public class AddPermissionToClass
    extends BaseBrowserAction
{
    /**
     * Action constructor.
     * 
     * @param logger the logger.
     * @param coralSessionFactory the coral session factory.
     */
    public AddPermissionToClass(Logger logger, CoralSessionFactory coralSessionFactory)
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
        long resourceClassId = parameters.getLong("res_class_id",-1);
        String permissionName = parameters.get("perm_name","");
        try
        {
            ResourceClass resourceClass = coralSession.getSchema().getResourceClass(resourceClassId);
            Permission permission = coralSession.getSecurity().getUniquePermission(permissionName);
            coralSession.getSecurity().addPermission(resourceClass, permission);
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
        templatingContext.put("result","altered_successfully");
    }
}




