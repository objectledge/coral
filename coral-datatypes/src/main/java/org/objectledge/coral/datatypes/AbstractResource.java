// 
// Copyright (c) 2003,2004 , Caltha - Gajda, Krzewski, Mach, Potempski Sp.J. 
// All rights reserved. 
// 
// Redistribution and use in source and binary forms, with or without modification,  
// are permitted provided that the following conditions are met: 
//  
// * Redistributions of source code must retain the above copyright notice,  
//	 this list of conditions and the following disclaimer. 
// * Redistributions in binary form must reproduce the above copyright notice,  
//	 this list of conditions and the following disclaimer in the documentation  
//	 and/or other materials provided with the distribution. 
// * Neither the name of the Caltha - Gajda, Krzewski, Mach, Potempski Sp.J.  
//	 nor the names of its contributors may be used to endorse or promote products  
//	 derived from this software without specific prior written permission. 
// 
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"  
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED  
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
// IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,  
// INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,  
// BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
// OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,  
// WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)  
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE  
// POSSIBILITY OF SUCH DAMAGE. 
// 
package org.objectledge.coral.datatypes;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jcontainer.dna.Logger;
import org.objectledge.coral.BackendException;
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
 * Common base class for Resource data objects implementations. 
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: AbstractResource.java,v 1.20 2005-02-21 15:44:16 zwierzem Exp $
 */
public abstract class AbstractResource implements Resource
{
    /** The database service. */
    private Database database;

    /** The logging facility. */
    protected Logger logger;

    /** Security delegate object. */
    protected Resource delegate;
    
    /** Set of AttributeDefinitions of the modified attributes. */
    protected Set modified = new HashSet();

    /** The hashcode. */
    private int hashCode;
    
    /** AttributeDefinition -> hosting instance map. */
    private Map attributeMap = new HashMap();    
    
    /** ResourceClass -> parent class instance. */
    private Map facets = new HashMap();
    
    /**
     * Constructor.
     * 
     * @param database the database.
     * @param logger the logger.
     */
    public AbstractResource(Database database, Logger logger)
    {
        this.database = database;
        this.logger = logger;
    }
    
