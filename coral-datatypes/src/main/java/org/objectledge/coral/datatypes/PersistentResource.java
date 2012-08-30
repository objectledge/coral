package org.objectledge.coral.datatypes;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.objectledge.coral.BackendException;
import org.objectledge.coral.entity.Entity;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.AttributeHandler;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.store.ConstraintViolationException;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ValueRequiredException;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.OutputRecord;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.PersistenceException;
import org.objectledge.database.persistence.Persistent;

/**
 * A common base class for Resource implementations using PersistenceService.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: PersistentResource.java,v 1.28 2005-06-22 11:35:33 rafal Exp $
 */
public class PersistentResource
    extends AbstractResource
{
    // instance variables ////////////////////////////////////////////////////

    public static final String[] KEY_COLUMNS = new String[] { "resource_id" };

    /** the unique id of the resource in it's db table. */
    protected boolean saved = false;

    /**
     * Constructor.
     */
    public PersistentResource()
    {
    }

    // interface to PersistentResourceHandler ////////////////////////////////

    private Map<ResourceClass<?>, InputRecord> getData(Resource deletage, Object data)
    {
        @SuppressWarnings("unchecked")
        Map<Long, Map<ResourceClass<?>, InputRecord>> rMap = (Map<Long, Map<ResourceClass<?>, InputRecord>>)data;
        Map<ResourceClass<?>, InputRecord> rcMap = rMap.get(deletage.getIdObject());
        if(rcMap != null)
        {
            return rcMap;
        }
        else
        {
            return null;
        }
    }

    synchronized void retrieve(Resource delegate, ResourceClass<?> rClass, Connection conn,
        Object data)
        throws SQLException
    {
        super.retrieve(delegate, rClass, conn, data);
        try
        {
            Map<ResourceClass<?>, InputRecord> in = getData(delegate, data);
            if(in != null)
            {
                setData(in);
            }
        }
        catch(PersistenceException e)
        {
            throw new BackendException("failed to restore resource state", e);
        }
    }

    synchronized void revert(ResourceClass<?> rClass, Connection conn, Object data)
        throws SQLException
    {
        super.revert(rClass, conn, data);
        try
        {
            Map<ResourceClass<?>, InputRecord> in = getData(delegate, data);
            if(in != null)
            {
                setData(in);
            }
        }
        catch(PersistenceException e)
        {
            throw new BackendException("failed to restore resource state", e);
        }
    }

    synchronized void create(Resource delegate, ResourceClass<?> rClass,
        Map<AttributeDefinition<?>, ?> attributes, Connection conn)
        throws SQLException, ValueRequiredException, ConstraintViolationException
    {
        super.create(delegate, rClass, attributes, conn);

        if(rClass.getDbTable() != null)
        {
            boolean hasConcreteAttributes = false;
            for(AttributeDefinition<?> attr : rClass.getDeclaredAttributes())
            {
                if((attr.getFlags() & (AttributeFlags.BUILTIN | AttributeFlags.SYNTHETIC)) == 0)
                {
                    hasConcreteAttributes = true;
                    break;
                }
            }
            if(hasConcreteAttributes)
            {
                try
                {
                    getPersistence().save(new CreateView(this, rClass, attributes, conn));
                    this.saved = true;
                }
                catch(PersistenceException e)
                {
                    throw new SQLException(e);
                }
            }
        }
    }

    synchronized void update(Connection conn)
        throws SQLException
    {
        super.update(conn);
        update(delegate.getResourceClass(), conn);
        for(ResourceClass<?> rClass : delegate.getResourceClass().getParentClasses())
        {
            update(rClass, conn);
        }
    }

    private void update(ResourceClass<?> rClass, Connection conn)
        throws SQLException
    {
        try
        {
            if(rClass.getDbTable() != null)
            {
                boolean hasConcreteAttributes = false;
                for(AttributeDefinition<?> attr : rClass.getDeclaredAttributes())
                {
                    if((attr.getFlags() & (AttributeFlags.BUILTIN | AttributeFlags.SYNTHETIC)) == 0)
                    {
                        hasConcreteAttributes = true;
                        break;
                    }
                }
                if(hasConcreteAttributes)
                {
                    getPersistence().save(new UpdateView(this, rClass, conn));
                }
            }
        }
        catch(PersistenceException e)
        {
            throw new BackendException("failed to update resource state", e);
        }
    }

    synchronized void delete(Connection conn)
        throws SQLException
    {
        super.delete(conn);

        for(AttributeDefinition<?> attr : delegate.getResourceClass().getAllAttributes())
        {
            if(!attr.getAttributeClass().getHandler().supportsExternalString()
                && !Entity.class.isAssignableFrom(attr.getAttributeClass().getJavaClass()))
            {
                long valueId = getValueId(attr);
                if(valueId != -1L)
                {
                    try
                    {
                        attr.getAttributeClass().getHandler().delete(valueId, conn);
                    }
                    catch(EntityDoesNotExistException e)
                    {
                        throw new BackendException("data integrity error", e);
                    }
                }
            }
        }

        delete(delegate.getResourceClass(), conn);
        for(ResourceClass<?> rClass : delegate.getResourceClass().getParentClasses())
        {
            delete(rClass, conn);
        }
    }

    private void delete(ResourceClass<?> rClass, Connection conn)
        throws SQLException
    {
        try
        {
            if(rClass.getDbTable() != null)
            {
                boolean hasConcreteAttributes = false;
                for(AttributeDefinition<?> attr : rClass.getDeclaredAttributes())
                {
                    if((attr.getFlags() & (AttributeFlags.BUILTIN | AttributeFlags.SYNTHETIC)) == 0)
                    {
                        hasConcreteAttributes = true;
                        break;
                    }
                }
                if(hasConcreteAttributes)
                {
                    getPersistence().delete(new DeleteView(delegate, rClass));
                }
            }
        }
        catch(PersistenceException e)
        {
            throw new BackendException("failed to delete resource state", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected <T> T loadAttribute(AttributeDefinition<T> attribute, long aId)
    {
        if(Entity.class.isAssignableFrom(attribute.getAttributeClass().getJavaClass()))
        {
            AttributeHandler<T> handler = attribute.getAttributeClass().getHandler();
            return handler.toAttributeValue(Long.toString(aId));
        }
        else
        {
            return super.loadAttribute(attribute, aId);
        }
    }

    // Persistent interface //////////////////////////////////////////////////

    private void setData(Map<ResourceClass<?>, InputRecord> records)
        throws PersistenceException
    {
        for(Map.Entry<ResourceClass<?>, InputRecord> entry : records.entrySet())
        {
            InputRecord record = entry.getValue();
            if(record != null)
            {
                for(AttributeDefinition<?> attr : entry.getKey().getDeclaredAttributes())
                {
                    if((attr.getFlags() & AttributeFlags.BUILTIN) == 0)
                    {
                        setAttribute(attr, record, this);
                    }
                }
            }
        }
    }

    private static <T> void setAttribute(AttributeDefinition<T> attr, InputRecord data,
        AbstractResource instance)
        throws PersistenceException
    {
        Class<T> aClass = attr.getAttributeClass().getJavaClass();
        final String name = attr.getName();
        if(attr.getAttributeClass().getHandler().supportsExternalString())
        {
            Object value = null;
            if(!data.isNull(name))
            {
                if(aClass.equals(Boolean.class))
                {
                    value = data.getBoolean(name);
                }
                if(aClass.equals(Byte.class))
                {
                    value = data.getByte(name);
                }
                if(aClass.equals(Short.class))
                {
                    value = data.getShort(name);
                }
                if(aClass.equals(Integer.class))
                {
                    value = data.getInteger(name);
                }
                if(aClass.equals(Long.class))
                {
                    value = data.getLong(name);
                }
                if(aClass.equals(BigDecimal.class))
                {
                    value = data.getBigDecimal(name);
                }
                if(aClass.equals(Float.class))
                {
                    value = data.getFloat(name);
                }
                if(aClass.equals(Double.class))
                {
                    value = data.getDouble(name);
                }
                if(aClass.equals(String.class))
                {
                    value = data.getString(name);
                }
                if(java.util.Date.class.isAssignableFrom(aClass))
                {
                    value = data.getDate(name);
                }
            }
            instance.setAttribute(attr, value);
        }
        else
        {
            if(data.isNull(name))
            {
                instance.setValueId(attr, -1);
            }
            else
            {
                instance.setValueId(attr, data.getLong(name));
            }
        }
    }

    /**
     * @return Returns the persistence.
     */
    private Persistence getPersistence()
    {
        return ((PersistentResourceHandler<?>)delegate.getResourceClass().getHandler())
            .getPersistence();
    }

    private static class CreateView
        implements Persistent
    {
        private final PersistentResource instance;

        private final ResourceClass<?> rClass;

        private final Map<AttributeDefinition<?>, ?> attrValues;

        private final Connection conn;

        public CreateView(final PersistentResource instance, final ResourceClass<?> rClass,
            final Map<AttributeDefinition<?>, ?> attrValues, final Connection conn)
        {
            this.instance = instance;
            this.rClass = rClass;
            this.attrValues = attrValues;
            this.conn = conn;
        }

        @Override
        public String getTable()
        {
            return rClass.getDbTable();
        }

        @Override
        public String[] getKeyColumns()
        {
            return KEY_COLUMNS;
        }

        @Override
        public void getData(OutputRecord record)
            throws PersistenceException
        {
            record.setLong("resource_id", instance.delegate.getId());
            try
            {
                for(AttributeDefinition<?> attr : rClass.getDeclaredAttributes())
                {
                    if((attr.getFlags() & (AttributeFlags.BUILTIN | AttributeFlags.SYNTHETIC)) == 0)
                    {
                        getAttribute(attr, record);
                    }
                }
            }
            catch(SQLException e)
            {
                throw new PersistenceException("failed to save resource data", e);
            }
        }

        private <T> void getAttribute(AttributeDefinition<T> attr, OutputRecord record)
            throws PersistenceException, SQLException
        {
            T value = (T)attrValues.get(attr);
            String name = attr.getName();
            AttributeHandler<T> handler = attr.getAttributeClass().getHandler();
            if(value != null)
            {
                value = handler.toAttributeValue(value);
                handler.checkDomain(attr.getDomain(), value);
            }

            if(attr.getAttributeClass().getHandler().supportsExternalString())
            {
                record.set(name, value);
            }
            else
            {
                long valueId = -1;
                if(value != null)
                {
                    if(value instanceof Entity)
                    {
                        valueId = ((Entity)value).getId();
                    }
                    else
                    {
                        valueId = handler.create(value, conn);
                        if(handler.shouldRetrieveAfterCreate())
                        {
                            try
                            {
                                value = handler.retrieve(valueId, conn);
                            }
                            catch(EntityDoesNotExistException e)
                            {
                                throw new BackendException("data integrity error", e);
                            }
                        }
                    }
                    record.setLong(name, valueId);
                }
                else
                {
                    record.setNull(name);
                }
                instance.setValueId(attr, valueId);
            }
            instance.setAttribute(attr, value);
        }

        @Override
        public void setData(InputRecord record)
            throws PersistenceException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean getSaved()
        {
            return false;
        }

        @Override
        public void setSaved(long id)
        {
            // noop
        }
    }

    static class RetrieveView
        implements Persistent
    {
        private final ResourceClass<?> rClass;

        public RetrieveView(ResourceClass<?> rClass)
        {
            this.rClass = rClass;

        }

        @Override
        public String getTable()
        {
            return rClass.getDbTable();
        }

        @Override
        public String[] getKeyColumns()
        {
            return PersistentResource.KEY_COLUMNS;
        }

        @Override
        public void getData(OutputRecord record)
            throws PersistenceException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setData(InputRecord record)
            throws PersistenceException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean getSaved()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setSaved(long id)
        {
            throw new UnsupportedOperationException();
        }
    }

    private static class UpdateView
        implements Persistent
    {
        private final PersistentResource instance;

        private final ResourceClass<?> rClass;

        private final Connection conn;

        public UpdateView(final PersistentResource instance, final ResourceClass<?> rClass,
            final Connection conn)
        {
            this.instance = instance;
            this.rClass = rClass;
            this.conn = conn;
        }

        @Override
        public String getTable()
        {
            return rClass.getDbTable();
        }

        @Override
        public String[] getKeyColumns()
        {
            return KEY_COLUMNS;
        }

        @Override
        public void getData(OutputRecord record)
            throws PersistenceException
        {
            record.setLong("resource_id", instance.delegate.getId());
            try
            {
                for(AttributeDefinition<?> attr : rClass.getDeclaredAttributes())
                {
                    if((attr.getFlags() & (AttributeFlags.BUILTIN | AttributeFlags.SYNTHETIC)) == 0)
                    {
                        if(instance.isAttributeModified(attr))
                        {
                            getAttribute(attr, record);
                        }
                    }
                }
            }
            catch(SQLException e)
            {
                throw new PersistenceException("failed to save resource data", e);
            }
        }

        private <A> void getAttribute(AttributeDefinition<A> attr, OutputRecord record)
            throws PersistenceException, SQLException
        {
            String name = attr.getName();
            A value = instance.getAttribute(attr);

            if(attr.getAttributeClass().getHandler().supportsExternalString())
            {
                record.set(name, value);
            }
            else
            {
                long id = instance.getValueId(attr);
                if(Entity.class.isAssignableFrom(attr.getAttributeClass().getJavaClass()))
                {
                    if(value != null)
                    {
                        long valueId = ((Entity)value).getId();
                        record.setLong(name, valueId);
                        instance.setValueId(attr, valueId);
                    }
                    else
                    {
                        record.setNull(name);
                        instance.setValueId(attr, -1L);
                    }
                }
                else
                {
                    AttributeHandler<A> handler = attr.getAttributeClass().getHandler();
                    if(value != null)
                    {
                        if(id == -1L)
                        {
                            long newId = handler.create(value, conn);
                            record.setLong(name, newId);
                            instance.setValueId(attr, newId);
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
                            record.setNull(name);
                            instance.setValueId(attr, -1L);
                        }
                    }
                }
            }
        }

        @Override
        public void setData(InputRecord record)
            throws PersistenceException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean getSaved()
        {
            return true;
        }

        @Override
        public void setSaved(long id)
        {
            throw new UnsupportedOperationException();
        }
    }

    private class DeleteView
        implements Persistent
    {
        private final ResourceClass<?> rClass;

        private final Resource delegate;

        public DeleteView(Resource delegate, ResourceClass<?> rClass)
        {
            this.delegate = delegate;
            this.rClass = rClass;
        }

        @Override
        public String getTable()
        {
            return rClass.getDbTable();
        }

        @Override
        public String[] getKeyColumns()
        {
            return PersistentResource.KEY_COLUMNS;
        }

        @Override
        public void getData(OutputRecord record)
            throws PersistenceException
        {
            record.setLong("resource_id", delegate.getId());
        }

        @Override
        public void setData(InputRecord record)
            throws PersistenceException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean getSaved()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setSaved(long id)
        {
            throw new UnsupportedOperationException();
        }
    }

}
