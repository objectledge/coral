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
 * @version $Id: PermissionSelectionState.java,v 1.1 2004-07-13 10:43:12 pablo Exp $
 */
public class PermissionSelectionState 
	extends CoralEntitySelectionState
{
    /**
     * Retrieve the state from session.
     * 
     * @param context the context.
     * @param name the name.
     * @return the selection state.
     */
    public static PermissionSelectionState getAbstractState(Context context, String name)
    {
        HttpContext httpContext = HttpContext.getHttpContext(context);
        HttpSession session = httpContext.getRequest().getSession();
        PermissionSelectionState currentState = (PermissionSelectionState)
            session.getAttribute(name);
        if(currentState == null)
        {
            currentState = new PermissionSelectionState(name);
            session.setAttribute(name, currentState);
        }
        return currentState;
    }

    /**
     * The constructor.
     * 
     * @param name the name of the state.
     */
    public PermissionSelectionState(String name)
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
