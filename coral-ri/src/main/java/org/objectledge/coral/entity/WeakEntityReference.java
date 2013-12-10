package org.objectledge.coral.entity;

import java.lang.ref.WeakReference;

/**
 * A WeakRefernce that retains referent entity id after the reference has been cleared and enqueued.
 * 
 * @author rafal.krzewski@caltha.pl
 * @param <E>
 */
public class WeakEntityReference<E extends Entity>
    extends WeakReference<E>
{
    private final long id;

    /**
     * Creates an EphemeralEntityRef
     * 
     * @param referent the Entity this reference refers to.
     * @param q the queue with which the reference is to be registered, or <tt>null</tt> if
     *        registration is not required
     */
    public WeakEntityReference(E referent, EntityReferenceQueue<? super E> q)
    {
        super(referent, q);
        this.id = referent != null ? referent.getId() : -1L;
    }

    /**
     * Returns the identifier of the referent Entity.
     * 
     * @return entity identifier, or -1 for {@code null} reference.
     */
    public long getId()
    {
        return id;
    }
}
