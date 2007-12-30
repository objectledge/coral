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
     * 
     * @param coralCore the Coral core.
     * @param resource the resource.
     */
    public ResourceRef(CoralCore coralCore, Resource resource)
    {
        super(coralCore, resource);
    }
    
    /**
     * Create a new reference instance.
     * 
     * @param coralCore the Coral core.
     * @param id resource id.
     */
    public ResourceRef(CoralCore coralCore, long id)
    {
        super(coralCore, id);
    }

    @Override
    protected Resource resolve(long id)
        throws EntityDoesNotExistException
    {
        return coralCore.getStore().getResource(id);
    }

}
