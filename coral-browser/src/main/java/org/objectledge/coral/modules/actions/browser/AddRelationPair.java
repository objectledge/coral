package org.objectledge.coral.modules.actions.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.relation.Relation;
import org.objectledge.coral.relation.RelationModification;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.store.Resource;
import org.objectledge.parameters.Parameters;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.web.mvc.MVCContext;
import org.objectledge.web.mvc.security.PolicySystem;

/**
 * Add relation action.
 * 
 * @author <a href="mailo:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: AddRelationPair.java,v 1.1 2006-03-06 13:03:35 rafal Exp $
 */
public class AddRelationPair
    extends BaseBrowserAction
{
    public AddRelationPair(PolicySystem policySystemArg, Logger logger)
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
			long relId = parameters.getLong("rel_id",-1);
			long headId = parameters.getLong("head",-1);
			long tailId = parameters.getLong("tail",-1);
			Resource head = coralSession.getStore().getResource(headId);
			Resource tail = coralSession.getStore().getResource(tailId);
            Relation rel = coralSession.getRelationManager().getRelation(relId);
            RelationModification mod = new RelationModification();
            mod.add(head, tail);
            coralSession.getRelationManager().updateRelation(rel, mod);
        }
        catch(Exception e)
        {
            logger.error("Coral exception: ",e);
            templatingContext.put("result","exception");
            return;
        } 
        templatingContext.put("result", "pair_added");        
    }
}




