package org.objectledge.coral.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpSession;

import org.objectledge.context.Context;
import org.objectledge.coral.entity.Entity;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.parameters.Parameters;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.web.HttpContext;

/**
 * 
 *
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * 
 * @version $Id: CoralEntitySelectionState.java,v 1.4 2005-01-18 11:03:40 rafal Exp $
 */
public abstract class CoralEntitySelectionState
{
    /**
     * Remove the state from session.
     * 
     * @param context the context.
     * @param state the state.
     */
    public static void removeState(Context context, CoralEntitySelectionState state)
    {
        HttpContext httpContext = HttpContext.getHttpContext(context);
        HttpSession session = httpContext.getRequest().getSession();
        session.removeAttribute(state.getName());
    }    
    
    /** Name of this entity selection state. */
    protected String name;
    
    /** Parameters prefix used by this selection state. */
    protected String prefix;
    
    /** Map representing the state. */
    protected Map state = new HashMap();
    
    /** <code>true</code> if the state object was created during this request. */
    protected boolean newState;

    /** 
     * Resource visibility parameter name - consists of prefix and <code>-visible</code> string.
     * For instance <code>resource-visible</code> or <code>category-visible</code>.
     */
    protected String visibleParamName;
    
    /** Prefix of resource selection parameters' names. */
    protected String entityIdParamPrefix;
    
    /**
     * The constructor.
     * 
     * @param name the name of the state.
     */
    public CoralEntitySelectionState(String name)
    {
        this.name = name;
        setPrefix("resource");
        newState = true;
    }

    /**
     * Get the name of the state.
     * 
     * @return the name.
     */
    public String getName()
    {
        return name;
    }

    /**
     * Get the prefix.
     * 
     * @return the prefix.
     */
    public String getPrefix()
    {
        return prefix;
    }

    /**
     * Set the prefix.
     * 
     * @param prefix the prefix.
     */
    public void setPrefix(String prefix)
    {
        this.prefix = prefix;
        visibleParamName = prefix+"-visible";
        entityIdParamPrefix = prefix+"-";
    }

    /**
     * Checks whether the state is new.
     * 
     * @return <code>true</code> if is new.
     */
    public boolean isNew()
    {
        return newState;
    }

	/** 
	 * Inits the selection state with a stat map, which maps resource ids onto state names:
	 * <code>Long -&gt; String</code>.
	 * 
	 * @param initialState the initial state.
	 */
	public void initWithIds(Map initialState)
	{
		state.clear();
		for(Iterator i=initialState.keySet().iterator(); i.hasNext();)
		{
			// this one only checks if keys are Long and values are String
			Long id = (Long)(i.next());
			String value = (String)(initialState.get(id));
			state.put(id, value);
		}

		// state was modified
		newState = false;
	}

    /**
     * Inits the selection state with a stat map, which maps resources onto state names:
     * <code>Resource -&gt; String</code>.
     * 
     * @param initialState the initial state.
     */
    public void init(Map initialState)
    {
        state.clear();
        for(Iterator i=initialState.keySet().iterator(); i.hasNext();)
        {
            Entity r = (Entity)(i.next());
            String value = (String)(initialState.get(r));
            Long id = r.getIdObject();
            state.put(id, value);
        }

        // state was modified
        newState = false;
    }
    
    /** 
     * Initializes the state from a space separated list of resource ids.
     * 
     * <p>Non-existent resources are <em>ignored</em>.</p>
     * 
     * @param coralSession the coral session.
     * @param ids string separated list of resource ids
     * @param state the state to be assigned to the resesources
     * @throws ProcessingException if the configuration is invalid.
     */
    public void init(CoralSession coralSession, String ids, String state)
        throws ProcessingException
    {
        try
        {
            Map map = new HashMap();
            StringTokenizer st = new StringTokenizer(ids, " ");
            while(st.hasMoreTokens())
            {
                long id = Long.parseLong(st.nextToken());
                try
                {
                    Entity res = getEntity(coralSession, id);
                    map.put(res, state);
                }
                catch(EntityDoesNotExistException e)
                {
                    // ignore
                }
            }
            init(map);
        }
        catch(Exception e)
        {
            throw new ProcessingException("invalid selection state configuration", e);
        }
    }    

