package org.objectledge.coral.modules.actions.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.security.Permission;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.parameters.Parameters;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.web.mvc.MVCContext;
import org.objectledge.web.mvc.security.PolicySystem;

/**
 * Add role action.
 * 
 * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: AddPermissionToClass.java,v 1.3 2005-05-24 05:40:28 pablo Exp $
 */
public class AddPermissionToClass
    extends BaseBrowserAction
{

    
    public AddPermissionToClass(PolicySystem policySystemArg, Logger logger)
    {
        super(policySystemArg, logger);
    }
    /**
     * Performs the action.
     */
    public void execute(Context context, Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, CoralSession coralSession)
        throws ProcessingException
    {
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
        templatingContext.put("result","altered_successfully");
    }
}




