package org.objectledge.coral.security;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.objectledge.coral.BackendException;
import org.objectledge.coral.entity.AbstractEntity;
import org.objectledge.coral.entity.CoralRegistry;
import org.objectledge.coral.event.CoralEventHub;
import org.objectledge.coral.event.PermissionAssignmentChangeListener;
import org.objectledge.coral.event.RoleAssignmentChangeListener;
import org.objectledge.coral.event.RoleChangeListener;
import org.objectledge.coral.event.RoleImplicationChangeListener;
import org.objectledge.coral.store.Resource;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.OutputRecord;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.PersistenceException;

/**
 * An implementaion of {@link org.objectledge.coral.security.Role} interface.
 *
 * @version $Id: RoleImpl.java,v 1.6 2004-02-23 13:50:26 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class RoleImpl
    extends AbstractEntity
    implements Role,
               PermissionAssignmentChangeListener,
               RoleAssignmentChangeListener,
               RoleImplicationChangeListener,
               RoleChangeListener
{
    // Instance variables ///////////////////////////////////////////////////////////////////////

    /** The CoralEventHub. */
    private CoralEventHub coralEventHub;
    
    /** The CoralRegistry. */
    private CoralRegistry coralRegistry;

    /** The role conatiner. */
    private RoleContainer roles = null;

    /** The permission conatiner. */
    private PermissionContainer permissions = null;

    /** The permission assignments. */
    private Set permissionAssignments;

    /** Read-only thread-safe copy of {@link #permissionAssignments}. */
    private Set cpermissionAssignments;
    
    /** The role assignments. */
    private Set roleAssignments;

    /** Read-only thread-safe copy of {@link #roleAssignments}. */
    private Set croleAssignments;

    /** The role implications. */
    private Set implications;
    
    /** Read-only thread-safe copy of {@link #implications}. */
    private Set cimplications;

    // Initalization ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs a {@link RoleImpl}.
     *
     * @param persistence the Peristence subsystem.
     * @param coralEventHub the CoralEventHub.
     * @param coralRegistry the CoralRegistry.
     */
    RoleImpl(Persistence persistence, CoralEventHub coralEventHub, CoralRegistry coralRegistry)
    {
        super(persistence);
        this.coralEventHub = coralEventHub;
        this.coralRegistry = coralRegistry;
    }

    /**
     * Constructs a {@link RoleImpl}.
     *
     * @param persistence the Peristence subsystem.
     * @param coralEventHub the CoralEventHub.
     * @param coralRegistry the CoralRegistry.
     *
     * @param name the name of the role.
     */
    RoleImpl(Persistence persistence, CoralEventHub coralEventHub, CoralRegistry coralRegistry,
        String name)
    {
        super(persistence, name);
        this.coralEventHub = coralEventHub;
        this.coralRegistry = coralRegistry;
    }
    
    // Persistent interface /////////////////////////////////////////////////////////////////////

    /** The key columns */
    private static final String[] KEY_COLUMNS = { "role_id" };    

    /**
     * Returns the name of the table this type is mapped to.
     *
     * @return the name of the table.
     */
    public String getTable()
    {
        return "arl_role";
    }
    
    /** 
     * Returns the names of the key columns.
     *
     * @return the names of the key columns.
     */
    public String[] getKeyColumns()
    {
        return KEY_COLUMNS;
    }

    /**
     * Stores the fields of the object into the specified record.
     *
     * <p>You need to call <code>getData</code> of your superclasses if they
     * are <code>Persistent</code>.</p>
     *
     * @param record the record to store state into.
     * @throws PersistenceException if there is a problem storing field values.
     */
    public void getData(OutputRecord record)
        throws PersistenceException
    {
        super.getData(record);
    }

    /**
     * Loads the fields of the object from the specified record.
     *
     * <p>You need to call <code>setData</code> of your superclasses if they
     * are <code>Persistent</code>.</p>
     * 
     * @param record the record to read state from.
     * @throws PersistenceException if there is a problem loading field values.
     */
    public void setData(InputRecord record)
        throws PersistenceException
    {
        super.setData(record);
        coralEventHub.getInbound().addRoleChangeListener(this, this);
    }

    // SubjectChangeListener interface //////////////////////////////////////////////////////////

    /**
     * Called when <code>Role</code>'s data change.
     *
     * @param role the role that changed.
     */
    public void roleChanged(Role role)
    {
        if(role.equals(this))
        {
            try
            {
                persistence.revert(this);
            }
            catch(PersistenceException e)
            {
                throw new BackendException("failed to revert entity state");
            }
        }
    }

    // Role interafce ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns all {@link org.objectledge.coral.security.RoleImplication} relationships that this 
     * role is involved in.
     *
     * <p>Use this method to discover direrct super and sub roles of a
     * role.</p>
     *
     * @return all {@link org.objectledge.coral.security.RoleImplication} relationships that this 
     * role is involved in.
     */
    public RoleImplication[] getImplications()
    {
        buildImplications();
        Set snapshot = cimplications;
        RoleImplication[] result = new RoleImplication[snapshot.size()];
        snapshot.toArray(result);
        return result;
    }

    /**
     * Returns all <code>Role</code>s that are implied by (contained in) this
     * role. 
     *
     * @return all <code>Role</code>s that are implied by (contained in) this
     * role. 
     */
    public Role[] getSubRoles()
    {
        buildRoles();
        Set snapshot = roles.getSubRoles();
        Role[] result = new Role[snapshot.size()];
        snapshot.toArray(result);
        return result;
    }

    /**
     * Returns all <code>Role</code>s that imply (contain) this role.
     *
     * @return all <code>Role</code>s that imply (contain) this role.
     */
    public Role[] getSuperRoles()
    {
        buildRoles();
        Set snapshot = roles.getSuperRoles();
        Role[] result = new Role[snapshot.size()];
        snapshot.toArray(result);
        return result;
    }

    /**
     * Returns <code>true</code> if the specified Role is implied by (contained
     * in) this role.
     *
     * @param role the role.
     * @return <code>true</code> if the specified Role is implied by (contained
     * in) this role.
     */
    public boolean isSubRole(Role role)
    {
        buildRoles();
        return roles.isSubRole(role);
    }

    /**
     * Returns <code>true</code> if the specified Role is implies (contains)
     * this role. 
     *
     * @param role the role.
     * @return <code>true</code> if the specified Role is implies (contains)
     * this role.
     */
    public boolean isSuperRole(Role role)
    {
        buildRoles();
        return roles.isSuperRole(role);
    }
    
    /**
     * Returns all role assignments that were made for this role.
     *
     * <p>Use this method to acquire information about explicit role grants.</p>
     *
     * @return all role assignments that were made for this role.
     */
    public RoleAssignment[] getRoleAssignments()
    {
        buildRoleAssignments();
        Set snapshot = croleAssignments;
        RoleAssignment[] result = new RoleAssignment[snapshot.size()];
        snapshot.toArray(result);
        return result;
    }

    /**
     * Returns all subjects that were assigned this role.
     *
     * <p>This method takes Role impications into account, thus all Subjects
     * that pass {@link org.objectledge.coral.security.Subject#hasRole(Role)} check for this
     * Role are returned.</p> 
     * 
     * @return all subjects that were assigned this role.
     */
    public Subject[] getSubjects()
    {
        buildRoles();
        Set temp = new HashSet();
        Set roleset = roles.getMatchingRoles();
        Iterator i=roleset.iterator();
        while(i.hasNext())
        {
            Role r = (Role)i.next();
            Set ras = coralRegistry.getRoleAssignments(r);
            Iterator j=ras.iterator();
            while(j.hasNext())
            {
                RoleAssignment ra = (RoleAssignment)j.next();
                temp.add(ra.getSubject());
            }
        }
        Subject[] result = new Subject[temp.size()];
        temp.toArray(result);
        return result;
    }

    /**
     * Returns all <code>PermissionAssignments</code> defined for this role.
     *
     * @return permission assignments defined for this role.
     */
    public PermissionAssignment[] getPermissionAssignments()
    {
        buildPermissionAssignments();
        Set snapshot = cpermissionAssignments;
        PermissionAssignment[] result = new PermissionAssignment[snapshot.size()];
        snapshot.toArray(result);
        return result;
    }
    
    /**
     * Returns <code>PermissionAssignments</code> defined for this role on a
     * particular <code>Resource</code>.
     *
     * @param resource the resource.
     * @return <code>PermissionAssignments</code> defined for this role on a
     * particular <code>Resource</code>.
     */
    public PermissionAssignment[] getPermissionAssignments(Resource resource)    
    {
        buildPermissions();
        return permissions.getPermissionAssignments(resource);
    }
    
    /**
     * Returns all permissions defined for this role on a particular resource.
     *
     * @param resource the resource.
     * @return permissions defined on this resource.
     */
    public Permission[] getPermissions(Resource resource)
    {
        buildPermissions();
        return permissions.getPermissions(resource);
    }

    /**
     * Returns <code>true</code> if the role has the specifed permission on 
     * the specified resource.
     *
     * @param resource the resource.
     * @param permission the permission.
     * @return <code>true</code> if the role has the specifed permission on 
     * the specified resource.
     */
    public boolean hasPermission(Resource resource, Permission permission)
    {
        buildPermissions();
        return permissions.hasPermission(resource, permission);
    }
    
    // PermissionAssignmentChangeListener interface /////////////////////////////////////////////

    /**
     * Called when permission assignemts on the <code>resource</code> change.
     *
     * @param assignment the permission assignment.
     * @param added <code>true</code> if the permission was added,
     *        <code>false</code> if removed.
     */
    public synchronized void permissionsChanged(PermissionAssignment assignment, boolean added)
    {
        if(assignment.getRole().equals(this))
        {
            if(added)
            {
                permissionAssignments.add(assignment);
            }
            else
            {
                permissionAssignments.remove(assignment);
            }
            cpermissionAssignments = (Set)((HashSet)permissionAssignments).clone();
        }
    }

    /**
     * Called when role assignemts change.
     *
     * @param assignment the role assignment.
     * @param added <code>true</code> if the assignments was added,
     *        <code>false</code> if removed.
     */
    public synchronized void rolesChanged(RoleAssignment assignment, boolean added)
    {
        if(assignment.getRole().equals(this))
        {
            if(added)
            {
                roleAssignments.add(assignment);
            }
            else
            {
                roleAssignments.remove(assignment);
            }
            croleAssignments = (Set)((HashSet)roleAssignments).clone();
        }
    }

    /**
     * Called when role assignemts change.
     *
     * @param implication the role implication.
     * @param added <code>true</code> if the implications was added,
     *        <code>false</code> if removed.
     */
    public synchronized void roleChanged(RoleImplication implication, boolean added)
    {
        if(implication.getSuperRole().equals(this) || 
           implication.getSubRole().equals(this))
        {
            if(added)
            {
                implications.add(implication);
            }
            else
            {
                implications.remove(implication);
            }
            cimplications = (Set)((HashSet)implications).clone();
        }
    }

    // Private //////////////////////////////////////////////////////////////////////////////////

    private synchronized void buildImplications()
    {
        if(implications == null)
        {
            implications = coralRegistry.getRoleImplications(this);
            cimplications = (Set)((HashSet)implications).clone();
            coralEventHub.getGlobal().addRoleImplicationChangeListener(this, this);
        }
    }

    private synchronized void buildRoles()
    {
        if(roles == null)
        {
            Set roleSet = new HashSet();
            roleSet.add(this);
            roles = new RoleContainer(coralEventHub, coralRegistry, roleSet, true);
        }
    }

    private synchronized void buildPermissions()
    {
        if(permissions == null)
        {
            buildRoles();
            permissions = new PermissionContainer(coralEventHub, coralRegistry, roles);
        }
    }

    private synchronized void buildPermissionAssignments()
    {
        if(permissionAssignments == null)
        {
            permissionAssignments = coralRegistry.getPermissionAssignments(this);
            cpermissionAssignments = (Set)((HashSet)permissionAssignments).clone();
            coralEventHub.getGlobal().addPermissionAssignmentChangeListener(this, this);
        }
    }

    private synchronized void buildRoleAssignments()
    {
        if(roleAssignments == null)
        {
            roleAssignments = coralRegistry.getRoleAssignments(this);
            croleAssignments = (Set)((HashSet)roleAssignments).clone();
            coralEventHub.getGlobal().addRoleAssignmentChangeListener(this, this);
        }
    }
}
