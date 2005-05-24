package org.objectledge.coral.modules.actions.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.coral.modules.actions.PolicyProtectedCoralAction;
import org.objectledge.web.mvc.security.PolicySystem;

/**
 * The base action for coral browser application.
 */
public abstract class BaseBrowserAction
	extends PolicyProtectedCoralAction
{
    protected Logger logger;
    
    public BaseBrowserAction(PolicySystem policySystemArg, Logger logger)
    {
        super(policySystemArg);
        this.logger = logger;
    }
}
