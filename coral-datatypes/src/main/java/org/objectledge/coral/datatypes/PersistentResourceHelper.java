package org.objectledge.coral.datatypes;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.objectledge.coral.BackendException;
import org.objectledge.coral.entity.Entity;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.AttributeHandler;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.store.Resource;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.OutputRecord;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.Persistent;

/**
 * A common base class for Resource implementations using PersistenceService.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: PersistentResource.java,v 1.28 2005-06-22 11:35:33 rafal Exp $
 */
public class PersistentResourceHelper
{
    // instance variables ////////////////////////////////////////////////////

    public static final String[] KEY_COLUMNS = new String[] { "resource_id" };

    private final Persistence persistence;

    private final Resource delegate;

    private final ResourceAttributes instance;

    /**
     * Constructor.
     */
    public PersistentResourceHelper(Resource delegate, ResourceAttributes instance,
        Persistence persistence)
    {
        this.delegate = delegate;
        this.instance = instance;
        this.persistence = persistence;
    }

    // interface to PersistentResourceHandler ////////////////////////////////

    public synchronized void retrieve(Object data, Connection conn, Set<ResourceClass<?>> classes)
        throws SQLException
    {
        Map<ResourceClass<?>, InputRecord> in = getInputRecords(data, classes);
        if(in != null)
        {
            setData(in, conn);
        }
    }

    public synchronized void revert(Object data, Connection conn, Set<ResourceClass<?>> classes)
        throws SQLException
    {
        Map<ResourceClass<?>, InputRecord> in = getInputRecords(data, classes);
        if(in != null)
        {
            setData(in, conn);
        }
    }

