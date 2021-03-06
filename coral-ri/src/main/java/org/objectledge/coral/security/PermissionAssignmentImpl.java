package org.objectledge.coral.security;

import java.sql.SQLException;
import java.util.Date;

import org.objectledge.coral.BackendException;
import org.objectledge.coral.CoralCore;
import org.objectledge.coral.entity.AbstractAssignment;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ResourceRef;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.OutputRecord;

/**
 * Represents assigment of a {@link org.objectledge.coral.security.Permission} on a 
 * {@link org.objectledge.coral.store.Resource} to a {@link org.objectledge.coral.security.Role}.
 *
 * <p><code>PermissionAssignment</code> objects are returned from {@link
 * org.objectledge.coral.store.Resource#getPermissionAssignments()} method. They experss security
 * constraints placed upon a specific resource (and optionally it's sub-resources). </p> 
 *
 * @version $Id: PermissionAssignmentImpl.java,v 1.11 2008-01-08 21:18:56 rafal Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class PermissionAssignmentImpl
    extends AbstractAssignment
    implements PermissionAssignment
{
    // Instance variables ///////////////////////////////////////////////////////////////////////
    
    /** The component hub. */
    private CoralCore coral;
        
    /** The {@link org.objectledge.coral.store.Resource}. */
    private ResourceRef resource;
    
    /** The {@link org.objectledge.coral.security.Role}. */
    private Role role;
    
    /** The {@link org.objectledge.coral.security.Permission}. */
    private Permission permission;
    
    /** The <code>inherited</code> flag. */
    private boolean inherited;
    
    // Initialization ///////////////////////////////////////////////////////////////////////////

    /**
     * Constructs a {@link PermissionAssignmentImpl}.
     *
     * @param coral the component hub.
     */
    public PermissionAssignmentImpl(CoralCore coral)
    {
        super(coral);
        this.coral = coral;
    }

    /**
     * Constructs a {@link PermissionAssignmentImpl}.
     *
     * @param coral the component hub.
     * 
     * @param grantor the grantor of the permission.
     * @param resource the resource.
     * @param role the role.
     * @param permission the permission.
     * @param inherited <code>true</code> if the permission is effective on child resources too.
     */
    public PermissionAssignmentImpl(CoralCore coral,
        Subject grantor, Resource resource, Role role, Permission permission, boolean inherited )
    {
        super(coral, grantor, new Date());
        this.coral = coral;
        this.resource = new ResourceRef(resource, coral);
        this.role = role;
        this.permission = permission;
        this.inherited = inherited;
    }

    // Hashing & equality ///////////////////////////////////////////////////////////////////////

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
            return getResource().equals(((PermissionAssignmentImpl)other).getResource()) &&
                role.equals(((PermissionAssignmentImpl)other).getRole()) &&
                permission.equals(((PermissionAssignmentImpl)other).getPermission());
        }
        return false;
    }

    // Persistent interface /////////////////////////////////////////////////////////////////////

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
        return "coral_permission_assignment";
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
     * <p>
     * You need to call <code>getData</code> of your superclasses if they are
     * <code>Persistent</code>.
     * </p>
     * 
     * @param record the record to store state into.
     * @throws SQLException if there is a problem storing field values.
     */
    public void getData(OutputRecord record)
        throws SQLException
    {
        super.getData(record);
        record.setLong("resource_id", resource.getId());
        record.setLong("role_id", role.getId());
        record.setLong("permission_id", permission.getId());
        record.setBoolean("is_inherited", inherited);
    }

    /**
     * Loads the fields of the object from the specified record.
     * <p>
     * You need to call <code>setData</code> of your superclasses if they are
     * <code>Persistent</code>.
     * </p>
     * 
     * @param record the record to read state from.
     * @throws SQLException if there is a problem loading field values.
     */
    public void setData(InputRecord record)
        throws SQLException
    {
        super.setData(record);
        try
        {
            long resourceId = record.getLong("resource_id");
            resource = new ResourceRef(resourceId, coral);
            long roleId = record.getLong("role_id");
            role = coral.getSecurity().getRole(roleId);
            long permissionId = record.getLong("permission_id");
            permission = coral.getSecurity().getPermission(permissionId);
        }
        catch(EntityDoesNotExistException e)
        {
            throw new SQLException("Failed to load PermissionAssignment", e);
        }
        inherited = record.getBoolean("is_inherited");
    }

    // PermissionAssignment interface ///////////////////////////////////////////////////////////

    /**
     * Returns the resouce the this security constraint applies to.
     *
     * @return the resouce the this security constraint applies to.
     */
    public Resource getResource()
    {
        try
        {
            return resource.get();
        }
        catch(EntityDoesNotExistException e)
        {
            throw new BackendException("in-memory data inconsistency", e);
        }
    }
    
    /**
     * Returns the {@link org.objectledge.coral.security.Role} involved in this security constraint.
     *
     * @return the {@link org.objectledge.coral.security.Role} involved in this security constraint.
     */
    public Role getRole()
    {
        return role;
    }

    /**
     * Returns the {@link org.objectledge.coral.security.Permission} involved in this security 
     * constraint.
     *
     * @return the {@link org.objectledge.coral.security.Permission} involved in this security 
     * constraint.
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

    //////////////////////////////////////////////////////////////////////////
    
    public String toString()
    {
        StringBuilder buff = new StringBuilder();
        buff.append(getClass().getName()).append(" ");
        buff.append("Role #").append(role.getIdString()).append(" ");
        buff.append("Resource #").append(resource.getId()).append(" ");
        buff.append("Permission #").append(permission.getIdString()).append(" ");
        buff.append(inherited ? "inherited" : "not inherited").append(" ");
        buff.append("@").append(Integer.toString(System.identityHashCode(this), 16));
        return buff.toString();
    } 
}
