package org.objectledge.coral.modules.actions.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.entity.EntityInUseException;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.parameters.Parameters;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.web.mvc.MVCContext;
import org.objectledge.web.mvc.security.PolicySystem;

/**
 * Delete resource class action.
 * 
 * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: DeleteResourceClass.java,v 1.3 2005-05-24 05:40:28 pablo Exp $
 */
public class DeleteResourceClass
    extends BaseBrowserAction
{
    
    public DeleteResourceClass(PolicySystem policySystemArg, Logger logger)
    {
        super(policySystemArg, logger);
    }    
    
    /**
     * Performs the action.
     */
    public void execute(Context context, Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, CoralSession coralSession)
    throws ProcessingException    
    {
        long resClassId = parameters.getLong("res_class_id",-1L);
        try
        {
            ResourceClass resourceClass = coralSession.getSchema().getResourceClass(resClassId);
            coralSession.getSchema().deleteResourceClass(resourceClass);
            parameters.remove("res_class_id");
        }
        catch(EntityDoesNotExistException e)
        {
            logger.error("ARLException: ",e);
            templatingContext.put("result","exception");
            //context.put("trace",StringUtils.stackTrace(e));
            return;
        }
        catch(EntityInUseException e)
        {
            logger.error("ARLException: ",e);
            templatingContext.put("result","exception");
            //context.put("trace",StringUtils.stackTrace(e));
            return;
        }
        templatingContext.put("result","deleted_successfully");
    }
}




