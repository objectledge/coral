package org.objectledge.coral.modules.views;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.CoralSession;
import org.objectledge.coral.CoralSessionFactory;
import org.objectledge.parameters.Parameters;
import org.objectledge.parameters.RequestParameters;
import org.objectledge.table.TableStateManager;
import org.objectledge.templating.TemplatingContext;

/**
 * The base screen for alr browser application.
 */
public abstract class BaseBrowserView
    extends BaseCoralView
{
    protected TableStateManager tableStateManager;
    
    protected CoralSession coralSession;

    protected Parameters parameters;

    protected TemplatingContext templatingContext;
    
    public BaseBrowserView(Logger logger, CoralSessionFactory sessionFactory,
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
