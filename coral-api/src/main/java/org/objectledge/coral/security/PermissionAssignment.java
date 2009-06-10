package org.objectledge.coral.security;

import org.objectledge.coral.entity.Assignment;
import org.objectledge.coral.store.Resource;

/**
 * Represents assigment of a {@link Permission} on a {@link Resource} to a
 * {@link Role}.
 *
 * <p><code>PermissionAssignment</code> objects are returned from {@link
 * Resource#getPermissionAssignments()} method. They experss security
 * constraints placed upon a specific resource (and optionally it's
 * sub-resources). </p> 
 *
 * @version $Id: PermissionAssignment.java,v 1.1 2004-02-18 14:21:27 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface PermissionAssignment
    extends Assignment
{
    /**
     * Returns the resouce the this security constraint applies to.
     *
     * @return the resouce the this security constraint applies to.
     */
    public Resource getResource();
    
    /**
     * Returns the {@link Role} involved in this security constraint.
     *
     * @return the {@link Role} involved in this security constraint.
     */
    public Role getRole();

    /**
     * Returns the {@link Permission} involved in this security constraint.
     *
     * @return the {@link Permission} involved in this security constraint.
     */
    public Permission getPermission();

    /**
     * Returns <code>true</code> if this security constraint is inherited by the
     * sub-resources of the resource.
     * 
     * @return <code>true</code> if the security constraint is inherited.
     */
    public boolean isInherited();
}
 
