package org.objectledge.coral.store;

import org.objectledge.coral.entity.Association;
import org.objectledge.coral.security.Subject;

/**
 * Represetnts owner -- resource relationship.
 *
 * <p>This is a synthetic association -- it does not correspond to 
 * any entity in the database, because of one to zero or one multiplicity of
 * the relationship. It was introduced for orthogonality of the
 * <code>event</code> package.
 */
public interface ResourceOwnership
    extends Association
{
    /**
     * Returns the owner.
     *
     * @return the owner.
     */
    public Subject getOwner();

    /**
     * Returns the resource.
     *
     * @return the resource.
     */
    public Resource getResource();
}
