package org.objectledge.coral.modules.actions;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.CoralSessionFactory;
import org.objectledge.coral.store.Resource;
import org.objectledge.pipeline.ProcessingException;

/**
 * Delete resource action.
 * 
 * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: DeleteResourceRecursive.java,v 1.2 2004-03-23 12:07:18 pablo Exp $
 */
public class DeleteResourceRecursive
    extends BaseBrowserAction
{
    /**
     * Action constructor.
     * 
     * @param logger the logger.
     * @param coralSessionFactory the coral session factory.
     */
    public DeleteResourceRecursive(Logger logger, CoralSessionFactory coralSessionFactory)
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
            coralSession.getStore().deleteTree(resource);
            parameters.remove("res_id");
        }
        catch(Exception e)
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




