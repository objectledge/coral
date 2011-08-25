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
 * Handles persistency of <code>java.lang.String</code> objects that may
 * contain more than 255 characters.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: TextAttributeHandler.java,v 1.4 2005-01-19 07:34:06 rafal Exp $
 */
public class TextAttributeHandler
    extends AttributeHandlerBase<String>
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
    public TextAttributeHandler(Database database, CoralStore coralStore,
                                CoralSecurity coralSecurity, CoralSchema coralSchema,
                                AttributeClass attributeClass)
    {
        super(database, coralStore, coralSecurity, coralSchema, attributeClass);
    }
    
    // AttributeHandler interface ////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public long create(String value, Connection conn)
        throws SQLException
    {
        long id = getNextId();
        Statement stmt = conn.createStatement();
        try
        {
            stmt.execute(
                "INSERT INTO "+getTable()+"(data_key, data) VALUES ("+
                id+", '"+escape(value)+"')"
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
            return unescape(rs.getString(1));
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
        Statement stmt = conn.createStatement();
        try
        {
            checkExists(id, stmt);
            stmt.execute(
                "UPDATE "+getTable()+" SET data = '"+
                escape(value)+
                "' WHERE data_key = "+id
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
        return CONDITION_EQUALITY | CONDITION_COMPARISON | 
            CONDITION_APPROXIMATION;
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
    public String toExternalString(String value)
    {
        checkValue(value);
        return "'"+escape(value)+"'";
    }
}
