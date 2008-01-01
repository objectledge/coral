package org.objectledge.coral.store;

import org.objectledge.coral.CoralCore;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.entity.EntityRef;

/**
 * GC friendly Resource reference.
 * 
 * @author Rafał Krzewski
 */
public class ResourceRef
    extends EntityRef<Resource>
{
    /**
     * Create a new reference instance.
     * @param resource the resource.
     * @param coralCore the Coral core.
     */
    public ResourceRef(Resource resource, CoralCore coralCore)
    {
        super(resource, coralCore);
    }
    
    /**
     * Create a new reference instance.
     * @param id resource id.
     * @param coralCore the Coral core.
     */
    public ResourceRef(long id, CoralCore coralCore)
    {
        super(id, coralCore);
    }

    @Override
    protected Resource resolve(long id)
        throws EntityDoesNotExistException
    {
        return coralCore.getStore().getResource(id);
    }

}
