package org.objectledge.coral.modules.views;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.web.mvc.builders.DefaultBuilder;

/**
 * The base screen for alr browser application.
 */
public abstract class BaseCoralView
    extends DefaultBuilder
{
    /** logger */
    protected Logger logger;
    
    /** sessionFactory */
    protected CoralSessionFactory coralSessionFactory;
  
    public BaseCoralView(Context context, Logger logger, CoralSessionFactory coralSessionFactory)
    {
        super(context);
        this.logger = logger;
        this.coralSessionFactory = coralSessionFactory;
    }
}
