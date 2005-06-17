package org.objectledge.coral.datatypes;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.jcontainer.dna.Logger;
import org.objectledge.coral.BackendException;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.AttributeHandler;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.store.ConstraintViolationException;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ValueRequiredException;
import org.objectledge.database.Database;

/**
 * A generic implementation of {@link Resource} interface.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: GenericResource.java,v 1.21 2005-06-17 07:42:58 rafal Exp $
 */
public class GenericResource
    extends AbstractResource
{
    // Member objects ////////////////////////////////////////////////////////

    /**
     * Constructor.
     * 
     * @param coralSchema the Coral Schema.
     * @param database the database.
     * @param logger the logger.
     */
    public GenericResource(CoralSchema coralSchema, Database database, Logger logger)
    {
        super(database, logger);
    }
    
    // Package private ///////////////////////////////////////////////////////

    synchronized void retrieve(Resource delegate, ResourceClass rClass, 
                                 Connection conn, Object data)
        throws SQLException
    {
        super.retrieve(delegate, rClass, conn, data);
        Map<Long,Map<AttributeDefinition,Long>> dataKeyMap = 
            (Map<Long,Map<AttributeDefinition,Long>>)data;
        Map<AttributeDefinition,Long> dataKeys = dataKeyMap.get(delegate.getIdObject());
        if(dataKeys != null)
        {            
            for(AttributeDefinition declared : rClass.getDeclaredAttributes())
            {
                Long id = dataKeys.get(declared);
                if(id != null)
                {
                    setValueId(declared, id);
                }
            }
        }
    }

    synchronized void revert(ResourceClass rClass, Connection conn, Object data)
        throws SQLException
    {
        super.revert(rClass, conn, data);
        Map<Long,Map<AttributeDefinition,Long>> dataKeyMap = 
            (Map<Long,Map<AttributeDefinition,Long>>)data;
        Map<AttributeDefinition,Long> dataKeys = dataKeyMap.get(delegate.getIdObject());
        if(dataKeys != null)
        {          
            for(AttributeDefinition declared : rClass.getDeclaredAttributes())
            {
                Long id = dataKeys.get(declared);
                if(id != null)
                {
                    setValueId(declared, id);
                }
            }
        }
    }

    synchronized void create(Resource delegate, ResourceClass rClass, 
                               Map attributes, Connection conn)
        throws SQLException, ValueRequiredException, ConstraintViolationException
    {
        super.create(delegate, rClass, attributes, conn);
        Statement stmt = conn.createStatement();
        for(AttributeDefinition attr : rClass.getDeclaredAttributes())
        {
            if((attr.getFlags() & AttributeFlags.BUILTIN) == 0)
            {
	            AttributeHandler handler = attr.getAttributeClass().getHandler();
	            Object value = attributes.get(attr);
	            if(value != null)
	            {
		            value = handler.toAttributeValue(value);
		            long newId = handler.create(value, conn);
		            setValueId(attr, newId);
		            stmt.execute(
		                "INSERT INTO coral_generic_resource "+
		                "(resource_id, attribute_definition_id, data_key) "+
		                "VALUES ("+delegate.getIdString()+", "+attr.getIdString()+", "+
		                newId+")"
		            );
		            if(handler.shouldRetrieveAfterCreate())
		            {
		                try
		                {
		                    setAttribute(attr, handler.retrieve(newId, conn));
		                }
		                catch(EntityDoesNotExistException e)
		                {
		                    throw new BackendException("data integrity error", e);
		                }
		            }
		            else
		            {
		                setAttribute(attr, value);
		            }
	            }
            }
        }
    }

    /**
     * Called from {@link GenericResourceHandler} and {@link #update()} method.
     *
     * @param conn the JDBC connection to use.
     */
    synchronized void update(Connection conn)
        throws SQLException
    {
        super.update(conn);
        Statement stmt = conn.createStatement();
        for(AttributeDefinition attr : delegate.getResourceClass().getDeclaredAttributes())
        {
            if(isAttributeModified(attr))
            {
                if((attr.getFlags() & AttributeFlags.BUILTIN) == 0)
                {
	                AttributeHandler handler = attr.getAttributeClass().getHandler();
	                Object value = getAttribute(attr);
	                long id = getValueId(attr);
	                if(value != null)
	                {
	                    if(id == -1L)
	                    {
	                        long newId = handler.create(value, conn);
	                        stmt.execute(
	                            "INSERT INTO coral_generic_resource "+
	                            "(resource_id, attribute_definition_id, data_key) "+
	                            "VALUES ("+delegate.getIdString()+", "+attr.getIdString()+", "+
	                            newId+")"
	                        );
	                        setValueId(attr, newId);
	                    }
	                    else
	                    {
	                        try
	                        {
	                            handler.update(id, value, conn);
	                        }
	                        catch(EntityDoesNotExistException e)
	                        {
	                            throw new BackendException("Internal error", e);
	                        }
	                    }
	                }
	                else
	                {
	                    if(id != -1L)
	                    {
	                        try
	                        {
	                            handler.delete(id, conn);
	                        }
	                        catch(EntityDoesNotExistException e)
	                        {
	                            throw new BackendException("Internal error", e);
	                        }
	                        stmt.execute(
	                            "DELETE FROM coral_generic_resource "+
	                            "WHERE resource_id = "+delegate.getIdString()+
	                            " AND attribute_definition_id = "+attr.getIdString()
	                        );
	                        setValueId(attr, -1L);
	                    }
	                }
                }    
            }
        }
        clearModified();
    }

    synchronized void delete(Connection conn)
        throws SQLException
    {
        super.delete(conn);
        AttributeDefinition[] declared = delegate.getResourceClass().getDeclaredAttributes();
        Statement stmt = conn.createStatement();
        for(AttributeDefinition attr : declared)
        {
            long atId = getValueId(attr);
            try
            {
                attr.getAttributeClass().getHandler().delete(atId, conn);
            }
            catch(EntityDoesNotExistException e)
            {
                throw new BackendException("internal error", e);
            }
            stmt.execute("DELETE FROM coral_generic_resource WHERE "+
                         " resource_id = "+delegate.getIdString()+
                         " AND attribute_definition_id = "+attr.getIdString());
        }
    }
}
