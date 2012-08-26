package org.objectledge.coral.datatypes;

import java.math.BigDecimal;
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
    extends AbstractResource implements Persistent
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
        Map<AttributeDefinition<?>, Object> attributes, Connection conn)
    	throws SQLException, ValueRequiredException, ConstraintViolationException
	{
        super.create(delegate, rClass, attributes, conn);
        for(AttributeDefinition<?> attr : delegate.getResourceClass().getAllAttributes())
        {
            Object value;
            if((attr.getFlags() & AttributeFlags.BUILTIN) != 0)
            {
                value = delegate.get(attr);
            }
            else
            {
            	value = attributes.get(attr);
            } 
            if(value != null)
            {
                AttributeHandler handler = attr.getAttributeClass().getHandler();
                value = handler.toAttributeValue(value);
                handler.checkDomain(attr.getDomain(), value);
                if(handler.supportsExternalString())
                {
                    setAttribute(attr, value);
                }
                else
                {
                    long valueId;
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
                                setAttribute(attr, handler.retrieve(valueId, conn));
                            }
                            catch(EntityDoesNotExistException e)
                            {
                                throw new BackendException("data integrity error", e);
                            }
                        }
                    }
                    setValueId(attr, valueId);
                }
            }
        }
        try
        {
            getPersistence().save(this);
        }
        catch(PersistenceException e)
        {
            throw new BackendException("failed to store resource state", e);
        }        
	}

    synchronized void update(Connection conn)
	    throws SQLException
	{
	    super.update(conn);
        try
        {
            getPersistence().save(this);
        }
        catch(PersistenceException e)
        {
            throw new BackendException("failed to store resource state", e);
        }        
	}

    synchronized void delete(Connection conn)
	    throws SQLException
	{
	    super.delete(conn);
        for(AttributeDefinition<?> attr : delegate.getResourceClass().getAllAttributes())
        {
            if(!Entity.class.isAssignableFrom(attr.getAttributeClass().getJavaClass()))
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
        try
        {
            getPersistence().delete(this);
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
            return handler.toAttributeValue(""+aId);
        }
        else
        {
            return super.loadAttribute(attribute, aId);
        }
    }
    
    // Persistent interface //////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public String getTable()
    {
        return delegate.getResourceClass().getDbTable();
    }
    
    /**
     * {@inheritDoc}
     */
    public String[] getKeyColumns()
    {
        return KEY_COLUMNS;
    }

    /**
     * {@inheritDoc}
     */
    public void getData(OutputRecord record)
        throws PersistenceException
    {
        record.setLong("resource_id", delegate.getId());
        for(AttributeDefinition attribute : delegate.getResourceClass().getAllAttributes())
        {
            AttributeHandler handler = attribute.getAttributeClass().getHandler();
            Object value = null;
            if((attribute.getFlags() & AttributeFlags.BUILTIN) != 0)
            {
                value = delegate.get(attribute);
            }
            else
            {
                value = getAttribute(attribute);
            }
            if(!attribute.getName().equals("id"))
            {
                if(handler.supportsExternalString())
                {
                    if(value != null)
                    {
                        if(value instanceof Date)
                        {
                            record.setTimestamp(attribute.getName(), (Date)value);
                        }
                        else
                        {
                            if(value instanceof String)
                            {
                                record.setString(attribute.getName(), (String)value);
                            }
                            else
                            {
                                record.setString(attribute.getName(), 
                                    handler.toExternalString(value));
                            }
                        }
                    }
                    else
                    {
                        record.setNull(attribute.getName());
                    }
                }
                else if(Entity.class.isAssignableFrom(attribute.getAttributeClass().getJavaClass()))
                {
                    if(isAttributeModified(attribute))
                    {
                        if(value != null)
                        {
                            record.setLong(attribute.getName(), ((Entity)value).getId());
                        }
                        else
                        {
                            record.setNull(attribute.getName());
                        }
                    }
                    else
                    {
                        long attrId = getValueId(attribute);
                        if(attrId != -1L)
                        {
                            record.setLong(attribute.getName(), attrId);
                        }
                        else
                        {
                            record.setNull(attribute.getName());
                        }
                    }
                }
                else
                {
                    long attrId = getValueId(attribute);
                    attrId = updateAttribute(attribute, attrId, value);
                    if(attrId != -1L)
                    {
                        record.setLong(attribute.getName(), attrId);
                    }
                    else
                    {
                        record.setNull(attribute.getName());
                    }
                }
            }
        }
    }
    
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

    /**
     * {@inheritDoc}
     */
    public void setData(InputRecord record)
        throws PersistenceException
    {
        throw new UnsupportedOperationException("should not be called directly");
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
                if(aClass.equals(Byte.class))
                {
                    value = data.getByte(name);
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
     * Returns the 'saved' flag for the object.
     *
     * <p>The flag is off for objects that haven't been saved in the db yet,
     * thus need <code>INSERT</code> statemets, and on for object that have
     * already been saved, thus need <code>UPDATE</code> statements.</p>
     *
     * @return the state of 'saved' flag.
     */
    public boolean getSaved()
    {
        return saved;
    }
    
    /**
     * Sets the 'saved' flag for the object.
     * <p>
     * The id generation will take place only for objects that declare a single column primary key.
     * Other objects will receive a <code>-1</code> as the <code>id</code> parameter. After this
     * call is made on an object, subsequent calls to {@link #getSaved()} on the same object should
     * return true.
     * </p>
     * 
     * @param id The generated value of the primary key.
     */
    public void setSaved(long id)
    {
        this.saved = true;
    }

    /**
     * @return Returns the persistence.
     */
    protected Persistence getPersistence()
    {
        return ((PersistentResourceHandler)delegate.getResourceClass().getHandler()).
            getPersistence();
    }
}
