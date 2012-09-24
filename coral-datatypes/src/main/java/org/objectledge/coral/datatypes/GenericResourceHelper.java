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
import org.objectledge.coral.store.Resource;
import org.objectledge.database.DatabaseUtils;

/**
 * A generic implementation of {@link Resource} interface.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: GenericResource.java,v 1.28 2007-04-04 23:16:03 rafal Exp $
 */
public class GenericResourceHelper
{
    private final Resource delegate;

    private final ResourceAttributesSupport instance;

    /**
     * Constructor.
     */
    public GenericResourceHelper(Resource delegate, ResourceAttributesSupport instance)
    {
        this.instance = instance;
        this.delegate = delegate;
    }

    // Package private ///////////////////////////////////////////////////////

    synchronized void retrieve(ResourceClass<?> rClass, Object data, Connection conn)
        throws SQLException
    {
        Map<AttributeDefinition<?>, Long> dataKeys = getDataKeys(data);
        if(dataKeys != null)
        {
            for(AttributeDefinition<?> declared : rClass.getDeclaredAttributes())
            {
                Long id = dataKeys.get(declared);
                if(id != null)
                {
                    instance.setValueId(declared, id);
                }
            }
        }
    }

    synchronized void revert(ResourceClass<?> rClass, Object data, Connection conn)
        throws SQLException
    {
        Map<AttributeDefinition<?>, Long> dataKeys = getDataKeys(data);
        if(dataKeys != null)
        {
            for(AttributeDefinition<?> declared : rClass.getDeclaredAttributes())
            {
                Long id = dataKeys.get(declared);
                if(id != null)
                {
                    instance.setValueId(declared, id);
                }
            }
        }
    }

    synchronized void create(ResourceClass<?> rClass, Map<AttributeDefinition<?>, ?> attributes,
        Connection conn)
        throws SQLException
    {
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
                        instance.setValueId(attr, newId);
                        stmt.execute("INSERT INTO coral_generic_resource "
                            + "(resource_id, attribute_definition_id, data_key) " + "VALUES ("
                            + delegate.getIdString() + ", " + attr.getIdString() + ", " + newId
                            + ")");
                        if(handler.shouldRetrieveAfterCreate())
                        {
                            try
                            {
                                setValue(attr, instance, handler.retrieve(newId, conn));
                            }
                            catch(EntityDoesNotExistException e)
                            {
                                throw new BackendException("data integrity error", e);
                            }
                        }
                        else
                        {
                            setValue(attr, instance, value);
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

    private static <A> void setValue(AttributeDefinition<A> attr, ResourceAttributes instance,
        Object rawValue)
    {
        A value = attr.getAttributeClass().getHandler().toAttributeValue(rawValue);
        instance.setValue(attr, value);
    }

    /**
     * Called from {@link GenericResourceHandler} and {@link #update()} method.
     * 
     * @param conn the JDBC connection to use.
     */
    synchronized void update(ResourceClass<?> rClass, Connection conn)
        throws SQLException
    {
        Statement stmt = conn.createStatement();
        try
        {
            for(AttributeDefinition<?> attr : rClass.getDeclaredAttributes())
            {
                if((attr.getFlags() & AttributeFlags.BUILTIN) == 0)
                {
                    if(instance.isValueModified(attr))
                    {
                        Object value = instance.getValue(attr);
                        long id = instance.getValueId(attr);
                        if(value != null)
                        {
                            if(id == -1L)
                            {
                                long newId = createAttrValue(attr, conn);
                                stmt.execute("INSERT INTO coral_generic_resource "
                                    + "(resource_id, attribute_definition_id, data_key) "
                                    + "VALUES (" + delegate.getIdString() + ", "
                                    + attr.getIdString() + ", " + newId + ")");
                                instance.setValueId(attr, newId);
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
                                stmt.execute("DELETE FROM coral_generic_resource "
                                    + "WHERE resource_id = " + delegate.getIdString()
                                    + " AND attribute_definition_id = " + attr.getIdString());
                                instance.setValueId(attr, -1L);
                            }
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

    synchronized void delete(ResourceClass<?> rClass, Connection conn)
        throws SQLException
    {
        AttributeDefinition<?>[] declared = rClass.getDeclaredAttributes();
        for(AttributeDefinition<?> attr : declared)
        {
            if((attr.getFlags() & AttributeFlags.BUILTIN) == 0)
            {
                long atId = instance.getValueId(attr);
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
            stmt.execute("DELETE FROM coral_generic_resource WHERE " + " resource_id = "
                + delegate.getIdString());
        }
        finally
        {
            DatabaseUtils.close(stmt);
        }
    }

    private Map<AttributeDefinition<?>, Long> getDataKeys(Object data)
    {
        @SuppressWarnings("unchecked")
        Map<Long, Map<AttributeDefinition<?>, Long>> dataKeyMap = (Map<Long, Map<AttributeDefinition<?>, Long>>)data;
        return dataKeyMap.get(delegate.getIdObject());
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
        A value = instance.getValue(attr);
        return handler.create(value, conn);
    }

    private <A> void updateAttrValue(AttributeDefinition<A> attr, Connection conn)
        throws EntityDoesNotExistException, SQLException
    {
        AttributeHandler<A> handler = attr.getAttributeClass().getHandler();
        A value = instance.getValue(attr);
        long valueId = instance.getValueId(attr);
        handler.update(valueId, value, conn);
    }
}
