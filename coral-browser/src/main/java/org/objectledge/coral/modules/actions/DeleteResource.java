package org.objectledge.coral.modules.actions;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.entity.EntityInUseException;
import org.objectledge.coral.store.Resource;
import org.objectledge.pipeline.ProcessingException;

/**
 * Delete resource action.
 * 
 * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: DeleteResource.java,v 1.2 2004-03-25 23:35:27 pablo Exp $
 */
public class DeleteResource
    extends BaseBrowserAction
{
    /**
     * Action constructor.
     * 
     * @param logger the logger.
     * @param coralSessionFactory the coral session factory.
     */
    public DeleteResource(Logger logger, CoralSessionFactory coralSessionFactory)
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
        long resId = parameters.getLong("res_id",-1L);
        try
        {
            Resource resource = coralSession.getStore().getResource(resId);
            coralSession.getStore().deleteResource(resource);
            parameters.remove("res_id");
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
        finally
        {
            coralSession.close();
        }
        templatingContext.put("result","deleted_successfully");
    }
}




