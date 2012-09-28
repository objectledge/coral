package org.objectledge.coral.security;

import java.sql.SQLException;

import org.objectledge.coral.CoralCore;
import org.objectledge.coral.entity.AbstractAssociation;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.OutputRecord;

/**
 * Represents an association between a {@link org.objectledge.coral.security.Permission} and a 
 * {@link org.objectledge.coral.schema.ResourceClass}. 
 *
 * @version $Id: PermissionAssociationImpl.java,v 1.8 2005-02-08 20:34:45 rafal Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class PermissionAssociationImpl
    extends AbstractAssociation
    implements PermissionAssociation
{
    // Instance variables ///////////////////////////////////////////////////////////////////////

    /** The component hub. */
    private CoralCore coral;

    /** The {@link org.objectledge.coral.schema.ResourceClass}. */
    private ResourceClass resourceClass;

    /** The {@link org.objectledge.coral.security.Permission}. */
    private Permission permission;

    // Initialization ///////////////////////////////////////////////////////////////////////////

    /**
     * Contstructs a PermissionAssociationImpl.
     *
     * @param coral the component hub.
     */
    public PermissionAssociationImpl(CoralCore coral)
    {
        this.coral= coral;
    }
    
    /**
     * Contstructs a PermissionAssociationImpl.
     *
     * @param coral the component hub.
     *
     * @param resourceClass the involved {@link org.objectledge.coral.schema.ResourceClass}.
     * @param permission the involved {@link org.objectledge.coral.security.Permission}.
     */
    public PermissionAssociationImpl(CoralCore coral,
         ResourceClass resourceClass, Permission permission)
    {
        this.coral = coral;
        this.resourceClass = resourceClass;
        this.permission = permission;
    }
    
    // Hashing & equality ///////////////////////////////////////////////////////////////////////

    /**
     * Returs the hashcode for this entity.
     *
     * @return the hashcode of the object.
     */
    public int hashCode()
    {
        return hashCode(resourceClass.getId()) ^ hashCode(permission.getId());
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
            return resourceClass.equals(((PermissionAssociationImpl)other).getResourceClass()) &&
                permission.equals(((PermissionAssociationImpl)other).getPermission());
        }
        return false;
    }

    // Persistent interface /////////////////////////////////////////////////////////////////////

    /** The key columns */
    private static final String[] KEY_COLUMNS = { "resource_class_id", "permission_id" };    

    /**
     * Returns the name of the table this type is mapped to.
     *
     * @return the name of the table.
     */
    public String getTable()
    {
        return "coral_permission_association";
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
        record.setLong("resource_class_id", resourceClass.getId());
        record.setLong("permission_id", permission.getId());
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
        long resourceClassId = record.getLong("resource_class_id");
        try
        {
            resourceClass = coral.getSchema().getResourceClass(resourceClassId);
        }
        catch(EntityDoesNotExistException e)
        {
            throw new SQLException("Failed to load PermissionAssociation", e);
        }
        long permissionId = record.getLong("permission_id");
        try
        {
            permission = coral.getSecurity().getPermission(permissionId);
        }
        catch(EntityDoesNotExistException e)
        {
            throw new SQLException("Failed to load PermissionAssociation", e);
        }
    }

    // PermissionAssociation interface //////////////////////////////////////////////////////////

    /**
     * Returns the involved {@link org.objectledge.coral.schema.ResourceClass}.
     *
     * @return the involved {@link org.objectledge.coral.schema.ResourceClass}.
     */
    public ResourceClass getResourceClass()
    {
        return resourceClass;
    }
    

    /**
     * Returns the involved {@link  org.objectledge.coral.security.Permission}.
     *
     * @return the involved {@link  org.objectledge.coral.security.Permission}.
     */
    public Permission getPermission()
    {
        return permission;
    }

    //////////////////////////////////////////////////////////////////////////
    
    public String toString()
    {
        StringBuilder buff = new StringBuilder();
        buff.append(getClass().getName()).append(" ");
        buff.append("Permission #").append(permission.getIdString()).append(" ");
        buff.append("ResourceClass #").append(resourceClass.getIdString()).append(" ");
        buff.append("@").append(Integer.toString(System.identityHashCode(this), 16));
        return buff.toString();
    }    
}
