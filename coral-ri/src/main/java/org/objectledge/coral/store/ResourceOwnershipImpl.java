package org.objectledge.coral.store;

import org.objectledge.coral.security.Subject;

/**
 * Represetnts owner -- resource relationship.
 *
 * <p>This is a synthetic association -- it does not correspond to 
 * any entity in the database, because of one to zero or one multiplicity of
 * the relationship. It was introduced for orthogonality of the
 * <code>event</code> package.
 */
public class ResourceOwnershipImpl
    implements ResourceOwnership
{
    // Instance variables ///////////////////////////////////////////////////////////////////////

    /** The owner. */
    private Subject owner;

    /** The resource. */
    private Resource resource;

    // Initialization ///////////////////////////////////////////////////////////////////////////

    /**
     * Constructs a {@link ResourceOwnershipImpl}.
     *
     * @param owner the owner.
     * @param resource the resource.
     */
    ResourceOwnershipImpl(Subject owner, Resource resource)
    {
        this.owner = owner;
        this.resource = resource;
    }

    // Hashing & equality ///////////////////////////////////////////////////////////////////////

    /**
     * A hashing function that attempts to achieve uniform bit distribution of
     * values derived from small integer numbers.
     *
     * @param id a long identifier
     * @return a hash value.
     */
    protected int hashCode(long id)
    {
        return (int)(id * 0x11111111);
    }

    /**
     * Returs the hashcode for this entity.
     *
     * @return the hashcode of the object.
     */
    public int hashCode()
    {
        return hashCode(owner.getId()) ^ hashCode(resource.getId());
    }

    /**
     * Checks if another object represens the same entity.
     *
     * @param other the other objects.
     * @return <code>true</code> if the other object represents the same entity.
     */
    public boolean equals(Object other)
    {
        if(other != null && other.getClass().equals(getClass()))
        {
            return owner.equals(((ResourceOwnership)other).getOwner()) &&
                resource.equals(((ResourceOwnership)other).getResource());
        }
        return false;
    }

    // ResourceInheritace interface /////////////////////////////////////////////////////////////

    /**
     * Returns the owner.
     *
     * @return the owner.
     */
    public Subject getOwner()
    {
        return owner;
    }

    /**
     * Returns the resource.
     *
     * @return the resource.
     */
    public Resource getResource()
    {
        return resource;
    }
}
