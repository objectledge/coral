package org.objectledge.coral.modules.components;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.parameters.Parameters;
import org.objectledge.parameters.RequestParameters;
import org.objectledge.table.TableStateManager;
import org.objectledge.templating.TemplatingContext;

/**
 * The base browse component class.
 */
public abstract class BaseBrowserComponent extends BaseCoralComponent
{
    protected TableStateManager tableStateManager;
    
    protected CoralSession coralSession;

    protected Parameters parameters;

    protected TemplatingContext templatingContext;
    
    public BaseBrowserComponent(Logger logger, CoralSessionFactory sessionFactory,
                                TableStateManager tableStateManager)
    {
        super(logger, sessionFactory);
        this.tableStateManager = tableStateManager;
    }
    
    /**
     * Prepare some usefull components, also it opens new coral session,
     * so do not forget to close it before the end of the processing.
     * 
     * @param context the context.
     */
    public void prepare(Context context)
    {
        coralSession = coralSessionFactory.getRootSession();
        parameters = RequestParameters.getRequestParameters(context);
        templatingContext = TemplatingContext.getTemplatingContext(context);
    }    
}
