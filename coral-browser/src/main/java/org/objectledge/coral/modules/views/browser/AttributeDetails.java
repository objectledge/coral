package org.objectledge.coral.modules.views.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.store.Resource;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.table.TableStateManager;

/**
 * The attribute details resource view screen.
 */
public class AttributeDetails extends BaseBrowserView
{
    public AttributeDetails(Context context, Logger logger, CoralSessionFactory sessionFactory,
                    TableStateManager tableStateManager)
    {
        super(context, logger, sessionFactory, tableStateManager);
    }
    
    public void process(Context context) throws ProcessingException
    {
        try
        {
            long resId = parameters.getLong("res_id",-1);
            String attrName = parameters.get("attr_name","");
            if (resId == -1 || attrName.length() == 0)
            {
                throw new ProcessingException("parameter not found");
            }
            Resource resource = coralSession.getStore().getResource(resId);
            templatingContext.put("resource", resource);
            AttributeDefinition attrDefinition = resource.getResourceClass().getAttribute(attrName);
            templatingContext.put("attr_def", attrDefinition);
        }
        catch (EntityDoesNotExistException e)
        {
            throw new ProcessingException("Resource not found", e);
        }
    }
}
