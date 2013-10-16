package org.objectledge.coral.datatypes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
 * Handles persistency of arbitray objects supporting
 * <code>java.lang.Nubmer</code> interface.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: NumberAttributeHandler.java,v 1.4 2005-01-19 07:34:06 rafal Exp $
 */
public class NumberAttributeHandler
    extends AttributeHandlerBase<Number>
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
    public NumberAttributeHandler(Database database, CoralStore coralStore,
        CoralSecurity coralSecurity, CoralSchema coralSchema, AttributeClass<Number> attributeClass)
    {
        super(database, coralStore, coralSecurity, coralSchema, attributeClass);
    }
    
    // AttributeHandler interface ////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public long create(Number value, Connection conn)
        throws SQLException
    {
        long id = getNextId();
        Statement stmt = conn.createStatement();
        try
        {
            stmt.execute(
                "INSERT INTO "+getTable()+"(data_key, data) VALUES ("+
                id+", "+(value).toString()+")"
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
    public Number retrieve(long id, Connection conn)
        throws EntityDoesNotExistException, SQLException
    {
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
            return new BigDecimal(rs.getString(1));
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
    public void update(long id, Number value, Connection conn)
        throws EntityDoesNotExistException, SQLException
    {
        Statement stmt = conn.createStatement();
        try
        {
            checkExists(id, stmt);
            stmt.execute(
                "UPDATE "+getTable()+" SET data = "+
                (value).toString()+
                " WHERE data_key = "+id
            );
        }
        finally
        {
            DatabaseUtils.close(stmt);
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
    protected Number fromString(String string)
    {
        return new BigDecimal(string);
    }    

    /**
     * {@inheritDoc}
     */
    protected String getDataColumn()
    {
        return "data";
    }

    /**
     * {@inheritDoc}
     */
    public String toExternalString(Number value)
    {
        checkValue(value);
        return value.toString();
    }

    public void setParameter(PreparedStatement pstmt, int position, Number value)
        throws SQLException
    {
        if(value instanceof Long)
        {
            pstmt.setLong(position, ((Long)value).longValue());
        }
        else if(value instanceof Integer)
        {
            pstmt.setInt(position, ((Integer)value).intValue());
        }
        else if(value instanceof Short)
        {
            pstmt.setShort(position, ((Short)value).shortValue());
        }
        else if(value instanceof Byte)
        {
            pstmt.setByte(position, ((Byte)value).byteValue());
        }
        else if(value instanceof Double)
        {
            pstmt.setDouble(position, ((Double)value).doubleValue());
        }
        else if(value instanceof Float)
        {
            pstmt.setFloat(position, ((Float)value).floatValue());
        }
        else if(value instanceof BigDecimal)
        {
            pstmt.setBigDecimal(position, (BigDecimal)value);
        }
        else
        {
            throw new IllegalArgumentException("unsupported Number format "
                + value.getClass().getName());
        }
    }
}
