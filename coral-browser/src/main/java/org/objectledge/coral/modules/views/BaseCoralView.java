package org.objectledge.coral.modules.views;

import org.jcontainer.dna.Logger;
import org.objectledge.coral.CoralSessionFactory;
import org.objectledge.pipeline.Valve;

/**
 * The base screen for alr browser application.
 */
public abstract class BaseCoralView
    implements Valve
{
    /** logger */
    protected Logger logger;
    
    /** sessionFactory */
    protected CoralSessionFactory coralSessionFactory;
  
    public BaseCoralView(Logger logger, CoralSessionFactory coralSessionFactory)
    {
        this.logger = logger;
        this.coralSessionFactory = coralSessionFactory;
    }
}
