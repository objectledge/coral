package org.objectledge.coral.session;

import java.security.Principal;

import org.objectledge.authentication.AuthenticationContext;
import org.objectledge.context.Context;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.pipeline.Valve;

/**
 * Coral session verification valve.
 *  
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: CoralSessionVerificationValve.java,v 1.1 2004-07-16 11:10:10 pablo Exp $
 */
public class CoralSessionVerificationValve implements Valve 
{
	/** coral session factory */
	private CoralSessionFactory sessionFactory;
	
	/**
	 * Valve constructor.
	 * 
	 * @param sessionFactory the session factory.
	 */
	public CoralSessionVerificationValve(CoralSessionFactory sessionFactory)
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
		CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
		if(coralSession != null)
		{
			if(!coralSession.getUserPrincipal().equals(principal))
			{
			    coralSession.close();
			    try
			    {
			        CoralSession newCoralSession = sessionFactory.getSession(principal);
			        context.setAttribute(CoralSession.class, newCoralSession);			
			    }
				catch(EntityDoesNotExistException e)
				{
					throw new ProcessingException("failed to init the coral session", e);
				}	        
			}
		}
	}
}
