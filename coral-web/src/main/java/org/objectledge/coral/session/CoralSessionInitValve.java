package org.objectledge.coral.session;

import java.security.Principal;

import org.objectledge.authentication.AuthenticationContext;
import org.objectledge.context.Context;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.pipeline.Valve;

/**
 * Coral session init valve.
 *  
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: CoralSessionInitValve.java,v 1.2 2004-06-29 13:34:37 zwierzem Exp $
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
		AuthenticationContext authenticationContext = 
            AuthenticationContext.getAuthenticationContext(context);
		if(authenticationContext == null)
		{
			throw new ProcessingException("failed to retrieve authentication context");
		}
		Principal principal = authenticationContext.getUserPrincipal();
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
