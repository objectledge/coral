package org.objectledge.coral.event;

import org.objectledge.coral.security.RoleAssignment;

/**
 * Is notified of role assignment changes on a particular, or all
 * subjects. 
 *
 * @see EventService#addListener(Class,Object,Object)
 * @see EventService#removeListener(Class,Object,Object)
 * @version $Id: RoleAssignmentChangeListener.java,v 1.1 2004-02-18 14:21:27 fil Exp $
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
