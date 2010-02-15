package org.objectledge.coral.security;

import org.objectledge.coral.entity.Entity;
import org.objectledge.coral.schema.ResourceClass;

/**
 * Each {@link ResourceClass} has an associated set of <code>Permission</code>s
 * that can be granted upon it's instances.
 *
 * @version $Id: Permission.java,v 1.2 2004-02-18 15:08:21 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface Permission
    extends Entity
{
    /**
     * Returns all <code>ResourceClass</code>es that this
     * <code>Permission</code> is associated with.
     *
     * @return all <code>ResourceClass</code>es that this
     * <code>Permission</code> is associated with.
     */
    public ResourceClass[] getResourceClasses();

    /**
     * Returns <code>true</code> if this permission is assoicated with the
     * specified resource class.
     *
     * @param permission the resource class.
     * @return <code>true</code> if this permission is assoicated with the
     * specified resource class.
     */
    public boolean isAssociatedWith(ResourceClass permission);
    
    /**
     * Returns all <code>PermissionAssignments</code> defined for this permission on all resources.
     * <p>
     * Use this method to acquire information about explicit permission grants on all resources.
     * Note that this operation involves database access on every invocation. 
     * </p>
     * 
     * @return all assignments of this permission.
     */
    public PermissionAssignment[] getPemrissionAssignments(); 
}
