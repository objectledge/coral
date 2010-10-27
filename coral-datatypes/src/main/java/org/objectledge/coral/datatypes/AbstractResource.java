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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.BitSet;
import java.util.Date;
import java.util.Map;

import org.jcontainer.dna.Logger;
import org.objectledge.coral.BackendException;
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
import org.objectledge.database.Database;

/**
 * Common base class for Resource data objects implementations. 
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: AbstractResource.java,v 1.40 2007-11-18 21:20:30 rafal Exp $
 */
public abstract class AbstractResource implements Resource
{
    /** Security delegate object. */
    protected Resource delegate;
    
    /** the attribute values. */
    private Object[] attributes;

    /** the external attribute ids. */
    private long[] ids;

    /** Set of AttributeDefinitions of the modified attributes. */
    private BitSet modified;

    /** The hashcode. */
    private int hashCode;    
    
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
            Resource resDelegate = res.getDelegate();
            if(resDelegate == null)
            {
                return delegate.equals(res);
            }
            else
            {
                return delegate.equals(resDelegate);
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
    
    void setDelegate(Resource delegate)
    {
        this.delegate = delegate;
        this.hashCode = delegate.hashCode();
        if(attributes == null)
        {
            int arraySize = delegate.getResourceClass().getMaxAttributeIndex() + 1;
            attributes = new Object[arraySize];
            ids = new long[arraySize];
            modified = new BitSet(arraySize);            
        }
        initDefinitions(delegate.getResourceClass());
    }
    
    synchronized void retrieve(Resource delegate, ResourceClass<?> rClass, Connection conn, 
        Object data)
    	throws SQLException
    {
        setDelegate(delegate);
        for(ResourceClass<?> parent : rClass.getDirectParentClasses())
        {
            retrieve(delegate, parent, conn, data);
        }
    }

    synchronized void create(Resource delegate, ResourceClass<?> rClass, Map attributes,
        Connection conn) throws SQLException, ValueRequiredException, ConstraintViolationException
    {
        setDelegate(delegate);
        for (ResourceClass<?> parent : rClass.getDirectParentClasses())
        {
            create(delegate, parent, attributes, conn);
        }
        for(AttributeDefinition attr : rClass.getDeclaredAttributes())
        {
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
    }
    
    synchronized void revert(ResourceClass<?> rClass, Connection conn, Object data)
	    throws SQLException
	{
        // recreate arrays - size might have changed
        int arraySize = delegate.getResourceClass().getMaxAttributeIndex() + 1;
        attributes = new Object[arraySize];
        ids = new long[arraySize];
        modified = new BitSet(arraySize);
        for(ResourceClass parent : rClass.getDirectParentClasses())
	    {
            revert(parent, conn, data);
	    }
	}

    synchronized void update(Connection conn)
	    throws SQLException
	{
	}
    
	synchronized void delete(Connection conn)
	    throws SQLException
	{
	}
    
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

    /**
     * Returns immediate children of the resource.
     * 
     * @return the immediate children of the resource.
     */
    public Resource[] getChildren()
    {
        return delegate.getChildren();
    }   
    
    // Resource interface - attributes //////////////////////////////////////////////////////////
    
    /**
     * {@inheritDoc}
     */
    public Object get(AttributeDefinition attribute) throws UnknownAttributeException
    {
        if((attribute.getFlags() & AttributeFlags.BUILTIN) != 0)
        {
            return delegate.get(attribute);
        }
        return getLocally(attribute);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isDefined(AttributeDefinition attribute) throws UnknownAttributeException
    {
        if((attribute.getFlags() & AttributeFlags.BUILTIN) != 0)
        {
            return delegate.isDefined(attribute);
        }
        int index = delegate.getResourceClass().getAttributeIndex(attribute);
        return isDefinedLocally(index);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean isModified(AttributeDefinition attribute) throws UnknownAttributeException
    {
        if((attribute.getFlags() & AttributeFlags.BUILTIN) != 0)
        {
            return delegate.isModified(attribute);
        }        
        return isAttributeModified(attribute);
    }
    
    /**
     * {@inheritDoc}
     */
    public void set(AttributeDefinition attribute, Object value) throws UnknownAttributeException,
        ModificationNotPermitedException, ValueRequiredException
    {
        if((attribute.getFlags() & AttributeFlags.BUILTIN) != 0)
        {
            delegate.set(attribute, value);
            return;
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
        int index = delegate.getResourceClass().getAttributeIndex(attribute);
        if(setLocally(index, value))
        {
            modified.set(index);
        }
    }
    
    /**
     * {@inheritDoc}
     */    
    public void setModified(AttributeDefinition attribute) throws UnknownAttributeException
    {
        if((attribute.getFlags() & AttributeFlags.BUILTIN) != 0)
        {
            delegate.setModified(attribute);
            return;
        }
        int index = delegate.getResourceClass().getAttributeIndex(attribute);
        modified.set(index);
    }
    
    /**
     * {@inheritDoc}
     */
    public void unset(AttributeDefinition attribute) throws ValueRequiredException,
        UnknownAttributeException
    {
        if((attribute.getFlags() & AttributeFlags.BUILTIN) != 0)
        {
            delegate.unset(attribute);
            return;
        }
        if((attribute.getFlags() & AttributeFlags.REQUIRED) != 0)
        {
            throw new ValueRequiredException("attribute "+attribute.getName()+
                                             "is declared as REQUIRED");
        }
        int index = delegate.getResourceClass().getAttributeIndex(attribute);
        unsetLocally(index);
        modified.set(index);
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
            controler = getDatabase().beginTransaction();
            conn = getDatabase().getConnection();
            update(conn);
            delegate.update();    
            getDatabase().commitTransaction(controler);
        }
        catch(Exception e)
        {
            try
            {
                getDatabase().rollbackTransaction(controler);
            }
            catch(SQLException ee)
            {
                getLogger().error("Failed to rollback transaction", ee);
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
                    getLogger().error("Failed to close connection", ee);
                }
            }
        }   
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
            conn = getDatabase().getConnection();
            AbstractResourceHandler handler = (AbstractResourceHandler)delegate.getResourceClass()
                .getHandler();
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
                    getLogger().error("Failed to close connection", ee);
                }
            }
        }   
    }

