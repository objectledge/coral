package org.objectledge.coral.modules.actions;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.CoralSessionFactory;
import org.objectledge.coral.datatypes.ResourceList;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.store.Resource;
import org.objectledge.pipeline.ProcessingException;

/**
 * Delete relation from cross reference action.
 * 
 * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: RemoveFromResourceList.java,v 1.1 2004-03-22 20:21:35 pablo Exp $
 */
public class RemoveFromResourceList
    extends BaseBrowserAction
{
    /**
     * Action constructor.
     * 
     * @param logger the logger.
     * @param coralSessionFactory the coral session factory.
     */
    public RemoveFromResourceList(Logger logger, CoralSessionFactory coralSessionFactory)
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
        try
        {
			long resId = parameters.getLong("res_id",-1);
			long res1 = parameters.getLong("res_1",-1);
			String attrName = parameters.get("attr_name","");
			if (resId == -1 || attrName.length() == 0 || res1 == -1)
			{
				throw new ProcessingException("parameter not found");
			}
			Resource resource = coralSession.getStore().getResource(resId);
			Resource resource1 = coralSession.getStore().getResource(res1);
			AttributeDefinition attrDefinition = resource.getResourceClass().getAttribute(attrName);
			ResourceList list = (ResourceList)resource.get(attrDefinition);
			list.remove(resource1);
			resource.set(attrDefinition, list);
			resource.update();
        }
        catch(Exception e)
        {
            logger.error("ARLException: ",e);
            templatingContext.put("result","exception");
            //templatingContext.put("trace",StringUtils.stackTrace(e));
            return;
        } 
        finally
        {
            coralSession.close();
        }
        templatingContext.put("result", "removed_successfully");        
    }
}




