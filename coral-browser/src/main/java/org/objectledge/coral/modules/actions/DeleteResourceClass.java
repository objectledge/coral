package org.objectledge.coral.modules.actions;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.entity.EntityInUseException;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.pipeline.ProcessingException;

/**
 * Delete resource class action.
 * 
 * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: DeleteResourceClass.java,v 1.2 2004-03-25 23:35:27 pablo Exp $
 */
public class DeleteResourceClass
    extends BaseBrowserAction
{
    /**
     * Action constructor.
     * 
     * @param logger the logger.
     * @param coralSessionFactory the coral session factory.
     */
    public DeleteResourceClass(Logger logger, CoralSessionFactory coralSessionFactory)
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
        finally
        {
            coralSession.close();
        }
        templatingContext.put("result","deleted_successfully");
    }
}




