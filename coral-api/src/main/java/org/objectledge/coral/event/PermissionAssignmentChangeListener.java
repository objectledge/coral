package org.objectledge.coral.event;

import org.objectledge.coral.security.PermissionAssignment;

/**
 * Is notified of permission assignment changes on a particular, or all
 * resources. 
 *
 * @see EventService#addListener(Class,Object,Object)
 * @see EventService#removeListener(Class,Object,Object)
 * @version $Id: PermissionAssignmentChangeListener.java,v 1.1 2004-02-18 14:21:27 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface PermissionAssignmentChangeListener
{
    /**
     * Called when permission assignemts on the <code>resource</code> change.
     *
     * @param resource the resource.
     * @param pa the permission assignment.
     * @param added <code>true</code> if the permission was added,
     *        <code>false</code> if removed.
     */
    public void permissionsChanged(PermissionAssignment assignment, boolean added);
}
