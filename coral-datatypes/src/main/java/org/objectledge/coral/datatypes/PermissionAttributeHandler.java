package org.objectledge.coral.datatypes;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.security.Permission;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.database.Database;

/**
 * Handles persistency of {@link Permission} references.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: PermissionAttributeHandler.java,v 1.2 2004-05-06 13:38:43 pablo Exp $
 */
public class PermissionAttributeHandler
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
    public PermissionAttributeHandler(Database database, CoralStore coralStore,
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
        long id = database.getNextId(getTable());
        Statement stmt = conn.createStatement();
        stmt.execute(
            "INSERT INTO "+getTable()+"(data_key, ref) VALUES ("+
            id+", "+((Permission)value).getId()+")"
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
            "SELECT ref FROM "+getTable()+" WHERE data_key = "+id
        );
        if(!rs.next())
        {
            throw new EntityDoesNotExistException("Item #"+id+
                                                  " does not exist in table "+
                                                  getTable());
        }
        return coralSecurity.getPermission(rs.getLong(1));
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
            "UPDATE "+getTable()+" SET ref = "+
            ((Permission)value).getId()+
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

    // protected /////////////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    protected Object fromString(String string)
    {
        if(Character.isDigit(string.charAt(0)))
        {
            long id = Long.parseLong(string);
            try
            {
                return coralSecurity.getPermission(id);
            }
            catch(EntityDoesNotExistException e)
            {
                throw new IllegalArgumentException("permission #"+id+
                                                   " not found");
            }
        }
        else
        {
            Permission[] permissions = coralSecurity.getPermission(string);
            if(permissions.length == 0)
            {
                throw new IllegalArgumentException("permission '"+string+
                                                   "' not found");
            }
            if(permissions.length > 1)
            {
                throw new IllegalArgumentException("permission name '"+string+
                                                   "' is ambigous");
            }
            return permissions[0];
        }
    }
}
