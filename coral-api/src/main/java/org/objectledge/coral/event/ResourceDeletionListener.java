package org.objectledge.coral.event;

import org.objectledge.coral.store.Resource;

/**
 * Is notified when a resource is deleted.
 * 
 * 	<p>The even occurs when resource deletion was requested, but before it 
 *     actually goes away from the database, allowing various cleanups to be
 *     made (for example eliminating references to the resource)</p>
 *
 *  <p>Listeners may be registered for a particualr ressource, a resource class
 *  or all resources (null anchor). </p>
 * 
 *  @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 *  @version $ID$
 */
public interface ResourceDeletionListener
{
	/**
	 * Called when <code>Resource</code> is being deleted.
	 *
	 * @param resource the resource that is being deleted.
     * @throws Exception if there is a problem handling the event.
	 */
	public void resourceDeleted(Resource resource)
        throws Exception;
}
