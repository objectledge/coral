package org.objectledge.coral.security;

import org.objectledge.coral.entity.AbstractAssociation;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.OutputRecord;
import org.objectledge.database.persistence.PersistenceException;

/**
 * Represents an association between a {@link Permission} and a {@link
 * ResourceClass}. 
 *
 * @version $Id: PermissionAssociationImpl.java,v 1.1 2004-02-23 09:19:44 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class PermissionAssociationImpl
    extends AbstractAssociation
    implements PermissionAssociation
{
    // Member objects ////////////////////////////////////////////////////////

    /** The CoralSchema. */
    private CoralSchema coralSchema;
    
    /** The CoralSecurity. */
    private CoralSecurity coralSecurity;

    /** The {@link ResourceClass}. */
    private ResourceClass resourceClass;

    /** The {@link Permission}. */
    private Permission permission;

    // Initialization ////////////////////////////////////////////////////////

    /**
     * Contstructs a PermissionAssociationImpl.
     */
    PermissionAssociationImpl(CoralSchema coralSchema, CoralSecurity coralSecurity)
    {
        this.coralSchema = coralSchema;
        this.coralSecurity = coralSecurity;
    }
    
    /**
     * Contstructs a PermissionAssociationImpl.
     *
     * @param resourceClass the involved {@link ResourceClass}.
     * @param permission the involved {@link Permission}.
     */
    PermissionAssociationImpl(CoralSchema coralSchema, CoralSecurity coralSecurity,
                              ResourceClass resourceClass, Permission permission)
    {
        this.coralSchema = coralSchema;
        this.coralSecurity = coralSecurity;
        this.resourceClass = resourceClass;
        this.permission = permission;
    }
    
    // Hashing & equality ////////////////////////////////////////////////////

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

    // Persistent interface //////////////////////////////////////////////////

    /** The key columns */
    private static final String[] KEY_COLUMNS = { "resource_class_id", "permission_id" };    

    /**
     * Returns the name of the table this type is mapped to.
     *
     * @return the name of the table.
     */
    public String getTable()
    {
        return "arl_permission_association";
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
        record.setLong("resource_class_id", resourceClass.getId());
        record.setLong("permission_id", permission.getId());
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
        long resourceClassId = record.getLong("resource_class_id");
        try
        {
            resourceClass = coralSchema.getResourceClass(resourceClassId);
        }
        catch(EntityDoesNotExistException e)
        {
            throw new PersistenceException("Failed to load PermissionAssociation", e);
        }
        long permissionId = record.getLong("permission_id");
        try
        {
            permission = coralSecurity.getPermission(permissionId);
        }
        catch(EntityDoesNotExistException e)
        {
            throw new PersistenceException("Failed to load PermissionAssociation", e);
        }
    }

    // PermissionAssociation interface ///////////////////////////////////////

    /**
     * Returns the involved {@link ResourceClass}.
     *
     * @return the involved {@link ResourceClass}.
     */
    public ResourceClass getResourceClass()
    {
        return resourceClass;
    }
    

    /**
     * Returns the involved {@link Permission}.
     *
     * @return the involved {@link Permission}.
     */
    public Permission getPermission()
    {
        return permission;
    }
}
