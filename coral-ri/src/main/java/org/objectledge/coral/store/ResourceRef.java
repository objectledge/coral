package org.objectledge.coral.store;

import java.lang.ref.ReferenceQueue;

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
     * @param queue reference queue, may be null.
     */
    public ResourceRef(Resource resource, CoralCore coralCore, ReferenceQueue<Resource> queue)
    {
        super(Resource.class, resource, coralCore, queue);
    }
    
    /**
     * Create a new reference instance.
     * @param id resource id.
     * @param coralCore the Coral core.
     * @param queue reference queue, may be null.
     */
    public ResourceRef(long id, CoralCore coralCore, ReferenceQueue<Resource> queue)
    {
        super(Resource.class, id, coralCore, queue);
    }

    /**
     * Create a new reference instance.
     * @param resource the resource.
     * @param coralCore the Coral core.
     */
    public ResourceRef(Resource resource, CoralCore coralCore)
    {
        super(Resource.class, resource, coralCore, null);
    }
    
    /**
     * Create a new reference instance.
     * @param id resource id.
     * @param coralCore the Coral core.
     */
    public ResourceRef(long id, CoralCore coralCore)
    {
        super(Resource.class, id, coralCore, null);
    }    
    
    @Override
    protected Resource resolve(long id)
        throws EntityDoesNotExistException
    {
        return coralCore.getStore().getResource(id);
    }

}
