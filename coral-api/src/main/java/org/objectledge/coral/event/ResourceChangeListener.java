package org.objectledge.coral.event;

import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.Resource;

/**
 * Is notified of changes to a <code>Resource</code> data.
 *
 * @see org.objectledge.event.EventWhiteboard#addListener(Class,Object,Object)
 * @see org.objectledge.event.EventWhiteboard#removeListener(Class,Object,Object)
 * @version $Id: ResourceChangeListener.java,v 1.3 2005-02-08 20:34:21 rafal Exp $
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
