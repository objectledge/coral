package org.objectledge.coral.modules.actions.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.parameters.Parameters;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.web.mvc.MVCContext;

/**
 * Delete attribute action.
 * 
 * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: DeleteAttribute.java,v 1.2 2005-02-06 22:30:48 pablo Exp $
 */
public class DeleteAttribute extends BaseBrowserAction
{


    public DeleteAttribute(Logger logger)
    {
        super(logger);
    }
    /**
     * Performs the action.
     */
    public void execute(Context context, Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, CoralSession coralSession)
    throws ProcessingException
    {
        try
        {
            long resClassId = parameters.getLong("res_class_id", -1L);
            String attrName = parameters.get("attr_name", "");
            if (attrName.length() == 0)
            {
                throw new ProcessingException("Attribute name not found");
            }
            if (resClassId == -1L)
            {
                throw new ProcessingException("Resource class id not found");
            }
            ResourceClass resourceClass = coralSession.getSchema().getResourceClass(resClassId);
            coralSession.getSchema().deleteAttribute(resourceClass, resourceClass.getAttribute(attrName));
        }
        catch (EntityDoesNotExistException e)
        {
            logger.error("ARLException: ", e);
            templatingContext.put("result", "exception");
            //context.put("trace",StringUtils.stackTrace(e));
            return;
        }
        templatingContext.put("result", "deleted_successfully");
    }
}
