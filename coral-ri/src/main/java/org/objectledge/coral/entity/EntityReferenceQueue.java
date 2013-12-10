package org.objectledge.coral.entity;

import java.lang.ref.ReferenceQueue;

/**
 * A reference queue for WeakEntityReferences
 * 
 * @author rafal.krzewski@caltha.pl
 * @param <E>
 */
public class EntityReferenceQueue<E extends Entity>
    extends ReferenceQueue<E>
{
    public WeakEntityReference<E> poll()
    {
        return (WeakEntityReference<E>)super.poll();
    }
}
