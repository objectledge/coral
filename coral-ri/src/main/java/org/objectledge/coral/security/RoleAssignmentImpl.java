package org.objectledge.coral.security;

import java.util.Date;

import org.objectledge.coral.CoralCore;
import org.objectledge.coral.entity.AbstractAssignment;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.OutputRecord;
import org.objectledge.database.persistence.PersistenceException;

/**
 * An implementation of {@link org.objectledge.coral.security.RoleAssignment} interface.
 *
 * @version $Id: RoleAssignmentImpl.java,v 1.9 2005-02-08 20:34:45 rafal Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class RoleAssignmentImpl
    extends AbstractAssignment
    implements RoleAssignment
{
    // Instance variables ///////////////////////////////////////////////////////////////////////

    /** The subject. */
    protected Subject subject;
    
    /** The role. */
    protected Role role;
    
    /** grantingAllowed flag. */
    protected boolean grantingAllowed;

    // initialization ///////////////////////////////////////////////////////////////////////////

    /**
     * Creates a {@link RoleAssignmentImpl}.
     *
     * @param coral the component hub.
     */
    public RoleAssignmentImpl(CoralCore coral)
    {
        super(coral);
    }
    
    /**
     * Creates a {@link RoleAssignmentImpl}.
     * 
     * @param coral the component hub.
     *
     * @param grantor the subject that created this assignment.
     * @param subject the involved subject.
     * @param role the involved role.
     * @param grantingAllowed is delagating of role allowed.
     */
    public RoleAssignmentImpl(CoralCore coral, 
        Subject grantor, Subject subject, Role role, boolean grantingAllowed)
    {
        super(coral, grantor, new Date());
        this.subject = subject;
        this.role = role;
        this.grantingAllowed = grantingAllowed;
    }

    // Hashing & equality ////////////////////////////////////////////////////

    /**
     * Returs the hashcode for this entity.
     *
     * @return the hashcode of the object.
     */
    public int hashCode()
    {
        return hashCode(subject.getId()) ^ hashCode(role.getId());
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
            return subject.equals(((RoleAssignmentImpl)other).getSubject()) &&
                role.equals(((RoleAssignmentImpl)other).getRole());
        }
        return false;
    }

    // Persistent interface /////////////////////////////////////////////////

    /** The key columns */
    private static final String[] KEY_COLUMNS = { "subject_id", "role_id" };

    /**
     * Returns the name of the table this type is mapped to.
     *
     * @return the name of the table.
     */
    public String getTable()
    {
        return "coral_role_assignment";
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
        record.setLong("subject_id", subject.getId());
        record.setLong("role_id", role.getId());
        record.setBoolean("granting_allowed", grantingAllowed);
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
        long roleId = record.getLong("role_id");
        try
        {
            this.role = coral.getSecurity().getRole(roleId);
        }
        catch(EntityDoesNotExistException e)
        {
            throw new PersistenceException("Failed to load RoleAssignment", e);
        }
        long subjectId = record.getLong("subject_id");
        try
        {
            subject = coral.getSecurity().getSubject(subjectId);
        }
        catch(EntityDoesNotExistException e)
        {
            throw new PersistenceException("Failed to load RoleAssignment", e);
        }
        grantingAllowed = record.getBoolean("granting_allowed");
    }
    
    // Role assignment interface /////////////////////////////////////////////
    
    /**
     * Returns the subject involved in this role assignment.
     *
     * @return the subject involved in this role assignment.
     */
    public Subject getSubject()
    {
        return subject;
    }

    /**
     * Returns the role involved this role assigment.
     *
     * @return the role involved this role assigment.
     */
    public Role getRole()
    {
        return role;
    }

    /**
     * Returns <code>true</code> if the subject is allowed to grant the role to
     * other subjects.
     *
     * @return <code>true</code> if the subject is allowed to grant the role to
     * other subjects.
     */
    public boolean isGrantingAllowed()
    {
        return grantingAllowed;
    }

    // implementation specific ///////////////////////////////////////////////
    
    // no setters here.
}
