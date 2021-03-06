package org.objectledge.coral.modules.actions.browser;

import java.util.HashMap;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.store.Resource;
import org.objectledge.parameters.Parameters;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.web.mvc.MVCContext;
import org.objectledge.web.mvc.security.PolicySystem;

/**
 * Add resource action.
 * 
 * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: AddResource.java,v 1.5 2005-05-24 05:40:28 pablo Exp $
 */
public class AddResource extends BaseBrowserAction
{

    
    public AddResource(PolicySystem policySystemArg, Logger logger)
    {
        super(policySystemArg, logger);
    }
    /**
     * Performs the action.
     */
    public void execute(Context context, Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, CoralSession coralSession)
    throws ProcessingException
    {
        try
        {
            String resClassName = parameters.get("res_class_name", "");
            if (resClassName.length() == 0)
            {
                //TODO:
                //route(data, "AddResource", "class_empty");
                return;
            }
            String name = parameters.get("name", "");
            if (name.length() == 0)
            {
                //route(data, "AddResource", "name_empty");
                return;
            }
            String parentPath = parameters.get("parent", "");

            /*
            String value = data.getParameters().get("value").asString("");
            String[] keys = data.getParameters().getKeys();
            int flags = 0;
            for(int i = 0; i < keys.length; i++)
            {
                if(keys[i].startsWith("flag_"))
                {
                    String flagName = keys[i].substring(5,keys[i].length());
                    flags = flags + AttributeFlags.flagValue(flagName);
                }
            }
            */
            ResourceClass resourceClass = coralSession.getSchema().getResourceClass(resClassName);
            
            Resource parent = null;
            if(parentPath.equals("") || parentPath.equals("/"))
            {
                parent = coralSession.getStore().getResource(1);   
            }
            else
            {
                parent = coralSession.getStore().getUniqueResourceByPath(parentPath);
            }
            HashMap attr = new HashMap();

            AttributeDefinition[] attrDef = resourceClass.getAllAttributes();
            for (int i = 0; i < attrDef.length; i++)
            {
                if (((attrDef[i].getFlags() & AttributeFlags.SYNTHETIC) == 0) && !attrDef[i].getDeclaringClass().getName().equals("coral.Node"))
                {
                    if (parameters.getBoolean("defined_" + attrDef[i].getName(), false))
                    {
                        String attrValue = parameters.get("attr_" + attrDef[i].getName(), "");
                        try
                        {
                            attr.put(attrDef[i], attrDef[i].getAttributeClass().getHandler().toAttributeValue(attrValue));
                        }
                        catch (Exception e)
                        {
                            //templatingContext.put("trace", "Illegal value of " + attrDef[i].getName() + " attribute\n" + StringUtils.stackTrace(e));
                            //route(data, "AddResource", "exception");
                            return;
                        }
                        finally
                        {
                            coralSession.close();
                        }
                    }
                    else
                    {
                        if ((attrDef[i].getFlags() & AttributeFlags.REQUIRED) != 0)
                        {
                            templatingContext.put("trace", "Required attribute '" + attrDef[i].getName() + "' not defined");
                            //route(data, "AddResource", "exception");
                            return;
                        }
                    }
                }
            }
            coralSession.getStore().createResource(name, parent, resourceClass, attr);
        }
        catch (Exception e)
        {
            logger.error("ARLException: ", e);
            templatingContext.put("result", "exception");
            templatingContext.put("exception",e);
            return;
        }
        templatingContext.put("result", "added_successfully");
    }
}
