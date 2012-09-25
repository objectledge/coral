package org.objectledge.coral.datatypes;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.BitSet;

import org.objectledge.coral.BackendException;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.schema.UnknownAttributeException;
import org.objectledge.coral.store.Resource;

/**
 * Bottom layer of ResourceData class concerned with storing of attribute values, attribute value
 * identifiers and modification flags.
 * 
 * @author rafal.krzewski@caltha.pl
 */
public abstract class ResourceAttributesSupport
    implements ResourceAttributes
{
    /** the attribute values. */
    private Object[] attributes;

    /** the external attribute ids. */
    private long[] ids;

    /** Set of AttributeDefinitions of the modified attributes. */
    private BitSet modified;

    /** Resource metadata delegate. */
    protected Resource delegate;

    // ///////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Set metadata delegate.
     * 
     * @param delegate the delegate object.
     */
    protected void setDelegate(Resource delegate)
    {
        this.delegate = delegate;
        if(attributes == null)
        {
            resetAttributes();
        }
        initDefinitions(delegate.getResourceClass());
    }

    public Resource getDelegate()
    {
        return delegate;
    }

    /**
     * Reset attribute storage.
     * <p>
     * Current values and value identifiers are discarded, and storage arrays are resized to to
     * match the number of resource class attributes.
     * </p>
     */
    protected void resetAttributes()
    {
        int arraySize = delegate.getResourceClass().getMaxAttributeIndex() + 1;
        attributes = new Object[arraySize];
        ids = new long[arraySize];
        modified = new BitSet(arraySize);
    }

    // Interface for resource storage helper objects /////////////////////////////////////////////

    /**
     * Sets a value of locally stored attribute.
     * 
     * @param attr the attribute.
     * @param value the value.
     */
    public <A> void setValue(AttributeDefinition<A> attr, A value)
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
    @SuppressWarnings("unchecked")
    public <T> T getValue(AttributeDefinition<T> attr)
    {
        int index = delegate.getResourceClass().getAttributeIndex(attr);
        return (T)attributes[index];
    }

    /**
     * Sets attribute value identifier.
     * 
     * @param attr the attribute.
     * @param id the identifier.
     */
    public void setValueId(AttributeDefinition<?> attr, long id)
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
    public long getValueId(AttributeDefinition<?> attr)
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
    public boolean isValueModified(AttributeDefinition<?> attr)
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

    // Interface to ResourceAPISupport ///////////////////////////////////////////////////////////

    /**
     * Retrieve the attribute value defined in this wrapper.
     * 
     * @param attribute the attribute definition.
     * @return the attribute value is defined in this wrapper.
     */
    protected synchronized <T> T getInternal(AttributeDefinition<T> attribute)
    {
        int index = delegate.getResourceClass().getAttributeIndex(attribute);
        @SuppressWarnings("unchecked")
        T value = (T)attributes[index];
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
                    value = delegate.getResourceClass().getHandler().loadValue(attribute, id);
                    attributes[index] = value;
                    return value;
                }
            }
        }
    }

    /**
     * Returns the value of the attribute, or the supplied default value if undefined.
     * 
     * @param attribute the attribute definition.
     * @param defaultValue the default value.
     * @return attribute value.
     */
    protected <T> T getInternal(AttributeDefinition<T> attribute, T defaultValue)
    {
        int index = delegate.getResourceClass().getAttributeIndex(attribute);
        @SuppressWarnings("unchecked")
        T value = (T)attributes[index];
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
                value = delegate.getResourceClass().getHandler().loadValue(attribute, id);
                attributes[index] = value;
                return value;
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
    protected synchronized <A> void setInternal(AttributeDefinition<A> attribute, A value)
    {
        int index = delegate.getResourceClass().getAttributeIndex(attribute);
        Object oldValue = attributes[index];
        boolean attributeModified = true;
        if(oldValue == null || value == null)
        {
            if(value == null && oldValue == null)
            {
                attributeModified = false;
            }
            else
            {
                attributes[index] = value;
                attributeModified = true;
            }
        }
        else if(oldValue.equals(value))
        {
            attributeModified = false;
        }
        attributes[index] = value;
        if(attributeModified)
        {
            modified.set(index);
        }
    }

    /**
     * Unset the attribute value in this wrapper.
     * 
     * @param index the attribute index.
     */
    protected synchronized void unsetInternal(AttributeDefinition<?> attribute)
    {
        int index = delegate.getResourceClass().getAttributeIndex(attribute);
        attributes[index] = null;
        modified.set(index);
    }

    /**
     * Check if the attribute value is defined in this wrapper.
     * 
     * @param index the attribute index.
     * @return <code>true</code> if the attribute value is defined in this wrapper.
     */
    protected synchronized boolean isDefinedInternal(AttributeDefinition<?> attribute)
    {
        int index = delegate.getResourceClass().getAttributeIndex(attribute);
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
     * Mark the attribute as modified.
     * 
     * @param attr the attribute.
     */
    protected void setModifiedInternal(AttributeDefinition<?> attribute)
    {
        int index = delegate.getResourceClass().getAttributeIndex(attribute);
        modified.set(index);
    }

    // <attribute>Def fields support /////////////////////////////////////////////////////////////

    private void initDefinitions(ResourceClass<?> rClass)
    {
        synchronized(getClass())
        {
            try
            {
                Field initialized = getClass().getDeclaredField("definitionsInitialized");
                initialized.setAccessible(true);
                if(!((Boolean)initialized.get(null)).booleanValue())
                {
                    Class<?> cl = getClass();
                    while(Resource.class.isAssignableFrom(cl))
                    {
                        initDefinitions(cl, rClass);
                        cl = cl.getSuperclass();
                    }
                }
                initialized.set(null, Boolean.TRUE);
            }
            catch(NoSuchFieldException e)
            {
                // definitionInitilazed is missing - we assume that *Def fields are not there either
                return;
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

    private void initDefinitions(Class<?> cl, ResourceClass<?> rClass)
    {
        for(Field f : cl.getDeclaredFields())
        {
            if(Modifier.isStatic(f.getModifiers()) && f.getName().endsWith("Def")
                && f.getType().equals(AttributeDefinition.class))
            {
                String attrName = f.getName().substring(0, f.getName().length() - 3);
                try
                {
                    AttributeDefinition<?> attr = rClass.getAttribute(attrName);
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
}
