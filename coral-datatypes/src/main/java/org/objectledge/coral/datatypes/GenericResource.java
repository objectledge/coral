package org.objectledge.coral.datatypes;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jcontainer.dna.Logger;
import org.objectledge.coral.BackendException;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.AttributeHandler;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.schema.ResourceClassInheritance;
import org.objectledge.coral.schema.ResourceHandler;
import org.objectledge.coral.schema.UnknownAttributeException;
import org.objectledge.coral.security.PermissionAssignment;
import org.objectledge.coral.security.Role;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.ConstraintViolationException;
import org.objectledge.coral.store.ModificationNotPermitedException;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ValueRequiredException;
import org.objectledge.database.Database;

/**
 * A generic implementation of {@link Resource} interface.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: GenericResource.java,v 1.5 2004-04-01 08:54:27 fil Exp $
 */
public class GenericResource
    implements Resource
{
    // Member objects ////////////////////////////////////////////////////////

    /** The database service. */
    private Database database;

    /** The logging facility. */
    private Logger logger;

    /** The delegate object. */
    private Resource delegate;
    
    /** The component resource class */
    private ResourceClass componentResourceClass;

    /** AttributeDefinition -> hosting instance map. */
    private Map attributeMap = new HashMap();
    
    /** ResourceClass -> Parent instance map. */
    private Map parents = new HashMap();
    
    /** AttributeDefinition -> attribute value map. */
    private Map attributes = new HashMap();

    /** Modified attributes / parent instances list. */
    private List modified = new ArrayList();

    /** AttributeDefinition -> attribute instance id map. */
    private Map ids = new HashMap();

    /**
     * Constructor.
     * 
     * @param database the database.
     * @param logger the logger.
     */
    public GenericResource(Database database, Logger logger)
    {
        this.database = database;
        this.logger = logger;
    }

    // equality & hashing ////////////////////////////////////////////////////

    /**
     * Returs the hashcode for this entity.
     *
     * <p>This implementation returns the hashcode of the delegate.</p>
     *
     * @returns the hashcode of the object.
     */
    public int hashCode()
    {
        return delegate.hashCode();
    }
    
    /**
     * Checks if another object represens the same entity.
     *
     * <p>This implementation compares the delegates of both resources for
     * equality.</p> 
     *
     * @param obj the other objects.
     * @return <code>true</code> if the other object represents the same entity.
     */
    public boolean equals(Object obj)
    {
        if(obj != null && (obj instanceof Resource))
        {
            if(obj instanceof GenericResource)
            {
                return delegate.equals(((Resource)obj).getDelegate());
            }
            else
            {
                return delegate.equals((Resource)obj);
            }
        }
        return false;
    }

    // Resource interface - identity+security (delegated) ////////////////////

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

    // Resource interface - attributes (implemented here) ////////////////////

    /** 
     * Checks if the specified attribute of the resource is defined.
     *
     * @param attribute the attribute to check.
     * @return <code>true</code> if the specified attribute is defined.
     * @throws UnknownAttributeException if <code>attribute</code> does not
     *         belong to the resource's class.
     */
    public synchronized boolean isDefined(AttributeDefinition attribute)
        throws UnknownAttributeException
    {
        if((attribute.getFlags() & AttributeFlags.BUILTIN) != 0)
        {
            return delegate.isDefined(attribute);
        }
        Resource host = getHost(attribute);
        if(host == this)
        {
            if(modified.contains(attribute))
            {
                return attributes.containsKey(attribute);
            }
            else
            {
                return ids.containsKey(attribute);
            }
        }
        else
        {
            return host.isDefined(attribute);
        }
    }
    
    /**
     * Retrieves the value of a specific attribute.
     * 
     * @param attribute the attribute to retrieve.
     * @return the value of the attribute, or <code>null</code> if undefined.
     * @throws UnknownAttributeException if <code>attribute</code> does not
     *         belong to the resource's class.
     */
    public synchronized Object get(AttributeDefinition attribute)
        throws UnknownAttributeException
    {
        if((attribute.getFlags() & AttributeFlags.BUILTIN) != 0)
        {
            return delegate.get(attribute);
        }
        Resource host = getHost(attribute);
        if(host == this)
        {
            if(modified.contains(attribute))
            {
                return attributes.get(attribute);
            }
            else
            {
                if(ids.containsKey(attribute))
                {
                    if(attributes.containsKey(attribute))
                    {
                        return attributes.get(attribute);
                    }
                    else
                    {
                        long aId = ((Long)ids.get(attribute)).longValue();
                        Object value = attributeOnDemand(attribute, aId);
                        attributes.put(attribute, value);
                        return value;
                    }
                }
                else
                {
                    return null;
                }
            }
        }
        else
        {
            return host.get(attribute);
        }
    }
    
    /**
     * Sets the value of a specific attribute.
     * 
     * @param attribute the attribute to set.
     * @param value the value of the attribute.
     * @throws UnknownAttributeException if <code>attribute</code> does not
     *         belong to the resource's class.
     * @throws ModificationNotPermitedException if <code>attribute</code> is
     *         <code>READONLY</code>.
     * @throws ValueRequiredException if <code>attribute</code> is
     *         <code>REQUIRED</code> and <code>value</code> is
     *         <code>null</code>.
     */
    public synchronized void set(AttributeDefinition attribute, Object value)
        throws UnknownAttributeException, ModificationNotPermitedException,
        ValueRequiredException, ConstraintViolationException
    {
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
        Resource host = getHost(attribute);
        value = attribute.getAttributeClass().getHandler().toAttributeValue(value);
        attribute.getAttributeClass().getHandler().checkDomain(attribute.getDomain(), value);
        if(host == this)
        {
            attributes.put(attribute, value);
            modified.add(attribute);
        }
        else
        {
            host.set(attribute, value);
            modified.add(host);
        }
    }

    /**
     * Removes the value of the specified attribute.
     *
     * @param attribute the attribute to remove.
     * @throws ValueRequiredException if the attribute is required for this
     *         resource type.
     * @throws UnknownAttributeException if <code>attribute</code> does not
     *         belong to the resource's class.
     */
    public synchronized void unset(AttributeDefinition attribute)
        throws ValueRequiredException, UnknownAttributeException
    {
        if((attribute.getFlags() & AttributeFlags.BUILTIN) != 0)
        {
            delegate.unset(attribute);
        }
        Resource host = getHost(attribute);
        if((attribute.getFlags() & AttributeFlags.REQUIRED) != 0)
        {
            throw new ValueRequiredException("attribute "+attribute.getName()+
                                             "is declared as REQUIRED");
        }
        if(host == this)
        {
            attributes.remove(attribute);
            modified.add(attribute);
        }
        else
        {
            host.unset(attribute);
            modified.add(host);
        }
    }
    
    /**
     * Sets the modified flag for the specified attribute.
     *
     * @param attribute the attribute to mark as modified.
     * @throws UnknownAttributeException if <code>attribute</code> does not
     *         belong to the resource's class.
     */
    public synchronized void setModified(AttributeDefinition attribute)
        throws UnknownAttributeException
    {
        if((attribute.getFlags() & AttributeFlags.BUILTIN) != 0)
        {
            delegate.setModified(attribute);
        }
        Resource host = getHost(attribute);
        if(host == this)
        {
            modified.add(attribute);
        }
        else
        {
            host.setModified(attribute);
            modified.add(host);
        }
    }
    
    /**
     * Checks the modified flag for the specified resource.
     *
     * @param attribute the attribute to check.
     * @return <code>true</code> if the attribute was modified.
     * @throws UnknownAttributeException if <code>attribute</code> does not
     *         belong to the resource's class.
     */
    public synchronized boolean isModified(AttributeDefinition attribute)
        throws UnknownAttributeException
    {
        if((attribute.getFlags() & AttributeFlags.BUILTIN) != 0)
        {
            return delegate.isModified(attribute);
        }
        Resource host = getHost(attribute);
        if(host == this)
        {
            return modified.contains(attribute);
        }
        else
        {
            return host.isModified(attribute);
        }
    }

    /**
     * Updates the image of the resource in the persistent storage.
     *
     * @param subject the subject that performs the update. 
     */
    public synchronized void update()
        throws UnknownAttributeException
    {
        Connection conn = null;
        boolean controler = false;
        try
        {
            controler = database.beginTransaction();
            conn = database.getConnection();
            update(conn);
            database.commitTransaction(controler);
        }
        catch(SQLException e)
        {
            try
            {
                database.rollbackTransaction(controler);
            }
            catch(SQLException ee)
            {
                logger.error("Failed to rollback transaction", ee);
            }
            throw new BackendException("Failed to update resource", e);
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
                    logger.error("Failed to close connection", ee);
                }
            }
        }   
        delegate.update();
    }

    /**
     * Reverts the Resource object to the state present in the persistent
     * storage. 
     */
    public synchronized void revert()
    {
        Connection conn = null;
        boolean controler = false;
        try
        {
            controler = database.beginTransaction();
            conn = database.getConnection();
            GenericResourceHandler handler = (GenericResourceHandler)delegate.
                getResourceClass().getHandler();
            Map dataKeyMap = handler.getDataKeys(delegate, conn); 
            revert(null, conn, dataKeyMap);
            database.commitTransaction(controler);
        }
        catch(SQLException e)
        {
            try
            {
                database.rollbackTransaction(controler);
            }
            catch(SQLException ee)
            {
                logger.error("Failed to roll back transaction", ee);
            }
            throw new BackendException("Failed to revert resource", e);
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
                    logger.error("Failed to close connection", ee);
                }
            }
        }   
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
    
    // Package private ///////////////////////////////////////////////////////

    synchronized void retrieve(Resource delegate, ResourceClass rClass, 
                                 Connection conn, Map dataKeyMap)
        throws SQLException
    {
        this.delegate = delegate;
        if(rClass == null)
        {
            rClass = delegate.getResourceClass();
        }
        this.componentResourceClass = rClass;
        ResourceClass[] parentClasses = getDirectParentClasses(rClass);
        for(int i=0; i<parentClasses.length; i++)
        {
            ResourceClass parent = parentClasses[i];
            Resource instance;
            if(parent.getHandler() instanceof GenericResourceHandler)
            {
                instance = ((GenericResourceHandler)parent.getHandler()).
                    retrieve(delegate, conn, dataKeyMap);
            }
            else
            {
                instance = parent.getHandler().retrieve(delegate, conn);
            }
            parents.put(parent, instance);
            AttributeDefinition[] hosted = parent.getAllAttributes();
            for(int j=0; j<hosted.length; j++)
            {
                if((hosted[j].getFlags() & AttributeFlags.BUILTIN) == 0)
                {
                    attributeMap.put(hosted[j], instance);
                }
            }
        }
        AttributeDefinition[] declared = rClass.getDeclaredAttributes();
        for(int i=0; i<declared.length; i++)
        {
            attributeMap.put(declared[i], this);
        }
        Map dataKeys = (Map)dataKeyMap.get(new Long(delegate.getId()));
        if(dataKeys != null)
        {
            for(int i=0; i<declared.length; i++)
            {
                Long id = (Long)dataKeys.get(declared[i]);
                if(id != null)
                {
                    ids.put(declared[i], id);
                }
            }
        }
    }

    synchronized void revert(ResourceClass rClass, Connection conn, Map dataKeyMap)
        throws SQLException
    {
        attributes.clear();
        ids.clear();
        attributeMap.clear();
        modified.clear();
        if(rClass == null)
        {
            rClass = delegate.getResourceClass();
        }
        ResourceClass[] parentClasses = getDirectParentClasses(rClass);
        parents.keySet().retainAll(Arrays.asList(parentClasses));
        for(int i=0; i<parentClasses.length; i++)
        {
            ResourceClass parent = parentClasses[i];
            Resource instance;
            if(parents.containsKey(parent))
            {
                instance = (Resource)parents.get(parent);
                if(parent.getHandler() instanceof GenericResourceHandler)
                {
                    ((GenericResourceHandler)parent.getHandler()).
                        revert(instance, conn, dataKeyMap);
                }
                else
                {
                    parent.getHandler().revert(instance, conn);
                }
            }
            else
            {
                instance = parent.getHandler().retrieve(delegate, conn);
                parents.put(parent, instance);
            }
            AttributeDefinition[] hosted = parent.getAllAttributes();
            for(int j=0; j<hosted.length; j++)
            {
                if((hosted[j].getFlags() & AttributeFlags.BUILTIN) == 0)
                {
                    attributeMap.put(hosted[j], instance);
                }
            }
        }
        AttributeDefinition[] declared = rClass.getDeclaredAttributes();
        for(int i=0; i<declared.length; i++)
        {
            attributeMap.put(declared[i], this);
        }
        Map dataKeys = (Map)dataKeyMap.get(new Long(delegate.getId()));
        if(dataKeys != null)
        {
            for(int i=0; i<declared.length; i++)
            {
                Long id = (Long)dataKeys.get(declared[i]);
                if(id != null)
                {
                    ids.put(declared[i], id);
                }
            }
        }
    }

    /**
     * Creates a new instance of the resource using provided attribute map.
     *
     * <p>Note that the map is keyed with {@link AttributeDefinition} objects
     * not attribute names.</p>
     *
     * @param delegate the security delegate.
     * @param rClass the resource class that is being instantiated. This is
     *        different than delegate.getResourceClass() for nested objects!
     * @param attributes the intial values of the attributes.
     * @param conn the JDBC Connection.
     * @throws SQLException if an database access error occurs.
     */
    synchronized void create(Resource delegate, ResourceClass rClass, 
                               Map attributes, Connection conn)
        throws SQLException, ValueRequiredException, ConstraintViolationException
    {
        this.delegate = delegate;
        if(rClass == null)
        {
            rClass = delegate.getResourceClass();
        }
        this.componentResourceClass = rClass; 
        ResourceClass[] parentClasses = getDirectParentClasses(rClass);
        for(int i=0; i<parentClasses.length; i++)
        {
            ResourceClass parent = parentClasses[i];
            Resource instance = parent.getHandler().
                create(delegate, attributes, conn);
            parents.put(parent, instance);
            AttributeDefinition[] hosted = parent.getAllAttributes();
            for(int j=0; j<hosted.length; j++)
            {
                if((hosted[j].getFlags() & AttributeFlags.BUILTIN) == 0)
                {
                    attributeMap.put(hosted[j], instance);
                }
            }
        }
        AttributeDefinition[] declared = rClass.getDeclaredAttributes();
        Statement stmt = conn.createStatement();
        loop: for(int i=0; i<declared.length; i++)
        {
            AttributeDefinition attr = declared[i];
            if((attr.getFlags() & AttributeFlags.BUILTIN) != 0)
            {
                continue loop;
            }
            AttributeHandler handler = attr.getAttributeClass().getHandler();
            Object value = attributes.get(attr);
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
                value = handler.toAttributeValue(value);
                handler.checkDomain(attr.getDomain(), value);
                long newId = handler.create(value, conn);
                ids.put(attr, new Long(newId));
                stmt.execute(
                    "INSERT INTO coral_generic_resource "+
                    "(resource_id, attribute_definition_id, data_key) "+
                    "VALUES ("+delegate.getId()+", "+attr.getId()+", "+
                    newId+")"
                );
                if(handler.shouldRetrieveAfterCreate())
                {
                    try
                    {
                        this.attributes.put(attr, handler.retrieve(newId, conn));
                    }
                    catch(EntityDoesNotExistException e)
                    {
                        throw new BackendException("data integrity error", e);
                    }
                }
                else
                {
                    this.attributes.put(attr, value);
                }
            }
            attributeMap.put(attr, this);
        }
    }

    /**
     * Called from {@link GenericResourceHandler} and {@link #update(Subject)}.
     *
     * @param subject the subject that performs the update. 
     * @param conn the JDBC connection to use.
     */
    synchronized void update(Connection conn)
        throws SQLException
    {
        Statement stmt = conn.createStatement();
        Iterator i = modified.iterator();
        while(i.hasNext())
        {
            Object o = i.next();
            if(o instanceof AttributeDefinition)
            {
                AttributeDefinition attr = (AttributeDefinition)o;
                if((attr.getFlags() & AttributeFlags.BUILTIN) != 0)
                {
                    continue;
                }
                AttributeHandler handler = attr.getAttributeClass().getHandler();
                Object value = attributes.get(attr);
                Long id = (Long)ids.get(attr);
                if(value != null)
                {
                    if(id == null)
                    {
                        long newId = handler.create(value, conn);
                        stmt.execute(
                            "INSERT INTO coral_generic_resource "+
                            "(resource_id, attribute_definition_id, data_key) "+
                            "VALUES ("+delegate.getId()+", "+attr.getId()+", "+
                            newId+")"
                        );
                        ids.put(attr, new Long(newId));
                    }
                    else
                    {
                        try
                        {
                            handler.update(id.longValue(), value, conn);
                        }
                        catch(EntityDoesNotExistException e)
                        {
                            throw new BackendException("Internal error", e);
                        }
                    }
                }
                else
                {
                    if(id != null)
                    {
                        try
                        {
                            handler.delete(id.longValue(), conn);
                        }
                        catch(EntityDoesNotExistException e)
                        {
                            throw new BackendException("Internal error", e);
                        }
                        stmt.execute(
                            "DELETE FROM coral_generic_resource "+
                            "WHERE resource_id = "+delegate.getId()+
                            " AND attribute_definition_id = "+attr.getId()
                        );
                        ids.remove(attr);
                    }
                }
                
            }
            else
            {
                Resource res = (Resource)o;
                ResourceHandler handler = res.getResourceClass().getHandler();
                handler.update(res, conn);
            }
            // reset the modified status of the item after it has been
            // succesfully updated
            i.remove();
        }
    }

    synchronized void delete(Connection conn)
        throws SQLException
    {
        Iterator i = parents.keySet().iterator();
        while(i.hasNext())
        {
            ResourceClass parentClass = (ResourceClass)i.next();
            Resource parent = (Resource)parents.get(parentClass);
            parentClass.getHandler().delete(parent, conn);
        }
        i = ids.keySet().iterator();
        if(i.hasNext())
        {
            Statement stmt = conn.createStatement();
            while(i.hasNext())
            {
                AttributeDefinition attr = (AttributeDefinition)i.next();
                long atId = ((Long)ids.get(attr)).longValue();
                try
                {
                    attr.getAttributeClass().getHandler().delete(atId, conn);
                }
                catch(EntityDoesNotExistException e)
                {
                    throw new BackendException("internal error", e);
                }
                stmt.execute("DELETE FROM coral_generic_resource WHERE "+
                             " resource_id = "+delegate.getId()+
                             " AND attribute_definition_id = "+attr.getId());
            }
        }
    }

    ResourceClass getResourceClass0()
    {
        return componentResourceClass;
    }

    // Private ///////////////////////////////////////////////////////////////

    private synchronized Resource getHost(AttributeDefinition attribute)
        throws UnknownAttributeException
    {
        Resource host = (Resource)attributeMap.get(attribute);
        if(host == null)
        {
            throw new UnknownAttributeException(delegate.getResourceClass().getName()+
                                                " does not contain "+
                                                attribute.getName()+" attribute "+
                                                "declared by "+
                                                attribute.getDeclaringClass().getName());
        } 
        return host;
    }
    
    private ResourceClass[] getDirectParentClasses(ResourceClass rc)
    {
        ArrayList temp = new ArrayList();
        ResourceClassInheritance[] rci = rc.getInheritance();
        for(int i=0; i<rci.length; i++)
        {
            if(rci[i].getChild().equals(rc))
            {
                temp.add(rci[i].getParent());
            }
        }
        ResourceClass[] result = new ResourceClass[temp.size()];
        temp.toArray(result);
        return result;
    }

    private Object attributeOnDemand(AttributeDefinition attribute, long aId)
    {
        Connection conn = null;
        try
        {
            conn = database.getConnection();
            return attribute.getAttributeClass().getHandler().
                retrieve(aId, conn);
        }
        catch(Exception e)
        {
            if(delegate == null)
            {
                throw new BackendException("failed to retrieve attribute value " +                    "(attribute definition = "+attribute.getName()+
                    " , attribute id = "+aId+")", e);
            }
            else
            {
                throw new BackendException("failed to retrieve attribute value " +
                    "(attribute definition = "+attribute.getName()+
                    " , attribute id = "+aId+") for resource: "+delegate.getId() , e);
            }
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
                    logger.error("Failed to close connection", ee);
                }
            }
        }
    }
}
