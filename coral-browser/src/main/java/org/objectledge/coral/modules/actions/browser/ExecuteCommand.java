package org.objectledge.coral.modules.actions.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.pipeline.ProcessingException;

/**
 * Add attribute action.
 * 
 * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: ExecuteCommand.java,v 1.3 2004-07-14 17:30:38 pablo Exp $
 */
public class ExecuteCommand extends BaseBrowserAction
{
    /**
     * Action constructor.
     * 
     * @param logger the logger.
     * @param coralSessionFactory the coral session factory.
     */
    public ExecuteCommand(Logger logger, CoralSessionFactory coralSessionFactory)
    {
        super(logger, coralSessionFactory);
    }

    /**
     * Runns the valve.
     *   
     * @param context the context.
     */
    public void process(Context context) throws ProcessingException
    {
        try
        {
            prepare(context);
            String command = parameters.get("command","");
            if(command.length()>0)
            {
                String result = coralSession.getScript().runScript(command);
                templatingContext.put("command", command);
                templatingContext.put("command_result", result);
            }
        }
        catch (Exception e)
        {
            logger.error("ARLException: ", e);
            templatingContext.put("result", "exception");
            templatingContext.put("command_result", e);
            return;
        }
        finally
        {
            coralSession.close();
        }
        templatingContext.put("result", "executed_successfully");
    }
}
