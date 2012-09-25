package org.objectledge.coral.datatypes;

import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.database.Database;

/**
 * Handles persistency of {@link org.objectledge.coral.schema.ResourceClass} references.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: ResourceClassAttributeHandler.java,v 1.6 2005-02-08 20:33:42 rafal Exp $
 */
public class ResourceClassAttributeHandler<T extends ResourceClass<?>>
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
    public ResourceClassAttributeHandler(Database database, CoralStore coralStore,
        CoralSecurity coralSecurity, CoralSchema coralSchema, AttributeClass<T> attributeClass)
    {
        super(database, coralStore, coralSecurity, coralSchema, attributeClass);
    }
    
    /**
     * {@inheritDoc} 
     */
    protected T instantiate(long id)
        throws EntityDoesNotExistException
    {
        return (T)coralSchema.getResourceClass(id);
    }
    
    /**
     * {@inheritDoc} 
     */
    @SuppressWarnings("unchecked")
    protected T[] instantiate(String name)
    {
        try
        {
            return (T[])new ResourceClass[] { coralSchema.getResourceClass(name) };
        }
        catch(EntityDoesNotExistException e)
        {
            return (T[])new ResourceClass[0];
        }
    }
}
