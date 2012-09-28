package org.objectledge.coral.datatypes;

import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.database.Database;

/**
 * Handles persistency of {@link org.objectledge.coral.security.Subject} references.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: SubjectAttributeHandler.java,v 1.6 2005-02-08 20:33:42 rafal Exp $
 */
public class SubjectAttributeHandler
    extends EntityAttributeHandler<Subject>
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
        CoralSecurity coralSecurity, CoralSchema coralSchema, AttributeClass<Subject> attributeClass)
    {
        super(database, coralStore, coralSecurity, coralSchema, attributeClass);
    }
    
    /**
     * {@inheritDoc} 
     */
    protected Subject instantiate(long id)
        throws EntityDoesNotExistException
    {
        return coralSecurity.getSubject(id);
    }
    
    /**
     * {@inheritDoc} 
     */
    protected Subject[] instantiate(String name)
    {
        try
        {
            return new Subject[] { coralSecurity.getSubject(name) };
        }
        catch(EntityDoesNotExistException e)
        {
            return new Subject[0];
        }
    }
}
