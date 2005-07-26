package org.objectledge.coral.modules.components;

import org.objectledge.context.Context;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.parameters.Parameters;
import org.objectledge.parameters.RequestParameters;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.templating.Template;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.web.mvc.MVCContext;
import org.objectledge.web.mvc.builders.BuildException;
import org.objectledge.web.mvc.components.AbstractComponent;

/**
 * The base coral component class.
 */
public abstract class BaseCoralComponent
    extends AbstractComponent
{
    /**
     * Constructor.
     * @param context the context.
     */
    public BaseCoralComponent(Context context)
    {
        super(context);
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
            process(parameters, templatingContext, mvcContext, coralSession);
            return super.build(template);
        }
        catch(Exception e)
        {
			if(template != null)
			{
				throw new BuildException("Failed to build the component with template: "+template.getName(),e);
			}
			throw new BuildException("Failed to build the component",e);
        }
    }    

    /**
     * To be implemented in particular components.
     * @param parameters the parameters
     * @param templatingContext the templating context.
     * @param mvcContext the mvc context.
     * @param coralSession the coral session.
     * @throws ProcessingException if the processing in the component fails.
     */    
    public abstract void process(Parameters parameters, TemplatingContext templatingContext, 
        						 MVCContext mvcContext,
        						 CoralSession coralSession)
        throws ProcessingException;    
}
