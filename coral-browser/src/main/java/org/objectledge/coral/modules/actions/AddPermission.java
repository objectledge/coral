package org.objectledge.coral.modules.actions;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.CoralSessionFactory;
import org.objectledge.pipeline.ProcessingException;

/**
 * Add role action.
 * 
 * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: AddPermission.java,v 1.2 2004-03-23 12:07:18 pablo Exp $
 */
public class AddPermission extends BaseBrowserAction
{
    /**
     * Action constructor.
     * 
     * @param logger the logger.
     * @param coralSessionFactory the coral session factory.
     */
    public AddPermission(Logger logger, CoralSessionFactory coralSessionFactory)
    {
        super(logger, coralSessionFactory);
    }

    /**
     * Performs the action.
     */
    public void process(Context context) throws ProcessingException
    {
        try
        {
            prepare(context);
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
        finally
        {
            coralSession.close();
        }
        templatingContext.put("result", "added_successfully");
    }
}
