package org.objectledge.coral.session;

import org.objectledge.context.Context;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.pipeline.Valve;

/**
 * Coral session cleanup valve.
 *  
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: CoralSessionCleanupValve.java,v 1.2 2005-02-21 14:04:35 rafal Exp $
 */
public class CoralSessionCleanupValve implements Valve 
{
	/**
	 * {@inheritDoc}
	 */
	public void process(Context context) throws ProcessingException
	{
		CoralSession coralSession = (CoralSession)context.getAttribute(CoralSession.class);
		if(coralSession != null)
		{
			coralSession.close();
		}
	}
}
