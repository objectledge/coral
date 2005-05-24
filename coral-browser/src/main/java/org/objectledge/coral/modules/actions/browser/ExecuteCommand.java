package org.objectledge.coral.modules.actions.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.parameters.Parameters;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.utils.StackTrace;
import org.objectledge.web.mvc.MVCContext;
import org.objectledge.web.mvc.security.PolicySystem;

/**
 * Add attribute action.
 * 
 * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: ExecuteCommand.java,v 1.7 2005-05-24 05:40:28 pablo Exp $
 */
public class ExecuteCommand extends BaseBrowserAction
{
    public ExecuteCommand(PolicySystem policySystemArg, Logger logger)
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
            String command = parameters.get("command","");
            if(command.length()>0)
            {
                String result = coralSession.getScript().runScript(command);
                templatingContext.put("command", command);
                templatingContext.put("commandResult", result);
            }
        }
        catch(VirtualMachineError e)
        {
            throw e;
        }
        catch(ThreadDeath e)
        {
            throw e;
        }
        catch(Throwable e)
        {
            logger.error("ARLException: ", e);
            templatingContext.put("result", "exception");
            templatingContext.put("commandResult", new StackTrace(e));
            return;
        }
        templatingContext.put("result", "executed_successfully");
    }
}
