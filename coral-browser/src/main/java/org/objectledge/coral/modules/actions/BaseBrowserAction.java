package org.objectledge.coral.modules.actions;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.CoralSession;
import org.objectledge.coral.CoralSessionFactory;
import org.objectledge.parameters.Parameters;
import org.objectledge.parameters.RequestParameters;
import org.objectledge.templating.TemplatingContext;

/**
 * The base action for coral browser application.
 */
public abstract class BaseBrowserAction
    extends BaseCoralAction
{
    protected CoralSession coralSession;
    
    protected Parameters parameters;
    
    protected TemplatingContext templatingContext; 
    
    public BaseBrowserAction(Logger logger, CoralSessionFactory sessionFactory)
    {
        super(logger, sessionFactory);
    }

    public void prepare(Context context)
    {
        coralSession = coralSessionFactory.getRootSession();
        parameters = RequestParameters.getRequestParameters(context);
        templatingContext = TemplatingContext.getTemplatingContext(context);
    }
}
