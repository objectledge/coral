package org.objectledge.coral.datatypes;

import org.objectledge.coral.entity.Entity;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.database.Database;

/**
 * Handles persistency of {@link Subject} references.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: SubjectAttributeHandler.java,v 1.5 2005-01-18 12:39:12 rafal Exp $
 */
public class SubjectAttributeHandler
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
    public SubjectAttributeHandler(Database database, CoralStore coralStore,
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
        return coralSecurity.getSubject(id);
    }
    
    /**
     * {@inheritDoc} 
     */
    protected Entity[] instantiate(String name)
    {
        try
        {
            return new Entity[] { coralSecurity.getSubject(name) };
        }
        catch(EntityDoesNotExistException e)
        {
            return new Entity[0];
        }
    }
}
