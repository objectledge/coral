package org.objectledge.coral.event;

import org.objectledge.coral.security.Role;

/**
 * Is notified of changes to a <code>Role</code> data.
 *
 * @see EventService#addListener(Class,Object,Object)
 * @see EventService#removeListener(Class,Object,Object)
 * @version $Id: RoleChangeListener.java,v 1.2 2004-02-18 15:08:21 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface RoleChangeListener
{
    /**
     * Called when <code>Role</code>'s data change.
     *
     * @param role the role that changed.
     */
    public void roleChanged(Role role);
}
