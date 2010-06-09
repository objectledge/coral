package org.objectledge.coral.store;

import org.objectledge.coral.entity.Association;

/**
 * Represetnts parent -- child relationship between two resources.
 *
 * <p>This is a synthetic association -- it does not correspond to 
 * any entity in the database, because of one to zero or one multiplicity of
 * the relationship. It was introduced for orthogonality of the
 * <code>event</code> package.
 */
public interface ResourceInheritance
    extends Association
{
    /**
     * Returns the parent resource.
     *
     * @return the parent resource.
     */
    public Resource getParent();

    /**
     * Returns the child resource.
     *
     * @return the child resource.
     */
    public Resource getChild();
}
