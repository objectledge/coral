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
 * @version $Id: ResourceAttributeHandler.java,v 1.1 2004-03-02 09:51:01 pablo Exp $
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
     * Creates a new attribute instance.
     *
     * @param value the value of the attribute.
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform
     *        the operation as a part of a JDBC transaction.
     * @return the identifier of the new attribute.
     * @throws SQLException in case of database problems. The caller metod
     *         should consider rolling back the whole transaction.
     */
    public long create(Object value, Connection conn)
        throws SQLException
    {
        long id = database.getNextId(getTable());
        Statement stmt = conn.createStatement();
        stmt.execute(
            "INSERT INTO "+getTable()+"(data_key, ref) VALUES ("+
            id+", "+((Resource)value).getId()+")"
        );
        return id;
    }

    /**
     * Retrieves an attribute value.
     *
     * @param id the identifier of the attribute.
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform
     *        the operation as a part of a JDBC transaction.
     * @throws SQLException in case of database problems. The caller metod
     *         should consider rolling back the whole transaction.
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
            throw new EntityDoesNotExistException("Item #"+id+" does not exist in table "+getTable());
        }
        return coralStore.getResource(rs.getLong(1));
    }

    /**
     * Modifies an existing attribute.
     *
     * @param id the identifier of the attribute.
     * @param value the value of the attribute.
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform
     *        the operation as a part of a JDBC transaction.
     * @throws EntityDoesNotExistException if the attribute with specified id
     *         does not exist. 
     * @throws SQLException in case of database problems. The caller metod
     *         should consider rolling back the whole transaction.
     */
    public void update(long id, Object value, Connection conn)
        throws EntityDoesNotExistException, SQLException
    {
        Statement stmt = conn.createStatement();
        checkExists(id, stmt);
        stmt.execute(
            "UPDATE "+getTable()+" SET ref = "+
            ((Resource)value).getId()+
            " WHERE data_key = "+id
        );
    }

    // meta information //////////////////////////////////////////////////////
    
    /**
     * Provides information about comparison operations supported by the
     * attribute type.
     *
     * <p>The returned value is a bitwise sum of the CONDITION_*
     * constants.</p>
     * @return information about comparison operations supported by the
     * attribute type.
     */
    public int getSupportedConditions()
    {
        return CONDITION_EQUALITY;
    }

    // protected /////////////////////////////////////////////////////////////

    /**
     * Converts a string into an attribute object.
     *
     * <p>If the string starts with a number, it is considered to be resource
     * id. Otherwise it is considered resource name. If the name is ambigous,
     * an exception will be thrown.</p>
     * 
     * @param string the string to convert.
     * @return the attribute object, or <code>null</code> if conversion not
     *         supported. 
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
     * Converts an attribute value into a human readable string.
     *
     * @param value the value to convert.
     * @return a human readable string.
     */
    public String toPrintableString(Object value)
    {
        checkValue(value);
        return ((Resource)value).getPath();
    }

    // value domain //////////////////////////////////////////////////////////

    /**
     * Check if the domain constraint is well formed.
     *
     * @param domain value domain constraint.
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
     * Check if an attribute value fullfills a domain constraint.
     *
     * @param domain value domain constraint.
     * @param value an attribute value.
     * @throws ConstraintViolationExcepion if the value does not fulfill the constraint.
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
     * Checks if the attributes of this type can impose integrity constraints 
     * on the data store.
     * 
     * @return <code>true</code> if the attribute can impose constraints on the
     * data store. 
     */
    public boolean containsResourceReferences()
    {
        return true;
    }
    
    /**
     * Returns the resources referenced by this attribute.
     * 
     * @param value the attribute value.
     * @return resources referenced by this attribute.
     * */
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
     * Removes all resource attributes from the attribute value.
     * 
     * <p>This method may be called during deletion of a group of 
     * interdependant resources.</p>
     * 
     * @param value attribute value.
     * @return <code>true</code> if the attribute value should be
     *         removed form the resource.
     */
    public boolean clearResourceReferences(Object value)
    {
        return true;
    }
}
