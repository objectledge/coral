package org.objectledge.coral.security;

import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.entity.EntityExistsException;
import org.objectledge.coral.entity.EntityInUseException;
import org.objectledge.coral.schema.CircularDependencyException;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.store.Resource;

/**
 * Manages {@link Subject}s, {@link Role}s and {@link Permission}s.
 *
 * @version $Id: CoralSecurity.java,v 1.3 2004-03-03 10:27:31 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface CoralSecurity
{
    // Subjects //////////////////////////////////////////////////////////////
    
    /**
     * Returns all {@link Subject}s defined in the system.
     *
     * @return all {@link Subject}s defined in the system.
     */
    public Subject[] getSubject();

    /**
     * Returns the {@link Subject} with a specific identifier.
     *
     * @param id the identifier.
     * @return the <code>Subject</code>.
     * @throws EntityDoesNotExistException if the <code>Subject</code>
     *         with the specified identifier does not exist.
     */
    public Subject getSubject(long id)
        throws EntityDoesNotExistException;
    
    /**
     * Returns the {@link Subject} with a specific name.
     *
     * <p>Subject names are guaranteed to be unique throughout the system,
     * therefore this method always returns the signle matched subject,
     * or throws exception</p>
     *
     * @param name the subject name.
     * @return the <code>Subject</code>.
     * @throws EntityDoesNotExistException if the <code>Subject</code>
     *         with the specified name does not exist.
     */
    public Subject getSubject(String name)
        throws EntityDoesNotExistException;
    
    /**
     * Creates a {@link Subject}.
     *
     * @param name the name of the class.
     * @return the newly crated subject.
     * @throws EntityExistsException if a subject with the given name already
     *         exists in the system.
     */
    public Subject createSubject(String name)
        throws EntityExistsException;
    
    /**
     * Removes a {@link Subject}.
     *
     * @param subject the subject to delete.
     * @throws EntityInUseException if there any security grants perfored for
     *         or by the subject, or resouruces created or modified by the
     *         subject
     */
    public void deleteSubject(Subject subject)
        throws EntityInUseException;

    /**
     * Renames the {@link Subject}.
     *
     * @param subject the subject to rename.
     * @param name the new name of the subject.
     * @throws EntityExistsException if a subject with the specified name already exists. 
     */
    public void setName(Subject subject, String name)
        throws EntityExistsException;
    
    // Roles /////////////////////////////////////////////////////////////////

    /**
     * Returns all {@link Role}s defined in the system.
     *
     * @return all {@link Role}s defined in the system.
     */
    public Role[] getRole();

    /**
     * Returns the {@link Role} with a specific identifier.
     *
     * @param id the identifier.
     * @return the <code>Role</code>.
     * @throws EntityDoesNotExistException if the <code>Role</code>
     *         with the specified identifier does not exist.
     */
    public Role getRole(long id)
        throws EntityDoesNotExistException;
    
    /**
     * Returns all <code>Role</code>s with the specified name.
     *
     * <p>In most situations it is desirable that <code>Role</code>
     * names are unique throughout the system, though this interface does not
     * enforce it. A zero-length array will be returned if no
     * <code>Role</code>s of that name exist.</p>
     *
     * @param name the name.
     * @return all <code>Role</code>s with the given name.
     */
    public Role[] getRole(String name);
    
    /**
     * Returns the role with the specifed name.
     *
     * @param name the name.
     * @return the role
     * @throws IllegalStateException if the name denotes multiple roles,
     *         or does not exist.
     */
    public Role getUniqueRole(String name)
        throws IllegalStateException;    

    /**
     * Creates a {@link Role}.
     *
     * @param name the name of the <code>Role</code>
     * @return newly created role.
     */
    public Role createRole(String name);
    
    /**
     * Removes an {@link Role}.
     *
     * @param role the {@link Role}.
     * @throws EntityInUseException if it has subroles or there are security
     *         assignments in which this role is involved.
     */
    public void deleteRole(Role role)
        throws EntityInUseException;

    /**
     * Renames an role.
     *
     * @param role the attribute class to rename.
     * @param name the new name.
     */
    public void setName(Role role, String name);
    
    /**
     * Creates implication relationship between two roles.
     *
     * @param superRole the implicating / containing role.
     * @param subRole the implied / contained role.
     * @throws CircularDependencyException if the <code>subRole</code> is
     *         actually a super role of the <code>superRole</code>.
     */
    public void addSubRole(Role superRole, Role subRole)
        throws CircularDependencyException;
    
    /**
     * Removes implication relationship between two roles.
     *
     * @param superRole the implicating / containing role.
     * @param subRole the implied / contained role.
     * @throws IllegalArgumentException if the <code>subRole</code> is not
     *         really a subrole of the <code>superRole</code>.
     */
    public void deleteSubRole(Role superRole, Role subRole) 
        throws IllegalArgumentException;
    
    /**
     * Grants a {@link Role} to a {@link Subject}. 
     *
     * <p>Granting is allowed if all of the following conditions are met:</p>
     * <ul>
     *   <li>The <code>grantor</code> has the involved <code>role</code>
     *   himself, whith {@link RoleAssignment#isGrantingAllowed() grantingAllowed} flag
     *   set to true, or is a superuser (has the {@link Role#ROOT} role)</li>
     *   <li>The <code>subject</code> is a {@link
     *   Subject#isSubordinate(Subject) subordinate} of the
     *   <code>grantor</code>, or the <code>grantor</code> is a
     *   superuser.</li> 
     *   <li>the <code>role</code> is not the {@link Role#NOBODY} role.</li>
     * </ul>
     * 
     * @param role the involved role.
     * @param subject the involved subject.
     * @param grantingAllowed will the subject be allowed to grant the role to
     *        other subjects.
     * @param grantor the subject that grants the role.
     * @throws SecurityException if the grantor is not allowed to grant the
     *         role.
     */
    public void grant(Role role, Subject subject, 
                      boolean grantingAllowed, Subject grantor)
        throws SecurityException;
    
    /**
     * Revokes a {@link Role} from the {@link Subject}.
     *
     * <p>Revocation is possible if the <code>revoker</code> granted the role
     * earlier, or <code>subject</code> is a {@link
     * Subject#isSubordinate(Subject)} of the <code>revoker</code>, or
     * <code>revoker</code> is the superuser (has {@link Role#ROOT} role).</p>
     * 
     * @param role the involved role.
     * @param subject the involved subject.
     * @param revoker the subject that revokes the role.
     * @throws IllegalArgumentException if the <code>subject</code> does not
     *         actually have the <code>role</code>.
     * @throws SecurityException if the <code>revoker</code> is not allowed to
     *         revoke the role.
     */
    public void revoke(Role role, Subject subject, Subject revoker)
        throws IllegalArgumentException, SecurityException;

    // Permissions ///////////////////////////////////////////////////////////

    /**
     * Returns all {@link Permission}s defined in the system.
     *
     * @return all {@link Permission}s defined in the system.
     */
    public Permission[] getPermission();

    /**
     * Returns the {@link Permission} with a specific identifier.
     *
     * @param id the identifier.
     * @return the <code>Permission</code>.
     * @throws EntityDoesNotExistException if the <code>Permission</code>
     *         with the specified identifier does not exist.
     */
    public Permission getPermission(long id)
        throws EntityDoesNotExistException;
    
    /**
     * Returns all {@link Permission}s with the specified name.
     *
     * <p>In most situation it is desirable that <code>Permission</code>
     * names are unique throughout the system, though this interface does not
     * enforce it. A zero-length array will be returned if no
     * <code>Permission</code>s of that name exist.</p>
     *
     * @param name the name.
     * @return all <code>Permission</code>es with the given name.
     */
    public Permission[] getPermission(String name);

    /**
     * Returns the permission with the specifed name.
     *
     * @param name the name.
     * @return the permission
     * @throws IllegalStateException if the name denotes multiple permissions,
     *         or does not exist.
     */
    public Permission getUniquePermission(String name)
        throws IllegalStateException;    
    
    /**
     * Creates a {@link Permission}.
     *
     * @param name the name of the class.
     * @return newly created permission.
     */
    public Permission createPermission(String name);
    
    /**
     * Removes a {@link Permission}.
     *
     * @param permission the {@link Permission}.
     * @throws EntityInUseException if there are security assignments that
     *         involve this permission.
     */
    public void deletePermission(Permission permission)
        throws EntityInUseException;
    
    /**
     * Renames an {@link Permission}.
     *
     * @param permission the {@link Permission} to rename.
     * @param name the new name.
     */
    public void setName(Permission permission, String name);

    /**
     * Associates a {@link Permission} with a {@link ResourceClass}.
     * 
     * @param resourceClass the resource class.
     * @param permission permission.
     */
    public void addPermission(ResourceClass resourceClass, Permission permission);
    
    /**
     * Unassociates a {@link Permission} with a {@link ResourceClass}.
     *
     * @param resourceClass the resource class.
     * @param permission permission.
     * @throws IllegalArgumentException if the <code>permission</code> is not
     *         associated with the <code>resourceClass</code>.
     */
    public void deletePermission(ResourceClass resourceClass, Permission permission)
        throws IllegalArgumentException;
    
    /**
     * Grants a {@link Permission} on a {@link Resource} to a {@link Role}.
     *
     * <p>Granting is allowed if the <code>grantor</code> is the owner of the
     * resource or one of it's parent resources, or the superuser.</p>
     *
     * @param resource the involved resource.
     * @param role the involved role.
     * @param permission the involved permission.
     * @param inherited <code>true</code> if the permission applies to the
     *        sub-resources of <code>resource</code> recursively.
     * @param grantor the subject that creates the assignment.
     * @throws SecurityException if the <code>grantor</code> is not allowed to
     *         create teh assignment.</code>
     */
    public void grant(Resource resource, Role role, Permission permission,
                      boolean inherited, Subject grantor)
        throws SecurityException;
    
    /**
     * Revokes a {@link Permission} on a {@link Resource} from a {@link Role}.
     *
     * <p>Revocation is allowed if the <code>revoker</code> creted the
     * assignment himself earlier, or is the superuser.</p>
     *
     * @param resource the involved resource.
     * @param role the involved role.
     * @param permission the involved permission.
     * @param revoker the subject that creates the assignment.
     * @throws IllegalArgumentException if no such permission grant exits.
     * @throws SecurityException if the revoker is not allowed to delete the assignment.
     */
    public void revoke(Resource resource, Role role, Permission permission,
                       Subject revoker)
        throws IllegalArgumentException, SecurityException;
}
