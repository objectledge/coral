package org.objectledge.coral.entity;

import java.util.Date;

import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.security.Subject;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.OutputRecord;
import org.objectledge.database.persistence.PersistenceException;

/**
 * Base of {@link Assignment} implementations.
 *
 * @version $Id: AbstractAssignment.java,v 1.3 2004-02-23 09:15:20 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public abstract class AbstractAssignment
    extends AbstractAssociation
    implements Assignment
{
    // Member objects ///////////////////////////////////////////////////////////////////////////

    /** CoralSecurity. */
    protected CoralSecurity coralSecurity;

    /** The grantor */
    protected Subject grantor;
    
    /** Grant time*/
    protected Date grantTime;

    // Initialization ///////////////////////////////////////////////////////////////////////////

    /**
     * Constructs an assignment.
     * 
     * @param security the CoralSecurity subsystem.
     */
    public AbstractAssignment(CoralSecurity security) 
    {
        this.coralSecurity = security;
    }

    /**
     * Constructs an assignment object.
     *
     * @param security the CoralSecurity subsystem.
     * @param grantor the grantor.
     * @param grantTime grant time.
     */
    public AbstractAssignment(CoralSecurity security, Subject grantor, Date grantTime)
    {
        this.grantor = grantor;
        this.grantTime = grantTime;
    }

    // Persistent interface /////////////////////////////////////////////////////////////////////

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
        record.setLong("grantor", grantor.getId());
        record.setDate("grant_time", grantTime);
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
        long grantorId = record.getLong("grantor");
        try
        {
            grantor = coralSecurity.getSubject(grantorId);
        }
        catch(EntityDoesNotExistException e)
        {
            String type = getClass().getName();
            int pos = type.lastIndexOf(".");
            type = type.substring(pos+1);
            throw new PersistenceException("Failed to load "+type, e);
        }
        grantTime = record.getDate("grant_time");
    }

    // Assignment interface /////////////////////////////////////////////////////////////////////
    
    /**
     * Returns the {@link Subject} that created this assignment.
     *
     * @return the {@link Subject} that created this assignment.
     */
    public Subject getGrantedBy()
    {
        return grantor;
    }

    /**
     * Returns the time the assignement was created.
     *
     * @return the time the assignement was created.
     */
    public Date getGrantTime()
    {
        return grantTime;
    }
    
    // implementation specific //////////////////////////////////////////////////////////////////

    /**
     * Sets the {@link Subject} that created this assignment.
     *
     * @param grantor the {@link Subject} that created this assignment.
     */
    public void setGrantedBy(Subject grantor)
    {
        this.grantor = grantor;
    }

    /**
     * Sets the time the assignement was created.
     *
     * @param grantTime the time the assignement was created.
     */
    public void setGrantTime(Date grantTime)
    {
        this.grantTime = grantTime;
    }
}
