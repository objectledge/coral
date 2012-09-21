package org.objectledge.coral.modules.actions.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.parameters.Parameters;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.web.mvc.MVCContext;
import org.objectledge.web.mvc.security.PolicySystem;

/**
 * Add attribute action.
 * 
 * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: AddAttribute.java,v 1.3 2005-05-24 05:40:28 pablo Exp $
 */
public class AddAttribute extends BaseBrowserAction
{
    public AddAttribute(PolicySystem policySystemArg, Logger logger)
    {
        super(policySystemArg, logger);
    }

    /**
     * Runns the valve.
     *   
     * @param context the context.
     */
    public void execute(Context context, Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, CoralSession coralSession)
        throws ProcessingException
    {
        try
        {
            String attrName = parameters.get("attr_name", "");
            if (attrName.length() == 0)
            {
                throw new ProcessingException("Attribute name not found");
            }
            long resClassId = parameters.getLong("res_class_id", -1L);
            if (resClassId == -1L)
            {
                throw new ProcessingException("Resource class id not found");
            }
            long attrClassId = parameters.getLong("attr_class_id", -1L);
            if (attrClassId == -1L)
            {
                throw new ProcessingException("Attribute class id not found");
            }
            boolean setDomain = parameters.getBoolean("set_domain", false);
            String domain = null;
            if (setDomain)
            {
                domain = parameters.get("domain", "");
            }
            boolean setDbColumn = parameters.getBoolean("set_db_column", false);
            String dbColumn = null;
            if(setDbColumn)
            {
                dbColumn = parameters.get("db_column", null);
            }

            boolean setValue = parameters.getBoolean("set_value", false);
            String value = parameters.get("value", "");
            String[] keys = parameters.getParameterNames();
            int flags = 0;
            for (int i = 0; i < keys.length; i++)
            {
                if (keys[i].startsWith("flag_"))
                {
                    String flagName = keys[i].substring(5, keys[i].length());
                    flags = flags + AttributeFlags.flagValue(flagName);
                }
            }
            AttributeClass attrClass = coralSession.getSchema().getAttributeClass(attrClassId);
            AttributeDefinition attrDefinition = coralSession.getSchema().createAttribute(attrName,
                attrClass, dbColumn, domain, flags);
            ResourceClass resourceClass = coralSession.getSchema().getResourceClass(resClassId);
            Object defaultValue = null;
            if (setValue)
            {
                defaultValue = attrClass.getHandler().toAttributeValue(value);
            }
            coralSession.getSchema().addAttribute(resourceClass, attrDefinition, defaultValue);
        }
        catch (Exception e)
        {
            logger.error("ARLException: ", e);
            templatingContext.put("result", "exception");
            return;
        }
        templatingContext.put("result", "added_successfully");
    }
}
