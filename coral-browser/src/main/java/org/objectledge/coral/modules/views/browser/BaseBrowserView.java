package org.objectledge.coral.modules.views.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.modules.views.BaseCoralView;
import org.objectledge.table.TableStateManager;

/**
 * The base screen for alr browser application.
 */
public abstract class BaseBrowserView
    extends BaseCoralView
{
    protected TableStateManager tableStateManager;
    
    protected Logger logger;
    
    public BaseBrowserView(Context context, Logger logger,
                           TableStateManager tableStateManager)
    {
        super(context);
        this.logger= logger;
        this.tableStateManager = tableStateManager;
    }
}
