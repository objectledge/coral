package org.objectledge.coral.modules.actions;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.pipeline.ProcessingException;

/**
 * Delete attribute action.
 * 
 * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: DeleteAttribute.java,v 1.3 2004-03-25 23:35:27 pablo Exp $
 */
public class DeleteAttribute extends BaseBrowserAction
{
    /**
     * Action constructor.
     * 
     * @param logger the logger.
     * @param coralSessionFactory the coral session factory.
     */
    public DeleteAttribute(Logger logger, CoralSessionFactory coralSessionFactory)
    {
        super(logger, coralSessionFactory);
    }

    /**
     * Performs the action.
     */
    public void process(Context context) throws ProcessingException
    {
        prepare(context);
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
        finally
        {
            coralSession.close();
        }
        templatingContext.put("result", "deleted_successfully");
    }
}
