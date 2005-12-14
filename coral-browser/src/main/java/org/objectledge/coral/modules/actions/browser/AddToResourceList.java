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
 * @version $Id: AddToResourceList.java,v 1.4 2005-12-14 11:42:25 pablo Exp $
 */
public class AddToResourceList
    extends BaseBrowserAction
{
    private CoralSessionFactory coralSessionFactory;
    
    public AddToResourceList(PolicySystem policySystemArg, Logger logger,
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
			ResourceList list = (ResourceList)resource.get(attrDefinition);
			list.add(resource1);
			resource.set(attrDefinition, new ResourceList(coralSessionFactory, list));
			resource.update();
        }
        catch(Exception e)
        {
            logger.error("ARLException: ",e);
            templatingContext.put("result","exception");
             //   context.put("trace",StringUtils.stackTrace(e));
            return;
        }
		templatingContext.put("result", "added_successfully"); 
    }
}




