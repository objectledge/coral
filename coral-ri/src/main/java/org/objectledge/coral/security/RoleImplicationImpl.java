package org.objectledge.coral.security;

import org.objectledge.coral.entity.AbstractAssociation;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.OutputRecord;
import org.objectledge.database.persistence.PersistenceException;

/**
 * Represents implication relationship between two roles.
 *
 * @version $Id: RoleImplicationImpl.java,v 1.4 2004-03-05 10:17:00 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class RoleImplicationImpl
    extends AbstractAssociation
    implements RoleImplication
{
    // Instance variables ///////////////////////////////////////////////////////////////////////

    /** The CoralSecurity. */
    private CoralSecurity coralSecurity;
    
    /** The super role. */
    private Role superRole;
    
    /** The sub role. */
    private Role subRole;
    
    // Initialization ///////////////////////////////////////////////////////////////////////////

    /**
     * Constructs a {@link RoleImplicationImpl}.
     * 
     * @param coralSecurity the CoralSecurity.
     */
    public RoleImplicationImpl(CoralSecurity coralSecurity)
    {
        this.coralSecurity = coralSecurity;
    }
    
    /**
     * Constructs a {@link RoleImplicationImpl}.
     * 
     * @param coralSecurity the CoralSecurity.
     *
     * @param superRole the implicating/containing role.
     * @param subRole the implied/contained role.
     */
    public RoleImplicationImpl(CoralSecurity coralSecurity,
        Role superRole, Role subRole)
    {
        this.coralSecurity = coralSecurity;
        this.superRole = superRole;
        this.subRole = subRole;
    }

    // Hashing & equality ///////////////////////////////////////////////////////////////////////

    /**
     * Returs the hashcode for this entity.
     *
     * @return the hashcode of the object.
     */
    public int hashCode()
    {
        return hashCode(superRole.getId()) ^ hashCode(subRole.getId());
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
            return superRole.equals(((RoleImplicationImpl)other).getSuperRole()) &&
                subRole.equals(((RoleImplicationImpl)other).getSubRole());
        }
        return false;
    }

    // Persistent interface /////////////////////////////////////////////////////////////////////

    /**
     * Returns the name of the table this type is mapped to.
     *
     * @return the name of the table.
     */
    public String getTable()
    {
        return "arl_role_implication";
    }

    /** The key columns */
    private static final String[] KEY_COLUMNS = { "super_role", "sub_role" };
    
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
        record.setLong("super_role", superRole.getId());
        record.setLong("sub_role", subRole.getId());
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
        long superRoleId = record.getLong("super_role");
        try
        {
            this.superRole = coralSecurity.getRole(superRoleId);
        }
        catch(EntityDoesNotExistException e)
        {
            throw new PersistenceException("Failed to load RoleImplication", e);
        }
        long subRoleId = record.getLong("sub_role");
        try
        {
            this.subRole = coralSecurity.getRole(subRoleId);
        }
        catch(EntityDoesNotExistException e)
        {
            throw new PersistenceException("Failed to load RoleImplication", e);
        }
    }

    // RoleImplication interface ////////////////////////////////////////////////////////////////

    /**
     * Returns the implicating/containing role.
     *
     * @return the implicating/containing role.
     */
    public Role getSuperRole()
    {
        return superRole;
    }
    
    /**
     * Returns the implied/contained role.
     *
     * @return the implied/contained role.
     */
    public Role getSubRole()
    {
        return subRole;
    }
}
