package org.objectledge.coral.security;

import java.util.Date;

import org.objectledge.coral.entity.AbstractAssignment;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.Resource;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.OutputRecord;
import org.objectledge.database.persistence.PersistenceException;

/**
 * Represents assigment of a {@link Permission} on a {@link Resource} to a
 * {@link Role}.
 *
 * <p><code>PermissionAssignment</code> objects are returned from {@link
 * Resource#getPermissionAssignments()} method. They experss security
 * constraints placed upon a specific resource (and optionally it's
 * sub-resources). </p> 
 *
 * @version $Id: PermissionAssignmentImpl.java,v 1.1 2004-02-23 09:15:35 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class PermissionAssignmentImpl
    extends AbstractAssignment
    implements PermissionAssignment
{
    // Instance variables ///////////////////////////////////////////////////////////////////////
    
    /** The CoralStore. */
    private CoralStore coralStore;
    
    /** The {@link Resource}. */
    private Resource resource;
    
    /** The {@link Role}. */
    private Role role;
    
    /** The {@link Permission}. */
    private Permission permission;
    
    /** The <code>inherited</code> flag. */
    private boolean inherited;
    
    // Initialization ///////////////////////////////////////////////////////////////////////////

    /**
     * Constructs a {@link RoleImpl}.
     *
     * @param arl the {@link ResourceServiceImpl}.
     */
    PermissionAssignmentImpl(CoralSecurity coralSecurity, CoralStore coralStore)
    {
        super(coralSecurity);
        this.coralStore = coralStore;
    }

    /**
     * Constructs a {@link RoleImpl}.
     *
     * @param arl the {@link ResourceServiceImpl}.
     */
    PermissionAssignmentImpl(CoralSecurity coralSecurity, CoralStore coralStore,
        Subject grantor, Resource resource, Role role, Permission permission, boolean inherited )
    {
        super(coralSecurity, grantor, new Date());
        this.resource = resource;
        this.role = role;
        this.permission = permission;
        this.inherited = inherited;
    }

    // Hashing & equality ////////////////////////////////////////////////////

    /**
     * Returs the hashcode for this entity.
     *
     * @return the hashcode of the object.
     */
    public int hashCode()
    {
        return hashCode(resource.getId()) ^ 
            hashCode(role.getId()) ^ 
            hashCode(permission.getId());
    }

    /**
     * Checks if another object represens the same entity.
     *
     * @param other the other objects.
     * @return <code>true</code> if the other object represents the same entity.
     */
    public boolean equals(Object other)
    {
        if(other != null && other.getClass().equals(getClass()))
        {
            return resource.equals(((PermissionAssignmentImpl)other).getResource()) &&
                role.equals(((PermissionAssignmentImpl)other).getRole()) &&
                permission.equals(((PermissionAssignmentImpl)other).getPermission());
        }
        return false;
    }

    // Persistent interface //////////////////////////////////////////////////

    /** The key columns. */
    private static final String[] KEY_COLUMNS = { "resource_id", "role_id", 
                                                 "permission_id" };
        /**
     * Returns the name of the table this type is mapped to.
     *
     * @return the name of the table.
     */
    public String getTable()
    {
        return "arl_permission_assignment";
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
        record.setLong("resource_id", resource.getId());
        record.setLong("role_id", role.getId());
        record.setLong("permission_id", permission.getId());
        record.setBoolean("is_inherited", inherited);
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
        try
        {
            long resourceId = record.getLong("resource_id");
            resource = coralStore.getResource(resourceId);
            long roleId = record.getLong("role_id");
            role = coralSecurity.getRole(roleId);
            long permissionId = record.getLong("permission_id");
            permission = coralSecurity.getPermission(permissionId);
        }
        catch(EntityDoesNotExistException e)
        {
            throw new PersistenceException("Failed to load PermissionAssignment", e);
        }
        inherited = record.getBoolean("is_inherited");
    }

    // PermissionAssignment interface ////////////////////////////////////////

    /**
     * Returns the resouce the this security constraint applies to.
     *
     * @return the resouce the this security constraint applies to.
     */
    public Resource getResource()
    {
        return resource;
    }
    
    /**
     * Returns the {@link Role} involved in this security constraint.
     *
     * @return the {@link Role} involved in this security constraint.
     */
    public Role getRole()
    {
        return role;
    }

    /**
     * Returns the {@link Permission} involved in this security constraint.
     *
     * @return the {@link Permission} involved in this security constraint.
     */
    public Permission getPermission()
    {
        return permission;
    }

    /**
     * Returns <code>true</code> if this security constraint is inherited by the
     * sub-resources of the resource.
     * 
     * @return <code>true</code> if the security constraint is inherited.
     */
    public boolean isInherited()
    {
        return inherited;
    }
}
 
