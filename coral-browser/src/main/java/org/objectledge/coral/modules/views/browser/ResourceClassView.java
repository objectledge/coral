package org.objectledge.coral.modules.views.browser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.table.comparator.AttributeDefnitionsComparator;
import org.objectledge.i18n.I18nContext;
import org.objectledge.parameters.Parameters;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.table.TableStateManager;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.web.mvc.MVCContext;
import org.objectledge.web.mvc.security.PolicySystem;

/**
 * The resource class view screen.
 */
public class ResourceClassView
    extends BaseBrowserView
{
    public ResourceClassView(Context context, PolicySystem policySystemArg, Logger logger, TableStateManager tableStateManager)
    {
        super(context, policySystemArg, logger, tableStateManager);
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
                List<AttributeDefinition> attributeDefinitionss = new ArrayList<AttributeDefinition>();
                attributeDefinitionss.addAll(Arrays.asList(resourceClass.getAllAttributes()));
                Collections.sort(attributeDefinitionss, new AttributeDefnitionsComparator());
                templatingContext.put("attributeDefinitions", attributeDefinitionss);
            }
            templatingContext.put("flags", new AttributeFlags());
        }
        catch(Exception e)
        {
            throw new ProcessingException("Resource class not found",e);
        }
    }
}
