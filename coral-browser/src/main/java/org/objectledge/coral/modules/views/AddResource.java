package org.objectledge.coral.modules.views;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.table.TableStateManager;

/**
 * The resource class view screen.
 */
public class AddResource extends BaseBrowserView
{
    public AddResource(Context context, Logger logger, CoralSessionFactory sessionFactory, 
                        TableStateManager tableStateManager)
    {
        super(context, logger, sessionFactory, tableStateManager);
    }

    public void process(Context context) throws ProcessingException
    {
        try
        {
            String resClassName = parameters.get("res_class_name", "");
            if (resClassName.length() > 0)
            {
                ResourceClass resourceClass = coralSession.getSchema().getResourceClass(resClassName);
                templatingContext.put("resourceClass", resourceClass);
            }
            templatingContext.put("flags", new AttributeFlags());
        }
        catch (EntityDoesNotExistException e)
        {
            throw new ProcessingException("Resource not found", e);
        }
        finally
        {
            coralSession.close();
        }
    }
}