    // equality /////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Returs the hashcode for this entity.
     *
     * <p>This implementation returns the hashcode of the delegate.</p>
     *
     * @return the hashcode of the object.
     */
    public final int hashCode()
    {
        return hashCode;
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
            Resource res = (Resource)obj;
            if(res.getDelegate() == null)
            {
                return delegate.equals(res);
            }
            else
            {
                return delegate.equals(res.getDelegate());
            }
        }
        return false;
    }
    
    /**
     * Returns a String representation of this object.
     *
     * <p> This method is overriden to augument debugging. The format of the representation is as 
     * following: 
     * <blockquote>
     *   <code>javaClass name #id @identity</code>
     * </blockquote>
     * Where:
     * <ul>
     *   <li><code>javaClass</code> is the actual implementation class of the object</li>
     *   <li><code>path</code> is the path of the resource as returned by the {@link #getPath()} 
     *     method.</li>
     *   <li><code>id</code> is the identifier of the resource as returned by the {@link #getId()}
     *     method.</li> 
     *   <li><code>idenity</code> is the object instance's identity hashcode as retured by the
     *     <code>System.getIdentityHashCode(Object)</code> function.</li>
     *  </ul>
     *  </p>
     * 
     * @return a String representation of this object.
     */
    public String toString()
    {
        StringBuilder buff = new StringBuilder();
        buff.append(getClass().getName());
        buff.append(' ');
        buff.append(getPath());
        buff.append(" #");
        buff.append(getIdString());
        buff.append(" @");
        buff.append(Integer.toString(System.identityHashCode(this), 16));
        return buff.toString();
    }

    // Resource interface - delegation //////////////////////////////////////////////////////////
    
    /**
     * {@inheritDoc}
     */
    public Resource getDelegate()
    {
        return delegate;
    }    
    
    synchronized void retrieve(Resource delegate, ResourceClass rClass, Connection conn, 
        Object data)
    	throws SQLException
    {
        ResourceClass[] parentClasses = getDirectParentClasses(rClass);
        for(int i=0; i<parentClasses.length; i++)
        {
            ResourceClass parent = parentClasses[i];
            Resource instance;
            if(parent.getHandler() instanceof AbstractResourceHandler)
            {
                retrieve(delegate, parent, conn, data);
                instance = this;
            }
            else
            {
                instance = parent.getHandler().retrieve(delegate, conn, data);
            }
            facets.put(parent, instance);
        }
        initAttributeMap(delegate, rClass);
    }

    synchronized void create(Resource delegate, ResourceClass rClass, Map attributes,
        Connection conn) throws SQLException, ValueRequiredException, ConstraintViolationException
    {
        ResourceClass[] parentClasses = getDirectParentClasses(rClass);
        for (int i = 0; i < parentClasses.length; i++)
        {
            ResourceClass parent = parentClasses[i];
            Resource instance;
            if(parent.getHandler() instanceof GenericResourceHandler)
            {
                create(delegate, parent, attributes, conn);
                instance = this;
            }
            else
            {
                instance = parent.getHandler().create(delegate, attributes, conn);
            }
            facets.put(parent, instance);
        }
        AttributeDefinition[] declared = rClass.getDeclaredAttributes();
        for(int i=0; i<declared.length; i++)
        {
            AttributeDefinition attr = declared[i];
            if((attr.getFlags() & AttributeFlags.BUILTIN) == 0)
            {
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
            }
        }
        initAttributeMap(delegate, rClass);
    }
    
    synchronized void revert(ResourceClass rClass, Connection conn, Object data)
	    throws SQLException
	{
	    attributeMap.clear();
	    modified.clear();
	    ResourceClass[] parentClasses = getDirectParentClasses(rClass);
	    for(int i=0; i<parentClasses.length; i++)
	    {
	        ResourceClass parent = parentClasses[i];
	        Resource instance;
	        if(facets.containsKey(parent))
	        {
	            instance = (Resource)facets.get(parent);
                if(parent.getHandler() instanceof AbstractResourceHandler)
                {
                    revert(parent, conn, data);
                }
                else
                {
                    parent.getHandler().revert(instance, conn, data);
                }
	        }
	        else
	        {
                if(parent.getHandler() instanceof AbstractResourceHandler)
                {
                    retrieve(delegate, parent, conn, data);
                    instance = this;
                }
                else
                {
                    instance = parent.getHandler().retrieve(delegate, conn, data);
                }
	            facets.put(parent, instance);
	        }
	    }
	    initAttributeMap(delegate, rClass);
	}

    synchronized void update(Connection conn)
	    throws SQLException
	{
	    Statement stmt = conn.createStatement();
	    Iterator i = modified.iterator();
	    while(i.hasNext())
	    {
	        Object o = i.next();
	        if(!(o instanceof AttributeDefinition))
	        {
	            Resource res = (Resource)o;
                if(res != this)
                {
                    ResourceHandler handler = res.getResourceClass().getHandler();
                    handler.update(res, conn);
                }
	            i.remove();
	        }
	    }
	}
    
	synchronized void delete(Connection conn)
	    throws SQLException
	{
	    Iterator i = facets.keySet().iterator();
	    while(i.hasNext())
	    {
	        ResourceClass parentClass = (ResourceClass)i.next();
	        Resource facet = (Resource)facets.get(parentClass);
            if(facet != this)
            {
                parentClass.getHandler().delete(facet, conn);
            }
	    }
	}
    
    // Resource interface - identity+security (delegated) ///////////////////////////////////////

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
     * Returns the numerical identifier of the entity as a Java object.
     * 
     * @return the numerical identifier of the entity as a Java object.
     */
    public Long getIdObject()
    {
        return delegate.getIdObject();
    }

    /**
     * Returns the numerical identifier of the entity as a string.
     * 
     * @return the numerical identifier of the entity as a string.
     */
    public String getIdString()
    {
        return delegate.getIdString();
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
     * Returns the parent resource's identifier.
     * 
     * <p>This method can be used to avoid lazy loading the parent resource in certain 
     * situations.</p>
     * 
     * @return the identifier of the parent resource or -1 if undefined.
     */
    public long getParentId()
    {
        return delegate.getParentId();
    }
    
    // Resource interface - attributes //////////////////////////////////////////////////////////
    
    /**
     * {@inheritDoc}
     */
    public Object get(AttributeDefinition attribute) throws UnknownAttributeException
    {
        checkAttribute(attribute);
        if((attribute.getFlags() & AttributeFlags.BUILTIN) != 0)
        {
            return delegate.get(attribute);
        }
        Resource host = getHost(attribute);
        if(host == this)
        {
            return getLocally(attribute);
        }
        else
        {
            return host.get(attribute);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isDefined(AttributeDefinition attribute) throws UnknownAttributeException
    {
        checkAttribute(attribute);
        if((attribute.getFlags() & AttributeFlags.BUILTIN) != 0)
        {
            return delegate.isDefined(attribute);
        }
        Resource host = getHost(attribute);
        if(host == this)
        {
            return isDefinedLocally(attribute);
        }
        else
        {
            return host.isDefined(attribute);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isModified(AttributeDefinition attribute) throws UnknownAttributeException
    {
        checkAttribute(attribute);
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
     * {@inheritDoc}
     */
    public void set(AttributeDefinition attribute, Object value) throws UnknownAttributeException,
        ModificationNotPermitedException, ValueRequiredException
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
        Resource host = getHost(attribute);
        value = attribute.getAttributeClass().getHandler().toAttributeValue(value);
        attribute.getAttributeClass().getHandler().checkDomain(attribute.getDomain(), value);
        if(host == this)
        {
            setLocally(attribute, value);
            modified.add(attribute);
        }
        else
        {
            host.set(attribute, value);
            modified.add(host);
        }
    }
    
    /**
     * {@inheritDoc}
     */    
    public void setModified(AttributeDefinition attribute) throws UnknownAttributeException
    {
        checkAttribute(attribute);
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
     * {@inheritDoc}
     */
    public void unset(AttributeDefinition attribute) throws ValueRequiredException,
        UnknownAttributeException
    {
        checkAttribute(attribute);
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
            unsetLocally(attribute);
            modified.add(attribute);
        }
        else
        {
            host.unset(attribute);
            modified.add(host);
        }
    }
 
    /**
     * Updates the image of the resource in the persistent storage.
     *
     * @throws UnknownAttributeException if attribute is unknown. 
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
            conn = database.getConnection();
            GenericResourceHandler handler = (GenericResourceHandler)delegate.
                getResourceClass().getHandler();
            Object data = handler.getData(delegate, conn); 
            revert(delegate.getResourceClass(), conn, data);
        }
        catch(SQLException e)
        {
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

    // implementation ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Initialize attribute map of the resource wrapper.
     * 
     * @param delegate the security delegate object.
     * @param rClass the resource class facet this wrapper represents.
     */
    protected void initAttributeMap(Resource delegate, ResourceClass rClass)
    {
        this.delegate = delegate;
        this.hashCode = delegate.hashCode();
        ResourceClass[] parentClasses = getDirectParentClasses(rClass);
        for(int i=0; i<parentClasses.length; i++)
        {
            ResourceClass parent = parentClasses[i];
            Resource instance = (Resource)facets.get(parent);
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
            if((declared[i].getFlags() & AttributeFlags.BUILTIN) == 0)
            {
                attributeMap.put(declared[i], this);
            }
        }        
    }
    
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

    private void checkAttribute(AttributeDefinition attribute)
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
    
    // subclass contract ////////////////////////////////////////////////////////////////////////
    
    /**
     * Check if the attribute value is defined in this wrapper.
     * 
     * @param attr the attribute defintion.
     * @return <code>true</code> if the attribute value is defined in this wrapper.
     */
    protected abstract boolean isDefinedLocally(AttributeDefinition attr);

    /**
     * Retrieve the attribute value defined in this wrapper.
     * 
     * @param attribute the attribute defintion.
     * @return the attribute value is defined in this wrapper.
     */
    protected abstract Object getLocally(AttributeDefinition attribute);

    /**
     * Set the attribute value in this wrapper.
     * 
     * @param attribute the attribute defintion.
     * @param value the attribute value.
     */
    protected abstract void setLocally(AttributeDefinition attribute, Object value);

    /**
     * Unset the attribute value in this wrapper.
     * 
     * @param attribute the attribute defintion.
     */
    protected abstract void unsetLocally(AttributeDefinition attribute);

    /**
     * Lazy-load attribute value.
     * 
     * @param attribute the attribute definition.
     * @param aId attribute value id.
     * @return attribute value.
     */
    protected Object loadAttribute(AttributeDefinition attribute, long aId)
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
                throw new BackendException("failed to retrieve attribute value " +
                    "(attribute definition = "+attribute.getName()+
                    " , attribute id = "+aId+")", e);
            }
            else
            {
                throw new BackendException("failed to retrieve attribute value " +
                    "(attribute definition = "+attribute.getName()+
                    " , attribute id = "+aId+") for resource: "+delegate.getIdString() , e);
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

    /**
     * {@inheritDoc}
     */
    protected Long updateAttribute(AttributeDefinition attribute, Long idObj, Object value)
    {
        long id = idObj != null ? idObj.longValue() : -1;
        AttributeHandler handler = attribute.getAttributeClass().getHandler();
        Connection conn = null;
        try
        {
            conn = database.getConnection();
            if(id != -1L)
            {
                if(value == null)
                {
                    handler.delete(id, conn);
                }
                else
                {
                    handler.update(id, value, conn);
                    return idObj;
                }
            }
            else
            {
                if(value != null)
                {
                    id = handler.create(value, conn);
                    return new Long(id);
                }
            }
            return null;
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
