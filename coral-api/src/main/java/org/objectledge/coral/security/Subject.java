package org.objectledge.coral.security;

import org.objectledge.coral.entity.Entity;
import org.objectledge.coral.store.Resource;

/**
 * A representation of an user or application accessing the resource store.
 *
 * @version $Id: Subject.java,v 1.2 2004-02-18 15:08:21 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface Subject
    extends Entity
{
    /** The identifier of the superuser subject. */
    public static final long ROOT = 1L;
    
    /**
     * Returns the role assigments made for this <code>Subject.</code>
     *
     * <p>Use this method to acquire information about explicit role grants.</p>
     *
     * @return the role assigments made for this <code>Subject.</code>
     */
    public RoleAssignment[] getRoleAssignments();

    /**
     * Returns the roles assigned to this <code>Subject</code>.
     *
     *
     * <p>This method takes Role impications into account, thus all Roles
     * that pass {@link #hasRole(Role)} check for this Subject are
     * returned.</p>
     *
     * @return the roles assigned to this <code>Subject</code>.
     */
    public Role[] getRoles();

    /**
     * Returns <code>true</code> if the <code>Subject</code> has a specific
     * role. 
     *
     * @param role the role to check.
     * @return <code>true</code> if the <code>Subject</code> has a specific
     * role. 
     */
    public boolean hasRole(Role role);

    /**
     * Returns the <code>Permission</code>s that the <code>Subject</code> has
     * on a specific <code>Resurce</code>.
     *
     * <p>This method takes into consideration both Role implications and
     * Resource parent-child relationships along with {@link
     * PermissionAssignment#isInherited()} checks.</p>
     *
     * @param resource the resource to get permissions on.
     * @return the <code>Permission</code>s that the <code>Subject</code> has
     * over a specific <code>Resurce</code>.
     */
    public Permission[] getPermissions(Resource resource);

    /**
     * Returns <code>true</code> if the <code>Subject</code> has a specific
     * <code>Permission</code> on a <code>Resource</code>.
     *
     * <p>This method takes into consideration both Role implications and
     * Resource parent-child relationships along with
     * {@link PermissionAssignment#isInherited()} checks.</p>
     *
     * @param resource the resource.
     * @param permission the permission.
     * @return <code>true</code> if the <code>Subject</code> has a specific
     * <code>Permission</code> on a <code>Resource</code>.
     */
    public boolean hasPermission(Resource resource, Permission permission);

    /**
     * Checks if the given subject is a subordinate of this subject.
     *
     * @param subject the subject to check.
     * @return <core>true</code> if the given subject is a subordinate of this
     * subject. 
     */
    public boolean isSubordinate(Subject subject);
    
    /**
     * Returns the supervisor of this subject.
     *
     * @return the supervisor of this subject.
     */
    public Subject getSupervisor();

    /**
     * Returns all subordinates of this subject.
     *
     * @return all subordinates of this subject.
     */
    public Subject[] getSubordinates();

    /**
     * Returns all Resources that are owned by this Subject.
     *
     * <p>This method takes Resource parent-child relationships into
     * consideration, so it can be <em>extremely<em> time and memory
     * consuming.</p>
     *
     * @return all Resources that are owned by this Subject.
     */
    public Resource[] getOwnedResources();
    
    /**
     * Returns all Resources that were created by this Subject.
     *
     * <p>This method may be very time and memory consuming.</p>
     *
     * @return all Resources that were created by this Subject.
     */
    public Resource[] getCreatedResources();
    
    /**
     * Returns all Role assignments performed by this Subject.
     *
     * @return all Role assignments performed by this Subject.
     */
    public RoleAssignment[] getGrantedRoleAssginments();

    /**
     * Returns all Permission assignments performed by this Subject.
     *
     * @return all Permission assignments performed by this Subject.
     */
    public PermissionAssignment[] getGrantedPermissionAssignemnts();
}

