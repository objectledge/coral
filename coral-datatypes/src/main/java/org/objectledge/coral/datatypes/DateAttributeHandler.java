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
 * Handles persistency of <code>java.util.Date</code> objects.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: DateAttributeHandler.java,v 1.8 2005-01-20 10:48:26 rafal Exp $
 */
public class DateAttributeHandler
    extends AttributeHandlerBase
{
    /** Preloading cache. */
    private Date[] cache;

    /**
     * The constructor.
     * 
     * @param database the database.
     * @param coralStore the store.
     * @param coralSecurity the security.
     * @param coralSchema the scheam.
     * @param attributeClass the attribute class.
     */
    public DateAttributeHandler(Database database, CoralStore coralStore,
                                 CoralSecurity coralSecurity, CoralSchema coralSchema,
                                 AttributeClass attributeClass)
    {
        super(database, coralStore, coralSecurity, coralSchema, attributeClass);
    }
    
    // AttributeHandler interface ////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public void preload(Connection conn)
        throws SQLException
    {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT max(data_key) from "+getTable());
        rs.next();
        int count = rs.getInt(1);
        cache = new Date[count+1];
        rs = stmt.executeQuery("SELECT data_key, data from "+getTable());
        while(rs.next())
        {
            cache[rs.getInt(1)] = new Date(rs.getTimestamp(2).getTime());
        }
    }

    /**
     * {@inheritDoc}
     */
    public long create(Object value, Connection conn)
        throws SQLException
    {
        long id = getNextId();
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO "+getTable()+
            "(data_key, data) VALUES (?, ?)");
        stmt.setLong(1, id);
        stmt.setTimestamp(2, new java.sql.Timestamp(((Date)value).getTime()));
        stmt.execute();
        return id;
    }

    /**
     * {@inheritDoc}
     */
    public Object retrieve(long id, Connection conn)
        throws EntityDoesNotExistException, SQLException
    {
        if(cache != null && id < cache.length)
        {
            Date value = cache[(int)id];
            if(value != null)
            {
                return value;
            }
        }
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT data FROM "+getTable()+" WHERE data_key = "+id
        );
        if(!rs.next())
        {
            throw new EntityDoesNotExistException("Item #"+id+" does not exist in table "+
                getTable());
        }
        Date value = new Date(rs.getTimestamp(1).getTime());
        if(cache != null && id < cache.length)
        {
            cache[(int)id] = value;
        }
        return value;
    }

    /**
     * {@inheritDoc}
     */
    public void update(long id, Object value, Connection conn)
        throws EntityDoesNotExistException, SQLException
    {
        if(cache != null && id < cache.length)
        {
            cache[(int)id] = (Date)value;
        }
        Statement stmt = null;
        PreparedStatement pstmt = null;
        try
        {
            stmt = conn.createStatement();
            checkExists(id, stmt);
            pstmt = conn.prepareStatement("UPDATE "+getTable()+" SET data = ?"+
                " WHERE data_key = ?");
            pstmt.setTimestamp(1, new java.sql.Timestamp(((Date)value).getTime()));
            pstmt.setLong(2, id);
            pstmt.execute();
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
    public void delete(long id, Connection conn)
        throws EntityDoesNotExistException, SQLException
    {
        super.delete(id, conn);
        if(cache != null && id < cache.length)
        {
            cache[(int)id] = null;
        }
    }
    
    // meta information //////////////////////////////////////////////////////
    
    /**
     * {@inheritDoc}
     */
    public int getSupportedConditions()
    {
        return CONDITION_EQUALITY | CONDITION_COMPARISON;
    }

    /**
     * {@inheritDoc}
     */
    public boolean supportsExternalString()
    {
        return true;
    }

    // protected /////////////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    protected Object fromString(String string)
    {
        return parseDate(string);
    }

    /**
     * {@inheritDoc}
     */
    public String toExternalString(Object value)
    {
        checkValue(value);
        return "'"+value.toString()+"'";
    }
}
