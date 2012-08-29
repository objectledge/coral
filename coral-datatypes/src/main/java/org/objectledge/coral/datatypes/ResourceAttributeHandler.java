package org.objectledge.coral.datatypes;

import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.store.ConstraintViolationException;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.Resource;
import org.objectledge.database.Database;

/**
 * Handles persistency of {@link Resource} references.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: ResourceAttributeHandler.java,v 1.6 2005-01-18 12:55:12 rafal Exp $
 */
public class ResourceAttributeHandler<T extends Resource>
    extends EntityAttributeHandler<T>
{
    /**
     * The constructor.
     * 
     * @param database the database.
     * @param coralStore the store.
     * @param coralSecurity the security.
     * @param coralSchema the scheam.
     * @param attributeClass the attribute class.
     */
    public ResourceAttributeHandler(Database database, CoralStore coralStore,
        CoralSecurity coralSecurity, CoralSchema coralSchema, AttributeClass<T> attributeClass)
    {
        super(database, coralStore, coralSecurity, coralSchema, attributeClass);
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * {@inheritDoc} 
     */
    protected T instantiate(long id)
        throws EntityDoesNotExistException
    {
        return (T)coralStore.getResource(id);
    }
    
    /**
     * {@inheritDoc} 
     */
    protected T[] instantiate(String name)
    {
        return (T[])coralStore.getResourceByPath(name);
    }

    // value domain /////////////////////////////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public void checkDomain(String domain)
    {
        if(domain != null)
        {
            try
            {
                coralSchema.getResourceClass(domain);
            }
            catch(EntityDoesNotExistException e)
            {
                throw new  IllegalArgumentException("malformed constraint '"+domain+
                                                    "', valid resource class name expected");    
            }
        }
    }   
    
    /**
     * {@inheritDoc}
     */
    public void checkDomain(String domain, Resource value)
        throws ConstraintViolationException
    {
        if(domain != null)
        {
            try
            {
                ResourceClass<?> rc = coralSchema.getResourceClass(domain);
                if(!rc.getJavaClass().isInstance(value))
                {
                    throw new ConstraintViolationException(value.getClass().getName()
                        + "is not a subclass of " + rc.getJavaClass().getName());
                }
            }
            catch(EntityDoesNotExistException e)
            {
                throw new IllegalArgumentException("malformed constraint '" + domain
                    + "', valid resource class name expected");
            }
        }
    }

    // integrity constraints ////////////////////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public boolean containsResourceReferences()
    {
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    public Resource[] getResourceReferences(Resource value)
    {
        Resource[] result;
        if(value != null)
        {
            result = new Resource[1];
            result[0] = value;
        }
        else
        {
            result = new Resource[0];
        }
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean clearResourceReferences(Resource value)
    {
        return true;
    }
    
    /**
     * Converts an attribute value into a human readable string.
     *
     * @param value the value to convert.
     * @return a human readable string.
     */
    public String toPrintableString(T value)
    {
        checkValue(value);
        return (value).getPath();
    }    
}
