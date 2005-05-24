package org.objectledge.coral.modules.views.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.modules.views.PolicyProtectedCoralView;
import org.objectledge.table.TableStateManager;
import org.objectledge.web.mvc.security.PolicySystem;

/**
 * The base screen for alr browser application.
 */
public abstract class BaseBrowserView
    extends PolicyProtectedCoralView
{
    protected TableStateManager tableStateManager;
    
    protected Logger logger;
    
    public BaseBrowserView(Context context, PolicySystem policySystemArg, 
			Logger logger, TableStateManager tableStateManager)
    {
        super(context, policySystemArg);
        this.logger= logger;
        this.tableStateManager = tableStateManager;
    }
}
