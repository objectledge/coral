package org.objectledge.coral.event;

import org.objectledge.coral.store.ResourceInheritance;

/**
 * Is notified of resource tree structure changes.
 *
 * @see EventService#addListener(Class,Object,Object)
 * @see EventService#removeListener(Class,Object,Object)
 * @version $Id: ResourceTreeChangeListener.java,v 1.1 2004-02-18 14:21:27 fil Exp $
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
