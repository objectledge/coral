package org.objectledge.coral.datatypes;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.database.Database;

/**
 * Handles persistency of <code>java.lang.Integer</code> objects.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: IntegerAttributeHandler.java,v 1.1 2004-03-02 09:51:01 pablo Exp $
 */
public class IntegerAttributeHandler
    extends AttributeHandlerBase
{
    /**
     * The constructor.
     * 
     * @param database the database.
     * @param coralStore the store.
     * @param coralSecurity the security.
     * @param coralSchema the scheam.
     * @param attributeClass the attribute class.
     */
    public IntegerAttributeHandler(Database database, CoralStore coralStore,
                                   CoralSecurity coralSecurity, CoralSchema coralSchema,
                                   AttributeClass attributeClass)
    {
        super(database, coralStore, coralSecurity, coralSchema, attributeClass);
    }

    // AttributeHandler interface ////////////////////////////////////////////

    /**
     * Creates a new attribute instance.
     *
     * @param value the value of the attribute.
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform
     *        the operation as a part of a JDBC transaction.
     * @return the identifier of the new attribute.
     * @throws SQLException in case of database problems. The caller metod
     *         should consider rolling back the whole transaction.
     */
    public long create(Object value, Connection conn)
        throws SQLException
    {
        long id = database.getNextId(getTable());
        Statement stmt = conn.createStatement();
        stmt.execute(
            "INSERT INTO "+getTable()+"(data_key, data) VALUES ("+
            id+", "+((Integer)value).intValue()+")"
        );
        return id;
    }

    /**
     * Retrieves an attribute value.
     *
     * @param id the identifier of the attribute.
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform
     *        the operation as a part of a JDBC transaction.
     * @throws SQLException in case of database problems. The caller metod
     *         should consider rolling back the whole transaction.
     */
    public Object retrieve(long id, Connection conn)
        throws EntityDoesNotExistException, SQLException
    {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT data FROM "+getTable()+" WHERE data_key = "+id
        );
        if(!rs.next())
        {
            throw new EntityDoesNotExistException("Item #"+id+" does not exist in table "+getTable());
        }
        return new Integer(rs.getInt(1));
    }

    /**
     * Modifies an existing attribute.
     *
     * @param id the identifier of the attribute.
     * @param value the value of the attribute.
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform
     *        the operation as a part of a JDBC transaction.
     * @throws EntityDoesNotExistException if the attribute with specified id
     *         does not exist. 
     * @throws SQLException in case of database problems. The caller metod
     *         should consider rolling back the whole transaction.
     */
    public void update(long id, Object value, Connection conn)
        throws EntityDoesNotExistException, SQLException
    {
        Statement stmt = conn.createStatement();
        checkExists(id, stmt);
        stmt.execute(
            "UPDATE "+getTable()+" SET data = "+
            ((Integer)value).intValue()+
            " WHERE data_key = "+id
        );
    }
    
    // meta information //////////////////////////////////////////////////////
    
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
        return CONDITION_EQUALITY | CONDITION_COMPARISON;
    }

    /**
     * Retruns <code>true</code> if the {@toExternalString()} is supported for
     * this attribute type.
     *
     * @return Retruns <code>true</code> if the {@toExternalString()} is
     *         supported for this attribute type.
     */
    public boolean supportsExternalString()
    {
        return true;
    }

    // protected /////////////////////////////////////////////////////////////

    /**
     * Converts a string into an attribute object.
     *
     * @param string the string to convert.
     * @return the attribute object, or <code>null</code> if conversion not
     *         supported. 
     */
    protected Object fromString(String string)
    {
        return new Integer(string);
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
        return value.toString();
    }
}
