package org.objectledge.coral.entity;

import java.sql.SQLException;

import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.OutputRecord;
import org.objectledge.database.persistence.PersistentObject;

/**
 * Common base class of {@link org.objectledge.coral.entity.Association} implementations.
 *
 */
public abstract class AbstractAssociation
    extends PersistentObject
    implements Association
{
    // Object implementaion /////////////////////////////////////////////////////////////////////

    /**
     * Returs the hashcode for this entity.
     *
     * @return the hashcode of the object.
     */
    public abstract int hashCode();

    /**
     * A hashing function that attempts to achieve uniform bit distribution of
     * values derived from small integer numbers.
     *
     * @param id a long identifier
     * @return a hash value.
     */
    protected int hashCode(long id)
    {
        return (int)(id * 0x11111111);
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
    public abstract boolean equals(Object obj);

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
     * @throws SQLException if there is a problem storing field values.
     */
    public abstract void getData(OutputRecord record)
        throws SQLException;
    
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
    public abstract void setData(InputRecord record)
        throws SQLException;
}
