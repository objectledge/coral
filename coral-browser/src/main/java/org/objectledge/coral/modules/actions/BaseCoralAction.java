package org.objectledge.coral.modules.actions;

import org.jcontainer.dna.Logger;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.pipeline.Valve;

/**
 * The base action for all coral application.
 */
public abstract class BaseCoralAction
    implements Valve
{
    /** logger */
    protected Logger logger;
    
    /** sessionFactory */
    protected CoralSessionFactory coralSessionFactory;
    
    public BaseCoralAction(Logger logger, CoralSessionFactory coralSessionFactory)
    {
        this.logger = logger;
        this.coralSessionFactory = coralSessionFactory;
    }
}
