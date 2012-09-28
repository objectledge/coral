package org.objectledge.coral.datatypes;

import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.security.Permission;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.database.Database;

/**
 * Handles persistency of {@link org.objectledge.coral.security.Permission} references.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: PermissionAttributeHandler.java,v 1.5 2005-02-08 20:33:42 rafal Exp $
 */
public class PermissionAttributeHandler
    extends EntityAttributeHandler<Permission>
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
    public PermissionAttributeHandler(Database database, CoralStore coralStore,
        CoralSecurity coralSecurity, CoralSchema coralSchema,
        AttributeClass<Permission> attributeClass)
    {
        super(database, coralStore, coralSecurity, coralSchema, attributeClass);
    }
    
    /**
     * {@inheritDoc} 
     */
    protected Permission instantiate(long id)
        throws EntityDoesNotExistException
    {
        return coralSecurity.getPermission(id);
    }
    
    /**
     * {@inheritDoc} 
     */
    protected Permission[] instantiate(String name)
    {
        return coralSecurity.getPermission(name);
    }
}
