package org.objectledge.coral.modules.components.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.web.mvc.components.DefaultComponent;

/**
 * The base browse component class.
 */
public abstract class BaseCoralComponent
    extends DefaultComponent
{
    /** logger */
    protected Logger logger;
    
    /** sessionFactory */
    protected CoralSessionFactory coralSessionFactory;
  
    public BaseCoralComponent(Context context, Logger logger, CoralSessionFactory coralSessionFactory)
    {
        super(context);
        this.logger = logger;
        this.coralSessionFactory = coralSessionFactory;
    }
}
