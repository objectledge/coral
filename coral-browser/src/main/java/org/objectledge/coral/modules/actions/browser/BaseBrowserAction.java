package org.objectledge.coral.modules.actions.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.coral.modules.actions.BaseCoralAction;

/**
 * The base action for coral browser application.
 */
public abstract class BaseBrowserAction
    extends BaseCoralAction
{
    protected Logger logger;
    
    public BaseBrowserAction(Logger logger)
    {
        super();
        this.logger = logger;
    }
}
