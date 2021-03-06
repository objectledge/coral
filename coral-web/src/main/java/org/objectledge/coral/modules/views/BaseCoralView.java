package org.objectledge.coral.modules.views;

import org.objectledge.authentication.AuthenticationContext;
import org.objectledge.context.Context;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.i18n.I18nContext;
import org.objectledge.parameters.Parameters;
import org.objectledge.parameters.RequestParameters;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.templating.Template;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.web.HttpContext;
import org.objectledge.web.mvc.MVCContext;
import org.objectledge.web.mvc.builders.AbstractBuilder;
import org.objectledge.web.mvc.builders.BuildException;

/**
 * The base screen for coral application.
 */
public abstract class BaseCoralView
    extends AbstractBuilder
{
    /**
     * The constructor.
     * 
     * @param context the context.
     */  
    public BaseCoralView(Context context)
    {
        super(context);
    }

    /**
     * {@inheritDoc}
     */
    public String build(Template template, String embeddedBuildResults) 
        throws BuildException
    {
    	CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
        Parameters parameters = RequestParameters.getRequestParameters(context);
        TemplatingContext templatingContext = TemplatingContext.getTemplatingContext(context);
        MVCContext mvcContext = MVCContext.getMVCContext(context);
        I18nContext i18nContext = I18nContext.getI18nContext(context);
        AuthenticationContext authenticationContext = 
            AuthenticationContext.getAuthenticationContext(context);
        HttpContext httpContext = HttpContext.getHttpContext(context);
        templatingContext.put("coralSession", coralSession);
        templatingContext.put("mvcContext", mvcContext);
        templatingContext.put("parameters", parameters);
        templatingContext.put("httpContext", httpContext);
        templatingContext.put("authenticationContext", authenticationContext);
        templatingContext.put("i18nContext", i18nContext);
    	try
        {
            process(parameters, templatingContext, mvcContext, i18nContext, coralSession);
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
     * @param i18nContext TODO
     * @param coralSession the coral session.
     * @throws ProcessingException if processing in the view fails.
     */    
    public abstract void process(Parameters parameters, TemplatingContext templatingContext, 
        						 MVCContext mvcContext, 
        						 I18nContext i18nContext, CoralSession coralSession)
        throws ProcessingException;

    /**
     * Retrieve coral session from context.
     * 
     * @param context the context.
     * @return the coral session.
     */
    protected CoralSession getCoralSession(Context context)
    {
        return (CoralSession)context.getAttribute(CoralSession.class);
    }
}
