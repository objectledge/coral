package org.objectledge.coral.datatypes;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jcontainer.dna.Logger;
import org.objectledge.coral.BackendException;
import org.objectledge.coral.entity.Entity;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.AttributeHandler;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.schema.UnknownAttributeException;
import org.objectledge.coral.store.ConstraintViolationException;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ValueRequiredException;
import org.objectledge.database.Database;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.OutputRecord;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.PersistenceException;
import org.objectledge.database.persistence.Persistent;

/**
 * A common base class for Resource implementations using PersistenceService.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: PersistentResource.java,v 1.7 2004-07-22 16:35:46 zwierzem Exp $
 */
public class PersistentResource
    extends AbstractResource implements Persistent
{
    // instance variables ////////////////////////////////////////////////////

    /** the persistence component. */
    protected Persistence persistence;
    
    /** the resource class. */
    protected ResourceClass resourceClass;

    /** the unique id of the resource in it's db table. */
    protected long id = -1;

    /** the resource's key column. */
    protected String[] keyColumns;

    /** the resource's db table. */
    protected String dbTable;
    
    /** the attributes (AttributeDefinition -> Object). */
    protected Map attributes = new HashMap();

    /** the external attribute ids. */
    protected Map ids = new HashMap();

    /** modified attributes (AttibuteDefinition set). */
    protected Set modified = new HashSet();
    
    /**
     * Constructor.
     * 
     * @param database the database.
     * @param logger the logger.
     * @param resourceClass the resource class.
     */
    public PersistentResource(Database database, Logger logger, ResourceClass resourceClass)
    {
        super(database, logger);
        this.resourceClass = resourceClass;
        dbTable = resourceClass.getDbTable();
        keyColumns = new String[] { dbTable+"_id" };
    }
        
    // interface to PersistentResourceHandler ////////////////////////////////
    
    synchronized void retrieve(Resource delegate, ResourceClass rClass, Connection conn, 
        Object data)
    	throws SQLException
	{
        super.retrieve(delegate, rClass, conn, data);
        try
        {
            setData((InputRecord)data);
        }
        catch(PersistenceException e)
        {
            throw new BackendException("failed to restore resource state", e);
        }
	}

    synchronized void revert(ResourceClass rClass, Connection conn, Object data)
        throws SQLException
    {
        super.revert(rClass, conn, data);
        attributes.clear();
        ids.clear();
        try
        {
            setData((InputRecord)data);
        }
        catch(PersistenceException e)
        {
            throw new BackendException("failed to restore resource state", e);
        }        
    }    
    
    synchronized void create(Resource delegate, ResourceClass rClass, 
        Map attributes, Connection conn)
    	throws SQLException, ValueRequiredException, ConstraintViolationException
	{
        super.create(delegate, rClass, attributes, conn);
        AttributeDefinition[] attrs = delegate.getResourceClass().getAllAttributes();
        for(int i=0; i<attrs.length; i++)
        {
            AttributeDefinition attr = attrs[i];
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
                    this.attributes.put(attr, value);
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
                                this.attributes.put(attr, handler.retrieve(valueId, conn));
                            }
                            catch(EntityDoesNotExistException e)
                            {
                                throw new BackendException("data integrity error", e);
                            }
                        }
                    }
                    ids.put(attr, new Long(valueId));
                }
            }
        }
        try
        {
            persistence.save(this);
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
            persistence.save(this);
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
        Iterator i = ids.keySet().iterator();
        while(i.hasNext())
        {
            AttributeDefinition attr = (AttributeDefinition)i.next();
            if(!Entity.class.isAssignableFrom(attr.getAttributeClass().getJavaClass()))
            {
                long valueId = ((Long)ids.get(attr)).longValue();
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
        try
        {
            persistence.delete(this);
        }
        catch(PersistenceException e)
        {
            throw new BackendException("failed to delete resource state", e);
        }        
	}    

    // attribute access //////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    protected boolean isDefinedLocally(AttributeDefinition attribute)
    {
        if(modified.contains(attribute))
        {
            return attributes.get(attribute) != null;
        }
        else
        {
            if(attributes.get(attribute) != null)
            {
                return true;
            }
            else
            {
                return ids.containsKey(attribute);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    protected Object getLocally(AttributeDefinition attribute)
        throws UnknownAttributeException
    {
        Object value = attributes.get(attribute);
        if(modified.contains(attribute))
        {
            return value;
        }
        else
        {
            if(value != null)
            {
                return value;
            }
            else
            {
                Long idObj = (Long)ids.get(attribute);
                if(idObj == null)
                {
                    return null;
                }
                else
                {
                    value = loadAttribute(attribute, idObj.longValue());
                    attributes.put(attribute, value);
                    return value;
                }
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    protected void setLocally(AttributeDefinition attribute, Object value)
    {
        attributes.put(attribute, value);
        modified.add(attribute);
    }

    /**
     * {@inheritDoc}
     */
    protected void unsetLocally(AttributeDefinition attribute)
    {
        attributes.remove(attribute);
        modified.add(attribute);
    }

    protected Object loadAttribute(AttributeDefinition attribute, long aId)
    {
        if(Entity.class.isAssignableFrom(attribute.getAttributeClass().getJavaClass()))
        {
            AttributeHandler handler = attribute.getAttributeClass().getHandler();
            return handler.toAttributeValue(""+id);
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
        return dbTable;
    }
    
    /**
     * {@inheritDoc}
     */
    public String[] getKeyColumns()
    {
        return keyColumns;
    }

    /**
     * {@inheritDoc}
     */
    public void getData(OutputRecord record)
        throws PersistenceException
    {
        AttributeDefinition[] attrs = resourceClass.getAllAttributes();
        record.setLong(getTable()+"_id", id);
        record.setLong("resource_id", delegate.getId());
        for(int i=0; i<attrs.length; i++)
        {
            AttributeDefinition attribute = attrs[i];
            AttributeHandler handler = attribute.getAttributeClass().getHandler();
            Object value = null;
            if((attribute.getFlags() & AttributeFlags.BUILTIN) != 0)
            {
                value = delegate.get(attribute);
            }
            else
            {
                value = attributes.get(attribute);
            }
            if(!attribute.getName().equals("id"))
            {
                if(handler.supportsExternalString())
                {
                    if(value != null)
                    {
                        if(value instanceof Date)
                        {
                            record.setDate(attribute.getName(), (Date)value);
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
                    if(modified.contains(attribute))
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
                        Long id = (Long)ids.get(attribute);
                        if(id != null)
                        {
                            record.setLong(attribute.getName(), id.longValue());
                        }
                        else
                        {
                            record.setNull(attribute.getName());
                        }
                    }
                }
                else
                {
                    Long id = (Long)ids.get(attribute);
                    id = updateAttribute(attribute, id, value);
                    if(id != null)
                    {
                        record.setLong(attribute.getName(), id.longValue());
                    }
                    else
                    {
                        record.setNull(attribute.getName());
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
        id = record.getLong(getTable()+"_id");
        AttributeDefinition[] attrs = resourceClass.getAllAttributes();
        for(int i=0; i<attrs.length; i++)
        {
            AttributeDefinition attribute = attrs[i];
            if((attribute.getFlags() & AttributeFlags.BUILTIN) == 0)
            {
                if(!record.isNull(attribute.getName()))
                {
                    AttributeHandler handler = attribute.getAttributeClass().getHandler();
                    if(attribute.getAttributeClass().getJavaClass().equals(Date.class))
                    {
                        Date value = record.getDate(attribute.getName());
                        attributes.put(attribute, value);
                    }
                    else 
                    {
                        if(handler.supportsExternalString())
                        {
                            Object value = handler.
                                toAttributeValue(record.getString(attribute.getName()));
                            attributes.put(attribute, value);
                        }
                        else
                        {
                            long valueId = record.getLong(attribute.getName());
                            ids.put(attribute, new Long(valueId));
                        }
                    }
                }
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
        return id != -1;
    }
    
    /**
     * Sets the 'saved' flag for the object.
     *
     * <p>The id generation will take place only for objects that declare a
     * single column primary key. Othre objects will receive a <code>-1</code>
     * as the <code>id</code> parameter. After this call is made on an object,
     * subsequent calls to {@link #getSaved()} on the same object should
     * return true.</p> 
     *
     * @param id The generated value of the primary key.
     */
    public void setSaved(long id)
    {
        this.id = id;
    }
}
