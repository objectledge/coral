package org.objectledge.coral.event;

import org.objectledge.coral.security.RoleAssignment;

/**
 * Is notified of role assignment changes on a particular, or all
 * subjects. 
 *
 * @see org.objectledge.event.EventWhiteboard#addListener(Class,Object,Object)
 * @see org.objectledge.event.EventWhiteboard#removeListener(Class,Object,Object)
 * @version $Id: RoleAssignmentChangeListener.java,v 1.2 2005-02-08 20:34:21 rafal Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface RoleAssignmentChangeListener
{
    /**
     * Called when role assignemts on the <code>subject</code> change.
     *
     * @param ra the role assignment.
     * @param added <code>true</code> if the role assignment was added,
     *        <code>false</code> if removed.
     */
    public void rolesChanged(RoleAssignment ra, boolean added);
}
