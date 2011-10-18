package org.objectledge.coral.modules.components.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.modules.components.BaseCoralComponent;
import org.objectledge.table.TableStateManager;

/**
 * The base browse component class.
 */
public abstract class BaseBrowserComponent extends BaseCoralComponent
{
    protected TableStateManager tableStateManager;
    
    protected Logger logger;
    
    public BaseBrowserComponent(Context context, Logger logger,
                                TableStateManager tableStateManager)
    {
        super(context);
        this.logger = logger;
        this.tableStateManager = tableStateManager;
    }
}
