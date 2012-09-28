package org.objectledge.coral.datatypes;

import java.util.List;

import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.Resource;
import org.objectledge.database.Database;

/**
 * Handles persistency of <code>java.util.List</code> 
 * objects containing weak reference to resources.
 *
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: WeakResourceListAttributeHandler.java,v 1.4 2005-02-21 11:52:20 rafal Exp $
 */
public class WeakResourceListAttributeHandler<T extends Resource>
    extends ResourceListAttributeHandler<T, WeakResourceList<T>>
{
    /**
     * The constructor.
     * 
     * @param database the database.
     * @param coralStore the store.
     * @param coralSecurity the security.
     * @param coralSchema the scheam.
     * @param coralSessionFactory the session factory.
     * @param attributeClass the attribute class.
     */
    public WeakResourceListAttributeHandler(Database database, CoralStore coralStore,
                                CoralSecurity coralSecurity, CoralSchema coralSchema,
                                CoralSessionFactory coralSessionFactory,
 AttributeClass<WeakResourceList<T>> attributeClass)
    {
        super(database, coralStore, coralSecurity, coralSchema, coralSessionFactory, 
            attributeClass);
    }

    // integrity constraints ////////////////////////////////////////////////    

    /**
     * {@inheritDoc}
     */
    public boolean containsResourceReferences()
    {
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    public Resource[] getResourceReferences(WeakResourceList<T> value)
    {
        return new Resource[0];
    }
    
    /**
     * Instantiates a resource list.
     * 
     * @param list list items.
     * @return a ResourceList instance.
     */
    protected WeakResourceList<T> instantiate(List<?> list)
    {
        return new WeakResourceList<T>(coralSessionFactory, list);
    }
}
