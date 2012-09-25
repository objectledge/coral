package org.objectledge.coral.security;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.objectledge.collections.ImmutableHashSet;
import org.objectledge.collections.ImmutableSet;
import org.objectledge.coral.BackendException;
import org.objectledge.coral.CoralCore;
import org.objectledge.coral.entity.AbstractEntity;
import org.objectledge.coral.event.CoralEventHub;
import org.objectledge.coral.event.PermissionAssignmentChangeListener;
import org.objectledge.coral.event.RoleAssignmentChangeListener;
import org.objectledge.coral.event.RoleChangeListener;
import org.objectledge.coral.event.RoleImplicationChangeListener;
import org.objectledge.coral.store.Resource;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.OutputRecord;
import org.objectledge.database.persistence.Persistence;

/**
 * An implementation of {@link org.objectledge.coral.security.Role} interface.
 *
 * @version $Id: RoleImpl.java,v 1.14 2007-12-30 23:55:38 rafal Exp $
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
    
    /** The component hub. */
    private CoralCore coral;

    /** The role conatiner. */
    private RoleContainer roles = null;

    /** The permission conatiner. */
    private PermissionContainer permissions = null;

    /** The permission assignments. */
    private ImmutableSet<PermissionAssignment> permissionAssignments;
    
    /** The role assignments. */
    private ImmutableSet<RoleAssignment> roleAssignments;

    /** The role implications. */
    private ImmutableSet<RoleImplication> implications;
   

    // Initalization ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs a {@link RoleImpl}.
     *
     * @param persistence the Peristence subsystem.
     * @param coralEventHub the CoralEventHub.
     * @param coral the component hub.
     */
    public RoleImpl(Persistence persistence, CoralEventHub coralEventHub, CoralCore coral)
    {
        super(persistence);
        this.coralEventHub = coralEventHub;
        this.coral = coral;
    }

    /**
     * Constructs a {@link RoleImpl}.
     *
     * @param persistence the Peristence subsystem.
     * @param coralEventHub the CoralEventHub.
     * @param coral the component hub.
     *
     * @param name the name of the role.
     */
    public RoleImpl(Persistence persistence, CoralEventHub coralEventHub, 
        CoralCore coral,
        String name)
    {
        super(persistence, name);
        this.coralEventHub = coralEventHub;
        this.coral = coral;
        coralEventHub.getInbound().addRoleChangeListener(this, this);
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
        return "coral_role";
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
     * @throws SQLException if there is a problem storing field values.
     */
    public void getData(OutputRecord record)
        throws SQLException
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
     * @throws SQLException if there is a problem loading field values.
     */
    public void setData(InputRecord record)
        throws SQLException
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
            catch(SQLException e)
            {
                throw new BackendException("failed to revert entity state", e);
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
        ImmutableSet<RoleImplication> snapshot = buildImplications();
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
        Set<Role> snapshot = buildRoles().getSubRoles();
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
        Set<Role> snapshot = buildRoles().getSuperRoles();
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
        return buildRoles().isSubRole(role);
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
        return buildRoles().isSuperRole(role);
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
        ImmutableSet<RoleAssignment> snapshot = buildRoleAssignments();
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
        Set<Subject> subjects = new HashSet<Subject>();
        Deque<Role> roleStack = new LinkedList<Role>();
        roleStack.push(this);
        while(!roleStack.isEmpty())
        {
            Role r = roleStack.pop();
            for(RoleAssignment ra : coral.getRegistry().getRoleAssignments(r))
            {
                subjects.add(ra.getSubject());
            }
            for(Role sr : r.getSuperRoles())
            {
                roleStack.push(sr);
            }
        }
        return subjects.toArray(new Subject[subjects.size()]);
    }

    /**
     * Returns all <code>PermissionAssignments</code> defined for this role.
     *
     * @return permission assignments defined for this role.
     */
    public PermissionAssignment[] getPermissionAssignments()
    {
        ImmutableSet<PermissionAssignment> snapshot = buildPermissionAssignments();
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
        Set<PermissionAssignment> pas = coral.getRegistry().getPermissionAssignments(resource);
		// filter out inherited grants
		ArrayList<PermissionAssignment> list = new ArrayList<PermissionAssignment>();
		for(PermissionAssignment pa : pas)
		{
			if(pa.getRole().equals(this) && pa.getResource().equals(resource))
			{
				list.add(pa);
			}
		}
		return list.toArray(new PermissionAssignment[list.size()]);
    }
    
    /**
     * Returns all permissions defined for this role on a particular resource.
     *
     * @param resource the resource.
     * @return permissions defined on this resource.
     */
    public Permission[] getPermissions(Resource resource)
    {
        return buildPermissions().getPermissions(resource);
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
        return buildPermissions().hasPermission(resource, permission);
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
                permissionAssignments = permissionAssignments.add(assignment);
            }
            else
            {
                permissionAssignments = permissionAssignments.remove(assignment);
            }
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
                roleAssignments = roleAssignments.add(assignment);
            }
            else
            {
                roleAssignments = roleAssignments.remove(assignment);
            }
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
                implications = implications.add(implication);
            }
            else
            {
                implications = implications.remove(implication);
            }
        }
    }

    // Private //////////////////////////////////////////////////////////////////////////////////

    private synchronized ImmutableSet<RoleImplication> buildImplications()
    {
        if(implications == null)
        {
            implications = new ImmutableHashSet<RoleImplication>(coral.getRegistry().getRoleImplications(this));
            coralEventHub.getGlobal().addRoleImplicationChangeListener(this, this);
        }
        return implications;
    }

    private synchronized RoleContainer buildRoles()
    {
        if(roles == null)
        {
            roles = new RoleContainer(coralEventHub, coral, this);
        }
        return roles;
    }

    private synchronized PermissionContainer buildPermissions()
    {
        if(permissions == null)
        {
            permissions = new PermissionContainer(coralEventHub, coral, buildRoles());
        }
        return permissions;
    }

    private synchronized ImmutableSet<PermissionAssignment> buildPermissionAssignments()
    {
        if(permissionAssignments == null)
        {
            permissionAssignments = new ImmutableHashSet<PermissionAssignment>(coral.getRegistry().getPermissionAssignments(this));
            coralEventHub.getGlobal().addPermissionAssignmentChangeListener(this, this);
        }
        return permissionAssignments;
    }

    private synchronized ImmutableSet<RoleAssignment> buildRoleAssignments()
    {
        if(roleAssignments == null)
        {
            roleAssignments = new ImmutableHashSet<RoleAssignment>(coral.getRegistry().getRoleAssignments(this));
            coralEventHub.getGlobal().addRoleAssignmentChangeListener(this, this);
        }
        return roleAssignments;
    }
}
