package org.objectledge.coral.modules.actions.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.security.Role;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.parameters.Parameters;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.web.mvc.MVCContext;
import org.objectledge.web.mvc.security.PolicySystem;

/**
 * Revoke role action.
 * 
 * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: RevokeRole.java,v 1.3 2005-05-24 05:40:28 pablo Exp $
 */
public class RevokeRole
    extends BaseBrowserAction
{
    public RevokeRole(PolicySystem policySystemArg, Logger logger)
    {
        super(policySystemArg, logger);
    }

    /**
     * Performs the action.
     */
    public void execute(Context context, Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, CoralSession coralSession)
    throws ProcessingException
    {
        long roleId = parameters.getLong("role_id",-1);
        long subjectId = parameters.getLong("sub_id",-1);
        try
        {
            Subject subject = coralSession.getSecurity().getSubject(subjectId);
            Role role = coralSession.getSecurity().getRole(roleId);
            coralSession.getSecurity().revoke(role, subject);
        }
        catch(Exception e)
        {
            logger.error("ARLException: ",e);
            //templatingContext.put("trace",StringUtils.stackTrace(e));
            templatingContext.put("result","exception");
            return;
        }
        templatingContext.put("result","revoked_successfully");
    }
}




