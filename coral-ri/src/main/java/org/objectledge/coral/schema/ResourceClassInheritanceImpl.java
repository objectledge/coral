package org.objectledge.coral.schema;

import org.objectledge.coral.CoralCore;
import org.objectledge.coral.entity.AbstractAssociation;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.OutputRecord;
import org.objectledge.database.persistence.PersistenceException;

/**
 * Represents resource class inheritance relationship.
 *
 * @version $Id: ResourceClassInheritanceImpl.java,v 1.6 2004-03-09 15:46:47 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class ResourceClassInheritanceImpl
    extends AbstractAssociation
    implements ResourceClassInheritance
{
    // Instance variables ///////////////////////////////////////////////////////////////////////

    /** The component hub. */
    private CoralCore coral;

    /** The parent class. */
    private ResourceClass parent;
    
    /** The child class. */
    private ResourceClass child;
    
    // Initialization ///////////////////////////////////////////////////////////////////////////

    /**
     * Contstructs a ResourceClassInheritanceImpl.
     * 
     * @param coral the component hub.
     */
    public ResourceClassInheritanceImpl(CoralCore coral)
    {
        super();
        this.coral= coral;
    }

    /**
     * Constructs a {@link ResourceClassInheritanceImpl}.
     *
     * @param coral the component hub.
     *
     * @param parent the parent class.
     * @param child the child class.
     */
    public ResourceClassInheritanceImpl(CoralCore coral, 
        ResourceClass parent, ResourceClass child)
    {
        super();
        this.parent = parent;
        this.child = child;
        this.coral= coral;
    }

    // Hashing & equality ///////////////////////////////////////////////////////////////////////

    /**
     * Returs the hashcode for this entity.
     *
     * @return the hashcode of the object.
     */
    public int hashCode()
    {
        return hashCode(parent.getId()) ^ hashCode(child.getId());
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
            return parent.equals(((ResourceClassInheritanceImpl)other).getParent()) &&
                child.equals(((ResourceClassInheritanceImpl)other).getChild());
        }
        return false;
    }

    // Persistent interface /////////////////////////////////////////////////////////////////////

    /** The key columns */
    private static final String[] KEY_COLUMNS = { "parent", "child" };    

    /**
     * Returns the name of the table this type is mapped to.
     *
     * @return the name of the table.
     */
    public String getTable()
    {
        return "coral_resource_class_inheritance";
    }

    /** 
     * Returns the names of the key columns.
     *
     * @return the names of the key columns.
     */
    public String[] getKeyColumns()
    {
        return KEY_COLUMNS;
    }
    
    /**
     * Stores the fields of the object into the specified record.
     *
     * <p>You need to call <code>getData</code> of your superclasses if they
     * are <code>Persistent</code>.</p>
     *
     * @param record the record to store state into.
     * @throws PersistenceException if there is a problem storing field values.
     */
    public void getData(OutputRecord record)
        throws PersistenceException
    {
        record.setLong("parent", parent.getId());
        record.setLong("child", child.getId());
    }

    /**
     * Loads the fields of the object from the specified record.
     *
     * <p>You need to call <code>setData</code> of your superclasses if they
     * are <code>Persistent</code>.</p>
     * 
     * @param record the record to read state from.
     * @throws PersistenceException if there is a problem loading field values.
     */
    public void setData(InputRecord record)
        throws PersistenceException
    {
        long parentId = record.getLong("parent");
        try
        {
            this.parent = coral.getSchema().getResourceClass(parentId);
        }
        catch(EntityDoesNotExistException e)
        {
            throw new PersistenceException("Failed to load ResourceClassInheritance", e);
        }
        long childId = record.getLong("child");
        try
        {
            this.child = coral.getSchema().getResourceClass(childId);
        }
        catch(EntityDoesNotExistException e)
        {
            throw new PersistenceException("Failed to load ResourceClassInheritance", e);
        }
    }

    // ResourceClassInheritance inerface ////////////////////////////////////////////////////////

    /**
     * Returns the parent class in this relationship.
     *
     * @return the parent class in this relationship.
     */
    public ResourceClass getParent()
    {
        return parent;
    }
    
    /**
     * Returns the child class in this relationship.
     *
     * @return the child class in this relationship.
     */
    public ResourceClass getChild()
    {
        return child;
    }
}
