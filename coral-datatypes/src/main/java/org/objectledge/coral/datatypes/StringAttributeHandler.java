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
 * Handles persistency of <code>java.lang.String</code> objects that contain
 * at most 255 characters.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: StringAttributeHandler.java,v 1.3 2004-05-06 13:38:43 pablo Exp $
 */
public class StringAttributeHandler
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
    public StringAttributeHandler(Database database, CoralStore coralStore,
                                  CoralSecurity coralSecurity, CoralSchema coralSchema,
                                  AttributeClass attributeClass)
    {
        super(database, coralStore, coralSecurity, coralSchema, attributeClass);
    }
        
    /**
     * {@inheritDoc}
     */
    public long create(Object value, Connection conn)
        throws SQLException
    {
        String str = (String)value;
        if(str.length() > 255)
        {
            throw new IllegalArgumentException("maximum lenght of string attributes "+
                                               "is 255 characters. Use text attributes "+
                                               "wherever greater capacity is desired");
        }

        long id = database.getNextId(getTable());
        Statement stmt = conn.createStatement();
        stmt.execute(
            "INSERT INTO "+getTable()+"(data_key, data) VALUES ("+
            id+", '"+escape(str)+"')"
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
		return unescape(rs.getString(1));
    }

    /**
     * {@inheritDoc}
     */
    public void update(long id, Object value, Connection conn)
        throws EntityDoesNotExistException, SQLException
    {
        String str = (String)value;
        if(str.length() > 255)
        {
            throw new IllegalArgumentException("maximum lenght of string attributes "+
                                               "is 255 characters. Use text attributes "+
                                               "wherever greater capacity is desired");
        }

        Statement stmt = conn.createStatement();
        checkExists(id, stmt);
        stmt.execute(
            "UPDATE "+getTable()+" SET data = '"+
            escape(str)+
            "' WHERE data_key = "+id
        );
    }

    /**
     * {@inheritDoc}
     */
    public boolean supportsExternalString()
    {
        return true;
    }

    // meta information //////////////////////////////////////////////////////
    
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
    public String toExternalString(Object value)
    {
        checkValue(value);
        return "'"+escape((String)value)+"'";
    }
}
