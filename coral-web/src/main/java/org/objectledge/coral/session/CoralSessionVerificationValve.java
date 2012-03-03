package org.objectledge.coral.session;

import java.security.Principal;

import org.jcontainer.dna.Logger;
import org.objectledge.authentication.AuthenticationContext;
import org.objectledge.context.Context;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.pipeline.Valve;

/**
 * Coral session verification valve.
 *  
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: CoralSessionVerificationValve.java,v 1.3 2005-05-19 04:41:20 pablo Exp $
 */
public class CoralSessionVerificationValve implements Valve 
{
	/** coral session factory */
	private CoralSessionFactory sessionFactory;
	
	/** logger */
	private Logger logger;
	
	/**
	 * Valve constructor.
	 * 
	 * @param sessionFactory the session factory.
	 * @param logger the logger;
	 */
	public CoralSessionVerificationValve(CoralSessionFactory sessionFactory, Logger logger)
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
		CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
		if(coralSession != null)
		{
			Principal previousPrincipal = coralSession.getUserPrincipal();
            if(!previousPrincipal.equals(principal))
			{
			    coralSession.close();
			    try
			    {
			        CoralSession newCoralSession = sessionFactory.getSession(principal);
			        context.setAttribute(CoralSession.class, newCoralSession);			
			    }
				catch(EntityDoesNotExistException e)
				{
                    logger.error(
                        "failed to switch Coral session from " + previousPrincipal.getName()
                            + " to " + principal.getName(), e);
					CoralSession newCoralSession = sessionFactory.getAnonymousSession();
					context.setAttribute(CoralSession.class, newCoralSession);
				}	        
			}
		}
	}
}
