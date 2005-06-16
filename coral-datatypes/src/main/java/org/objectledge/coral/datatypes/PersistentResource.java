package org.objectledge.coral.datatypes;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jcontainer.dna.Logger;
import org.objectledge.coral.BackendException;
import org.objectledge.coral.entity.Entity;
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
import org.objectledge.database.persistence.DefaultPersistence;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.OutputRecord;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.PersistenceException;
import org.objectledge.database.persistence.Persistent;

/**
 * A common base class for Resource implementations using PersistenceService.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: PersistentResource.java,v 1.20 2005-06-16 06:43:23 rafal Exp $
 */
public class PersistentResource
    extends AbstractResource implements Persistent
{
    // instance variables ////////////////////////////////////////////////////

    /** the persistence component. */
    protected Persistence persistence;
    
    /** the unique id of the resource in it's db table. */
    protected long id = -1;

    /** the resource's key column. */
    protected String[] keyColumns;

    /** the resource's db table. */
    protected String dbTable;
    
    /**
     * Constructor.
     * 
     * @param coralSchema the Coral Schema
     * @param database the database.
     * @param logger the logger.
     */
    public PersistentResource(CoralSchema coralSchema, Database database, Logger logger)
    {
        super(database, logger);
        // it would be better to have it passed through constructor. OTOH persistence is 
        // lightweitght and stateless so it's not much of a problem.
        persistence = new DefaultPersistence(database, logger);
    }
        
    // interface to PersistentResourceHandler ////////////////////////////////
    
    synchronized void retrieve(Resource delegate, ResourceClass rClass, Connection conn, 
        Object data)
    	throws SQLException
	{
        super.retrieve(delegate, rClass, conn, data);
        try
        {
        	InputRecord in = (InputRecord)((Map)data).get(delegate.getIdObject());
            setData(in);
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
        try
        {
            InputRecord in = (InputRecord)((Map)data).get(delegate.getIdObject());
            setData(in);
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

    /**
     * {@inheritDoc}
     */
    protected Object loadAttribute(AttributeDefinition attribute, long aId)
    {
        if(Entity.class.isAssignableFrom(attribute.getAttributeClass().getJavaClass()))
        {
            AttributeHandler handler = attribute.getAttributeClass().getHandler();
            return handler.toAttributeValue(""+aId);
        }
        else
        {
            return super.loadAttribute(attribute, aId);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    protected void initAttributeMap(Resource delegate, ResourceClass resourceClass,
        List<ResourceClass> directParentClasses)
    {
    	super.initAttributeMap(delegate, resourceClass, directParentClasses);
    	initPersistence(delegate);
    }
    
    void initPersistence(Resource delegate)
    {
        this.delegate = delegate;
    	dbTable = delegate.getResourceClass().getDbTable();
    	keyColumns = new String[1];
    	keyColumns[0] = dbTable+"_id";    	
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
        AttributeDefinition[] attrs = delegate.getResourceClass().getAllAttributes();
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
                        Long attrId = (Long)ids.get(attribute);
                        if(attrId != null)
                        {
                            record.setLong(attribute.getName(), attrId.longValue());
                        }
                        else
                        {
                            record.setNull(attribute.getName());
                        }
                    }
                }
                else
                {
                    Long attrId = (Long)ids.get(attribute);
                    attrId = updateAttribute(attribute, attrId, value);
                    if(attrId != null)
                    {
                        record.setLong(attribute.getName(), attrId.longValue());
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
        AttributeDefinition[] attrs = delegate.getResourceClass().getAllAttributes();
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
    
    /**
     * Gets the local identifier unique for object class. 
     * 
     * @return the persistent id.
     */
    public long getPersistentId()
    {
    	return id;
    }
}
