package org.objectledge.coral.modules.actions.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.security.Role;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.parameters.Parameters;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.web.mvc.MVCContext;

/**
 * Grant role action.
 * 
 * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: AddSuperRole.java,v 1.2 2005-02-06 22:30:48 pablo Exp $
 */
public class AddSuperRole
    extends BaseBrowserAction
{
    
    public AddSuperRole(Logger logger)
    {
        super(logger);
    }    
    
    /**
     * Performs the action.
     */
    public void execute(Context context, Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, CoralSession coralSession)
    throws ProcessingException    
    {
        long roleId = parameters.getLong("role_id",-1);
        String roleName = parameters.get("role_name","");
        try
        {
            Role role = coralSession.getSecurity().getRole(roleId);
            Role superRole = coralSession.getSecurity().getUniqueRole(roleName);
            coralSession.getSecurity().addSubRole(superRole, role);
        }
        catch(Exception e)
        {
            logger.error("ARLException: ",e);
            //context.put("trace",StringUtils.stackTrace(e));
            templatingContext.put("result","exception");
            return;
        }
        templatingContext.put("result","added_successfully");
    }
}




