package org.objectledge.coral.modules.components;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.parameters.Parameters;
import org.objectledge.parameters.RequestParameters;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.templating.Template;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.web.mvc.MVCContext;
import org.objectledge.web.mvc.builders.BuildException;
import org.objectledge.web.mvc.components.DefaultComponent;

/**
 * The base coral component class.
 */
public abstract class BaseCoralComponent
    extends DefaultComponent
{
    /** logger */
    protected Logger logger;
    
    /**
     * Constructor.
     * 
     * @param context the context.
     * @param logger the logger.
     */
    public BaseCoralComponent(Context context, Logger logger)
    {
        super(context);
        this.logger = logger;
    }

    /**
     * {@inheritDoc}
     */
    public String build(Template template) 
        throws BuildException
    {
    	CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
        Parameters parameters = RequestParameters.getRequestParameters(context);
        TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
        MVCContext mvcContext = MVCContext.getMVCContext(context);
        try
        {
            process(context, parameters, templatingContext, mvcContext, coralSession);
            return super.build(template);
        }
        catch(Exception e)
        {
            throw new BuildException("Failed to build the view",e);
        }
    }    

    /**
     * To be implemented in particular views.
     * @param context the context.
     * @param parameters the parameters
     * @param templatingContext the templating context.
     * @param mvcContext the mvc context.
     * @param coralSession TODO
     */    
    public abstract void process(Context context, Parameters parameters, TemplatingContext templatingContext, MVCContext mvcContext, CoralSession coralSession)
        throws ProcessingException;    
}
