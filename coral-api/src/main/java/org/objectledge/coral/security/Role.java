package org.objectledge.coral.security;

import org.objectledge.coral.entity.Entity;
import org.objectledge.coral.store.Resource;

/**
 * A set of <code>Role</code>s are assigned to {@link Subject}, and {@link
 * Permission}s upon {@link Resource}s are in turn assigned to
 * <code>Role</code>s. 
 *
 * @version $Id: Role.java,v 1.2 2004-02-18 15:08:21 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface Role
    extends Entity
{
    /** The identifier of the superuser role. */
    public static final long ROOT = 1L;
    
    /** The identifier of role that is never assigned to any subject. */
    public static final long NOBODY = 2L; 
    
    /** Maximum role identifier reserved for system usage. */
    public static final long MAX_RESERVED = 3L;

    /**
     * Returns all {@link RoleImplication} relationships that this role is
     * involved in.
     *
     * <p>Use this method to discover direrct super and sub roles of a
     * role.</p>
     *
     * @return all {@link RoleImplication} relationships that this role is
     * involved in.
     */
    public RoleImplication[] getImplications();

    /**
     * Returns all <code>Role</code>s that are implied by (contained in) this
     * role. 
     *
     * @return all <code>Role</code>s that are implied by (contained in) this
     * role.  
     */
    public Role[] getSubRoles();

    /**
     * Returns all <code>Role</code>s that imply (contain) this role.
     *
     * @return all <code>Role</code>s that imply (contain) this role.
     */
    public Role[] getSuperRoles();

    /**
     * Returns <code>true</code> if the specified Role is implied by (contained
     * in) this role.
     *
     * @param role the role
     * @return <code>true</code> if the specified Role is implied by (contained
     * in) this role.
     */
    public boolean isSubRole(Role role);

    /**
     * Returns <code>true</code> if the specified Role is a implies (contains)
     * this role. 
     *
     * @param role the role
     * @return <code>true</code> if the specified Role is implies (contains)
     * this role.
     */
    public boolean isSuperRole(Role role);

    /**
     * Returns all role assignments that were made for this role.
     *
     * <p>Use this method to acquire information about explicit role grants.</p>
     *
     * @return all role assignments that were made for this role.
     */
    public RoleAssignment[] getRoleAssignments();

    /**
     * Returns all subjects that were assigned this role.
     *
     * <p>This method takes Role impications into account, thus all Subjects
     * that pass {@link Subject#hasRole(Role)} check for this Role are
     * returned.</p> 
     * 
     * @return all subjects that were assigned this role.
     */
    public Subject[] getSubjects();

    /**
     * Returns all <code>PermissionAssignments</code> defined for this role on
     * all resources.
     *
     * <p>Use this method to acquire information about explicit permission
     * grants on all resources.</p> 
     *
     * @return permission assignments defined for this role.
     */
    public PermissionAssignment[] getPermissionAssignments();
    
    /**
     * Returns <code>PermissionAssignments</code> defined for this role on a
     * particular <code>Resource</code>
     *
     * <p>Use this method to acquire information about explicit permission
     * grants on a specific resource.</p> 
     *
     * @param resource the resource.
     * @return the permission assignments.
     */
    public PermissionAssignment[] getPermissionAssignments(Resource resource);
    
    /**
     * Returns all permissions defined for this role on a particular resource.
     *
     * <p>This method takes into consideration both Role implications and
     * Resource parent-child relationships along with
     * {@link PermissionAssignment#isInherited()} checks.</p>
     *
     * @param resource the resource.
     * @return permissions defined on this resource.
     */
    public Permission[] getPermissions(Resource resource);

    /**
     * Returns <code>true</code> if the role has the specifed permission on 
     * the specified resource.
     *
     * <p>This method takes into consideration both Role implications and
     * Resource parent-child relationships along with
     * {@link PermissionAssignment#isInherited()} checks.</p>
     *
     * @param resource the resource.
     * @param permission the permission.
     * @return <code>true</code> if the role has the specifed permission on 
     * the specified resource.
     */
    public boolean hasPermission(Resource resource, Permission permission);
}
