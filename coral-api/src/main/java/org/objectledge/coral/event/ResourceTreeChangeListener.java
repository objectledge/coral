package org.objectledge.coral.event;

import org.objectledge.coral.store.ResourceInheritance;

/**
 * Is notified of resource tree structure changes.
 *
 * @see org.objectledge.event.EventWhiteboard#addListener(Class,Object,Object)
 * @see org.objectledge.event.EventWhiteboard#removeListener(Class,Object,Object)
 * @version $Id: ResourceTreeChangeListener.java,v 1.2 2005-02-08 20:34:21 rafal Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface ResourceTreeChangeListener
{
    /**
     * Called when resource relationships change.
     *
     * @param item the ralationship
     * @param added <code>true</code> if the relationship was added,
     *        <code>false</code> if removed.
     */
    public void resourceTreeChanged(ResourceInheritance item, boolean added);
}
