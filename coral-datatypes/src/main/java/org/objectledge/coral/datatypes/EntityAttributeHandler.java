package org.objectledge.coral.datatypes;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.BitSet;
import java.util.Comparator;

import org.objectledge.coral.entity.Entity;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.database.Database;

/**
 * Common base class for various entity attribute handlers.
 */
public abstract class EntityAttributeHandler extends AttributeHandlerBase
{
    /** cached identifiers. */
    protected long[] cache;

    /** information about defined items. */
    protected BitSet defined;    
    
    /**
     * The base constructor.
     * 
     * @param database the database.
     * @param coralStore the store.
     * @param coralSecurity the security.
     * @param coralSchema the scheam.
     * @param attributeClass the attribute class.
     */
    public EntityAttributeHandler(Database database, CoralStore coralStore,
                                 CoralSecurity coralSecurity, CoralSchema coralSchema,
                                 AttributeClass attributeClass)
    {
        super(database, coralStore, coralSecurity, coralSchema, attributeClass);
    }
    
    /** 
     * Instantiate an entity.
     * 
     * @param id the identifier of the entity.
     * @return the entity instance.
     * 
     * @throws EntityDoesNotExistException if the identifier does not designate an existing entity.
     */
    protected abstract Entity instantiate(long id)
        throws EntityDoesNotExistException;

    /** 
     * Instantiate an entity.
     * 
     * @param name the name of the entity.
     * @return the entity instance.
     */
    protected abstract Entity[] instantiate(String name);

    /**
     * Preloads all defined attribute values from the database.
     * 
     * @param conn the database conneciton.
     * @throws SQLException if a database exception occurs.
     */
    protected void preload(Connection conn) throws SQLException
    {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT max(data_key) from " + getTable());
        rs.next();
        int count = rs.getInt(1);
        cache = new long[count + 1];
        defined = new BitSet(count + 1);
        rs = stmt.executeQuery("SELECT data_key, ref from " + getTable());
        while(rs.next())
        {
            cache[rs.getInt(1)] = rs.getLong(2);
            defined.set(rs.getInt(1));
        }
    }

    /**
     * Removes an existing attribute.
     *
     * <p>This method is implemented here because it is identical for all
     * generic attributes.</p>
     *
     * @param id the identifier of the attribute.
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform
     *        the operation as a part of a JDBC transaction.
     * @throws SQLException in case of database problems. The caller metod
     *         should consider rolling back the whole transaction.
     * @throws EntityDoesNotExistException if the attribute with specified id
     *         does not exist. 
     */
    public void delete(long id, Connection conn) throws EntityDoesNotExistException, SQLException
    {
        super.delete(id, conn);
        if(cache != null && id < cache.length)
        {
            defined.clear((int)id);
        }
    }

    /**
     * Creates a new attribute instance.
     * 
     * @param value
     *            the value of the attribute.
     * @param conn
     *            the JDBC <code>Connection</code> to use. Needed to perform the operation as a
     *            part of a JDBC transaction.
     * @return the identifier of the new attribute.
     * @throws SQLException
     *             in case of database problems. The caller metod should consider rolling back the
     *             whole transaction.
     */
    public long create(Object value, Connection conn) throws SQLException
    {
        long id = database.getNextId(getTable());
        Statement stmt = conn.createStatement();
        stmt.execute("INSERT INTO " + getTable() + "(data_key, ref) VALUES (" + id + ", "
            + ((Entity)value).getIdString() + ")");
        return id;
    }

