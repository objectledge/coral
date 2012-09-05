package org.objectledge.coral.schema;

import static java.lang.Math.max;

import java.util.Collection;

import org.objectledge.coral.entity.Entity;

/**
 * Represents mapping of attribute definition to attribute indexes. The implementation is immutable,
 * and is intended to be used through a {@code java.util.concurrent.atomic.AtomicReference}.
 * 
 * @author rafal.krzewski@objectledge.org
 */
class AttributeIndexTable
{
    /**
     * The attribute indexes, indexed by attribute definition id. Values stored in the array are
     * equal to actual index + 1, and hence 0 marks an invalid entry - an attribute undefined for
     * this class.
     */
    private final int[] table;

    /** Largest attribute index used. */
    private final int maxIndex;

    /** ResourceClass this table belongs to */
    private final ResourceClass<?> rClass;

    /**
     * Creates a new index table for a given collection of attributes.
     * 
     * @param attrs attributes.
     */
    public AttributeIndexTable(Collection<AttributeDefinition<?>> attrs, ResourceClass<?> rClass)
    {
        this.rClass = rClass;
        table = new int[maxId(attrs) + 1];
        int index = 1;
        for(AttributeDefinition<?> attr : attrs)
        {
            if((attr.getFlags() & AttributeFlags.BUILTIN) == 0)
            {
                table[(int)attr.getId()] = index++;
            }
        }
        maxIndex = index;
    }

    /**
     * Crates a new index table extending previous table with one attribute.
     * 
     * @param prev previous index table.
     * @param attr new attribute.
     */
    public AttributeIndexTable(AttributeIndexTable prev, AttributeDefinition<?> attr)
    {
        this.rClass = prev.rClass;
        table = new int[Math.max(prev.table.length, (int)attr.getId() + 1)];
        System.arraycopy(prev.table, 0, table, 0, prev.table.length);
        maxIndex = prev.maxIndex + 1;
        if((attr.getFlags() & AttributeFlags.BUILTIN) == 0)
        {
            table[(int)attr.getId()] = maxIndex;
        }
    }

    /**
     * Crates a new index table extending previous table with a collection of attributes.
     * 
     * @param prev previous index table.
     * @param attrs new attributes.
     */
    public AttributeIndexTable(AttributeIndexTable prev, Collection<AttributeDefinition<?>> attrs)
    {
        this.rClass = prev.rClass;
        table = new int[max(prev.table.length, maxId(attrs) + 1)];
        System.arraycopy(prev.table, 0, table, 0, prev.table.length);
        int index = prev.maxIndex;
        for(AttributeDefinition<?> attr : attrs)
        {
            if((attr.getFlags() & AttributeFlags.BUILTIN) == 0)
            {
                table[(int)attr.getId()] = index++;
            }
        }
        maxIndex = index;
    }

    /**
     * Returns the index of the attribute definition within a resource class.
     * <p>
     * Index of the attribute definition is an integer greater or equal to 0. It is defined for all
     * declared and inherited attributes of the class. It is guaranteed to remain constant through
     * the entire runtime of a Coral instance however it may change across different Coral
     * instantiations. An attempt will be made to keep indexes as small as possible.
     * </p>
     * <p>
     * This mechanism is provided to enable datatypes implementations to use Java arrays as a
     * space-effective means of storing resource attribute values.
     * </p>
     * 
     * @param attr the attribute.
     * @return the index of the attribute definition within a resource class.
     * @throws UnknownAttributeException if the attribute was not declared by the class or one of
     *         it's parent classes.
     */
    public int getIndex(AttributeDefinition<?> attr)
        throws UnknownAttributeException
    {
        int index = table[(int)attr.getId()];
        if(index == 0)
        {
            throw new UnknownAttributeException("attribute " + attr
                + " does not belong to resource class " + rClass);
        }
        return index - 1;
    }

    /**
     * Returns the maximum attribute index used at this moment by the resource class.
     * 
     * @return the maximum attribute index used at this moment by the resource class.
     */
    public int getMaxIndex()
    {
        return maxIndex;
    }

    /**
     * Finds the greatest id in a collection of Entities.
     * 
     * @param c entities.
     * @return the greatest id found.
     */
    private <T extends Entity> int maxId(Collection<T> c)
    {
        int maxId = 0;
        for(T e : c)
        {
            int id = (int)e.getId();
            if(id > maxId)
            {
                maxId = id;
            }
        }
        return maxId;
    }
}
