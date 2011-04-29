package org.objectledge.coral.session;

import java.util.Collections;
import java.util.List;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.pipeline.Valve;

public class CoralSessionDiagnosticsValve implements Valve
{
    private final CoralSessionFactory coralSessionFactory;
    
    private final Logger log;

    public CoralSessionDiagnosticsValve(CoralSessionFactory coralSessionFactory, Logger log)
    {
        this.coralSessionFactory = coralSessionFactory;
        this.log = log;        
    }
    
    /**
     * {@inheritDoc}
     */
    public void process(Context context) throws ProcessingException
    {
        List<CoralSession> activeSessions = coralSessionFactory.getAllSessions();
        Collections.reverse(activeSessions);
        for(CoralSession session : activeSessions)
        {            
            Throwable openingStackTrace = session.getOpeningStackTrace();
            if(openingStackTrace != null)
            {
                log.error("thread still owns a CoralSession opened at", openingStackTrace);
            }
            else
            {
                log.error("thread still owns a CoralSession, enable logging CoralSessionImpl class for more details");
            }
        }
    }
}
