package org.objectledge.coral.datatypes;

import java.util.List;

import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.Resource;
import org.objectledge.database.Database;

/**
 * Handles persistency of <code>java.util.List</code> 
 * objects containing weak reference to resources.
 *
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: WeakResourceListAttributeHandler.java,v 1.3 2005-01-19 06:44:17 rafal Exp $
 */
public class WeakResourceListAttributeHandler
    extends ResourceListAttributeHandler
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
    public WeakResourceListAttributeHandler(Database database, CoralStore coralStore,
                                CoralSecurity coralSecurity, CoralSchema coralSchema,
                                AttributeClass attributeClass)
    {
        super(database, coralStore, coralSecurity, coralSchema, attributeClass);
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
    public Resource[] getResourceReferences(Object value)
    {
        return new Resource[0];
    }
    
    /**
     * Instantiates a resource list.
     * 
     * @param list list items.
     * @return a ResourceList instance.
     */
    protected ResourceList instantiate(List list)
    {
        return new WeakResourceList(coralStore, list);
    }
}
