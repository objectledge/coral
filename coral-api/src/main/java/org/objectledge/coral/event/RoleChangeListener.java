package org.objectledge.coral.event;

import org.objectledge.coral.security.Role;

/**
 * Is notified of changes to a <code>Role</code> data.
 *
 * @see org.objectledge.event.EventWhiteboard#addListener(Class,Object,Object)
 * @see org.objectledge.event.EventWhiteboard#removeListener(Class,Object,Object)
 * @version $Id: RoleChangeListener.java,v 1.3 2005-02-08 20:34:21 rafal Exp $
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
