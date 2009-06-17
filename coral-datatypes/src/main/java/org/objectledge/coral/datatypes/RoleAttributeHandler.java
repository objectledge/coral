package org.objectledge.coral.datatypes;

import org.objectledge.coral.entity.Entity;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.database.Database;

/**
 * Handles persistency of {@link org.objectledge.coral.security.Role} refereces.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: RoleAttributeHandler.java,v 1.6 2005-02-08 20:33:42 rafal Exp $
 */
public class RoleAttributeHandler
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
    public RoleAttributeHandler(Database database, CoralStore coralStore,
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
        return coralSecurity.getRole(id);
    }
    
    /**
     * {@inheritDoc} 
     */
    protected Entity[] instantiate(String name)
    {
        return coralSecurity.getRole(name);
    }
}
