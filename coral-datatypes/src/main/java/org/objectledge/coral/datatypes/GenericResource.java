package org.objectledge.coral.datatypes;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jcontainer.dna.Logger;
import org.objectledge.coral.BackendException;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.AttributeHandler;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.schema.ResourceClassInheritance;
import org.objectledge.coral.schema.ResourceHandler;
import org.objectledge.coral.schema.UnknownAttributeException;
import org.objectledge.coral.security.PermissionAssignment;
import org.objectledge.coral.security.Role;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.ConstraintViolationException;
import org.objectledge.coral.store.ModificationNotPermitedException;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ValueRequiredException;
import org.objectledge.database.Database;

/**
 * A generic implementation of {@link Resource} interface.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: GenericResource.java,v 1.10 2004-06-29 12:19:19 fil Exp $
 */
public class GenericResource
    extends AbstractResource
{
    // Member objects ////////////////////////////////////////////////////////

    /** AttributeDefinition -> attribute value map. */
    private Map attributes = new HashMap();

    /** AttributeDefinition -> attribute instance id map. */
    private Map ids = new HashMap();

    /**
     * Constructor.
     * 
     * @param database the database.
     * @param logger the logger.
     */
    public GenericResource(Database database, Logger logger)
    {
        super(database, logger);
    }

    // Resource interface - attributes (implemented here) ////////////////////

    protected synchronized boolean isDefinedLocally(AttributeDefinition attribute)
        throws UnknownAttributeException
    {
        if(modified.contains(attribute))
        {
            return attributes.containsKey(attribute);
        }
        else
        {
            return ids.containsKey(attribute);
        }
    }
    
    protected synchronized Object getLocally(AttributeDefinition attribute)
        throws UnknownAttributeException
    {
        if(modified.contains(attribute))
        {
            return attributes.get(attribute);
        }
        else
        {
            if(ids.containsKey(attribute))
            {
                if(attributes.containsKey(attribute))
                {
                    return attributes.get(attribute);
                }
                else
                {
                    long aId = ((Long)ids.get(attribute)).longValue();
                    Object value = attributeOnDemand(attribute, aId);
                    attributes.put(attribute, value);
                    return value;
                }
            }
            else
            {
                return null;
            }
        }
    }
    
    /**
     * Sets the value of a specific attribute.
     * 
     * @param attribute the attribute to set.
     * @param value the value of the attribute.
     * @throws UnknownAttributeException if <code>attribute</code> does not
     *         belong to the resource's class.
     * @throws ModificationNotPermitedException if <code>attribute</code> is
     *         <code>READONLY</code>.
     * @throws ValueRequiredException if <code>attribute</code> is
     *         <code>REQUIRED</code> and <code>value</code> is
     *         <code>null</code>.
     * @throws ConstraintViolationException if contraints are being violated.
     */
    protected synchronized void setLocally(AttributeDefinition attribute, Object value)
    {
        attributes.put(attribute, value);
        modified.add(attribute);
    }

    /**
     * Removes the value of the specified attribute.
     *
     * @param attribute the attribute to remove.
     * @throws ValueRequiredException if the attribute is required for this
     *         resource type.
     * @throws UnknownAttributeException if <code>attribute</code> does not
     *         belong to the resource's class.
     */
    protected synchronized void unsetLocally(AttributeDefinition attribute)
    {
        attributes.remove(attribute);
        modified.add(attribute);
    }    
    
    // Package private ///////////////////////////////////////////////////////

    synchronized void retrieve(Resource delegate, ResourceClass rClass, 
                                 Connection conn, Object data)
        throws SQLException
    {
        super.retrieve(delegate, rClass, conn, data);
        Map dataKeyMap = (Map)data;
        Map dataKeys = (Map)dataKeyMap.get(new Long(delegate.getId()));
        if(dataKeys != null)
        {
            AttributeDefinition[] declared = rClass.getDeclaredAttributes();
            for(int i=0; i<declared.length; i++)
            {
                Long id = (Long)dataKeys.get(declared[i]);
                if(id != null)
                {
                    ids.put(declared[i], id);
                }
            }
        }
    }

    synchronized void revert(ResourceClass rClass, Connection conn, Object data)
        throws SQLException
    {
        super.revert(rClass, conn, data);
        attributes.clear();
        ids.clear();
        Map dataKeyMap = (Map)data;
        Map dataKeys = (Map)dataKeyMap.get(new Long(delegate.getId()));
        if(dataKeys != null)
        {
            AttributeDefinition[] declared = rClass.getDeclaredAttributes();
            for(int i=0; i<declared.length; i++)
            {
                Long id = (Long)dataKeys.get(declared[i]);
                if(id != null)
                {
                    ids.put(declared[i], id);
                }
            }
        }
    }

    synchronized void create(Resource delegate, ResourceClass rClass, 
                               Map attributes, Connection conn)
        throws SQLException, ValueRequiredException, ConstraintViolationException
    {
        super.create(delegate, rClass, attributes, conn);
        AttributeDefinition[] declared = rClass.getDeclaredAttributes();
        Statement stmt = conn.createStatement();
        for(int i=0; i<declared.length; i++)
        {
            AttributeDefinition attr = declared[i];
            if((attr.getFlags() & AttributeFlags.BUILTIN) == 0)
            {
	            AttributeHandler handler = attr.getAttributeClass().getHandler();
	            Object value = attributes.get(attr);
	            if(value != null)
	            {
		            value = handler.toAttributeValue(value);
		            long newId = handler.create(value, conn);
		            ids.put(attr, new Long(newId));
		            stmt.execute(
		                "INSERT INTO coral_generic_resource "+
		                "(resource_id, attribute_definition_id, data_key) "+
		                "VALUES ("+delegate.getId()+", "+attr.getId()+", "+
		                newId+")"
		            );
		            if(handler.shouldRetrieveAfterCreate())
		            {
		                try
		                {
		                    this.attributes.put(attr, handler.retrieve(newId, conn));
		                }
		                catch(EntityDoesNotExistException e)
		                {
		                    throw new BackendException("data integrity error", e);
		                }
		            }
		            else
		            {
		                this.attributes.put(attr, value);
		            }
	            }
            }
        }
    }

    /**
     * Called from {@link GenericResourceHandler} and {@link #update(Subject)}.
     *
     * @param subject the subject that performs the update. 
     * @param conn the JDBC connection to use.
     */
    synchronized void update(Connection conn)
        throws SQLException
    {
        super.update(conn);
        Statement stmt = conn.createStatement();
        Iterator i = modified.iterator();
        while(i.hasNext())
        {
            Object o = i.next();
            if(o instanceof AttributeDefinition)
            {
                AttributeDefinition attr = (AttributeDefinition)o;
                if((attr.getFlags() & AttributeFlags.BUILTIN) == 0)
                {
	                AttributeHandler handler = attr.getAttributeClass().getHandler();
	                Object value = attributes.get(attr);
	                Long id = (Long)ids.get(attr);
	                if(value != null)
	                {
	                    if(id == null)
	                    {
	                        long newId = handler.create(value, conn);
	                        stmt.execute(
	                            "INSERT INTO coral_generic_resource "+
	                            "(resource_id, attribute_definition_id, data_key) "+
	                            "VALUES ("+delegate.getId()+", "+attr.getId()+", "+
	                            newId+")"
	                        );
	                        ids.put(attr, new Long(newId));
	                    }
	                    else
	                    {
	                        try
	                        {
	                            handler.update(id.longValue(), value, conn);
	                        }
	                        catch(EntityDoesNotExistException e)
	                        {
	                            throw new BackendException("Internal error", e);
	                        }
	                    }
	                }
	                else
	                {
	                    if(id != null)
	                    {
	                        try
	                        {
	                            handler.delete(id.longValue(), conn);
	                        }
	                        catch(EntityDoesNotExistException e)
	                        {
	                            throw new BackendException("Internal error", e);
	                        }
	                        stmt.execute(
	                            "DELETE FROM coral_generic_resource "+
	                            "WHERE resource_id = "+delegate.getId()+
	                            " AND attribute_definition_id = "+attr.getId()
	                        );
	                        ids.remove(attr);
	                    }
	                }
                }    
                i.remove();
            }
        }
    }

    synchronized void delete(Connection conn)
        throws SQLException
    {
        super.delete(conn);
        Iterator i = ids.keySet().iterator();
        if(i.hasNext())
        {
            Statement stmt = conn.createStatement();
            while(i.hasNext())
            {
                AttributeDefinition attr = (AttributeDefinition)i.next();
                long atId = ((Long)ids.get(attr)).longValue();
                try
                {
                    attr.getAttributeClass().getHandler().delete(atId, conn);
                }
                catch(EntityDoesNotExistException e)
                {
                    throw new BackendException("internal error", e);
                }
                stmt.execute("DELETE FROM coral_generic_resource WHERE "+
                             " resource_id = "+delegate.getId()+
                             " AND attribute_definition_id = "+attr.getId());
            }
        }
    }

    // Private ///////////////////////////////////////////////////////////////

    private Object attributeOnDemand(AttributeDefinition attribute, long aId)
    {
        Connection conn = null;
        try
        {
            conn = database.getConnection();
            return attribute.getAttributeClass().getHandler().
                retrieve(aId, conn);
        }
        catch(Exception e)
        {
            if(delegate == null)
            {
                throw new BackendException("failed to retrieve attribute value " +                    "(attribute definition = "+attribute.getName()+
                    " , attribute id = "+aId+")", e);
            }
            else
            {
                throw new BackendException("failed to retrieve attribute value " +
                    "(attribute definition = "+attribute.getName()+
                    " , attribute id = "+aId+") for resource: "+delegate.getId() , e);
            }
        }
        finally
        {
            if(conn != null)
            {
                try
                {
                    conn.close();
                }
                catch(SQLException ee)
                {
                    logger.error("Failed to close connection", ee);
                }
            }
        }
    }
}
