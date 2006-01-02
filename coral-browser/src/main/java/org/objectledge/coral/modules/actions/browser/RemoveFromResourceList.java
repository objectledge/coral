package org.objectledge.coral.modules.actions.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.datatypes.ResourceList;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.store.Resource;
import org.objectledge.parameters.Parameters;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.web.mvc.MVCContext;
import org.objectledge.web.mvc.security.PolicySystem;

/**
 * Delete relation from cross reference action.
 * 
 * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: RemoveFromResourceList.java,v 1.5 2006-01-02 14:57:58 rafal Exp $
 */
public class RemoveFromResourceList
    extends BaseBrowserAction
{
    private CoralSessionFactory coralSessionFactory;
    
    public RemoveFromResourceList(PolicySystem policySystemArg, Logger logger, 
        CoralSessionFactory coralSessionFactory)
    {
        super(policySystemArg, logger);
        this.coralSessionFactory = coralSessionFactory;
    }
    
    /**
     * Performs the action.
     */
    public void execute(Context context, Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, CoralSession coralSession)
        throws ProcessingException
    {
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
			ResourceList list = new ResourceList(coralSessionFactory, (ResourceList)resource.get(attrDefinition));
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
        templatingContext.put("result", "removed_successfully");        
    }
}




