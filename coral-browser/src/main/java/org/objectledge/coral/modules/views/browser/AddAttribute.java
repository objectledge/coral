package org.objectledge.coral.modules.views.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.i18n.I18nContext;
import org.objectledge.parameters.Parameters;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.table.TableStateManager;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.web.mvc.MVCContext;
import org.objectledge.web.mvc.security.PolicySystem;

/**
 * The add attribute screen.
 */
public class AddAttribute extends BaseBrowserView
{
    public AddAttribute(Context context, PolicySystem policySystemArg, Logger logger, TableStateManager tableStateManager)
    {
        super(context, policySystemArg, logger, tableStateManager);
    }

    public void process(Parameters parameters, TemplatingContext templatingContext, 
        MVCContext mvcContext, I18nContext i18nContext, CoralSession coralSession)
        throws ProcessingException
    {
        try
        {
            AttributeClass[] attributeClass = coralSession.getSchema().getAttributeClass();
            templatingContext.put("attrClasses", attributeClass);
            templatingContext.put("flags", new AttributeFlags());
            long resClassId = parameters.getLong("res_class_id",-1);
            ResourceClass resourceClass = coralSession.getSchema().getResourceClass(resClassId);
            templatingContext.put("resourceClass", resourceClass);
        }
        catch (Exception e)
        {
            throw new ProcessingException("Resource class not found", e);
        }
    }
}
