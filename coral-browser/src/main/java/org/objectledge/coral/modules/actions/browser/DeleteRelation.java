package org.objectledge.coral.modules.actions.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.relation.Relation;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.parameters.Parameters;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.web.mvc.MVCContext;
import org.objectledge.web.mvc.security.PolicySystem;

/**
 * Delete relation action.
 * 
 * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: DeleteRelation.java,v 1.6 2007-11-18 21:19:46 rafal Exp $
 */
public class DeleteRelation
    extends BaseBrowserAction
{
    public DeleteRelation(PolicySystem policySystemArg, Logger logger)
    {
        super(policySystemArg, logger);
    }
    
    /**
     * Performs the action.
     */
    public void execute(Context context, Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, CoralSession coralSession)
    throws ProcessingException    
    {
        long relId = parameters.getLong("rel_id",-1L);
        try
        {
            Relation relation = coralSession.getRelationManager().getRelation(relId);
            coralSession.getRelationManager().deleteRelation(relation);
            parameters.remove("rel_id");
        }
        catch(EntityDoesNotExistException e)
        {
            logger.error("Coral exception: ",e);
            templatingContext.put("result","exception");
            return;
        }
        templatingContext.put("result","deleted_successfully");
    }
}
