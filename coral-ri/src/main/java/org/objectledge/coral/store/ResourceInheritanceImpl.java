package org.objectledge.coral.store;


/**
 * Represetnts parent -- child relationship between two resources.
 *
 * <p>This is a synthetic association -- it does not correspond to 
 * any entity in the database, because of one to zero or one multiplicity of
 * the relationship. It was introduced for orthogonality of the
 * <code>event</code> package.
 */
public class ResourceInheritanceImpl
    implements ResourceInheritance
{
    // Instance variables ///////////////////////////////////////////////////////////////////////

    /** The parent. */
    private Resource parent;

    /** The parent. */
    private Resource child;

    // Initialization ///////////////////////////////////////////////////////////////////////////

    /**
     * Constructs a {@link ResourceInheritanceImpl}.
     *
     * @param parent the parent resource.
     * @param child the child resource.
     */
    public ResourceInheritanceImpl(Resource parent, Resource child)
    {
        this.parent = parent;
        this.child = child;
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
        return hashCode(child.getId()) ^ hashCode(child.getId());
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
            return parent.equals(((ResourceInheritance)other).getParent()) &&
                child.equals(((ResourceInheritance)other).getChild());
        }
        return false;
    }

    // ResourceInheritace interface /////////////////////////////////////////////////////////////

    /**
     * Returns the parent resource.
     *
     * @return the parent resource.
     */
    public Resource getParent()
    {
        return parent;
    }

    /**
     * Returns the child resource.
     *
     * @return the child resource.
     */
    public Resource getChild()
    {
        return child;
    }
}
