package org.objectledge.coral.session;

import java.security.Principal;

import org.jcontainer.dna.Logger;
import org.objectledge.authentication.AuthenticationContext;
import org.objectledge.context.Context;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.pipeline.Valve;

/**
 * Coral session init valve.
 *  
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: CoralSessionInitValve.java,v 1.4 2005-05-19 04:41:20 pablo Exp $
 */
public class CoralSessionInitValve implements Valve 
{
	/** coral session factory */
	private CoralSessionFactory sessionFactory;
	
	/** logger */
	private Logger logger;
	
	/**
	 * Valve constructor.
	 * 
	 * @param sessionFactory the session factory.
	 */
	public CoralSessionInitValve(CoralSessionFactory sessionFactory, Logger logger)
	{
		this.sessionFactory = sessionFactory;
		this.logger = logger;
	}
	
	/**
	 * {@inheritDoc}  
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
			logger.error("failed to init the coral session", e);
			CoralSession coralSession = sessionFactory.getAnonymousSession();
			context.setAttribute(CoralSession.class, coralSession);
		}
	}
}
