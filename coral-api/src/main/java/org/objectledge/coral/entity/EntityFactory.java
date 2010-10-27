package org.objectledge.coral.entity;

/**
 * A generic factory for Entity objects.
 */
public interface EntityFactory<E extends Entity>
{
    /**
     * The factory method.
     * 
     * @param id
     * @return an Entity
     */
    public E getEntity(long id)
        throws EntityDoesNotExistException;

}
