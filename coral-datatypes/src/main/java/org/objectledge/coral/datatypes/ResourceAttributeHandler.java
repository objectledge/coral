package org.objectledge.coral.datatypes;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.store.ConstraintViolationException;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.Resource;
import org.objectledge.database.Database;

/**
 * Handles persistency of {@link Resource} references.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: ResourceAttributeHandler.java,v 1.4 2005-01-18 10:08:41 rafal Exp $
 */
public class ResourceAttributeHandler
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
    public ResourceAttributeHandler(Database database, CoralStore coralStore,
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
        long id = database.getNextId(getTable());
        Statement stmt = conn.createStatement();
        stmt.execute(
            "INSERT INTO "+getTable()+"(data_key, ref) VALUES ("+
            id+", "+((Resource)value).getIdString()+")"
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
            throw new EntityDoesNotExistException("Item #"+id+" does not exist in table "+
                getTable());
        }
        return coralStore.getResource(rs.getLong(1));
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
            ((Resource)value).getIdString()+
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
                return coralStore.getResource(id);
            }
            catch(EntityDoesNotExistException e)
            {
                throw new IllegalArgumentException("resource #"+id+" not found");
            }
        }
        else
        {
            Resource[] res = coralStore.getResourceByPath(string);
            if(res.length == 0)
            {
                throw new IllegalArgumentException("resource '"+string+"' not found");
            }
            if(res.length > 1)
            {
                throw new IllegalArgumentException("resource name '"+string+"' is ambigous");
            }
            return res[0];
        }
    }

    /**
     * {@inheritDoc}
     */
    public String toPrintableString(Object value)
    {
        checkValue(value);
        return ((Resource)value).getPath();
    }

    // value domain //////////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public void checkDomain(String domain)
    {
        if(domain != null)
        {
            try
            {
                coralSchema.getResourceClass(domain);
            }
            catch(EntityDoesNotExistException e)
            {
                throw new  IllegalArgumentException("malformed constraint '"+domain+
                                                    "', valid resource class name expected");    
            }
        }
    }   
    
    /**
     * {@inheritDoc}
     */
    public void checkDomain(String domain, Object value)
        throws ConstraintViolationException
    {
        if(domain == null)
        {
            if(!(value instanceof Resource))
            {
                throw new ConstraintViolationException(value.getClass().getName()+
                                                       " is not a subclass of Resource");
            }
        }
        else
        {
            try
            {
                ResourceClass rc = coralSchema.getResourceClass(domain);
                if(!rc.getJavaClass().isInstance(value))
                {
                    throw new ConstraintViolationException(value.getClass().getName()+
                                                           "is not a subclass of "+
                                                           rc.getJavaClass().getName());
                }
            }
            catch(EntityDoesNotExistException e)
            {
                throw new  IllegalArgumentException("malformed constraint '"+domain+
                                                    "', valid resource class name expected");    
            }
        }
    }

    // integrity constraints ////////////////////////////////////////////////    

    /**
     * {@inheritDoc}
     */
    public boolean containsResourceReferences()
    {
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    public Resource[] getResourceReferences(Object value)
    {
        Resource[] result;
        if(value != null)
        {
            result = new Resource[1];
            result[0] = (Resource)value;
        }
        else
        {
            result = new Resource[0];
        }
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean clearResourceReferences(Object value)
    {
        return true;
    }
}