    /** 
     * Updates the resource selection using given parameters pack. Avoids double update by removing
     * a list of visible resources names from given parameter pack. Because of this successive call
     * won't modify this resource selection state unless parameter container was modified.
     * This protects from a situation in which an action modifies a resource selection state and
     * after that this changes are overwritten by update call in screen code.
     * 
     * @param parameters the parameters.
     */
    public void update(Parameters parameters)
    {
        // get visible resources id's
        if(!parameters.isDefined(visibleParamName))
        {
            // no visible resources - nothing to do
            return;
        }
        Set visibleResourceIds = getIds(parameters, visibleParamName);
        // remove visibleParamName because it was already used
        //    - this is for actions it will block another state modifications for current request
        parameters.remove(visibleParamName);

        for(Iterator i=visibleResourceIds.iterator(); i.hasNext();)
        {
            Long id = (Long)i.next();
            String paramName = entityIdParamPrefix+id.toString();
            String value = parameters.get(paramName,"");
            state.put(id, value);
        }

        // state was modified
        newState = false;
    }
    
    /**
     * Remove the entity from selection.
     * 
     * @param entity the entity.
     */
    public void remove(Entity entity)
    {
		remove(entity.getIdObject());
    }

    /**
     * Remove the entity from selection.
     * 
     * @param id the id.
     */
	public void remove(long id)
	{
		remove(new Long(id));
	}

    /**
     * Remove the entity from selection.
     * 
     * @param id the id.
     */
	public void remove(Long id)
	{
		state.remove(id);
	}

	/**
     * Get string value of the entity.
     * 
     * @param entity the entity.
     */
    public String getValue(Entity entity)
    {
        return getValue(entity.getIdObject());
    }

	/**
     * Get string value of the entity.
     * 
     * @param id the id.
     */
	public String getValue(long id)
	{
		return getValue(new Long(id));
	}

	/**
     * Get string value of the entity.
     * 
     * @param id the id.
     */
	public String getValue(Long id)
	{
		return (String)(state.get(id));
	}

	/**
	 * Get the entities map.
	 * 
	 * @param coralSession the coral session
	 * @return the entities map.
	 * @throws ProcessingException
	 */
    public Map getEntities(CoralSession coralSession)
        throws ProcessingException
    {
        return getEntities(coralSession, null);
    }

	/**
	 * Get the entities map.
	 * 
	 * @param coralSession the coral session
	 * @param requestedValue the requested value.
	 * @return the entities map.
	 * @throws ProcessingException
	 */
    public Map getEntities(CoralSession coralSession, String requestedValue)
        throws ProcessingException
    {
        CoralStore storeService = coralSession.getStore();
        Map entities = new HashMap();
        try
        {
            for(Iterator i=state.keySet().iterator(); i.hasNext();)
            {
                Long id = (Long)(i.next());
                String value = (String)(state.get(id));
                if(requestedValue == null || requestedValue.equals(value))
                {
                    entities.put(getEntity(coralSession, id.longValue()), value);
                }
            }
        }
        catch(Exception e)
        {
            throw new ProcessingException("cannot retrieve resource", e);
        }
        return entities;
    }

    /**
     * Get the id map.
     * 
     * @return the map of id.
     */
    public Map getIds()
    {
        return new HashMap(state);
    }

    /**
     * Get the id map.
     *
     * @param requestedValue the requested value. 
     * @return the map of id.
     */
    public Map getIds(String requestedValue)
    {
        Map ids = new HashMap();
        for(Iterator i=state.keySet().iterator(); i.hasNext();)
        {
            Long id = (Long)(i.next());
            String value = (String)(state.get(id));
            if(requestedValue.equals(value))
            {
                ids.put(id, value);
            }
        }
        return ids;
    }

    /**
     * Get the entity from id.
     * 
     * @param coralSession the coral session.
     * @param id the id of the entity.
     * @return the entity.
     */
    public abstract Entity getEntity(CoralSession coralSession, long id)
    	throws Exception;
    
    // utitlity ////////////////////////////////////////////////////////////////////////////////////

    /**
     * Get id set.
     * 
     * @param parameters the parameters.
     * @param parameterName the parameter name.
     * @return the set of id's.
     */
    public static Set getIds(Parameters parameters, String parameterName)
    {
        Set ids = new HashSet();
        long[] paramAr = parameters.getLongs(parameterName);
        for(int i=0; i<paramAr.length; i++)
        {
            ids.add(new Long(paramAr[i]));
        }
        return ids;
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer(getClass().getName());
        for(Iterator i=state.keySet().iterator(); i.hasNext();)
        {
            Long id = (Long)(i.next());
            String value = (String)(state.get(id));
            sb.append(":[");
            sb.append(id);
            sb.append(",");
            sb.append(value);
            sb.append("]");
        }        
        return sb.toString();
    }
}
