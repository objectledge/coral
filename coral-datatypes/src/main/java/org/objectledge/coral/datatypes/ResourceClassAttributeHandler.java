package org.objectledge.coral.datatypes;

import org.objectledge.coral.entity.Entity;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.database.Database;

/**
 * Handles persistency of {@link ResourceClass} references.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: ResourceClassAttributeHandler.java,v 1.5 2005-01-18 12:39:12 rafal Exp $
 */
public class ResourceClassAttributeHandler
    extends EntityAttributeHandler
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
                                          CoralSecurity coralSecurity, CoralSchema coralSchema,
                                          AttributeClass attributeClass)
    {
        super(database, coralStore, coralSecurity, coralSchema, attributeClass);
    }
    
    /**
     * {@inheritDoc} 
     */
    protected Entity instantiate(long id)
        throws EntityDoesNotExistException
    {
        return coralSchema.getResourceClass(id);
    }
    
    /**
     * {@inheritDoc} 
     */
    protected Entity[] instantiate(String name)
    {
        try
        {
            return new Entity[] { coralSchema.getResourceClass(name) };
        }
        catch(EntityDoesNotExistException e)
        {
            return new Entity[0];
        }
    }
}
