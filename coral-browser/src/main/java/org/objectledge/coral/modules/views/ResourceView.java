package org.objectledge.coral.modules.views;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.store.Resource;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.table.TableStateManager;

/**
 * The resource view screen.
 */
public class ResourceView
    extends BaseBrowserView
{
    public ResourceView(Context context, Logger logger, CoralSessionFactory sessionFactory,
                    TableStateManager tableStateManager)
    {
        super(context, logger, sessionFactory, tableStateManager);
    }

    public void process(Context context) throws ProcessingException
    {
        try
        {
            long resId = parameters.getLong("res_id",-1);
            if(resId != -1)
            {
                Resource resource = coralSession.getStore().getResource(resId);
                templatingContext.put("resource",resource);
            }
        }
        catch(Exception e)
        {
            throw new ProcessingException("Resource not found",e);
        }
        finally
        {
            coralSession.close();
        }
    }
}
