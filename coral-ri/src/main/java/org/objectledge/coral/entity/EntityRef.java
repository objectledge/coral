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
    protected final CoralCore coralCore;

    /** Identifier of the entity. */
    private final long id;
    
    /** Necessary to implement equals() method at the abstract class level. */
    private final Class<E> entityClass;

    /** Pre-computed hashcode */
    private final int hashCode;

    /** Weak reference to the entity object. */
    private volatile WeakReference<E> ref;

    /** Queue that the reference should be associated with. */
    private final EntityReferenceQueue<E> queue;
    
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
    public EntityRef(Class<E> entityClass, E entity, CoralCore coralCore,
        EntityReferenceQueue<E> queue)
    {
        this.coralCore = coralCore;
        this.entityClass = entityClass;
        this.queue = queue;
        ref = new WeakEntityReference<>(entity, queue);
        id = entity != null ? entity.getId() : -1L;
        this.hashCode = entityClass.hashCode() ^ (int)(id * 0x11111111);
    }
    
    /**
     * Creates a new instance of EntityRef.
     * @param id Entity identitfier.
     * @param coralCore The Coral Core.
     * @param queue reference queue, may be null.
     */
    public EntityRef(Class<E> entityClass, long id, CoralCore coralCore,
        EntityReferenceQueue<E> queue)
    {
        this.coralCore = coralCore;
        this.queue = queue;
        this.ref = null;
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
    public E get()
        throws EntityDoesNotExistException
    {
        E entity = null;
        if(ref != null)
        {
            entity = ref.get();
        }
        if(entity == null && id != -1L)
        {
            entity = resolve(id);
            ref = new WeakEntityReference<>(entity, queue);
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
    public boolean equals(Object o)
    {
        if(o == null || !(o instanceof EntityRef))
        {
            return false;
        }
        EntityRef<?> r = (EntityRef<?>)o;
        return r.entityClass.equals(entityClass) && r.id == id;
    }
    
    /**
     * HashCode implementation following Entity contract.
     */
    public int hashCode()
    {
        return hashCode;
    }

    public String toString()
    {
        StringBuilder buff = new StringBuilder();
        buff.append(getClass().getName());
        buff.append(" #");
        buff.append(id);
        buff.append(" @");
        buff.append(Integer.toString(System.identityHashCode(this), 16));
        return buff.toString();
    }
}
