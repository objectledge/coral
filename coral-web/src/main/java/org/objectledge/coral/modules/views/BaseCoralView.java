package org.objectledge.coral.modules.views;

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
import org.objectledge.web.mvc.builders.DefaultBuilder;

/**
 * The base screen for coral application.
 */
public abstract class BaseCoralView
    extends DefaultBuilder
{
    /** logger */
    protected Logger logger;
    
    /**
     * The constructor.
     * 
     * @param context the context.
     * @param logger the logger.
     */  
    public BaseCoralView(Context context, Logger logger)
    {
        super(context);
        this.logger = logger;
    }

    /**
     * {@inheritDoc}
     */
    public String build(Template template, String embeddedBuildResults) 
        throws BuildException
    {
    	CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);;
        Parameters parameters = RequestParameters.getRequestParameters(context);
        TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
        MVCContext mvcContext = MVCContext.getMVCContext(context);
    	try
        {
            process(parameters, templatingContext, mvcContext, coralSession);
            return super.build(template, embeddedBuildResults);
        }
        catch(ProcessingException e)
        {
            throw new BuildException("Failed to build the view",e);
        }
    }
    
    /**
     * To be implemented in views.
     * 
     * @param parameters the parameters.
     * @param templatingContext the templating context.
     * @param mvcContext the mvcContext
     * @param coralSession the coral session.
     */    
    public abstract void process(Parameters parameters, TemplatingContext templatingContext, 
        						 MVCContext mvcContext, 
        						 CoralSession coralSession)
        throws ProcessingException;
}
