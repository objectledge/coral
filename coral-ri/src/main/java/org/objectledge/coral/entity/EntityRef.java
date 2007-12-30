package org.objectledge.coral.entity;

import java.lang.ref.WeakReference;

import org.objectledge.coral.CoralCore;

/**
 * A helper object that allows caching reference to an Entity that can be garbage collected and then
 * reloaded upon request.
 * 
 * @author Rafał Krzewski
 */
public abstract class EntityRef<E extends Entity>
{
    /** The Coral core. */
    protected CoralCore coralCore;

    /** Identifier of the entity. */
    private long id;

    /** Weak reference to the entity object. */
    private WeakReference<E> ref;

    /**
     * Resolves identifier to a concrete Entity object using Coral core.
     * 
     * @param id identifier of the Entity.
     * @return Entity object.
     * @throws EntityDoesNotExistException
     */
    protected abstract E resolve(long id)
        throws EntityDoesNotExistException;

    /**
     * Creates a new instance of EntityRef.
     * 
     * @param coralCore The Coral Core.
     * @param entity An Entity object.
     */
    public EntityRef(CoralCore coralCore, E entity)
    {
        this.coralCore = coralCore;
        ref = new WeakReference<E>(entity);
        if(entity != null)
        {
            id = entity.getId();
        }
        else
        {
            id = -1L;
        }
    }
    
    /**
     * Creates a new instance of EntityRef.
     * 
     * @param coralCore The Coral Core.
     * @param id Entity identitfier.
     */
    public EntityRef(CoralCore coralCore, long id)
    {
        this.coralCore = coralCore;
        ref = new WeakReference<E>(null);
        this.id = id;
    }    

    /**
     * Returns the referent Entity.
     * 
     * @return the referent Entity.
     * @throws EntityDoesNotExistException
     */
    public synchronized E get()
        throws EntityDoesNotExistException
    {
        E entity = ref.get();
        if(entity == null && id != -1L)
        {
            entity = resolve(id);
            ref = new WeakReference<E>(entity);
        }
        return entity;
    }
    
    /**
     * Returns the identifier of the Entity.
     * 
     * @return identifier of the Entity or -1 for null reference;
     */
    public long getId()
    {
        return id;
    }
}
