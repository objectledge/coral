package org.objectledge.coral.modules.actions.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.parameters.Parameters;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.utils.StackTrace;
import org.objectledge.web.mvc.MVCContext;

/**
 * Add role action.
 * 
 * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: AddRole.java,v 1.2 2005-02-06 22:30:48 pablo Exp $
 */
public class AddRole extends BaseBrowserAction
{


    public AddRole(Logger logger)
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
            String roleName = parameters.get("role_name", "");
            if (roleName.length() == 0)
            {
                templatingContext.put("result", "invalid_name");
                return;
            }
            coralSession.getSecurity().createRole(roleName);
        }
        catch (Exception e)
        {
            logger.error("ARLException: ", e);
            templatingContext.put("trace",new StackTrace(e));
            templatingContext.put("result", "exception");
            return;
        }
        templatingContext.put("result", "added_successfully");
    }
}
