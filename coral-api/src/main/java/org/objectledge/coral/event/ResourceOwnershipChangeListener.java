package org.objectledge.coral.event;

import org.objectledge.coral.store.ResourceOwnership;

/**
 * Is notified of resource ownership changes.
 *
 * @see EventService#addListener(Class,Object,Object)
 * @see EventService#removeListener(Class,Object,Object)
 * @version $Id: ResourceOwnershipChangeListener.java,v 1.1 2004-02-18 14:21:27 fil Exp $
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
