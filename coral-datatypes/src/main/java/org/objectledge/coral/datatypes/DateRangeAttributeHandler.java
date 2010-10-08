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
import org.objectledge.database.DatabaseUtils;

/**
 * Handles persistency of {@link DateRange} objects.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: DateRangeAttributeHandler.java,v 1.9 2005-01-19 07:34:06 rafal Exp $
 */
public class DateRangeAttributeHandler
    extends AttributeHandlerBase<DateRange>
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
    public long create(DateRange value, Connection conn)
        throws SQLException
    {
        long id = getNextId();
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO "+getTable()+
            "(data_key, start_date, end_date) VALUES (?, ?, ?)");
        try
        {
            stmt.setLong(1, id);
            stmt.setTimestamp(2, new java.sql.Timestamp((value).getStart().getTime()));
            stmt.setTimestamp(3, new java.sql.Timestamp((value).getEnd().getTime()));
            stmt.execute();
            (value).clearModified();
            return id;
        }
        finally
        {
            DatabaseUtils.close(stmt);
        }
    }

    /**
     * {@inheritDoc}
     */
    public DateRange retrieve(long id, Connection conn)
        throws EntityDoesNotExistException, SQLException
    {
        Statement stmt = conn.createStatement();
        ResultSet rs = null;
        try
        {
            rs = stmt.executeQuery(
                "SELECT start_date, end_date FROM "+getTable()+" WHERE data_key = "+id
            );
            if(!rs.next())
            {
                throw new EntityDoesNotExistException("Item #"+id+" does not exist in table "
                                                       +getTable());
            }
            return new DateRange(new Date(rs.getTimestamp(1).getTime()), 
                new Date(rs.getTimestamp(2).getTime()));
        }
        finally
        {
            DatabaseUtils.close(rs);
            DatabaseUtils.close(stmt);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void update(long id, DateRange value, Connection conn)
        throws EntityDoesNotExistException, SQLException
    {
        Statement stmt = conn.createStatement();
        PreparedStatement pstmt = conn.prepareStatement("UPDATE "+getTable()+
        " SET start_date = ?, end_date = ? WHERE data_key = ?");
        try
        {
            checkExists(id, stmt);
            pstmt.setTimestamp(1, new java.sql.Timestamp((value).getStart().getTime()));
            pstmt.setTimestamp(2, new java.sql.Timestamp((value).getEnd().getTime()));
            pstmt.setLong(3, id);
            pstmt.execute();
            (value).clearModified();
        }
        finally
        {
            DatabaseUtils.close(stmt);
            DatabaseUtils.close(pstmt);
        }
    }        
    
    /**
     * {@inheritDoc}
     */
    public boolean isModified(DateRange value)
    {
        return (value).isModified();
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
    protected DateRange fromString(String string)
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
    public String toPrintableString(DateRange value)
    {
        checkValue(value);
        return (value).getStart().toString()+
            " :: "+
            (value).getEnd().toString();
    }
}
