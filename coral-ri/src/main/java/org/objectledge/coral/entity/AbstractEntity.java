package org.objectledge.coral.entity;

import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.OutputRecord;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.PersistenceException;
import org.objectledge.database.persistence.PersistentObject;

/**
 * A base of all Coral entitity implementations.
 *
 * @version $Id: AbstractEntity.java,v 1.3 2004-03-09 15:46:47 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public abstract class AbstractEntity
    extends PersistentObject
    implements Entity
{
    // Instance variables ///////////////////////////////////////////////////////////////////////

    /** The persistence system. */
    protected Persistence persistence;

    /** The numerical identifier of the entity. */
    protected long id;
    
    /** The name of the entity. */
    protected String name;

    // Initialization ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Constructs an entity
     *
     * @param persistence the persistence system.
     * @param name the name of the entity.
     */
    public AbstractEntity(Persistence persistence, String name)
    {
        this.persistence = persistence;
        this.id = -1L;
        this.name = name;
    }

    /**
     * Constructs an entity
     *
     * @param persistence the persistence system.
     */
    public AbstractEntity(Persistence persistence)
    {
        this.persistence = persistence;
        this.id = -1L;
        this.name = null;
    }

    // Hashing & equality ///////////////////////////////////////////////////////////////////////

    /**
     * Returs the hashcode for this entity.
     *
     * <p>The hashcode if computed as a XOR of the entity's actual class
     * hashcode and the identifier multiplied by 0x11111111.</p>
     *
     * @return the hashcode of the object.
     */
    public int hashCode()
    {
        return getClass().hashCode() ^ (int)(id * 0x11111111);
    }

    /**
     * Checks if another object represens the same entity.
     *
     * <p>The check involves the actual class of the object and the value of the
     * indetifier.</p>
     *
     * @param obj the other objects.
     * @return <code>true</code> if the other object represents the same entity.
     */
    public boolean equals(Object obj)
    {
        if(obj != null && obj.getClass().equals(getClass()))
        {
            return id == ((Entity)obj).getId();
        }
        return false;
    }    

    /**
     * Returns a String representation of this object.
     *
     * <p> This method is overriden to augument debugging. Not only the
     * semantical hashcode is inculded in the represtentation, but also the
     * object instance's identity hashcode, as returned by
     * <code>System.getIdentityHashCode(Object)</code>. This is useful for
     * debugging data consistency issues. </p>
     * 
     * @return a String representation of this object.
     */
    public String toString()
    {
        StringBuffer buff = new StringBuffer();
        buff.append(getClass().getName());
        buff.append('@');
        buff.append(Integer.toString(System.identityHashCode(this), 16));
        buff.append(':');
        buff.append(Integer.toString(hashCode(), 16));
        return buff.toString();
    }

    // Persistent interface /////////////////////////////////////////////////////////////////////

    /** 
     * Returns the name of the primary key column.
     *
     * @return the name of the primary key column.
     */
    public abstract String[] getKeyColumns();

    /**
     * Stores the fields of the object into the specified record.
     *
     * <p>You need to call <code>getData</code> of your superclasses if they
     * are <code>Persistent</code>.</p>
     *
     * @param record the record to store state into.
     * @throws PersistenceException
     * @throws PersistenceException if there is a problem storing field values.
     */
    public void getData(OutputRecord record)
        throws PersistenceException
    {
        record.setLong(getKeyColumns()[0], id);
        record.setString("name", name);
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
        id = record.getLong(getKeyColumns()[0]);
        name = record.getString("name");
    }

    /**
     * Sets the 'saved' flag for the object.
     *
     * <p>The id generation will take place only for objects that declare a
     * single column primary key. Othre objects will receive a <code>-1</code>
     * as the <code>id</code> parameter. After this call is made on an object,
     * subsequent calls to {@link #getSaved()} on the same object should
     * return true.</p> 
     *
     * @param id The generated value of the primary key.
     */
    public void setSaved(long id)
    {
        this.saved = true;
        this.id = id;
    }

    // Entity inteface //////////////////////////////////////////////////////////////////////////
    
    /**
     * Returns the numerical identifier of the entity.
     * 
     * @return the numerical identifier of the entity.
     */
    public long getId()
    {
        return id;
    }
    
    /**
     * Returns the name of the entity.
     *
     * @return the name of the entity.
     */
    public String getName()
    {
        return name;
    }
    
    // Package private setter methods ///////////////////////////////////////////////////////////

    /**
     * Sets the numerical identifier of the entity.
     * 
     * @param id the numerical identifier of the entity.
     */
    void setId(long id)
    {
        this.id = id;
    }
    
    /**
     * Sets the name of the entity.
     *
     * @param name the name of the entity.
     */
    void setName(String name)
    {
        this.name = name;
    }
}
