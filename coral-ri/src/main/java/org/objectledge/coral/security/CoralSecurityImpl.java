package org.objectledge.coral.security;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.objectledge.coral.BackendException;
import org.objectledge.coral.entity.CoralRegistry;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.entity.EntityExistsException;
import org.objectledge.coral.entity.EntityInUseException;
import org.objectledge.coral.event.CoralEventHub;
import org.objectledge.coral.schema.CircularDependencyException;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.Resource;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.PersistenceException;
import org.objectledge.database.persistence.Persistent;

/**
 * Manages {@link Subject}s, {@link Role}s and {@link Permission}s.
 *
 * @version $Id: CoralSecurityImpl.java,v 1.1 2004-03-03 07:47:04 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class CoralSecurityImpl
    implements CoralSecurity
{
    // Instance variables ///////////////////////////////////////////////////////////////////////

    /** The persistence. */
    private Persistence persistence;
    
    /** The event hub. */
    private CoralEventHub coralEventHub;

    /** The CoralRegistry. */
    private CoralRegistry coralRegistry;
    
    /** The CoralSchema. */
    private CoralSchema coralSchema;
    
    /** The CoralSchema. */
    private CoralStore coralStore;

    /** The {@link Role#ROOT} role. */
    private Role root;
    
    /** The {@link Role#NOBODY} role. */
    private Role nobody;

    // Initialization ///////////////////////////////////////////////////////////////////////////

    /**
     * Constructs the {@link SecurityService} implementation.
     *
     * @param persistence the persistence subsystem
     * @param coralEventHub the even hub.
     * @param coralRegistry the registry.
     * @param coralSchema the CoralSchema
     * @param coralStore the CoralStore.
     */
    public CoralSecurityImpl(Persistence persistence, CoralEventHub coralEventHub,
        CoralRegistry coralRegistry, CoralSchema coralSchema, CoralStore coralStore)
    {
        this.persistence = persistence;
        this.coralEventHub = coralEventHub;
        this.coralRegistry = coralRegistry;
        this.coralSchema = coralSchema;
        this.coralStore = coralStore;
    }

    // Subjects /////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Returns all {@link Subject}s defined in the system.
     *
     * @return all {@link Subject}s defined in the system.
     */
    public Subject[] getSubject()
    {
        return coralRegistry.getSubject();
    }

    /**
     * Returns the {@link Subject} with a specific identifier.
     *
     * @param id the identifier.
     * @return the <code>Subject</code>.
     * @throws EntityDoesNotExistException if the <code>Subject</code>
     *         with the specified identifier does not exist.
     */
    public Subject getSubject(long id)
        throws EntityDoesNotExistException
    {
        return coralRegistry.getSubject(id);
    }
    
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
        throws EntityDoesNotExistException
    {
        return coralRegistry.getSubject(name);
    }
    
    /**
     * Creates a {@link Subject}.
     *
     * @param name the name of the class.
     * @return newly created subject.
     * @throws EntityExistsException if a subject with the given name already
     *         exists in the system.
     */
    public Subject createSubject(String name)
        throws EntityExistsException
    {
        Subject subject = new SubjectImpl(persistence, coralEventHub, coralRegistry, this, 
            name, null);
        coralRegistry.addSubject(subject);
        return subject;
    }
    
    /**
     * Removes a {@link Subject}.
     *
     * @param subject the {@link Subject}.
     * @throws EntityInUseException if there any security grants perfored for
     *         or by the subject, or resouruces created or modified by the
     *         subject
     */
    public void deleteSubject(Subject subject)
        throws EntityInUseException
    {
        coralRegistry.deleteSubject(subject);
    }

    /**
     * Renames the {@link Subject}.
     *
     * @param subject the subject to rename.
     * @param name the new name of the subject.
     * @throws EntityExistsException if another subject by that name exists.
     */
    public void setName(Subject subject, String name)
        throws EntityExistsException
    {
        coralRegistry.renameSubject(subject, name);
        coralEventHub.getOutbound().fireSubjectChangeEvent(subject);
    }

    /**
     * Creates a supervisor -- subordinate relationship.
     *
     * @param subordinate the subordinate.
     * @param supervisor the supervisor.
     */
    public void setSupervisor(Subject subordinate, Subject supervisor)
    {
        ((SubjectImpl)subordinate).setSupervisor(supervisor);
        try
        {
            persistence.save((Persistent)subordinate);
        }
        catch(PersistenceException e)
        {
            throw new BackendException("Failed to update Subject", e);
        }
        coralEventHub.getOutbound().fireSubjectChangeEvent(subordinate);
    }

    /**
     * Removes a supervisor -- subordinate relationship.
     *
     * @param subordinate the subordinate.
     */
    public void unsetSupervisor(Subject subordinate)
    {
        ((SubjectImpl)subordinate).setSupervisor(null);
        try
        {
            persistence.save((Persistent)subordinate);
        }
        catch(PersistenceException e)
        {
            throw new BackendException("Failed to update Subject", e);
        }
        coralEventHub.getOutbound().fireSubjectChangeEvent(subordinate);
    }
    
    // Roles ////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns all {@link Role}s defined in the system.
     *
     * @return all {@link Role}s defined in the system.
     */
    public Role[] getRole()
    {
        return coralRegistry.getRole();
    }

    /**
     * Returns the {@link Role} with a specific identifier.
     *
     * @param id the identifier.
     * @return the <code>Role</code>.
     * @throws EntityDoesNotExistException if the <code>Role</code>
     *         with the specified identifier does not exist.
     */
    public Role getRole(long id)
        throws EntityDoesNotExistException
    {
        return coralRegistry.getRole(id);
    }
    
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
    public Role[] getRole(String name)
    {
        return coralRegistry.getRole(name);
    }
    
    /**
     * Returns the role with the specifed name.
     *
     * @param name the name.
     * @return the role
     * @throws IllegalStateException if the name denotes multiple roles,
     *         or does not exist.
     */
    public Role getUniqueRole(String name)
        throws IllegalStateException
    {
        return coralRegistry.getUniqueRole(name);
    }

    /**
     * Creates a {@link Role}.
     *
     * @param name the name of the <code>Role</code>.
     * @return a newly created role.
     */
    public Role createRole(String name)
    {
        Role role = new RoleImpl(persistence, coralEventHub, coralRegistry, name);
        coralRegistry.addRole(role);
        return role;
    }
    
    /**
     * Removes an {@link Role}.
     *
     * @param role the {@link Role}.
     * @throws EntityInUseException if it has subroles or there are security
     *         assignments in which this role is involved.
     */
    public void deleteRole(Role role)
        throws EntityInUseException
    {
        coralRegistry.deleteRole(role);
    }

    /**
     * Renames an role.
     *
     * @param role the attribute class to rename.
     * @param name the new name.
     */
    public void setName(Role role, String name)
    {
        coralRegistry.renameRole(role, name);
        coralEventHub.getOutbound().fireRoleChangeEvent(role);
    }
    
    /**
     * Creates implication relationship between two roles.
     *
     * @param superRole the implicating / containing role.
     * @param subRole the implied / contained role.
     * @throws CircularDependencyException if the <code>subRole</code> is
     *         actually a super role of the <code>superRole</code>.
     */
    public void addSubRole(Role superRole, Role subRole)
        throws CircularDependencyException
    {
        // if the implications is alredy defined, quit happily
        Set implications = coralRegistry.getRoleImplications(subRole);
        RoleImplication item = new RoleImplicationImpl(this, superRole, subRole);
        if(implications.contains(item))
        {
            return;
        }
        // check for dependency loop
        ArrayList stack = new ArrayList();
        stack.add(subRole);
        while(stack.size() > 0)
        {
            Role role = (Role)stack.remove(stack.size()-1);
            if(role.equals(superRole))
            {
                StringBuffer buff = new StringBuffer();
                buff.append("circular dependency: ");
                buff.append(superRole.getName());
                buff.append('>');
                for(int i=0; i<stack.size(); i++)
                {
                    buff.append(((Role)stack.get(i)).getName());
                    buff.append('>');
                }
                buff.append(role.getName());
                throw new CircularDependencyException(buff.toString());
            }
            else
            {
                Role[] subroles = role.getSubRoles();
                for(int i=0; i<subroles.length; i++)
                {
                    stack.add(subroles[i]);
                }
            }
        }
        coralRegistry.addRoleImplication(item);
    }
    
    /**
     * Removes implication relationship between two roles.
     *
     * @param superRole the implicating / containing role.
     * @param subRole the implied / contained role.
     * @throws IllegalArgumentException if the <code>subRole</code> is not
     *         really a subrole of the <code>superRole</code>.
     */
    public void deleteSubRole(Role superRole, Role subRole) 
        throws IllegalArgumentException
    {
        if(!superRole.isSubRole(subRole))
        {
            throw new IllegalArgumentException(subRole.getName()+
                                               " is not a direct sub-role of "+
                                               superRole.getName());
        }
        RoleImplication item = new RoleImplicationImpl(this, superRole, subRole);
        coralRegistry.deleteRoleImplication(item);
    }
    
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
        throws SecurityException
    {

        RoleAssignment item = new RoleAssignmentImpl(this, 
            grantor, subject, role, grantingAllowed);

        // if the assignemnt already exists, quit happily
        Set assignments = coralRegistry.getRoleAssignments(subject);
        if(assignments.contains(item))
        {
            return;
        }

        if(!grantor.hasRole(getRootRole()))
        {
            RoleAssignment[] raa = grantor.getRoleAssignments();
            int i;
            loop: for(i=0; i<raa.length; i++)
            {
                if(raa[i].getRole().equals(role) && raa[i].isGrantingAllowed())
                {
                    break loop;
                }
            }
            if(i == raa.length)
            {
                throw new SecurityException(grantor.getName()+
                                            " is not allowed to grant "+
                                            role.getName()+" role");
            }
            /*
              FIXME: this is disabled until many-to-many supervision
              relationships are implemented.
            if(!subject.isSubordinate(grantor))
            {
                throw new SecurityException(grantor.getName()+
                                            " is not allowed to grant roles to "+
                                            subject.getName());
            } 
            */
        }
        if(role.equals(getNobodyRole()))
        {
            throw new SecurityException("granting of 'nobody' role is not allowed");
        }
        coralRegistry.addRoleAssignment(item);
    }
    
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
        throws IllegalArgumentException, SecurityException
    {
        RoleAssignment item = new RoleAssignmentImpl(this, 
            revoker, subject, role, false);
        Set assignments = coralRegistry.getRoleAssignments(subject);
        if(!assignments.contains(item))
        {
            throw new IllegalArgumentException(subject.getName()+" was not explicitly granted"+
                                              " role "+role.getName());
        }
        if(!revoker.hasRole(getRootRole()))
        {
            RoleAssignment[] raa = subject.getRoleAssignments();
            int i;
            loop: for(i=0; i<raa.length; i++)
            {
                if(raa[i].getRole().equals(role) && raa[i].getGrantedBy().equals(revoker))
                {
                    break loop;
                }
            }
            if(i == raa.length)
            {
                throw new SecurityException(revoker.getName()+" is not allowed to "+
                                            " revoke role "+role.getName()+" from "+
                                            subject.getName());
            }
            /*
              FIXME: this is disabled until many-to-many supervision
              relationships are implemented.
            if(!subject.isSubordinate(revoker))
            {
                throw new SecurityException(revoker.getName()+" is not allowed to "+
                                            " revoke roles from "+subject.getName());
            }            
            */
        }
        coralRegistry.deleteRoleAssignment(item);
    }

    // Permissions //////////////////////////////////////////////////////////////////////////////

    /**
     * Returns all {@link Permission}s defined in the system.
     *
     * @return all {@link Permission}s defined in the system.
     */
    public Permission[] getPermission()
    {
        return coralRegistry.getPermission();
    }

    /**
     * Returns the {@link Permission} with a specific identifier.
     *
     * @param id the identifier.
     * @return the <code>Permission</code>.
     * @throws EntityDoesNotExistException if the <code>Permission</code>
     *         with the specified identifier does not exist.
     */
    public Permission getPermission(long id)
        throws EntityDoesNotExistException
    {
        return coralRegistry.getPermission(id);
    }
    
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
    public Permission[] getPermission(String name)
    {
        return coralRegistry.getPermission(name);
    }
    
    /**
     * Returns the permission with the specifed name.
     *
     * @param name the name.
     * @return the permission
     * @throws IllegalStateException if the name denotes multiple permissions,
     *         or does not exist.
     */
    public Permission getUniquePermission(String name)
        throws IllegalStateException
    {
        return coralRegistry.getUniquePermission(name);
    }

    /**
     * Creates a {@link Permission}.
     *
     * @param name the name of the class
     * @return a newly created Permission.
     */
    public Permission createPermission(String name)
    {
        Permission permission = new PermissionImpl(persistence, coralEventHub, coralRegistry, 
            name);
        coralRegistry.addPermission(permission);
        return permission;
    }
    
    /**
     * Removes a {@link Permission}.
     *
     * @param permission the {@link Permission}.
     * @throws EntityInUseException if there are security assignments that
     *         involve this permission.
     */
    public void deletePermission(Permission permission)
        throws EntityInUseException
    {
        coralRegistry.deletePermission(permission);
    }
    
    /**
     * Renames an {@link Permission}.
     *
     * @param permission the {@link Permission} to rename.
     * @param name the new name.
     */
    public void setName(Permission permission, String name)
    {
        coralRegistry.renamePermission(permission, name);
        coralEventHub.getOutbound().firePermissionChangeEvent(permission);
    }

    /**
     * Associates a {@link Permission} with a {@link ResourceClass}.
     * 
     * @param resourceClass the resource class.
     * @param permission permission.
     */
    public void addPermission(ResourceClass resourceClass, Permission permission)
    {
        Set associations = coralRegistry.getPermissionAssociations(resourceClass);
        PermissionAssociation item = 
            new PermissionAssociationImpl(coralSchema, this, 
                resourceClass, permission);
        if(associations.contains(item))
        {
            return;
        }
        coralRegistry.addPermissionAssociation(item);
    }
    
    /**
     * Unassociates a {@link Permission} with a {@link ResourceClass}.
     *
     * @param resourceClass the resource class.
     * @param permission permission.
     * @throws IllegalArgumentException if the <code>permission</code> is not
     *         associated with the <code>resourceClass</code>.
     */
    public void deletePermission(ResourceClass resourceClass, Permission permission)
        throws IllegalArgumentException
    {
        Set associations = coralRegistry.getPermissionAssociations(resourceClass);
        PermissionAssociation item = new PermissionAssociationImpl(coralSchema, this, 
            resourceClass, permission);
        if(!associations.contains(item))
        {
            throw new IllegalArgumentException(resourceClass.getName()+
                                               " is not associated with "+
                                               permission.getName()+" permission");
        }
        coralRegistry.deletePermissionAssociation(item);
    }
    
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
        throws SecurityException
    {
        if(!grantor.hasRole(getRootRole()))
        {
            Resource res = resource;
            loop: while(res != null)
            {
                if(res.getOwner().equals(grantor))
                {
                    break loop;
                }
                res = res.getParent();
            }
            if(res == null)
            {
                throw new SecurityException(grantor.getName()+" is not allowed to "+
                                            "grant permissions on resource #"+
                                            resource.getId());
            }
        }
        if(!resource.getResourceClass().isAssociatedWith(permission))
        {
            throw new SecurityException("permission "+permission.getName()+
                                        " cannot be granted on resources of type "+
                                        resource.getResourceClass().getName());
        }
        PermissionAssignment item = new PermissionAssignmentImpl(this, coralStore, 
            grantor, resource, role, permission, inherited);
        coralRegistry.addPermissionAssignment(item);
    }
    
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
     * @throws SecurityException if the <code>revoker</code> is not allowed to
     *         delete the assignment.</code>
     */
    public void revoke(Resource resource, Role role, Permission permission,
                       Subject revoker)
        throws SecurityException
    {
        if(!role.hasPermission(resource, permission))
        {
            throw new SecurityException("role "+role.getName()+" does not have "+
                                        permission.getName()+" on resource #"+
                                        resource.getId());
        }
        if(!revoker.hasRole(getRootRole()))
        {
            Set assignments = coralRegistry.getPermissionAssignments(resource);
            Iterator i = assignments.iterator();
            PermissionAssignmentImpl pa = null;
            loop: while(i.hasNext())
            {
                pa = (PermissionAssignmentImpl)i.next();
                if(pa.getRole().equals(role) &&
                   pa.getPermission().equals(permission) &&
                   pa.getGrantedBy().equals(revoker))
                {
                    break loop;
                }
            }
            if(!pa.getRole().equals(role) ||
               !pa.getPermission().equals(permission) ||
               !pa.getGrantedBy().equals(revoker))
            {
                throw new SecurityException(revoker.getName()+" is not allowed to revoke "+
                                            permission.getName()+" permission from role "+
                                            role.getName()+" on resource #"+resource.getId());
            }
        }
        PermissionAssignment item = new PermissionAssignmentImpl(this, coralStore, 
            revoker,resource, role, permission, false);
        coralRegistry.deletePermissionAssignment(item);           
    }
    
    // implementation ///////////////////////////////////////////////////////////////////////////

    /**
     * Laziliy loads the Root role.
     * 
     * @return the Root role.
     */    
    private Role getRootRole()
    {
        if(root == null)
        {
            try
            {
                root = coralRegistry.getRole(Role.ROOT);
            }
            catch(EntityDoesNotExistException e)
            {
                throw new BackendException("Failed to lookup the root role.", e);
            }
        }
        return root;
    }    

    /**
     * Laziliy loads the Nobody role.
     * 
     * @return the Nobody role.
     */    
    private Role getNobodyRole()
    {
        if(root == null)
        {
            try
            {
                root = coralRegistry.getRole(Role.NOBODY);
            }
            catch(EntityDoesNotExistException e)
            {
                throw new BackendException("Failed to lookup the nobody role.", e);
            }
        }
        return root;
    }    
}
