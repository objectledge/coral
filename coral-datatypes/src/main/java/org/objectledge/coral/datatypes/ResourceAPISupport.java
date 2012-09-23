package org.objectledge.coral.datatypes;

import java.util.Date;

import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.schema.UnknownAttributeException;
import org.objectledge.coral.security.PermissionAssignment;
import org.objectledge.coral.security.Role;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.ModificationNotPermitedException;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ValueRequiredException;

/**
 * Middle layer of ResourceData class implementing most of {@link Resource} contract using metadata
 * delegate object and {@link ResourceAttributesSupport} {@code *Interal} methods.
 * 
 * @author rafal.krzewski@caltha.pl
 */
public abstract class ResourceAPISupport
    extends ResourceAttributesSupport
    implements Resource
{
    /** The hashcode. */
    private int hashCode;

    // ///////////////////////////////////////////////////////////////////////////////////////////

    protected void setDelegate(Resource delegate)
    {
        super.setDelegate(delegate);
        this.hashCode = delegate.hashCode();
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the hashcode for this entity.
     * <p>
     * This implementation returns the hashcode of the delegate.
     * </p>
     * 
     * @return the hashcode of the object.
     */
    public final int hashCode()
    {
        return hashCode;
    }

    /**
     * Checks if another object represents the same entity.
     * <p>
     * This implementation compares the delegates of both resources for equality.
     * </p>
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

    // ///////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns a String representation of this object.
     * <p>
     * This method is overridden to augment debugging. The format of the representation is as
     * following: <blockquote> <code>javaClass name #id @identity</code> </blockquote> Where:
     * <ul>
     * <li><code>javaClass</code> is the actual implementation class of the object</li>
     * <li><code>path</code> is the path of the resource as returned by the {@link #getPath()}
     * method.</li>
     * <li><code>id</code> is the identifier of the resource as returned by the {@link #getId()}
     * method.</li>
     * <li><code>identity</code> is the object instance's identity hashcode as retured by the
     * <code>System.getIdentityHashCode(Object)</code> function.</li>
     * </ul>
     * </p>
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
     * <p>
     * The path name is composed of the names of all of the resource's parents, separated by /
     * characters. If the top level parent (resource that has <code>null</code> parent) is the
     * 'root' resource #1, the pathname will start with a /. Please note that the pathname can also
     * denote other resources than this one, unless all resources in your system have unique names.
     * </p>
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
    public ResourceClass<?> getResourceClass()
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
     * <p>
     * <code>null</code> is returned for top-level (root) resources. Depending on the application
     * one or more top-level resources exist in the system.
     * </p>
     * 
     * @return the parent resource.
     */
    public Resource getParent()
    {
        return delegate.getParent();
    }

    /**
     * Returns the parent resource's identifier.
     * <p>
     * This method can be used to avoid lazy loading the parent resource in certain situations.
     * </p>
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
    public <T> T get(AttributeDefinition<T> attribute)
        throws UnknownAttributeException
    {
        if((attribute.getFlags() & AttributeFlags.BUILTIN) != 0)
        {
            return delegate.get(attribute);
        }
        return getInternal(attribute);
    }

    /**
     * {@inheritDoc}
     */
    public <T> T get(AttributeDefinition<T> attribute, T defaultValue)
        throws UnknownAttributeException
    {
        if((attribute.getFlags() & AttributeFlags.BUILTIN) != 0)
        {
            return delegate.get(attribute);
        }
        return getInternal(attribute, defaultValue);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDefined(AttributeDefinition<?> attribute)
        throws UnknownAttributeException
    {
        if((attribute.getFlags() & AttributeFlags.BUILTIN) != 0)
        {
            return delegate.isDefined(attribute);
        }
        return isDefinedInternal(attribute);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isModified(AttributeDefinition<?> attribute)
        throws UnknownAttributeException
    {
        if((attribute.getFlags() & AttributeFlags.BUILTIN) != 0)
        {
            return delegate.isModified(attribute);
        }
        return isModifiedInternal(attribute);
    }

    /**
     * {@inheritDoc}
     */
    public <T> void set(AttributeDefinition<T> attribute, T value)
        throws UnknownAttributeException, ModificationNotPermitedException, ValueRequiredException
    {
        if((attribute.getFlags() & AttributeFlags.BUILTIN) != 0)
        {
            delegate.set(attribute, value);
            return;
        }
        if((attribute.getFlags() & AttributeFlags.READONLY) != 0)
        {
            throw new ModificationNotPermitedException("attribute " + attribute.getName()
                + " is declared READONLY");
        }
        if(value == null && (attribute.getFlags() & AttributeFlags.REQUIRED) != 0)
        {
            throw new ValueRequiredException("attribute " + attribute.getName()
                + "is declared as REQUIRED");
        }
        value = attribute.getAttributeClass().getHandler().toAttributeValue(value);
        attribute.getAttributeClass().getHandler().checkDomain(attribute.getDomain(), value);
        setInternal(attribute, value);
    }

    /**
     * {@inheritDoc}
     */
    public void unset(AttributeDefinition<?> attribute)
        throws ValueRequiredException, UnknownAttributeException
    {
        if((attribute.getFlags() & AttributeFlags.BUILTIN) != 0)
        {
            delegate.unset(attribute);
            return;
        }
        if((attribute.getFlags() & AttributeFlags.REQUIRED) != 0)
        {
            throw new ValueRequiredException("attribute " + attribute.getName()
                + "is declared as REQUIRED");
        }
        unsetInternal(attribute);
    }

    /**
     * {@inheritDoc}
     */
    public void setModified(AttributeDefinition<?> attribute)
        throws UnknownAttributeException
    {
        if((attribute.getFlags() & AttributeFlags.BUILTIN) != 0)
        {
            delegate.setModified(attribute);
            return;
        }
        setModifiedInternal(attribute);
    }
}
