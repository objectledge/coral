package org.objectledge.coral.modules.actions;

import org.objectledge.context.Context;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.parameters.Parameters;
import org.objectledge.parameters.RequestParameters;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.pipeline.Valve;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.web.mvc.MVCContext;

/**
 * The base screen for coral application.
 * 
 * @version $Id: BaseCoralAction.java,v 1.5 2005-02-11 11:30:58 pablo Exp $
 */
public abstract class BaseCoralAction implements Valve
{
    /**
     * {@inheritDoc}
     */
    public void process(Context context)
        throws ProcessingException 
    {
    	Parameters parameters;
    	TemplatingContext templatingContext;
    	MVCContext mvcContext;
    	CoralSession coralSession;
    	
        parameters = RequestParameters.getRequestParameters(context);
        templatingContext = TemplatingContext.getTemplatingContext(context);
        mvcContext = MVCContext.getMVCContext(context);
        coralSession = (CoralSession)context.getAttribute(CoralSession.class);
        execute(context, parameters, mvcContext, templatingContext, coralSession);
    }

    /**
     * To be implemented in action.
     * @param context the context.
     * @param parameters the parameters.
     * @param mvcContext the mvc context.
     * @param templatingContext the templating context.
     * @param coralSession the coralSession.
     */    
    public abstract void execute(Context context, Parameters parameters, MVCContext mvcContext, TemplatingContext templatingContext, CoralSession coralSession)
        throws ProcessingException;  
    
    /**
     * Retrieve coral session from context.
     * 
     * @return the coral session.
     */
    protected CoralSession getCoralSession(Context context)
    {
        return (CoralSession)context.getAttribute(CoralSession.class);
    }
}
