package org.objectledge.coral.util;

import javax.servlet.http.HttpSession;

import org.objectledge.context.Context;
import org.objectledge.coral.entity.Entity;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.web.HttpContext;

/**
 * 
 *
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * 
 * @version $Id: SubjectSelectionState.java,v 1.2 2004-07-14 17:31:06 pablo Exp $
 */
public class SubjectSelectionState 
	extends CoralEntitySelectionState
{
    /**
     * Retrieve the state from session.
     * 
     * @param context the context.
     * @param name the name.
     * @return the selection state.
     */
    public static SubjectSelectionState getState(Context context, String name)
    {
        HttpContext httpContext = HttpContext.getHttpContext(context);
        HttpSession session = httpContext.getRequest().getSession();
        SubjectSelectionState currentState = (SubjectSelectionState)
            session.getAttribute(name);
        if(currentState == null)
        {
            currentState = new SubjectSelectionState(name);
            session.setAttribute(name, currentState);
        }
        return currentState;
    }

    /**
     * The constructor.
     * 
     * @param name the name of the state.
     */
    public SubjectSelectionState(String name)
    {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    public Entity getEntity(CoralSession coralSession, long id)
    	throws Exception
    {
        return coralSession.getSecurity().getSubject(id);
    }
}
