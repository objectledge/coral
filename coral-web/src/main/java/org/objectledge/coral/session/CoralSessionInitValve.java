package org.objectledge.coral.session;

import java.security.Principal;

import org.objectledge.context.Context;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.pipeline.Valve;
import org.objectledge.web.mvc.MVCContext;

/**
 * Coral session init valve.
 *  
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: CoralSessionInitValve.java,v 1.1 2004-06-23 14:11:03 pablo Exp $
 */
public class CoralSessionInitValve implements Valve 
{
	/** coral session factory */
	private CoralSessionFactory sessionFactory;
	
	/**
	 * Valve constructor.
	 * 
	 * @param sessionFactory the session factory.
	 */
	public CoralSessionInitValve(CoralSessionFactory sessionFactory)
	{
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @inheritDoc{}  
	 */
	public void process(Context context) throws ProcessingException
	{
		MVCContext mvcContext = MVCContext.getMVCContext(context);
		if(mvcContext == null)
		{
			throw new ProcessingException("failed to retrieve mvc context");
		}
		Principal principal = mvcContext.getUserPrincipal();
		if(principal == null)
		{
			throw new ProcessingException("failed to retrieve principal from context");			
		}
		try
		{
			CoralSession coralSession = sessionFactory.getSession(principal);
			context.setAttribute(CoralSession.class, coralSession);			
		}
		catch(EntityDoesNotExistException e)
		{
			throw new ProcessingException("failed to init the coral session", e);
		}
	}
}
