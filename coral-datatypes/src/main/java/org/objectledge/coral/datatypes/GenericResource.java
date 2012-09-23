package org.objectledge.coral.datatypes;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import org.objectledge.coral.BackendException;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.AttributeHandler;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.store.ConstraintViolationException;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ValueRequiredException;
import org.objectledge.database.DatabaseUtils;

/**
 * A generic implementation of {@link Resource} interface.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: GenericResource.java,v 1.28 2007-04-04 23:16:03 rafal Exp $
 */
public class GenericResource
    extends AbstractResource
{
    // Member objects ////////////////////////////////////////////////////////

    /**
     * Constructor.
     */
    public GenericResource()
    {
    }
    
    // Package private ///////////////////////////////////////////////////////

    synchronized void retrieve(Resource delegate, ResourceClass<?> rClass, 
                                 Connection conn, Object data)
        throws SQLException
    {
        super.retrieve(delegate, rClass, conn, data);
        Map<Long,Map<AttributeDefinition<?>,Long>> dataKeyMap = 
            (Map<Long,Map<AttributeDefinition<?>,Long>>)data;
        Map<AttributeDefinition<?>,Long> dataKeys = dataKeyMap.get(delegate.getIdObject());
        if(dataKeys != null)
        {            
            for(AttributeDefinition<?> declared : rClass.getDeclaredAttributes())
            {
                Long id = dataKeys.get(declared);
                if(id != null)
                {
                    setValueId(declared, id);
                }
            }
        }
    }

    synchronized void revert(ResourceClass<?> rClass, Connection conn, Object data)
        throws SQLException
    {
        super.revert(rClass, conn, data);
        Map<Long,Map<AttributeDefinition<?>,Long>> dataKeyMap = 
            (Map<Long,Map<AttributeDefinition<?>,Long>>)data;
        Map<AttributeDefinition<?>,Long> dataKeys = dataKeyMap.get(delegate.getIdObject());
        if(dataKeys != null)
        {          
            for(AttributeDefinition<?> declared : rClass.getDeclaredAttributes())
            {
                Long id = dataKeys.get(declared);
                if(id != null)
                {
                    setValueId(declared, id);
                }
            }
        }
    }

    synchronized void create(Resource delegate, ResourceClass<?> rClass, 
        Map<AttributeDefinition<?>, ?> attributes, Connection conn)
        throws SQLException, ValueRequiredException, ConstraintViolationException
    {
        super.create(delegate, rClass, attributes, conn);
        Statement stmt = conn.createStatement();
        try
        {
            
            for(AttributeDefinition<?> attr : rClass.getDeclaredAttributes())
            {
                if((attr.getFlags() & AttributeFlags.BUILTIN) == 0)
                {
                    AttributeHandler<?> handler = attr.getAttributeClass().getHandler();
                    Object value = attributes.get(attr);
                    if(value != null)
                    {
                        value = handler.toAttributeValue(value);
                        long newId = createAttrValue(attr, value, conn);
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
                                setValue(attr, handler.retrieve(newId, conn));
                            }
                            catch(EntityDoesNotExistException e)
                            {
                                throw new BackendException("data integrity error", e);
                            }
                        }
                        else
                        {
                            setValue(attr, value);
                        }
                    }
                }
            }
        }
        finally
        {
            DatabaseUtils.close(stmt);
        }
    }

    /**
     * Called from {@link GenericResourceHandler} and {@link #update()} method.
     *
     * @param conn the JDBC connection to use.
     */
    synchronized void update(ResourceClass<?> rClass, Connection conn)
        throws SQLException
    {
        super.update(rClass, conn);
        Statement stmt = conn.createStatement();
        try
        {
            for(AttributeDefinition<?> attr : rClass.getDeclaredAttributes())
            {
                if((attr.getFlags() & AttributeFlags.BUILTIN) == 0)
                {
                    if(isModifiedInternal(attr))
                    {
    	                Object value = getValue(attr);
    	                long id = getValueId(attr);
    	                if(value != null)
    	                {
    	                    if(id == -1L)
    	                    {
                                long newId = createAttrValue(attr, conn);
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
                                    updateAttrValue(attr, conn);
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
                                    attr.getAttributeClass().getHandler().delete(id, conn);
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
        finally
        {
            DatabaseUtils.close(stmt);            
        }
    }

    synchronized void delete(ResourceClass<?> rClass, Connection conn)
        throws SQLException
    {
        super.delete(rClass, conn);
        AttributeDefinition<?>[] declared = rClass.getDeclaredAttributes();
        for(AttributeDefinition<?> attr : declared)
        {
            if((attr.getFlags() & AttributeFlags.BUILTIN) == 0) 
            {                
                long atId = getValueId(attr);
                if(atId != -1L)
                {
                    try
                    {
                        attr.getAttributeClass().getHandler().delete(atId, conn);
                    }
                    catch(EntityDoesNotExistException e)
                    {
                        throw new BackendException("internal error", e);
                    }
                }
            }
        }
        Statement stmt = conn.createStatement();
        try
        {
            stmt.execute("DELETE FROM coral_generic_resource WHERE "+
                " resource_id = "+delegate.getIdString());
        }
        finally
        {
            DatabaseUtils.close(stmt);
        }
    }

    private <A> long createAttrValue(AttributeDefinition<A> attr, Object rawValue, Connection conn)
        throws SQLException
    {
        AttributeHandler<A> handler = attr.getAttributeClass().getHandler();
        A value = handler.toAttributeValue(rawValue);
        return handler.create(value, conn);
    }

    private <A> long createAttrValue(AttributeDefinition<A> attr, Connection conn)
        throws SQLException
    {
        AttributeHandler<A> handler = attr.getAttributeClass().getHandler();
        A value = getValue(attr);
        return handler.create(value, conn);
    }

    private <A> void updateAttrValue(AttributeDefinition<A> attr, Connection conn)
        throws EntityDoesNotExistException, SQLException
    {
        AttributeHandler<A> handler = attr.getAttributeClass().getHandler();
        A value = getValue(attr);
        long valueId = getValueId(attr);
        handler.update(valueId, value, conn);
    }
}
