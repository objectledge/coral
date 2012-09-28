package org.objectledge.coral.datatypes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

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

    public synchronized void retrieve(Set<ResourceClass<?>> classes, Object data, Connection conn)
        throws SQLException
    {
        Map<AttributeDefinition<?>, Long> dataKeys = getDataKeys(data);
        if(dataKeys != null)
        {
            for(ResourceClass<?> rClass : classes)
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
    }

    public synchronized void revert(Set<ResourceClass<?>> classes, Object data, Connection conn)
        throws SQLException
    {
        Map<AttributeDefinition<?>, Long> dataKeys = getDataKeys(data);
        if(dataKeys != null)
        {
            for(ResourceClass<?> rClass : classes)
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
    }

    public synchronized void create(Set<ResourceClass<?>> classes,
        Map<AttributeDefinition<?>, ?> attributes, Connection conn)
        throws SQLException
    {
        PreparedStatement stmt = null;
        try
        {
            for(ResourceClass<?> rClass : classes)
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
                            if(stmt == null)
                            {
                                stmt = conn
                                    .prepareStatement("INSERT INTO coral_generic_resource "
                                        + "(resource_id, attribute_definition_id, data_key) VALUES (?, ?, ?)");
                                stmt.setLong(1, delegate.getId());
                            }
                            stmt.setLong(2, attr.getId());
                            stmt.setLong(3, newId);
                            stmt.addBatch();
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
            if(stmt != null)
            {
                stmt.executeBatch();
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
    public synchronized void update(Set<ResourceClass<?>> classes, Connection conn)
        throws SQLException
    {
        PreparedStatement createStmt = null;
        PreparedStatement deleteStmt = null;
        try
        {
            for(ResourceClass<?> rClass : classes)
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
                                    if(createStmt == null)
                                    {
                                        createStmt = conn
                                            .prepareStatement("INSERT INTO coral_generic_resource "
                                                + "(resource_id, attribute_definition_id, data_key) VALUES (?, ?, ?)");
                                        createStmt.setLong(1, delegate.getId());
                                    }
                                    createStmt.setLong(2, attr.getId());
                                    createStmt.setLong(3, newId);
                                    createStmt.addBatch();
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
                                    if(deleteStmt == null)
                                    {
                                        deleteStmt = conn
                                            .prepareStatement("DELETE FROM coral_generic_resource WHERE resource_id = ? "
                                                + "AND attribute_definition_id = ?");
                                        deleteStmt.setLong(1, delegate.getId());
                                    }
                                    deleteStmt.setLong(2, attr.getId());
                                    deleteStmt.addBatch();
                                    instance.setValueId(attr, -1L);
                                }
                            }
                        }
                    }
                }
            }
            if(createStmt != null)
            {
                createStmt.executeBatch();
            }
            if(deleteStmt != null)
            {
                deleteStmt.executeBatch();
            }
        }
        finally
        {
            DatabaseUtils.close(createStmt);
            DatabaseUtils.close(deleteStmt);
        }
    }

    public synchronized void delete(Set<ResourceClass<?>> classes, Connection conn)
        throws SQLException
    {
        PreparedStatement stmt = null;
        try
        {
            for(ResourceClass<?> rClass : classes)
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
                if(stmt == null)
                {
                    stmt = conn
                        .prepareStatement("DELETE FROM coral_generic_resource WHERE resource_id = ?");
                }
                stmt.setLong(1, delegate.getId());
                stmt.addBatch();
            }
            if(stmt != null)
            {
                stmt.executeBatch();
            }
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
