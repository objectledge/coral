package org.objectledge.coral.datatypes;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

import org.objectledge.coral.BackendException;
import org.objectledge.coral.entity.Entity;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.AttributeHandler;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.schema.UnknownAttributeException;
import org.objectledge.coral.security.PermissionAssignment;
import org.objectledge.coral.security.Role;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.ConstraintViolationException;
import org.objectledge.coral.store.ModificationNotPermitedException;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ValueRequiredException;
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
        Map<ResourceClass<?>, InputRecord> in = getData(delegate, data);
        if(in != null)
        {
            setData(in);
        }
    }

    synchronized void revert(ResourceClass<?> rClass, Connection conn, Object data)
        throws SQLException
    {
        super.revert(rClass, conn, data);
        Map<ResourceClass<?>, InputRecord> in = getData(delegate, data);
        if(in != null)
        {
            setData(in);
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
                getPersistence().save(new CreateView(this, rClass, attributes, conn));
                this.saved = true;
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
        if(rClass.getDbTable() != null)
        {
            boolean hasConcreteAttributes = false;
            boolean hasChangedAttributes = false;
            for(AttributeDefinition<?> attr : rClass.getDeclaredAttributes())
            {
                if((attr.getFlags() & (AttributeFlags.BUILTIN | AttributeFlags.SYNTHETIC)) == 0)
                {
                    hasConcreteAttributes = true;
                    if(isModified(attr))
                    {
                        hasChangedAttributes = true;
                    }
                    break;
                }
            }
            if(hasConcreteAttributes && hasChangedAttributes)
            {
                getPersistence().save(new UpdateView(this, rClass, conn));
            }
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
                        setAttribute(attr, record, this);
                    }
                }
            }
        }
    }

    private static <T> void setAttribute(AttributeDefinition<T> attr, InputRecord data,
        AbstractResource instance)
        throws SQLException
    {
        final String name = attr.getName();
        if(attr.getAttributeClass().getHandler().supportsExternalString())
        {
            Object value = null;
            if(!data.isNull(name))
            {
                value = data.getObject(name);
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

    static Persistent getRetrieveView(ResourceClass<?> rClass)
    {
        return new RetrieveView(rClass);
    }

    static Persistent getCreateView(final ResourceClass<?> rClass,
        final Map<AttributeDefinition<?>, ?> attrValues,
        final Connection conn)
    {
        PersistentResource instance = new PersistentResource();
        instance.setDelegate(new SyntheticDelegate(rClass));
        return new CreateView(instance, rClass, attrValues, conn);
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
        private final PersistentResource instance;

        private final Map<AttributeDefinition<?>, ?> attrValues;

        private final Connection conn;

        public CreateView(final PersistentResource instance, final ResourceClass<?> rClass,
            final Map<AttributeDefinition<?>, ?> attrValues, final Connection conn)
        {
            super(rClass);
            this.instance = instance;
            this.attrValues = attrValues;
            this.conn = conn;
        }

        @Override
        public void getData(OutputRecord record)
            throws SQLException
        {
            record.setLong("resource_id", instance.delegate.getId());
            for(AttributeDefinition<?> attr : rClass.getDeclaredAttributes())
            {
                if((attr.getFlags() & (AttributeFlags.BUILTIN | AttributeFlags.SYNTHETIC)) == 0)
                {
                    getAttribute(attr, record);
                }
            }
        }

        private <T> void getAttribute(AttributeDefinition<T> attr, OutputRecord record)
            throws SQLException, SQLException
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

    private static class RetrieveView
        extends PersistentView
    {
        public RetrieveView(ResourceClass<?> rClass)
        {
            super(rClass);
        }
    }

    private static class UpdateView
        extends PersistentView
    {
        private final PersistentResource instance;

        private final Connection conn;

        public UpdateView(final PersistentResource instance, final ResourceClass<?> rClass,
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
            record.setLong("resource_id", instance.delegate.getId());
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

        private <A> void getAttribute(AttributeDefinition<A> attr, OutputRecord record)
            throws SQLException, SQLException
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

    private static class SyntheticDelegate
        implements Resource
    {
        private final ResourceClass<?> rClass;

        public SyntheticDelegate(ResourceClass<?> rClass)
        {
            this.rClass = rClass;
        }

        @Override
        public long getId()
        {
            return -1l;
        }

        @Override
        public Long getIdObject()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getIdString()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getName()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getPath()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public ResourceClass<?> getResourceClass()
        {
            return rClass;
        }

        @Override
        public Subject getCreatedBy()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Date getCreationTime()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Subject getModifiedBy()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Date getModificationTime()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Subject getOwner()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public PermissionAssignment[] getPermissionAssignments()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public PermissionAssignment[] getPermissionAssignments(Role role)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Resource getParent()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getParentId()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Resource[] getChildren()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isDefined(AttributeDefinition<?> attribute)
            throws UnknownAttributeException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T get(AttributeDefinition<T> attribute)
            throws UnknownAttributeException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T get(AttributeDefinition<T> attribute, T defaultValue)
            throws UnknownAttributeException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> void set(AttributeDefinition<T> attribute, T value)
            throws UnknownAttributeException, ModificationNotPermitedException,
            ValueRequiredException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void unset(AttributeDefinition<?> attribute)
            throws ValueRequiredException, UnknownAttributeException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setModified(AttributeDefinition<?> attribute)
            throws UnknownAttributeException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isModified(AttributeDefinition<?> attribute)
            throws UnknownAttributeException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void update()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public void revert()
        {
            throw new UnsupportedOperationException();

        }

        @Override
        public Resource getDelegate()
        {
            throw new UnsupportedOperationException();
        }
    }
}
