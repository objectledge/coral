package org.objectledge.coral.modules.actions.browser;


import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.store.Resource;
import org.objectledge.pipeline.ProcessingException;

/**
 * Delete relation from cross reference action.
 * 
 * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: DeleteRelation.java,v 1.1 2004-03-26 14:07:06 pablo Exp $
 */
public class DeleteRelation
    extends BaseBrowserAction
{
    /**
     * Action constructor.
     * 
     * @param logger the logger.
     * @param coralSessionFactory the coral session factory.
     */
    public DeleteRelation(Logger logger, CoralSessionFactory coralSessionFactory)
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
			long res2 = parameters.getLong("res_2",-1);
			String attrName = parameters.get("attr_name","");
			if (resId == -1 || attrName.length() == 0 || res1 == -1 || res2 == -1)
			{
				throw new ProcessingException("parameter not found");
			}
			Resource resource = coralSession.getStore().getResource(resId);
			Resource resource1 = coralSession.getStore().getResource(res1);
			Resource resource2 = coralSession.getStore().getResource(res2);
			AttributeDefinition attrDefinition = resource.getResourceClass().getAttribute(attrName);
            //TODO cross to relation
			//CrossReference refs = (CrossReference)resource.get(attrDefinition);
			//refs.remove(resource1, resource2);
			//resource.set(attrDefinition, refs);
			resource.update();
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
        templatingContext.put("result", "relation_deleted");        
    }
}




