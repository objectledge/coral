package org.objectledge.coral.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.objectledge.context.Context;
import org.objectledge.coral.entity.Entity;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.store.Resource;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.web.HttpContext;

/**
 * 
 *
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * 
 * @version $Id: ResourceSelectionState.java,v 1.2 2004-08-02 14:06:07 pablo Exp $
 */
public class ResourceSelectionState 
	extends CoralEntitySelectionState
{
    /**
     * Retrieve the state from session.
     * 
     * @param context the context.
     * @param name the name.
     * @return the selection state.
     */
    public static ResourceSelectionState getState(Context context, String name)
    {
        HttpContext httpContext = HttpContext.getHttpContext(context);
        HttpSession session = httpContext.getRequest().getSession();
        ResourceSelectionState currentState = (ResourceSelectionState)
            session.getAttribute(name);
        if(currentState == null)
        {
            currentState = new ResourceSelectionState(name);
            session.setAttribute(name, currentState);
        }
        return currentState;
    }

    /**
     * The constructor.
     * 
     * @param name the name of the state.
     */
    public ResourceSelectionState(String name)
    {
        super(name);
    }

    public String[] getExpandedIds(CoralSession coralSession, long rootId)
    	throws ProcessingException
    {
        Set expandedIds = new HashSet();
        expandedIds.add(Long.toString(rootId));
        
        Set resources = getEntities(coralSession).keySet();
        for(Iterator i=resources.iterator(); i.hasNext(); )
        {
            Resource res = (Resource)(i.next());
            while(res != null && res.getId() != rootId)
            {
                expandedIds.add(Long.toString(res.getId()));
                res = res.getParent();
            }
        }
        
        String[] ids = new String[expandedIds.size()];
        ids = (String[])(expandedIds.toArray(ids));
        return ids;
    }

    /**
     * {@inheritDoc}
     */
    public Entity getEntity(CoralSession coralSession, long id)
    	throws Exception
    {
        return coralSession.getStore().getResource(id);
    }
}
