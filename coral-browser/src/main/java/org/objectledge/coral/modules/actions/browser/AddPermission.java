package org.objectledge.coral.modules.actions.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.parameters.Parameters;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.web.mvc.MVCContext;

/**
 * Add role action.
 * 
 * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: AddPermission.java,v 1.2 2005-02-06 22:30:48 pablo Exp $
 */
public class AddPermission extends BaseBrowserAction
{
    public AddPermission(Logger logger)
    {
        super(logger);
    }

    /**
     * Performs the action.
     */
    public void execute(Context context, Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, CoralSession coralSession)
        throws ProcessingException
    {
        try
        {
            String permissionName = parameters.get("permission_name", "");
            if (permissionName.length() == 0)
            {
                templatingContext.put("result", "invalid_name");
                return;
            }
            coralSession.getSecurity().createPermission(permissionName);
        }
        catch (Exception e)
        {
            logger.error("ARLException: ", e);
            //templatingContext.put("trace",StringUtils.stackTrace(e));
            templatingContext.put("result", "exception");
            return;
        }
        templatingContext.put("result", "added_successfully");
    }
}
