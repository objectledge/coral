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
import org.objectledge.coral.security.PermissionAssignment;
import org.objectledge.coral.security.Role;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.ModificationNotPermitedException;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ValueRequiredException;
import org.objectledge.database.Database;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.OutputRecord;
import org.objectledge.database.persistence.PersistenceException;
import org.objectledge.database.persistence.Persistent;

/**
 * A common base class for Resource implementations using PersistenceService.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: PersistentResource.java,v 1.4 2004-05-06 13:13:07 pablo Exp $
 */
public class PersistentResource
    implements Resource, Persistent
{
    // instance variables ////////////////////////////////////////////////////

    /** the database service. */
    protected Database database;
    
    /** the logging facility. */
    protected Logger logger;

    /** the resource class. */
    protected ResourceClass resourceClass;

    /** the security delegate object. */
    protected Resource delegate;

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
        this.database = database;
        this.logger = logger;
        this.resourceClass = resourceClass;
        dbTable = resourceClass.getDbTable();
        keyColumns = new String[] { dbTable+"_id" };
    }
        
    // interface to PersistentResourceHandler ////////////////////////////////
    
    /**
     * Load the resource.
     * 
     * @param delegate the delegate.
     */
    public void loadResource(Resource delegate)
    {
        this.delegate = delegate;
    }

    /**
     * Create the resource.
     * 
     * @param delegate the delegate resource.
     * @param attributes the attributes map.
     * @param conn the connection.
     * @throws ValueRequiredException if required attribute was not set.
     * @throws SQLException if failed to create the resourceo in database.
     */
    public void createResource(Resource delegate, Map attributes, Connection conn)
        throws ValueRequiredException, SQLException
    {
        this.delegate = delegate;
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
            if(value == null)
            {
                if((attr.getFlags() & AttributeFlags.REQUIRED) != 0)
                {
                    throw new ValueRequiredException("value for REQUIRED attribute "+
                                                     attr.getName()+" is missing");
                }
            }
            else
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
    }

    /**
     * Delete the resource.
     *  
     * @param conn the connection.
     * @throws SQLException if failed to delete.
     */
    public void deleteResource(Connection conn)
        throws SQLException
    {
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
    }

    // Resource interface ////////////////////////////////////////////////////

    /**
     * Returns the numerical identifier of the entity.
     * 
     * @return the numerical identifier of the entity.
     */
    public long getId()
    {
        return delegate.getId();
    }
    
    /**
     * Returns the name of the entity.
     *
     * @return the name of the entity.
     */
    public String getName()
    {
        return delegate.getName();
    }

    /**
     * Returns the path name of the resource.
     *
     * <p>The path name is composed of the names of all of the resource's
     * parents, separated by / characters. If the top level parent (resource
     * that has <code>null</code> parent) is the 'root' resource #1, the
     * pathname will start with a /. Please note that the pathname can also
     * denote other resources than this one, unless all resources in your
     * system have unique names.</p>
     *
     * @return the pathname of the resource.
     */
    public String getPath()
    {
        return delegate.getPath();
    }
    
    /**
     * Returns the class this resource belongs to.
     *
     * @return the class this resource belongs to.
     */
    public ResourceClass getResourceClass()
    {
        return delegate.getResourceClass();
    }

    /**
     * Returns the {@link Subject} that created this resource.
     *
     * @return the {@link Subject} that created this resource.
     */
    public Subject getCreatedBy()
    {
        return delegate.getCreatedBy();
    }
    
    /**
     * Returns the creation time for this resource.
     *
     * @return the creation time for this resource.
     */
    public Date getCreationTime()
    {
        return delegate.getCreationTime();
    }

    /**
     * Returns the {@link Subject} that modified this resource most recently.
     *
     * @return the {@link Subject} that modified this resource most recently.
     */
    public Subject getModifiedBy()
    {
        return delegate.getModifiedBy();
    }

    /**
     * Returns the last modification time for this resource.
     *
     * @return the last modification time for this resource.
     */
    public Date getModificationTime()
    {
        return delegate.getModificationTime();
    }

    /**
     * Returns the owner of the resource.
     *
     * @return the owner of the resource.
     */
    public Subject getOwner()
    {
        return delegate.getOwner();
    }

    /**
     * Returns the access control list for this resource.
     *
     * @return the access control list for this resource.
     */
    public PermissionAssignment[] getPermissionAssignments()
    {
        return delegate.getPermissionAssignments();
    }

    /**
     * Returns the access control list entries for a specific role.
     *
     * @param role the role.
     * @return the access control list entries for a specific role.
     */
    public PermissionAssignment[] getPermissionAssignments(Role role)
    {
        return delegate.getPermissionAssignments(role);
    }

    /**
     * Returns the parent resource.
     *
     * <p><code>null</code> is returned for top-level (root)
     * resources. Depending on the application one or more top-level resources
     * exist in the system.</p>
     *
     * @return the parent resource.
     */
    public Resource getParent()
    {
        return delegate.getParent();
    }

    /**
     * Returns the security delegate object.
     *
     * @return the security delegate object.
     */
    public Resource getDelegate()
    {
        return delegate;
    }

    // attribute access //////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public boolean isDefined(AttributeDefinition attribute)
    {
        checkAttribute(attribute);
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
    public Object get(AttributeDefinition attribute)
        throws UnknownAttributeException
    {
        checkAttribute(attribute);
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
    public void set(AttributeDefinition attribute, Object value)
        throws UnknownAttributeException, ModificationNotPermitedException,
               ValueRequiredException
    {
        checkAttribute(attribute);
        if((attribute.getFlags() & AttributeFlags.BUILTIN) != 0)
        {
            delegate.set(attribute, value);
        }
        if((attribute.getFlags() & AttributeFlags.READONLY) != 0)
        {
            throw new ModificationNotPermitedException("attribute "+attribute.getName()+
                                                       " is declared READONLY");
        }
        if(value == null && (attribute.getFlags() & AttributeFlags.REQUIRED) != 0)
        {
            throw new ValueRequiredException("attribute "+attribute.getName()+
                                             "is declared as REQUIRED");
        }
        value = attribute.getAttributeClass().getHandler().toAttributeValue(value);
        attribute.getAttributeClass().getHandler().checkDomain(attribute.getDomain(), value);
        attributes.put(attribute, value);
        modified.add(attribute);
    }

    /**
     * {@inheritDoc}
     */
    public void unset(AttributeDefinition attribute)
        throws ValueRequiredException, UnknownAttributeException
    {
        checkAttribute(attribute);
        if((attribute.getFlags() & AttributeFlags.BUILTIN) != 0)
        {
            delegate.unset(attribute);
        }
        if((attribute.getFlags() & AttributeFlags.REQUIRED) != 0)
        {
            throw new ValueRequiredException("attribute "+attribute.getName()+
                                             "is declared as REQUIRED");
        }
        attributes.remove(attribute);
    }
    
    /**
     * {@inheritDoc}
     */
    public void setModified(AttributeDefinition attribute)
        throws UnknownAttributeException
    {
        checkAttribute(attribute);
        modified.add(attribute);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isModified(AttributeDefinition attribute)
        throws UnknownAttributeException
    {
        checkAttribute(attribute);
        return modified.contains(attribute);
    }

    // update & revert ///////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public void update()
        throws IllegalArgumentException, UnknownAttributeException
    {
        delegate.update();
        try
        {
            delegate.getResourceClass().getHandler().update(this, null);
        }
        catch(SQLException e)
        {
            throw new BackendException("unexpected SQLException", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void revert()
    {
        try
        {
            delegate.getResourceClass().getHandler().revert(this, null);
        }
        catch(SQLException e)
        {
            throw new BackendException("unexpected SQLException", e);
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
                        Long valueIdObj = (Long)ids.get(attribute);
                        long valueId = valueIdObj != null ? valueIdObj.longValue() : -1;
                        if(valueId != -1)
                        {
                            record.setLong(attribute.getName(), valueId);
                        }
                        else
                        {
                            record.setNull(attribute.getName());
                        }
                    }
                }
                else
                {
                    Long valueIdObj = (Long)ids.get(attribute);
                    long valueId = valueIdObj != null ? valueIdObj.longValue() : -1;
                    valueId = updateAttribute(attribute, valueId, value);
                    if(valueId != -1)
                    {
                        record.setLong(attribute.getName(), valueId);
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

    // implementation ////////////////////////////////////////////////////////

    /**
     * Checks if the specified attribute belongs to this resource's class.
     *
     * @param attribute the attribute.
     * @throws UnknownAttributeException if attribute is unknown.
     */
    protected void checkAttribute(AttributeDefinition attribute)
        throws UnknownAttributeException
    {
        if(delegate == null)
        {
            throw new IllegalStateException("cannot verity attribute against null delegate");
        }
        if(!(attribute.getDeclaringClass().equals(delegate.getResourceClass()) ||
             attribute.getDeclaringClass().isParent(delegate.getResourceClass())))
        {
            throw new UnknownAttributeException("class "+delegate.getResourceClass().getName()+
                                                "does not have a "+attribute.getName()+
                                                " attribute declared by "+
                                                attribute.getDeclaringClass().getName());
        }
    }

    /**
     * Loads an external attribute when it's value is requested.
     *
     * @param attribute the attribute.
     * @param id value id.
     * @return the object.
     */
    protected Object loadAttribute(AttributeDefinition attribute, long id)
    {
        AttributeHandler handler = attribute.getAttributeClass().getHandler();
        if(Entity.class.isAssignableFrom(attribute.getAttributeClass().getJavaClass()))
        {
            return handler.toAttributeValue(""+id);
        }
        Connection conn = null;
        try
        {
            conn = database.getConnection();
            return handler.retrieve(id, conn);
        }
        catch(Exception e)
        {
            throw new BackendException("failed to retrieve attribute value", e);
        }
        finally
        {
            if(conn != null)
            {
                try
                {
                    conn.close();
                }
                catch(SQLException ee)
                {
                    logger.error("failed to close connection", ee);
                }
            }
        }
    }

    /**
     * Update the attribute.
     * 
     * @param attribute the attribute.
     * @param id the id.
     * @param value the atribute value.
     * @return the attribute identifier.
     */
    protected long updateAttribute(AttributeDefinition attribute, long id, Object value)
    {
        AttributeHandler handler = attribute.getAttributeClass().getHandler();
        Connection conn = null;
        try
        {
            conn = database.getConnection();
            if(id != -1)
            {
                if(value == null)
                {
                    handler.delete(id, conn);
                }
                else
                {
                    handler.update(id, value, conn);
                }
            }
            else
            {
                if(value != null)
                {
                    id = handler.create(value, conn);
                }
            }
            return id;
        }
        catch(Exception e)
        {
            throw new BackendException("failed to update attribute value", e);
        }
        finally
        {
            if(conn != null)
            {
                try
                {
                    conn.close();
                }
                catch(SQLException ee)
                {
                    logger.error("failed to close connection", ee);
                }
            }
        }
    }
}
