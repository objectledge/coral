package org.objectledge.coral.datatypes;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
 * @version $Id: DateRangeAttributeHandler.java,v 1.7 2004-12-23 04:10:58 rafal Exp $
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
     * {@inheritDoc}
     */
    public long create(Object value, Connection conn)
        throws SQLException
    {
        long id = database.getNextId(getTable());
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO "+getTable()+
            "(data_key, start_date, end_date) VALUES (?, ?, ?)");
        stmt.setLong(1, id);
        stmt.setTimestamp(2, new java.sql.Timestamp(((DateRange)value).getStart().getTime()));
        stmt.setTimestamp(3, new java.sql.Timestamp(((DateRange)value).getEnd().getTime()));
        stmt.execute();
        stmt.close();
        return id;
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    public void update(long id, Object value, Connection conn)
        throws EntityDoesNotExistException, SQLException
    {
        Statement stmt = conn.createStatement();
        checkExists(id, stmt);
        stmt.close();
        PreparedStatement pstmt = conn.prepareStatement("UPDATE "+getTable()+
            " SET start_date = ?, end_date = ? WHERE data_key = ?");
        pstmt.setTimestamp(1, new java.sql.Timestamp(((DateRange)value).getStart().getTime()));
        pstmt.setTimestamp(2, new java.sql.Timestamp(((DateRange)value).getEnd().getTime()));
        pstmt.setLong(3, id);
        pstmt.execute();
        pstmt.close();
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isComposite()
    {
        return true;
    }
    
    // protected /////////////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    protected Object fromString(String string)
    {
        int pos = string.indexOf("::");
        if(pos < 0)
        {
            throw new IllegalArgumentException("Two dates spearated with '::' expected");
        }
        Date date1 = parseDate(string.substring(0,pos));
        Date date2 = parseDate(string.substring(pos+2));
        return new DateRange(date1, date2);
    }

    /**
     * {@inheritDoc}
     */
    public String toPrintableString(Object value)
    {
        checkValue(value);
        return ((DateRange)value).getStart().toString()+
            " :: "+
            ((DateRange)value).getEnd().toString();
    }
}
