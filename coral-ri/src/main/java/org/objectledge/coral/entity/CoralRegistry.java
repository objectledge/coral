package org.objectledge.coral.entity;

import java.util.Set;

import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.schema.ResourceClassInheritance;
import org.objectledge.coral.security.Permission;
import org.objectledge.coral.security.PermissionAssignment;
import org.objectledge.coral.security.PermissionAssociation;
import org.objectledge.coral.security.Role;
import org.objectledge.coral.security.RoleAssignment;
import org.objectledge.coral.security.RoleImplication;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.Resource;

/**
 * Manages persistency of {@link org.objectledge.coral.entity.Entity}, 
 * {@link org.objectledge.coral.entity.Assignment} and 
 * {@link org.objectledge.coral.entity.Association} objects.
 * 
 * @version $Id: CoralRegistry.java,v 1.3 2004-03-03 10:27:30 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface CoralRegistry
{
    // Schema - AttributeClass ///////////////////////////////////////////////

    /**
     * Returns all {@link AttributeClass}es defined in the system.
     *
     * @return all {@link AttributeClass}es defined in the system.
     */
    public AttributeClass[] getAttributeClass();

    /**
     * Returns the {@link AttributeClass} with a specific identifier.
     *
     * @param id the identifier.
     * @return the <code>AttributeClass</code>.
     * @throws EntityDoesNotExistException if the <code>AttributeClass</code>
     *         with the specified identifier does not exist.
     */
    public AttributeClass getAttributeClass(long id)
        throws EntityDoesNotExistException;
    
    /**
     * Returns the attribute class with the specified name.
     *
     * @param name the name.
     * @return the <code>AttributeClass</code> with the given name.
     * @throws EntityDoesNotExistException if the <code>AttributeClass</code>
     *         with the specified name does not exist.
     */
    public AttributeClass getAttributeClass(String name)
        throws EntityDoesNotExistException;

    /**
     * Adds attribute class to the persitent storage and in-memory cache.
     *
     * @param attributeClass the <code>AttributeClass</code> to add.
     * @throws EntityExistsException if an attribute class with specified name
     *         already exists.
     */
    public void addAttributeClass(AttributeClass attributeClass)
        throws EntityExistsException;
    
    /**
     * Removes attribute class from the persitent storage and in-memory cache.
     *
     * @param attributeClass the <code>AttributeClass</code> to remove.
     * @throws EntityInUseException if there is an <code>ResourceClass</code>
     *         that has attributes of this class.
     */
    public void deleteAttributeClass(AttributeClass attributeClass)
        throws EntityInUseException;

    /**
     * Renames an attribute class.
     *
     * @param item the attribute class to rename.
     * @param name the new name.
     * @throws EntityExistsException if an attribute class with specified name
     *         already exists.
     */
    public void renameAttributeClass(AttributeClass item, String name)
        throws EntityExistsException;

    // Schema - ResourceClass ////////////////////////////////////////////////

    /**
     * Returns all {@link ResourceClass}es defined in the system.
     *
     * @return all {@link ResourceClass}es defined in the system.
     */
    public ResourceClass[] getResourceClass();

    /**
     * Returns the {@link ResourceClass} with a specific identifier.
     *
     * @param id the identifier.
     * @return the <code>ResourceClass</code>.
     * @throws EntityDoesNotExistException if the <code>ResourceClass</code>
     *         with the specified identifier does not exist.
     */
    public ResourceClass getResourceClass(long id)
        throws EntityDoesNotExistException;

    /**
     * Returns the resource class with the specified name.
     *
     * @param name the name.
     * @return the <code>ResourceClass</code> with the given name.
     * @throws EntityDoesNotExistException if the <code>ResourceClass</code>
     *         with the specified name does not exist.
     */
    public ResourceClass getResourceClass(String name)
        throws EntityDoesNotExistException;

    /**
     * Adds resource class to the persitent storage and in-memory cache.
     *
     * @param item the <code>ResourceClass</code> to add.
     * @throws EntityExistsException if an resource class with specified name
     *         already exists.
     */
    public void addResourceClass(ResourceClass item)
        throws EntityExistsException;

    /**
     * Removes resource class from the persitent storage and in-memory cache.
     *
     * @param item the <code>ResourceClass</code> to remove.
     * @throws EntityInUseException if there any resource of this class exists.
     */
    public void deleteResourceClass(ResourceClass item)
        throws EntityInUseException;

    /**
     * Renames an resource class.
     *
     * @param item the resource class to rename.
     * @param name the new name.
     * @throws EntityExistsException if an resource class with specified name
     *         already exists.
     */
    public void renameResourceClass(ResourceClass item, String name)
        throws EntityExistsException;

    // Schema - AttributeDefinition //////////////////////////////////////////

    /**
     * Returns attributes defined by a particular {@link ResourceClass}.
     *
     * @param resourceClass the resource class.
     * @return attributes defined by a particular {@link ResourceClass}.
     */
    public Set getDeclaredAttributes(ResourceClass resourceClass);

    /**
     * Returns all attributes defined by classes in the system.
     * 
     * @return all attributes defined by classes in the system.
     */
    public AttributeDefinition[] getAttributeDefinition();
    
    /**
     * Returns an attribute defienition with the given id.
     * 
     * @param id the identifier of the attribute definition.
     * @return an attribute defienition with the given id.
     * @throws EntityDoesNotExistException if no such attribute exists.
     */
    public AttributeDefinition getAttributeDefinition(long id)
        throws EntityDoesNotExistException;

    /**
     * Renames an attribute definition.
     *
     * @param item the attribute definition to rename.
     * @param name the new name.
     * @throws EntityExistsException if an attribute class with specified name
     *         already exists.
     */
    public void renameAttributeDefinition(AttributeDefinition item, String name)
        throws EntityExistsException;

    /**
     * Adds an attribute definition to the persistent storage and in-memory
     * cache. 
     *
     * @param item the {@link AttributeDefinition} to add.
     */
    public void addAttributeDefinition(AttributeDefinition item);

    /**
     * Removes resource class from the persitent storage and in-memory cache.
     *
     * @param item the <code>ResourceClass</code> to remove.
     */
    public void deleteAttributeDefinition(AttributeDefinition item);

    // Schema - ResourceClassInheritance /////////////////////////////////////

    /**
     * Returns inheritance relationships in which the {@link ResourceClass}
     * is involved as the parent or the child.
     *
     * @param resourceClass the resource class.
     * @return a set of {@link ResourceClassInheritance} objects.
     */
    public Set getResourceClassInheritance(ResourceClass resourceClass);

    /**
     * Adds an resource class inheritance record to the persistent storage and
     * in-memory cache. 
     *
     * @param item the {@link ResourceClassInheritance} to add.
     */
    public void addResourceClassInheritance(ResourceClassInheritance item);
    
    /**
     * Removes resource class inheritance record from the persitent storage
     * and in-memory cache. 
     *
     * @param item the <code>ResourceClass</code> to remove.
     */
    public void deleteResourceClassInheritance(ResourceClassInheritance item);

    // Security - Subject ////////////////////////////////////////////////////

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
     * Adds attribute class to the persitent storage and in-memory cache.
     *
     * @param item the <code>Subject</code> to add.
     * @throws EntityExistsException if an subject with specified name
     *         already exists.
     */
    public void addSubject(Subject item)
        throws EntityExistsException;
    
    /**
     * Removes attribute class from the persitent storage and in-memory cache.
     *
     * @param item the <code>Subject</code> to remove.
     * @throws EntityInUseException if there is any reference to this subject
     *        in the system.
     */
    public void deleteSubject(Subject item)
        throws EntityInUseException;

    /**
     * Renames an subject.
     *
     * @param item the subject to rename.
     * @param name the new name.
     * @throws EntityExistsException if an subject  with specified name
     *         already exists.
     */
    public void renameSubject(Subject item, String name)
        throws EntityExistsException;

    // Security - Role ///////////////////////////////////////////////////////

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
     * Adds attribute class to the persitent storage and in-memory cache.
     *
     * @param item the <code>Role</code> to add.
     */
    public void addRole(Role item);
    
    /**
     * Removes attribute class from the persitent storage and in-memory cache.
     *
     * @param item the <code>Role</code> to remove.
     * @throws EntityInUseException if there are any sub-roles, role
     *         assignments or permission assignments involving this role.
     */
    public void deleteRole(Role item)
        throws EntityInUseException;

    /**
     * Renames an role.
     *
     * @param item the role to rename.
     * @param name the new name.
     */
    public void renameRole(Role item, String name);    

    // Security - Permission /////////////////////////////////////////////////

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
     * Adds attribute class to the persitent storage and in-memory cache.
     *
     * @param item the <code>Permission</code> to add.
     */
    public void addPermission(Permission item);

    /**
     * Removes attribute class from the persitent storage and in-memory cache.
     *
     * @param item the <code>Permission</code> to remove.
     * @throws EntityInUseException if there are any permission associations
     *         or assignments involving this permission.
     */
    public void deletePermission(Permission item)
        throws EntityInUseException;

    /**
     * Renames an permission.
     *
     * @param item the permission to rename.
     * @param name the new name.
     */
    public void renamePermission(Permission item, String name);    

    // Security - RoleImplication ////////////////////////////////////////////

    /**
     * Returns role implications in which particular role is involved (as super,
     * or sub role).
     * 
     * @param role the role.
     * @return role implications in which particular role is involved (as super,
     *         or sub role).
     */
    public Set getRoleImplications(Role role);

    /**
     * Adds an resource class inheritance record to the persistent storage and
     * in-memory cache. 
     *
     * @param item the {@link RoleImplication} to add.
     */
    public void addRoleImplication(RoleImplication item);
    
    /**
     * Removes resource class inheritance record from the persitent storage
     * and in-memory cache. 
     *
     * @param item the <code>Role</code> to remove.
     */
    public void deleteRoleImplication(RoleImplication item);

    // Security - RoleAssignment /////////////////////////////////////////////

    /**
     * Returns the role assignments for a particular subject.
     *
     * @param subject the subject.
     * @return the role assignments for a particular subject.
     */
    public Set getRoleAssignments(Subject subject);

    /**
     * Returns the assignments of a particular role.
     *
     * @param role the role.
     * @return the assignments of a particular role.
     */
    public Set getRoleAssignments(Role role);

    /**
     * Adds an attribute definition to the persistent storage and in-memory
     * cache. 
     *
     * @param item the {@link RoleAssignment} to add.
     */
    public void addRoleAssignment(RoleAssignment item);
    
    /**
     * Removes attribute definition from the persitent storage and in-memory cache.
     *
     * @param item the <code>Subject</code> to remove.
     */
    public void deleteRoleAssignment(RoleAssignment item);

    // Security - PermissionAssociation //////////////////////////////////////

    /**
     * Returns pemission associations for a particular {@link ResourceClass}.
     *
     * @param resourceClass the resourceClass.
     * @return pemission associations for a particular {@link ResourceClass}.
     */
    public Set getPermissionAssociations(ResourceClass resourceClass);
    
    /**
     * Returns pemission associations for a particular {@link Permission}.
     *
     * @param permission the permission.
     * @return pemission associations for a particular {@link Permission}.
     */
    public Set getPermissionAssociations(Permission permission);

    /**
     * Adds an permission association to the persistent storage and in-memory
     * cache. 
     *
     * @param item the {@link PermissionAssociation} to add.
     */
    public void addPermissionAssociation(PermissionAssociation item);

    /**
     * Removes permission association from the persitent storage and in-memory cache.
     *
     * @param item the <code>ResourceClass</code> to remove.
     */
    public void deletePermissionAssociation(PermissionAssociation item);

    // Security - PermissionAssignment ///////////////////////////////////////

    /**
     * Returns permission assignment information for a particular {@link
     * Resource}.
     *
     * @param resource the {@link Resource}.
     * @return permission assignment information for a particular {@link
     * Resource}.
     */
    public Set getPermissionAssignments(Resource resource);
 
     /**
     * Returns permission assignment information for a particular {@link
     * Role}.
     *
     * @param role the {@link Role}.
     * @return permission assignment information for a particular {@link
     * Role}.
     */
    public Set getPermissionAssignments(Role role);

    /**
     * Adds an permission assignment to the persistent storage and in-memory
     * cache. 
     *
     * @param item the {@link PermissionAssignment} to add.
     */
    public void addPermissionAssignment(PermissionAssignment item);
    
    /**
     * Removes permission assignment from the persitent storage and in-memory cache.
     *
     * @param item the <code>Resource</code> to remove.
     */
    public void deletePermissionAssignment(PermissionAssignment item);

    // Non-cached cross-reference information ////////////////////////////////
    
    /**
     * Returns all resoruces created by a Subject.
     *
     * @param subject the subject.
     * @return all resources created by the Subject.
     */
    public Resource[] getCreatedResources(Subject subject);
    
    /**
     * Returns all resource owned by the subject.
     *
     * <p>The results include all resources that are children of the resources
     * owned directly by the subject. This method may be extremely time and
     * memory consuming!</p>
     *
     * @param subject the subject.
     * @return all resoures owned by the Subject.
     */
    public Resource[] getOwnedResources(Subject subject);

    /**
     * Resturns all role assignments granted by the subject.
     *
     * @param subject the subject.
     * @return all role assignments granted by the subject.
     */
    public RoleAssignment[] getGrantedRoleAssignments(Subject subject);

    /**
     * Resturns all permission assignments granted by the subject.
     *
     * @param subject the subject.
     * @return all permission assignments granted by the subject.
     */
    public PermissionAssignment[] getGrantedPermissionAssignments(Subject subject);
}
