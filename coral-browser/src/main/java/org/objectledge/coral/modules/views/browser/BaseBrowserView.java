package org.objectledge.coral.modules.views.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.parameters.Parameters;
import org.objectledge.parameters.RequestParameters;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.table.TableStateManager;
import org.objectledge.templating.Template;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.web.mvc.builders.BuildException;

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
    
    public BaseBrowserView(Context context, Logger logger, CoralSessionFactory sessionFactory,
                           TableStateManager tableStateManager)
    {
        super(context, logger, sessionFactory);
        this.tableStateManager = tableStateManager;
    }

    /**
     * {@inheritDoc}
     */
    public String build(Template template, String embeddedBuildResults) 
        throws BuildException
    {
        coralSession = (CoralSession)context.getAttribute(CoralSession.class);
        parameters = RequestParameters.getRequestParameters(context);
        templatingContext = TemplatingContext.getTemplatingContext(context);
        try
        {
            process(context);
            return super.build(template, embeddedBuildResults);
        }
        catch(ProcessingException e)
        {
            throw new BuildException("Failed to build the view",e);
        }
    }

    /**
     * To be implemented in browser views.
     * 
     * @param context the context.
     */    
    public abstract void process(Context context)
        throws ProcessingException;
}