    public synchronized void create(ResourceClass<?> rClass,
        Map<AttributeDefinition<?>, ?> attributes,
        Connection conn)
        throws SQLException
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
                persistence.save(new CreateView(instance, rClass, attributes, conn, true));
            }
        }
    }

    public synchronized void update(ResourceClass<?> rClass, Connection conn)
        throws SQLException
    {
        if(rClass.getDbTable() != null)
        {
            boolean hasConcreteAttributes = false;
            boolean hasChangedAttributes = false;
            for(AttributeDefinition<?> attr : rClass.getDeclaredAttributes())
            {
                if((attr.getFlags() & (AttributeFlags.BUILTIN | AttributeFlags.SYNTHETIC)) == 0)
                {
                    hasConcreteAttributes = true;
                    if(instance.isValueModified(attr))
                    {
                        hasChangedAttributes = true;
                        break;
                    }
                }
            }
            if(hasConcreteAttributes && hasChangedAttributes)
            {
                persistence.save(new UpdateView(instance, rClass, conn));
            }
        }
    }

    public synchronized void delete(ResourceClass<?> rClass, Connection conn)
        throws SQLException
    {
        for(AttributeDefinition<?> attr : rClass.getDeclaredAttributes())
        {
            if(!attr.getAttributeClass().getHandler().supportsExternalString()
                && !Entity.class.isAssignableFrom(attr.getAttributeClass().getJavaClass()))
            {
                long valueId = instance.getValueId(attr);
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
                persistence.delete(new DeleteView(delegate, rClass), false);
            }
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
            return delegate.getResourceClass().getHandler().loadValue(attribute, aId);
        }
    }

    // Persistent interface //////////////////////////////////////////////////

    // interface to PersistentResourceHandler ////////////////////////////////

    private static String getColumnName(AttributeDefinition<?> attr)
    {
        String dbColumn = attr.getDbColumn();
        return dbColumn != null ? dbColumn : attr.getName();
    }

    private Map<ResourceClass<?>, InputRecord> getInputRecords(Object data, Set<ResourceClass<?>> classes)
    {
        @SuppressWarnings("unchecked")
        Map<Long, Map<ResourceClass<?>, InputRecord>> rMap = (Map<Long, Map<ResourceClass<?>, InputRecord>>)data;
        Map<ResourceClass<?>, InputRecord> rcMap = rMap.get(delegate.getIdObject());
        if(rcMap != null)
        {
            Map<ResourceClass<?>, InputRecord> records = new HashMap<>();
            for(ResourceClass<?> rc : classes)
            {
                records.put(rc, rcMap.get(rc));
            }
            return records;
        }
        else
        {
            return null;
        }
    }

    private void setData(Map<ResourceClass<?>, InputRecord> records, Connection conn)
        throws SQLException
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
                        setAttribute(attr, record);
                    }
                }
            }
        }
    }

    private <T> void setAttribute(AttributeDefinition<T> attr, InputRecord data)
        throws SQLException
    {
        final String name = getColumnName(attr);
        if(attr.getAttributeClass().getHandler().supportsExternalString())
        {
            if(!data.isNull(name))
            {
                setValue(attr, instance, data.getObject(name));
            }
            else
            {
                instance.setValue(attr, null);
            }
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

    private static <A> void setValue(AttributeDefinition<A> attr, ResourceAttributes instance,
        Object rawValue)
    {
        A value = attr.getAttributeClass().getHandler().toAttributeValue(rawValue);
        instance.setValue(attr, value);
    }

    static Persistent getCreateView(ResourceAttributes instance,
        final ResourceClass<?> rClass, final Map<AttributeDefinition<?>, ?> attrValues,
        final Connection conn)
    {
        return new CreateView(instance, rClass, attrValues, conn, false);
    }

    private static abstract class PersistentView
        implements Persistent
    {
        protected final ResourceClass<?> rClass;

        public PersistentView(final ResourceClass<?> rClass)
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
            return KEY_COLUMNS;
        }

        @Override
        public void getData(OutputRecord record)
            throws SQLException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setData(InputRecord record)
            throws SQLException
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

    private static class CreateView
        extends PersistentView
    {
        private final ResourceAttributes instance;

        private final Map<AttributeDefinition<?>, ?> attrValues;

        private final Connection conn;

        private boolean insertCustomAttrs;

        public CreateView(final ResourceAttributes instance, final ResourceClass<?> rClass,
            final Map<AttributeDefinition<?>, ?> attrValues, final Connection conn,
            final boolean immediateCustomAttrs)
        {
            super(rClass);
            this.instance = instance;
            this.attrValues = attrValues;
            this.conn = conn;
            this.insertCustomAttrs = immediateCustomAttrs;
        }

        @Override
        public void getData(OutputRecord record)
            throws SQLException
        {
            record.setLong("resource_id", instance.getDelegate().getId());
            for(AttributeDefinition<?> attr : rClass.getDeclaredAttributes())
            {
                if((attr.getFlags() & (AttributeFlags.BUILTIN | AttributeFlags.SYNTHETIC)) == 0)
                {
                    getAttribute(attr, record);
                }
            }
            insertCustomAttrs = true;
        }

        private <T> void getAttribute(AttributeDefinition<T> attr, OutputRecord record)
            throws SQLException, SQLException
        {
            T value = (T)attrValues.get(attr);
            String name = getColumnName(attr);
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
                        if(insertCustomAttrs)
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
                    }
                    record.setLong(name, valueId);
                }
                else
                {
                    record.setNull(name);
                }
                instance.setValueId(attr, valueId);
            }
            instance.setValue(attr, value);
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

    private static class UpdateView
        extends PersistentView
    {
        private final ResourceAttributes instance;

        private final Connection conn;

        public UpdateView(final ResourceAttributes instance, final ResourceClass<?> rClass,
            final Connection conn)
        {
            super(rClass);
            this.instance = instance;
            this.conn = conn;
        }

        @Override
        public void getData(OutputRecord record)
            throws SQLException
        {
            record.setLong("resource_id", instance.getDelegate().getId());
            for(AttributeDefinition<?> attr : rClass.getDeclaredAttributes())
            {
                if((attr.getFlags() & (AttributeFlags.BUILTIN | AttributeFlags.SYNTHETIC)) == 0)
                {
                    if(instance.isValueModified(attr))
                    {
                        getAttribute(attr, record);
                    }
                }
            }
        }

        private <A> void getAttribute(AttributeDefinition<A> attr, OutputRecord record)
            throws SQLException, SQLException
        {
            String name = getColumnName(attr);
            A value = instance.getValue(attr);

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
                            id = handler.create(value, conn);
                            record.setLong(name, id);
                            instance.setValueId(attr, id);
                            record.setLong(name, id);
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
                        record.setNull(name);
                    }
                }
            }
        }

        @Override
        public boolean getSaved()
        {
            return true;
        }
    }

    private class DeleteView
        extends PersistentView
    {
        private final Resource delegate;

        public DeleteView(Resource delegate, ResourceClass<?> rClass)
        {
            super(rClass);
            this.delegate = delegate;
        }

        @Override
        public void getData(OutputRecord record)
            throws SQLException
        {
            record.setLong("resource_id", delegate.getId());
        }
    }
}
