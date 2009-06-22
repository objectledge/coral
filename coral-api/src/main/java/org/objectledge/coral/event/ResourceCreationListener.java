package org.objectledge.coral.event;

import org.objectledge.coral.store.Resource;

/**
 * Notified when a new resource is created.
 * 
 * <p>Listeners may be registered on a resource (notified when children
 * are added), a resource class (notified when objects of that class,
 * or it's sub classes are created), or null (all object creations).</p>
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: ResourceCreationListener.java,v 1.2 2004-02-18 15:08:21 fil Exp $
 */
public interface ResourceCreationListener
{
    /**
     * Called when <code>Resource</code> is being created.
     *
     * @param resource the newly created resorce.
     */
    public void resourceCreated(Resource resource);
}