    /**
     * Retrieves an attribute value.
     * 
     * @param id the identifier of the attribute.
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform the operation as a
     * part of a JDBC transaction.
     * @return the value of the attribute.
     * @throws EntityDoesNotExistException if the requested entity does not exist.
     * @throws SQLException if a database exception occurs.
     */
    public Object retrieve(long id, Connection conn) throws EntityDoesNotExistException,
        SQLException
    {
        if(cache != null && id < cache.length)
        {
            if(defined.get((int)id))
            {
                return instantiate(cache[(int)id]);
            }
        }
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT ref FROM " + getTable() + " WHERE data_key = "
            + id);
        if(!rs.next())
        {
            throw new EntityDoesNotExistException("Item #" + id + " does not exist in table "
                + getTable());
        }
        if(cache != null && id < cache.length)
        {
            cache[(int)id] = rs.getLong(1);
            defined.set((int)id);
        }
        return instantiate(rs.getLong(1));
    }

    /**
     * Modifies an existing attribute.
     * 
     * @param id
     *            the identifier of the attribute.
     * @param value
     *            the value of the attribute.
     * @param conn
     *            the JDBC <code>Connection</code> to use. Needed to perform the operation as a
     *            part of a JDBC transaction.
     * @throws EntityDoesNotExistException
     *             if the attribute with specified id does not exist.
     * @throws SQLException
     *             in case of database problems. The caller metod should consider rolling back the
     *             whole transaction.
     */
    public void update(long id, Object value, Connection conn) throws EntityDoesNotExistException,
        SQLException
    {
        if(cache != null && id < cache.length)
        {
            cache[(int)id] = ((Entity)value).getId();
            defined.set((int)id);
        }
        Statement stmt = conn.createStatement();
        checkExists(id, stmt);
        stmt.execute("UPDATE " + getTable() + " SET ref = " + ((Entity)value).getIdString()
            + " WHERE data_key = " + id);
    }

    /**
     * Provides information about comparison operations supported by the
     * attribute type.
     *
     * <p>The returned value is a bitwise sum of the CONDITION_*
     * constants.</p>
     * @return information about comparison operations supported by the
     * attribute type.
     */
    public int getSupportedConditions()
    {
        return CONDITION_EQUALITY;
    }

    /**
     * Converts a string into an attribute object.
     *
     * <p>If the string starts with a number, it is considered to be resource
     * id. Otherwise it is considered resource name. If the name is ambigous,
     * an exception will be thrown.</p>
     * 
     * @param string the string to convert.
     * @return the attribute object, or <code>null</code> if conversion not
     *         supported. 
     */
    protected Object fromString(String string)
    {
        if(Character.isDigit(string.charAt(0)))
        {
            long id = Long.parseLong(string);
            try
            {
                return instantiate(id);
            }
            catch(EntityDoesNotExistException e)
            {
                throw new IllegalArgumentException(attributeClass.getName()+" #"+id+" not found");
            }
        }
        else
        {
            Entity[] res = instantiate(string);
            if(res.length == 0)
            {
                throw new IllegalArgumentException(attributeClass.getName()+" '"+
                    string+"' not found");
            }
            if(res.length > 1)
            {
                throw new IllegalArgumentException(attributeClass.getName()+" name '"+
                    string+"' is ambigous");
            }
            return res[0];
        }
    }

    /**
     * Converts an attribute value into a human readable string.
     *
     * @param value the value to convert.
     * @return a human readable string.
     */
    public String toPrintableString(Object value)
    {
        checkValue(value);
        return ((Entity)value).getName();
    }

    /**
     * Converts an attribute value into a string representation suitable for
     * using in queries against the underlying data store, like a relational
     * database.
     *
     * @param value the value to convert.
     * @return a string representation suitable for using in queries agains
     *         the underlying data store.
     */
    public String toExternalString(Object value)
    {
        checkValue(value);
        return ((Entity)value).getIdString();
    }
    
    /**
     * Return a new Comparator, or null if not supported.
     * 
     * @return a comparator for the attribute values.
     */
    protected Comparator createComparator()
    {
         return new EntityAttributeHandler.EntityComparator();   
    }

    /**
     * Compares ARL entities by name.
     */
    public static class EntityComparator
        implements Comparator
    {
        /**
         * {@inheritDoc}
         */
        public int compare(Object o1, Object o2)
        {
            Entity e1 = (Entity)o1;
            Entity e2 = (Entity)o2;
            return e1.getName().compareTo(e2.getName());
        }
    }
}