package org.objectledge.coral.event;

import org.objectledge.coral.store.ResourceOwnership;

/**
 * Is notified of resource ownership changes.
 *
 * @see org.objectledge.event.EventWhiteboard#addListener(Class,Object,Object)
 * @see org.objectledge.event.EventWhiteboard#removeListener(Class,Object,Object)
 * @version $Id: ResourceOwnershipChangeListener.java,v 1.2 2005-02-08 20:34:21 rafal Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface ResourceOwnershipChangeListener
{
    /**
     * Called when resource ownership changes.
     *
     * @param item the ownership information
     * @param added <code>true</code> if the relationship was added,
     *        <code>false</code> if removed.
     */
    public void resourceOwnershipChanged(ResourceOwnership item, boolean added);
}
