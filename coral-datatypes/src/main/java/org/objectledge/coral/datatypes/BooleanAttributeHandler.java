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
 * Handles persistency of <code>java.lang.Boolean</code> objects.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: BooleanAttributeHandler.java,v 1.4 2005-01-19 07:34:06 rafal Exp $
 */
public class BooleanAttributeHandler
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
    public BooleanAttributeHandler(Database database, CoralStore coralStore,
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
        long id = getNextId();
        Statement stmt = conn.createStatement();
        stmt.execute(
            "INSERT INTO "+getTable()+"(data_key, data) VALUES ("+
            id+", "+(((Boolean)value).booleanValue() ? "1" : "0")+")"
        );
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
            "SELECT data FROM "+getTable()+" WHERE data_key = "+id
        );
        if(!rs.next())
        {
            throw new EntityDoesNotExistException("Item #"+id+" does not exist in table "+
                getTable());
        }
        return new Boolean(rs.getBoolean(1));
    }

    /**
     * {@inheritDoc}
     */
    public void update(long id, Object value, Connection conn)
        throws EntityDoesNotExistException, SQLException
    {
        Statement stmt = conn.createStatement();
        checkExists(id, stmt);
        stmt.execute(
            "UPDATE "+getTable()+" SET data = "+
            (((Boolean)value).booleanValue() ? "1" : "0")+
            " WHERE data_key = "+id
        );
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
    protected Object fromString(String string)
    {
        if(string.equalsIgnoreCase("yes") || string.equalsIgnoreCase("true") || string.equals("1"))
        {
            return Boolean.TRUE;
        }
        if(string.equalsIgnoreCase("false") || string.equalsIgnoreCase("no") || string.equals("0"))
        {
            return Boolean.FALSE;
        }
        throw new IllegalArgumentException("Invalid boolean value '"+string+"'");
    }

    /**
     * {@inheritDoc}
     */
    public String toPrintableString(Object value)
    {
        checkValue(value);
        if(((Boolean)value).booleanValue())
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
    public String toExternalString(Object value)
    {
        checkValue(value);
        if(((Boolean)value).booleanValue())
        {
            return "1";
        }
        else
        {
            return "0";
        }
    }
}
