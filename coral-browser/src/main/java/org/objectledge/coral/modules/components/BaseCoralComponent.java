package org.objectledge.coral.modules.components;

import org.jcontainer.dna.Logger;
import org.objectledge.coral.CoralSessionFactory;
import org.objectledge.pipeline.Valve;

/**
 * The base browse component class.
 */
public abstract class BaseCoralComponent
    implements Valve
{
    /** logger */
    protected Logger logger;
    
    /** sessionFactory */
    protected CoralSessionFactory coralSessionFactory;
  
    public BaseCoralComponent(Logger logger, CoralSessionFactory coralSessionFactory)
    {
        this.logger = logger;
        this.coralSessionFactory = coralSessionFactory;
    }
}
