package org.objectledge.coral.event;

import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.Resource;

/**
 * Is notified of changes to a <code>Resource</code> data.
 *
 * @see EventService#addListener(Class,Object,Object)
 * @see EventService#removeListener(Class,Object,Object)
 * @version $Id: ResourceChangeListener.java,v 1.2 2004-02-18 15:08:21 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface ResourceChangeListener
{
    /**
     * Called when <code>Resource</code>'s data change.
     *
     * @param resource the resource that changed.
     * @param subject the subject that performed the change.
     */
    public void resourceChanged(Resource resource, Subject subject);
}
