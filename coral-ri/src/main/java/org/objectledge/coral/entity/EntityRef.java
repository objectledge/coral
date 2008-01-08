package org.objectledge.coral.entity;

import java.lang.ref.ReferenceQueue;
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
    
    /** Necessary to implement equals() method at the abstract class level. */
    private Class<E> entityClass;

    /** Weak reference to the entity object. */
    private WeakReference<E> ref;
    
    /** Precomputed hashcode */
    private int hashCode;

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
     * @param entity An Entity object.
     * @param coralCore The Coral Core.
     * @param queue reference queue, may be null.
     */
    public EntityRef(Class<E> entityClass, E entity, CoralCore coralCore, ReferenceQueue<E> queue)
    {
        this.coralCore = coralCore;
        this.entityClass = entityClass;
        ref = new WeakReference<E>(entity, queue);
        if(entity != null)
        {
            id = entity.getId();
        }
        else
        {
            id = -1L;
        }
        this.hashCode = entityClass.hashCode() ^ (int)(id * 0x11111111);
    }
    
    /**
     * Creates a new instance of EntityRef.
     * @param id Entity identitfier.
     * @param coralCore The Coral Core.
     * @param queue reference queue, may be null.
     */
    public EntityRef(Class<E> entityClass, long id, CoralCore coralCore, ReferenceQueue<E> queue)
    {
        this.coralCore = coralCore;
        ref = new WeakReference<E>(null);
        this.entityClass = entityClass;
        this.id = id;
        this.hashCode = entityClass.hashCode() ^ (int)(id * 0x11111111);
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
    
    /**
     * Equals operation based on underlying entity class & id equalities.
     */
    @SuppressWarnings("unchecked")
    public boolean equals(Object o)
    {
        if(o == null || !(o instanceof EntityRef))
        {
            return false;
        }
        EntityRef r = (EntityRef)o;
        return r.entityClass.equals(entityClass) && r.id == id;
    }
    
    /**
     * HashCode implementation following Entity contract.
     */
    public int hashCode()
    {
        return hashCode;
    }
}
