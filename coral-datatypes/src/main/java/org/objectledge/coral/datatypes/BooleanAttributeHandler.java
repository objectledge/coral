package org.objectledge.coral.datatypes;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.BitSet;

import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.database.Database;
import org.objectledge.database.DatabaseUtils;

/**
 * Handles persistency of <code>java.lang.Boolean</code> objects.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: BooleanAttributeHandler.java,v 1.5 2005-01-20 10:48:26 rafal Exp $
 */
public class BooleanAttributeHandler
    extends AttributeHandlerBase<Boolean>
{
    /** Preloading cache - values. */
    private BitSet cache;
    
    /** Preloading cache - defined status. */
    private BitSet defined;

    /**
     * The constructor.
     * 
     * @param database the database.
     * @param coralStore the store.
     * @param coralSecurity the security.
     * @param coralSchema the scheam.
     * @param attributeClass the attribute class.
     */
    public BooleanAttributeHandler(Database database, CoralStore coralStore,
        CoralSecurity coralSecurity, CoralSchema coralSchema, AttributeClass<Boolean> attributeClass)
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
        ResultSet rs = stmt.executeQuery("SELECT max(data_key) FROM "+getTable());
        rs.next();
        int count = rs.getInt(1);
        cache = new BitSet(count+1);
        defined = new BitSet(count+1);
        try
        {
            rs = stmt.executeQuery("SELECT data_key, data FROM "+getTable());
            while(rs.next())
            {
                cache.set(rs.getInt(1), rs.getBoolean(2));
                defined.set(rs.getInt(1));
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
    public long create(Boolean value, Connection conn)
        throws SQLException
    {
        long id = getNextId();
        Statement stmt = conn.createStatement();
        try
        {
            stmt.execute(
                "INSERT INTO "+getTable()+"(data_key, data) VALUES ("+
 id + ", "
                + ((value).booleanValue() ? "true" : "false") + ")"
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
    public Boolean retrieve(long id, Connection conn)
        throws EntityDoesNotExistException, SQLException
    {
        if(cache != null && id < cache.length())
        {
            if(defined.get((int)id))
            {
                return cache.get((int)id) ? Boolean.TRUE : Boolean.FALSE;
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
            boolean value = rs.getBoolean(1);
            if(cache != null && id < cache.length())
            {
                cache.set((int)id, value);
                defined.set((int)id);
            }            
            return value ? Boolean.TRUE : Boolean.FALSE;
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
    public void update(long id, Boolean value, Connection conn)
        throws EntityDoesNotExistException, SQLException
    {
        if(cache != null && id < cache.length())
        {
            cache.set((int)id, (value).booleanValue());
            defined.set((int)id);
        }
        Statement stmt = conn.createStatement();
        try
        {
            checkExists(id, stmt);
            stmt.execute(
                "UPDATE "+getTable()+" SET data = "+
 ((value).booleanValue() ? "true" : "false") +
                " WHERE data_key = "+id
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
        if(cache != null && id < cache.length())
        {
            defined.clear((int)id);
        }
    }

    // meta information //////////////////////////////////////////////////////
    
    /**
     * {@inheritDoc}
     */
    public int getSupportedConditions()
    {
        return CONDITION_EQUALITY;
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
    protected Boolean fromString(String string)
    {
        if(string.equalsIgnoreCase("yes") || string.equalsIgnoreCase("true") || string.equals("1")
            || string.equalsIgnoreCase("t"))
        {
            return Boolean.TRUE;
        }
        if(string.equalsIgnoreCase("false") || string.equalsIgnoreCase("no") || string.equals("0")
            || string.equalsIgnoreCase("f"))
        {
            return Boolean.FALSE;
        }
        throw new IllegalArgumentException("Invalid boolean value '"+string+"'");
    }

    /**
     * {@inheritDoc}
     */
    public String toPrintableString(Boolean value)
    {
        checkValue(value);
        if((value).booleanValue())
        {
            return "true";
        }
        else
        {
            return "false";
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public String toExternalString(Boolean value)
    {
        checkValue(value);
        if((value).booleanValue())
        {
            return "1";
        }
        else
        {
            return "0";
        }
    }
}
