package org.objectledge.coral.event;

import org.objectledge.coral.security.PermissionAssignment;

/**
 * Is notified of permission assignment changes on a particular, or all
 * resources. 
 *
 * @see EventService#addListener(Class,Object,Object)
 * @see EventService#removeListener(Class,Object,Object)
 * @version $Id: PermissionAssignmentChangeListener.java,v 1.2 2004-02-18 15:08:21 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface PermissionAssignmentChangeListener
{
    /**
     * Called when permission assignemts on the <code>resource</code> change.
     *
     * @param assignment the permission assignment.
     * @param added <code>true</code> if the permission assignment was added,
     *        <code>false</code> if removed.
     */
    public void permissionsChanged(PermissionAssignment assignment, boolean added);
}
