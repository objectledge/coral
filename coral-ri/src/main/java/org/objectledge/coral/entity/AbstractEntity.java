package org.objectledge.coral.entity;

import java.sql.SQLException;

import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.OutputRecord;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.PersistentObject;

/**
 * A base of all Coral entitity implementations.
 *
 * @version $Id: AbstractEntity.java,v 1.10 2005-02-21 15:48:33 zwierzem Exp $
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
    
    /** The numerical identifier of the entity as a Java object. */
    protected Long idObject;
    
    /** The numerical identifier of the entity as a string. */
    protected String idString;

    /** The name of the entity. */
    protected String name;
    
    /** Precomputed hashcode. */
    protected int hashCode;

    // Initialization ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Constructs an entity.
     *
     * @param persistence the persistence system.
     * @param name the name of the entity.
     */
    public AbstractEntity(Persistence persistence, String name)
    {
        this.persistence = persistence;
        this.id = -1L;
        this.name = name;
        computeHashCode();
    }

    /**
     * Constructs an entity.
     *
     * @param persistence the persistence system.
     */
    public AbstractEntity(Persistence persistence)
    {
        this.persistence = persistence;
        this.id = -1L;
        this.name = null;
        computeHashCode();
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
    public final int hashCode()
    {
        return hashCode;
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
     * <p> This method is overriden to augument debugging. The format of the representation is as 
     * following: 
     * <blockquote>
     *   <code>javaClass name #id @identity</code>
     * </blockquote>
     * Where:
     * <ul>
     *   <li><code>javaClass</code> is the actual implementation class of the object</li>
     *   <li><code>name</code> is the name of the entity as returned by the {@link #getName()} 
     *     method.</li>
     *   <li><code>id</code> is the identifier of the entity as returned by the {@link #getId()}
     *     method.</li> 
     *   <li><code>idenity</code> is the obeject instance's identity hashcode as retured by the
     *     <code>System.getIdentityHashCode(Object)</code> function.</li>
     *  </ul>
     *  </p>
     * 
     * @return a String representation of this object.
     */
    public String toString()
    {
        StringBuilder buff = new StringBuilder();
        buff.append(getClass().getName());
        buff.append(' ');
        buff.append(getName());
        buff.append(" #");
        buff.append(getIdString());
        buff.append(" @");
        buff.append(Integer.toString(System.identityHashCode(this), 16));
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
     * <p>
     * You need to call <code>getData</code> of your superclasses if they are
     * <code>Persistent</code>.
     * </p>
     * 
     * @param record the record to store state into.
     * @throws SQLException
     * @throws SQLException if there is a problem storing field values.
     */
    public void getData(OutputRecord record)
        throws SQLException
    {
        if(id != -1L)
        {
            record.setLong(getKeyColumns()[0], id);
        }
        record.setString("name", name);
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
        id = record.getLong(getKeyColumns()[0]);
        name = record.getString("name");
        computeHashCode();
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
        computeHashCode();
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
     * Returns the numerical identifier of the entity as a Java object.
     * 
     * @return the numerical identifier of the entity as a Java object.
     */
    public Long getIdObject()
    {
        return idObject;
    }

    /**
     * Returns the numerical identifier of the entity as a string.
     * 
     * @return the numerical identifier of the entity as a string.
     */
    public String getIdString()
    {
        return idString;
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
        computeHashCode();
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
    
    // implementation ///////////////////////////////////////////////////////////////////////////
    
    private void computeHashCode()
    {
        idObject = new Long(id);
        idString = idObject.toString();
        hashCode = getClass().hashCode() ^ (int)(id * 0x11111111); 
    }
}
