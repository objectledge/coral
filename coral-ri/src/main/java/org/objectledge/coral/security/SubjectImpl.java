package org.objectledge.coral.security;

import java.security.Principal;
import java.sql.SQLException;
import java.util.Set;

import org.objectledge.collections.ImmutableHashSet;
import org.objectledge.collections.ImmutableSet;
import org.objectledge.coral.BackendException;
import org.objectledge.coral.CoralCore;
import org.objectledge.coral.entity.AbstractEntity;
import org.objectledge.coral.event.CoralEventHub;
import org.objectledge.coral.event.RoleAssignmentChangeListener;
import org.objectledge.coral.event.SubjectChangeListener;
import org.objectledge.coral.store.Resource;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.OutputRecord;
import org.objectledge.database.persistence.Persistence;

/**
 * A representation of an user or application accessing the resource store.
 *
 * @version $Id: SubjectImpl.java,v 1.13 2005-05-05 08:27:08 rafal Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class SubjectImpl
    extends AbstractEntity
    implements Subject, RoleAssignmentChangeListener, SubjectChangeListener
{
    // Instance variables ///////////////////////////////////////////////////////////////////////

    /** The event hub. */
    private CoralEventHub coralEventHub;
    
    /** The component hub. */
    private CoralCore coral;
    
    /** The role container. */
    private RoleContainer roles = null;

    /** The role assignments for this subject. */
    private ImmutableSet<RoleAssignment> roleAssignments = null;

    // Initialization ///////////////////////////////////////////////////////////////////////////

    /**
     * Constructs a {@link SubjectImpl}.
     * 
     * @param persistence the Persistence subsystem.
     * @param coralEventHub the event hub.
     * @param coral the component hub.
     */
    public SubjectImpl(Persistence persistence, CoralEventHub coralEventHub, 
        CoralCore coral)
    {
        super(persistence);
        this.coralEventHub = coralEventHub;
        this.coral = coral;
    }
    
    /**
     * Constructs a {@link SubjectImpl}.
     *
     * @param persistence the Persistence subsystem.
     * @param coralEventHub the CoralEventHub.
     * @param coral the component hub.
     * 
     * @param name the name of the subject.
     */
    public SubjectImpl(Persistence persistence, CoralEventHub coralEventHub, CoralCore coral,  
        String name)
    {
        super(persistence, name);
        this.coralEventHub = coralEventHub;
        this.coral = coral;
        coralEventHub.getInbound().addSubjectChangeListener(this, this);
    }

    // Persistent interface ////////////////////////////////////////////////////////////////////

    /** The key columns */
    private static final String[] KEY_COLUMNS = { "subject_id" };
    
    /**
     * Returns the name of the table this type is mapped to.
     *
     * @return the name of the table.
     */
    public String getTable()
    {
        return "coral_subject";
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
        coralEventHub.getInbound().addSubjectChangeListener(this, this);
    }

    // SubjectChangeListener interface //////////////////////////////////////////////////////////

    /**
     * Called when <code>Subject</code>'s data change.
     *
     * @param subject the subject that changed.
     */
    public void subjectChanged(Subject subject)
    {
        if(subject.equals(this))
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

    // Subject inteface /////////////////////////////////////////////////////////////////////////

    /**
     * Returns a Principal object that can be used to uniquely identify this subject.
     * 
     * @return a Principal object.
     */
    public Principal getPrincipal()
    {
        return new Principal()
            {
                public String getName()
                {
                    return SubjectImpl.this.getName();
                }

                public int hashCode()
                {
                    return SubjectImpl.this.getName().hashCode();
                }

                public boolean equals(Object that)
                {
                    if(that instanceof Principal)
                    {
                        return ((Principal)that).getName().equals(SubjectImpl.this.getName());
                    }
                    return false;
                }
            };
    }
    

    /**
     * Returns the role assignments made for this <code>Subject</code>.
     *
     * @return the role assignments made for this <code>Subject</code>.
     */
    public RoleAssignment[] getRoleAssignments()
    {
        ImmutableSet<RoleAssignment> snapshot = buildRoleAssignments();
        RoleAssignment[] result = new RoleAssignment[snapshot.size()];
        snapshot.toArray(result);
        return result;
    }

    /**
     * Returns the roles assigned to this <code>Subject</code>.
     *
     * @return the roles assigned to this <code>Subject</code>.
     */
    public Role[] getRoles()
    {
        Set<Role> snapshot = buildRoles().getMatchingRoles();
        Role[] result = new Role[snapshot.size()];
        snapshot.toArray(result);
        return result;
    }

    /**
     * Returns <code>true</code> if the <code>Subject</code> has a specific
     * role. 
     *
     * @param role the role to check.
     * @return <code>true</code> if the <code>Subject</code> has a specific
     * role. 
     */
    public boolean hasRole(Role role)
    {
        return buildRoles().isMatchingRole(role);
    }   

    /**
     * Returns the <code>Permission</code>s that the <code>Subject</code> has
     * on a specific <code>Resurce</code>.
     *
     * @param resource the resource.
     * @return the <code>Permission</code>s that the <code>Subject</code> has
     * over a specific <code>Resurce</code>.
     */
    public Permission[] getPermissions(Resource resource)
    {
        return buildRoles().getPermissions().getPermissions(resource);
    }

    /**
     * Returns <code>true</code> if the <code>Subject</code> has a specific
     * <code>Permission</code> on a <code>Resource</code>.
     *
     * @param resource the resource.
     * @param permission the permission. 
     * @return <code>true</code> if the <code>Subject</code> has a specific
     * <code>Permission</code> on a <code>Resource</code>.
     */
    public boolean hasPermission(Resource resource, Permission permission)
    {
        return buildRoles().getPermissions().hasPermission(resource, permission);
    }

    /**
     * Returns all Resources that are owned by this Subject.
     *
     * <p>This method takes Resource parent-child relationships into
     * consideration, so it can be <em>extremely</em> time and memory
     * consuming.</p>
     *
     * @return all Resources that are owned by this Subject.
     */
    public Resource[] getOwnedResources()
    {
        return coral.getRegistry().getOwnedResources(this);
    }
    
    /**
     * Returns all Resources that were created by this Subject.
     *
     * <p>This method may be very time and memory consuming.</p>
     *
     * @return all Resources that were created by this Subject.
     */
    public Resource[] getCreatedResources()
    {
        return coral.getRegistry().getCreatedResources(this);
    }
    
    /**
     * Returns all Role assignments performed by this Subject.
     *
     * @return all Role assignments performed by this Subject.
     */
    public RoleAssignment[] getGrantedRoleAssginments()
    {
        return coral.getRegistry().getGrantedRoleAssignments(this);
    }

    /**
     * Returns all Permission assignments performed by this Subject.
     *
     * @return all Permission assignments performed by this Subject.
     */
    public PermissionAssignment[] getGrantedPermissionAssignemnts()
    {
        return coral.getRegistry().getGrantedPermissionAssignments(this);
    }

    // RoleAssignmentChangeListener interface ///////////////////////////////////////////////////

    /**
     * Called when role assignemts on the <code>subject</code> change.
     *
     * @param ra the role assignment.
     * @param added <code>true</code> if the role assignment was added,
     *        <code>false</code> if removed.
     */
    public synchronized void rolesChanged(RoleAssignment ra, boolean added)
    {
        Subject subject = ra.getSubject();
        if(!subject.equals(this))
        {
            return;
        }
        if(added)
        {
            roleAssignments = roleAssignments.add(ra);
            if(roles != null)
            {
                roles.addRole(ra.getRole());
            }
        }
        else
        {
            roleAssignments = roleAssignments.remove(ra);
            if(roles != null)
            {
                roles.removeRole(ra.getRole());
            }
        }
    }

    // private //////////////////////////////////////////////////////////////////////////////////

    private synchronized ImmutableSet<RoleAssignment> buildRoleAssignments()
    {
        if(roleAssignments == null)
        {
            roleAssignments = new ImmutableHashSet<RoleAssignment>(coral.getRegistry().getRoleAssignments(this));
            coralEventHub.getGlobal().addRoleAssignmentChangeListener(this, this);
        }
        return roleAssignments;
    }

    private synchronized RoleContainer buildRoles()
    {
        if(roles == null)
        {
            roles = new RoleContainer(coralEventHub, coral, buildRoleAssignments());
        }
        return roles;
    }
}
