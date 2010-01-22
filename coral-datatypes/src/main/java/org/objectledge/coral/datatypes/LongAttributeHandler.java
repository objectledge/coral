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
 * Handles persistency of <code>java.lang.Long</code> objects.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: LongAttributeHandler.java,v 1.4 2005-01-19 07:34:06 rafal Exp $
 */
public class LongAttributeHandler
    extends AttributeHandlerBase<Long>
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
    public LongAttributeHandler(Database database, CoralStore coralStore,
                                   CoralSecurity coralSecurity, CoralSchema coralSchema,
                                   AttributeClass attributeClass)
    {
        super(database, coralStore, coralSecurity, coralSchema, attributeClass);
    }

    // AttributeHandler interface ////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public long create(Long value, Connection conn)
        throws SQLException
    {
        long id = getNextId();
        Statement stmt = conn.createStatement();
        stmt.execute(
            "INSERT INTO "+getTable()+"(data_key, data) VALUES ("+
            id+", "+(value).longValue()+")"
        );
        return id;
    }

    /**
     * {@inheritDoc}
     */
    public Long retrieve(long id, Connection conn)
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
        return new Long(rs.getLong(1));
    }

    /**
     * {@inheritDoc}
     */
    public void update(long id, Long value, Connection conn)
        throws EntityDoesNotExistException, SQLException
    {
        Statement stmt = conn.createStatement();
        checkExists(id, stmt);
        stmt.execute(
            "UPDATE "+getTable()+" SET data = "+
            (value).longValue()+
            " WHERE data_key = "+id
        );
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
    protected Long fromString(String string)
    {
        return new Long(string);
    }
    
    /**
     * {@inheritDoc}
     */
    public String toExternalString(Long value)
    {
        checkValue(value);
        return value.toString();
    }
}
