package org.objectledge.coral.event;

import org.objectledge.coral.security.Permission;

/**
 * Is notified of changes to a <code>Permission</code> data.
 *
 * @see EventService#addListener(Class,Object,Object)
 * @see EventService#removeListener(Class,Object,Object)
 * @version $Id: PermissionChangeListener.java,v 1.1 2004-02-18 14:21:27 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface PermissionChangeListener
{
    /**
     * Called when <code>Permission</code>'s data change.
     *
     * @param Permission the permission that changed.
     */
    public void permissionChanged(Permission permission);
}
