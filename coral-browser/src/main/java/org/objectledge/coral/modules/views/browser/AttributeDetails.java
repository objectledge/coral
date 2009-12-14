package org.objectledge.coral.modules.views.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.store.Resource;
import org.objectledge.i18n.I18nContext;
import org.objectledge.parameters.Parameters;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.table.TableStateManager;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.web.mvc.MVCContext;
import org.objectledge.web.mvc.security.PolicySystem;

/**
 * The attribute details resource view screen.
 */
public class AttributeDetails extends BaseBrowserView
{
    public AttributeDetails(Context context, PolicySystem policySystemArg, Logger logger, TableStateManager tableStateManager)
    {
        super(context, policySystemArg, logger, tableStateManager);
    }

    public void process(Parameters parameters, TemplatingContext templatingContext, 
        MVCContext mvcContext, I18nContext i18nContext, CoralSession coralSession)
        throws ProcessingException
    {
        try
        {
            long resId = parameters.getLong("res_id",-1);
            String attrName = parameters.get("attr_name","");
            if (resId == -1 || attrName.length() == 0)
            {
                throw new ProcessingException("parameter not found");
            }
            Resource resource = coralSession.getStore().getResource(resId);
            templatingContext.put("resource", resource);
            AttributeDefinition attrDefinition = resource.getResourceClass().getAttribute(attrName);
            templatingContext.put("attrDef", attrDefinition);
        }
        catch (EntityDoesNotExistException e)
        {
            throw new ProcessingException("Resource not found", e);
        }
    }
}
