package org.objectledge.coral.modules.views.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.i18n.I18nContext;
import org.objectledge.parameters.Parameters;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.table.TableStateManager;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.web.mvc.MVCContext;

/**
 * The resource class view screen.
 */
public class ResourceClassView
    extends BaseBrowserView
{
    public ResourceClassView(Context context, Logger logger, TableStateManager tableStateManager)
    {
        super(context, logger, tableStateManager);
    }
    
    public void process(Parameters parameters, TemplatingContext templatingContext, 
        MVCContext mvcContext, I18nContext i18nContext, CoralSession coralSession)
        throws ProcessingException
    {
        try
        {
            long resClassId = parameters.getLong("res_class_id",-1);
            if(resClassId != -1)
            {
                ResourceClass resourceClass = coralSession.getSchema().getResourceClass(resClassId);
                templatingContext.put("resourceClass",resourceClass);
            }
            templatingContext.put("flags", new AttributeFlags());
        }
        catch(Exception e)
        {
            throw new ProcessingException("Resource class not found",e);
        }
    }
}