    // implementation ///////////////////////////////////////////////////////////////////////////

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
     * @param index the attribute index.
     * @return <code>true</code> if the attribute value is defined in this wrapper.
     */
    private synchronized boolean isDefinedLocally(int index)
    {
        if(modified.get(index))
        {
            return attributes[index] != null;
        }
        else
        {
            return attributes[index] != null || ids[index] > 0;
        } 
    }

    /**
     * Retrieve the attribute value defined in this wrapper.
     * 
     * @param attribute the attribute definition.
     * @return the attribute value is defined in this wrapper.
     */
    private synchronized Object getLocally(AttributeDefinition attribute)
    {
        int index = delegate.getResourceClass().getAttributeIndex(attribute);
        Object value = attributes[index];
        if(modified.get(index))
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
                long id = ids[index] - 1;
                if(id == -1)
                {
                    return null;
                }
                else
                {
                    value = loadAttribute(attribute, id);
                    attributes[index] = value;
                    return value;
                }
            }
        }        
    }

    /**
     * Set the attribute value in this wrapper.
     * 
     * @param index the attribute index.
     * @param value the attribute value.
     * @return <code>true</code> if values were different.
     */
    private synchronized boolean setLocally(int index, Object value)
    {
        Object oldValue = attributes[index];
        if(oldValue == null || value == null)
        {
            if(value == null && oldValue == null)
            {
                return false;
            }
            else
            {
                attributes[index] = value;
                return true;
            }
        }
        if(oldValue.equals(value))
        {
            return false;
        }
        attributes[index] = value;
        return true;
    }

    /**
     * Unset the attribute value in this wrapper.
     * 
     * @param index the attribute index.
     */
    private synchronized void unsetLocally(int index)
    {
        attributes[index] = null;
    }

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
            conn = getDatabase().getConnection();
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
                    getLogger().error("Failed to close connection", ee);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    protected long updateAttribute(AttributeDefinition attribute, long id, Object value)
    {
        AttributeHandler handler = attribute.getAttributeClass().getHandler();
        Connection conn = null;
        try
        {
            conn = getDatabase().getConnection();
            if(id != -1L)
            {
                if(value == null)
                {
                    handler.delete(id, conn);
                }
                else
                {
                    handler.update(id, value, conn);
                    return id;
                }
            }
            else
            {
                if(value != null)
                {
                    id = handler.create(value, conn);
                    return id;
                }
            }
            return -1;
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
                    getLogger().error("failed to close connection", ee);
                }
            }
        }
    }

    /**
     * Sets a value of locally stored attribute.
     * 
     * @param attr the attribute.
     * @param value the value.
     */
    protected void setAttribute(AttributeDefinition attr, Object value)
    {
        int index = delegate.getResourceClass().getAttributeIndex(attr);
        attributes[index] = value;
    }
    
    /**
     * Sets a value of locally stored attribute.
     * 
     * @param attr the attribute.
     * @return the value.
     */
    protected Object getAttribute(AttributeDefinition attr)
    {
        int index = delegate.getResourceClass().getAttributeIndex(attr);
        return attributes[index];
    }
    
    /**
     * Sets attribute value identifier.
     * 
     * @param attr the attribute.
     * @param id the identifier.
     */
    protected void setValueId(AttributeDefinition attr, long id)
    {
        int index = delegate.getResourceClass().getAttributeIndex(attr);
        ids[index] = id + 1;
    }
    
    /**
     * Gets attribute value identifier.
     * 
     * @param attr the attribute.
     * @return the identifier.
     */
    protected long getValueId(AttributeDefinition attr)
    {
        int index = delegate.getResourceClass().getAttributeIndex(attr);
        return ids[index] - 1;
    }
    
    /**
     * Checks if an attribute value was modified since loading.
     * 
     * @param attr the attribute.
     * @return <code>true</code> if the attribute was modified.
     */
    protected boolean isAttributeModified(AttributeDefinition attr)
    {
        int index = delegate.getResourceClass().getAttributeIndex(attr);
		return modified.get(index) || ids[index] != -1L && attributes[index] != null
				&& attr.getAttributeClass().getHandler().isModified(attributes[index]);
    }
    
    /**
     * Resets the modification flags for all attributes.
     */
    protected void clearModified()
    {
        modified.clear();
    }

    /**
     * @return Returns the database.
     */
    private Database getDatabase()
    {
        return ((AbstractResourceHandler)delegate.getResourceClass().getHandler()).getDatabase();
    }

    /**
     * @return Returns the logger.
     */
    protected Logger getLogger()
    {
        return ((AbstractResourceHandler)delegate.getResourceClass().getHandler()).getLogger();
    }
    
    /////////////////////////////////////////////////////////////////////////////////////////////
    
    private void initDefinitions(ResourceClass rClass)
    {
        synchronized(getClass())
        {
            try
            {
                Field initialized = getClass().getDeclaredField("definitionsInitialized");
                initialized.setAccessible(true);
                if(!((Boolean)initialized.get(null)).booleanValue())
                {
                    Class cl = getClass();
                    while(Resource.class.isAssignableFrom(cl))
                    {
                        initDefinitions(cl, rClass);
                        cl = cl.getSuperclass();
                    }
                }
                initialized.set(null, Boolean.TRUE);
            }
            catch(Exception e)
            {
                if(e instanceof BackendException)
                {
                    throw (BackendException)e;
                }
                throw new BackendException("failed to initialize wrapper class "
                    + getClass().getName(), e);
            }
        }
    }

    private void initDefinitions(Class cl, ResourceClass rClass)
    {
        for(Field f : cl.getDeclaredFields())
        {
            if(Modifier.isStatic(f.getModifiers()) && f.getName().endsWith("Def")
                && f.getType().equals(AttributeDefinition.class))
            {
                String attrName = f.getName().substring(0, f.getName().length() - 3);
                try
                {
                    AttributeDefinition attr = rClass.getAttribute(attrName);
                    f.setAccessible(true);
                    f.set(null, attr);
                }
                catch(UnknownAttributeException e)
                {
                    throw new BackendException("missing attribute " + attrName + " in class "
                        + rClass.getName(), e);
                }
                catch(Exception e)
                {
                    throw new BackendException("failed to initialize field", e);
                }
            }
        }
    }    
    
    /////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Returns the value of the attribute, or the supplied default value if undefined.
     * 
     * @param attribute the attribute definiiton.
     * @param defaultValue the default value.
     * @return attribute value.
     */
    protected Object getInternal(AttributeDefinition attribute, Object defaultValue)
    {
        int index = delegate.getResourceClass().getAttributeIndex(attribute);
        Object value = attributes[index];
        if(value != null)
        {
            return value;
        }
        else if(modified.get(index))
        {
            return defaultValue;
        }
        else
        {
            long id = ids[index] - 1;
            if(id == -1)
            {
                return defaultValue;
            }
            else
            {
                value = loadAttribute(attribute, id);
                attributes[index] = value;
                return value;
            }                
        }
    }
}
