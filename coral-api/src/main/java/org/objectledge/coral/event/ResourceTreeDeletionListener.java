package org.objectledge.coral.event;

import org.objectledge.coral.store.Resource;

/**
 * Is notified when a resource is being moved to temp node during tree deletion.
 * 
 * 	<p>The even occurs when resource tree deletion was requested, but before it 
 *     moves to temporary node, allowing various cleanups to be
 *     made (for example eliminating references to the resource) especially
 *     those based on tree hierarchy</p>
 *
 *  <p>Listeners may be registered for a particualr ressource, a resource class
 *  or all resources (null anchor). </p>
 * 
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: ResourceTreeDeletionListener.java,v 1.3 2004-04-22 17:59:58 zwierzem Exp $
 */
public interface ResourceTreeDeletionListener
{
	/**
	 * Called when <code>Resource</code> is being moved
	 * to temp node during tree deletion.
	 *
	 * @param resource the resource that is being moved.
	 */
	public void resourceTreeDeleted(Resource resource);
}
