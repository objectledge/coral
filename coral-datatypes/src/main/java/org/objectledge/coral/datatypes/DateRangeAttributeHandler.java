package org.objectledge.coral.datatypes;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.database.Database;

/**
 * Handles persistency of {@link DateRange} objects.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: DateRangeAttributeHandler.java,v 1.2 2004-03-09 15:40:43 pablo Exp $
 */
public class DateRangeAttributeHandler
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
    public DateRangeAttributeHandler(Database database, CoralStore coralStore,
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
            "INSERT INTO "+getTable()+"(data_key, start_date, end_date) VALUES ("+id+", '"+
            ((DateRange)value).getStart().toString()+"', '"+
            ((DateRange)value).getEnd().toString()+"')"
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
     * @throws EntityDoesNotExistException when failed to retireve data.
     */
    public Object retrieve(long id, Connection conn)
        throws EntityDoesNotExistException, SQLException
    {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT start_date, end_date FROM "+getTable()+" WHERE data_key = "+id
        );
        if(!rs.next())
        {
            throw new EntityDoesNotExistException("Item #"+id+" does not exist in table "
                                                   +getTable());
        }
        return new DateRange(rs.getTimestamp(1), rs.getTimestamp(2));
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
            "UPDATE "+getTable()+" SET start_date = '"+
            ((DateRange)value).getStart().toString()+", end_date = '"+
            ((DateRange)value).getEnd().toString()+
            "' WHERE data_key = "+id
        );
    }
    
    /**
     * Is a composite attribute (wraps the actual values iside an object).
     */
    public boolean isComposite()
    {
        return true;
    }
    
    // protected /////////////////////////////////////////////////////////////

    /**
     * Converts a string into an attribute object.
     *
     * <p>The string should be composed of two date represtntations in format
     * described in <code>java.util.Date.parse(String)</code> method
     * documentation, separated by :: (two consequtive colon characters).</p>
     * 
     * @param string the string to convert.
     * @return the attribute object, or <code>null</code> if conversion not
     *         supported. 
     */
    protected Object fromString(String string)
    {
        int pos = string.indexOf("::");
        if(pos < 0)
        {
            throw new IllegalArgumentException("Two dates spearated with '::' expected");
        }
        Date date1 = new Date(string.substring(0,pos));
        Date date2 = new Date(string.substring(pos+2));
        return new DateRange(date1, date2);
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
        return ((DateRange)value).getStart().toString()+
            " :: "+
            ((DateRange)value).getEnd().toString();
    }
}
