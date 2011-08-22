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
import org.objectledge.database.DatabaseUtils;

/**
 * Handles persistency of <code>java.lang.String</code> objects that contain
 * at most 255 characters.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: StringAttributeHandler.java,v 1.5 2005-01-20 10:48:26 rafal Exp $
 */
public class StringAttributeHandler
    extends AttributeHandlerBase<String>
{
    /** preloading cache. */
    private String[] cache;

    /**
     * The constructor.
     * 
     * @param database the database.
     * @param coralStore the store.
     * @param coralSecurity the security.
     * @param coralSchema the scheam.
     * @param attributeClass the attribute class.
     */
    public StringAttributeHandler(Database database, CoralStore coralStore,
                                  CoralSecurity coralSecurity, CoralSchema coralSchema,
                                  AttributeClass attributeClass)
    {
        super(database, coralStore, coralSecurity, coralSchema, attributeClass);
    }
    /////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * {@inheritDoc}
     */
    public void preload(Connection conn)
        throws SQLException
    {
        Statement stmt = conn.createStatement();
        ResultSet rs = null;
        try
        {
            rs = stmt.executeQuery("SELECT max(data_key) from "+getTable());
            rs.next();
            int count = rs.getInt(1);
            cache = new String[count+1];
            rs.close();
            rs = stmt.executeQuery("SELECT data_key, data from "+getTable());
            while(rs.next())
            {
                cache[rs.getInt(1)] = unescape(rs.getString(2));
            }            
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
    public long create(String value, Connection conn)
        throws SQLException
    {
        String str = value;
        if(str.length() > 255)
        {
            throw new IllegalArgumentException("maximum lenght of string attributes "+
                                               "is 255 characters. Use text attributes "+
                                               "wherever greater capacity is desired");
        }

        long id = getNextId();
        Statement stmt = conn.createStatement();
        try
        {
            stmt.execute(
                "INSERT INTO "+getTable()+"(data_key, data) VALUES ("+
                id+", '"+escape(str)+"')"
            );
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
    public String retrieve(long id, Connection conn)
        throws EntityDoesNotExistException, SQLException
    {
        if(cache != null && id < cache.length)
        {
            String value = cache[(int)id];
            if(value != null)
            {
                return value;
            }
        }
        Statement stmt = conn.createStatement();
        ResultSet rs = null;
        try
        {
            rs = stmt.executeQuery(
                "SELECT data FROM "+getTable()+" WHERE data_key = "+id
            );
            if(!rs.next())
            {
                throw new EntityDoesNotExistException("Item #"+id+" does not exist in table "+
                    getTable());
            }
            String value = unescape(rs.getString(1));
            if(cache != null && id < cache.length)
            {
                cache[(int)id] = value;
            }
            return value;
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
    public void update(long id, String value, Connection conn)
        throws EntityDoesNotExistException, SQLException
    {
        String str = value;
        if(str.length() > 255)
        {
            throw new IllegalArgumentException("maximum lenght of string attributes "+
                                               "is 255 characters. Use text attributes "+
                                               "wherever greater capacity is desired");
        }
        if(cache != null && id < cache.length)
        {
            cache[(int)id] = str;
        }
        Statement stmt = conn.createStatement();
        try
        {
            checkExists(id, stmt);
            stmt.execute(
                "UPDATE "+getTable()+" SET data = '"+
                escape(str)+
                "' WHERE data_key = "+id
            );
        }
        finally
        {
            DatabaseUtils.close(stmt);
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
    public boolean supportsExternalString()
    {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public int getSupportedConditions()
    {
        return CONDITION_EQUALITY | CONDITION_COMPARISON | 
            CONDITION_APPROXIMATION;
    }

    // protected /////////////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public String toExternalString(String value)
    {
        checkValue(value);
        return "'"+escape(value)+"'";
    }
}
