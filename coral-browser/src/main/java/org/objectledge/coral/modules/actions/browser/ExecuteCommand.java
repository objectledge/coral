package org.objectledge.coral.modules.actions.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.utils.StackTrace;

/**
 * Add attribute action.
 * 
 * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: ExecuteCommand.java,v 1.5 2004-08-30 09:04:25 rafal Exp $
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
        finally
        {
            coralSession.close();
        }
        templatingContext.put("result", "executed_successfully");
    }
}
