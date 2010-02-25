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

/**
 * Handles persistency of <code>java.lang.Integer</code> objects.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: IntegerAttributeHandler.java,v 1.6 2005-06-22 06:56:59 pablo Exp $
 */
public class IntegerAttributeHandler
    extends AttributeHandlerBase<Integer>
{
    /** preloading cache - values. */
    private int[] cache;
    
    /** preloading cache - defined status. */
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
    public IntegerAttributeHandler(Database database, CoralStore coralStore,
                                   CoralSecurity coralSecurity, CoralSchema coralSchema,
                                   AttributeClass attributeClass)
    {
        super(database, coralStore, coralSecurity, coralSchema, attributeClass);
    }

    // AttributeHandler interface ////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public long create(Integer value, Connection conn)
        throws SQLException
    {
        long id = getNextId();
        Statement stmt = conn.createStatement();
        stmt.execute(
            "INSERT INTO "+getTable()+"(data_key, data) VALUES ("+
            id+", "+(value).intValue()+")"
        );
        return id;
    }

    /**
     * {@inheritDoc}
     */
    public Integer retrieve(long id, Connection conn)
        throws EntityDoesNotExistException, SQLException
    {
        if(cache != null && id < cache.length)
        {
            if(defined.get((int)id))
            {
                return new Integer(cache[(int)id]);
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
        int value = rs.getInt(1);
        if(cache != null && id < cache.length)
        {
            cache[(int)id] = value;
            defined.set((int)id);
        }
        return new Integer(value);
    }

    /**
     * {@inheritDoc}
     */
    public void update(long id, Integer value, Connection conn)
        throws EntityDoesNotExistException, SQLException
    {
        if(cache != null && id < cache.length)
        {
            cache[(int)id] = (value).intValue();
            defined.set((int)id);
        }
        Statement stmt = conn.createStatement();
        checkExists(id, stmt);
        stmt.execute(
            "UPDATE "+getTable()+" SET data = "+
            (value).intValue()+
            " WHERE data_key = "+id
        );
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
            defined.clear((int)id);
        }
    }
    
    public void preload(Connection conn)
        throws SQLException
    {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT max(data_key) from "+getTable());
        rs.next();
        int count = rs.getInt(1);
        cache = new int[count+1];
        defined = new BitSet(count+1);
        rs = stmt.executeQuery("SELECT data_key, data from "+getTable());
        while(rs.next())
        {
            cache[rs.getInt(1)] = rs.getInt(2);
            defined.set(rs.getInt(1));
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
    protected Integer fromString(String string)
    {
        return new Integer(string);
    }

    /**
     * {@inheritDoc}
     */
    public String toExternalString(Integer value)
    {
        checkValue(value);
        return value.toString();
    }
}
