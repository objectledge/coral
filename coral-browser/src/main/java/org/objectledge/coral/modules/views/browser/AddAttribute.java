package org.objectledge.coral.modules.views.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.table.TableStateManager;

/**
 * The add attribute screen.
 */
public class AddAttribute extends BaseBrowserView
{
    public AddAttribute(Context context, Logger logger, CoralSessionFactory sessionFactory,
                        TableStateManager tableStateManager)
    {
        super(context, logger, sessionFactory, tableStateManager);
    }

    public void process(Context context) throws ProcessingException
    {
        try
        {
            AttributeClass[] attributeClass = coralSession.getSchema().getAttributeClass();
            templatingContext.put("attr_classes", attributeClass);
            templatingContext.put("flags", new AttributeFlags());
            long resClassId = parameters.getLong("res_class_id",-1);
            ResourceClass resourceClass = coralSession.getSchema().getResourceClass(resClassId);
            templatingContext.put("resourceClass", resourceClass);
        }
        catch (Exception e)
        {
            throw new ProcessingException("Resource class not found", e);
        }
    }
}
