package org.objectledge.coral.event;

import org.objectledge.coral.security.Permission;

/**
 * Is notified of changes to a <code>Permission</code> data.
 *
 * @see org.objectledge.event.EventWhiteboard#addListener(Class,Object,Object)
 * @see org.objectledge.event.EventWhiteboard#removeListener(Class,Object,Object)
 * @version $Id: PermissionChangeListener.java,v 1.3 2005-02-08 20:34:21 rafal Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface PermissionChangeListener
{
    /**
     * Called when <code>Permission</code>'s data change.
     *
     * @param permission the permission that changed.
     */
    public void permissionChanged(Permission permission);
}
