package org.objectledge.coral.table.comparator;

import java.math.BigDecimal;
import java.text.Collator;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.store.Resource;

/**
 * Compares attributes using a given attribute, taking into consideration that the attributes may be
 * undefined for particular objects.
 * <p>
 * This class contains logic extracted from TimeComparator that is applicable to all attribute
 * types. Unfortunately refactoring TimeComparator to reuse this class is rather cumbersome, because
 * an instance of relevant ResourceClass<T> would have to be provided each time an instance of any
 * class derived from TimeComparator were to be constructed.
 * </p>
 * 
 * @author rafal.krzewski@caltha.pl
 * @param <V>
 * @param <T>
 */
public abstract class AttributeComparator<T extends Resource, V>
    implements Comparator<T>
{
    public enum Direction
    {
        ASC, DESC
    }

    private final Direction direction;

    private final AttributeDefinition<V> attrDef;

    public AttributeComparator(AttributeDefinition<V> attrDef, Direction direction)
    {
        this.attrDef = attrDef;
        this.direction = direction;
    }

    /**
     * Compare a pair of non-null values of type V.
     * 
     * @param v1 first value.
     * @param v2 second value.
     * @return the result of the comparison.
     */
    protected abstract int compareTo(V v1, V v2);

    /**
     * Compares two attributes values. Values may be null, the contract is:
     * <ul>
     * <li>If both values are not null, they are simply compared, but when sort direction is DESC,
     * comparison is reversed.</li>
     * <li>If both are null, values are considered equal</li>
     * <li>If one of the values is null, and sort direction is ASC, null value is considered
     * greater, and when direction is DESC, null values is considered lesser than then non-null
     * value.</li>
     * </ul>
     * 
     * @param v1 first value.
     * @param v2 second value.
     * @return the result of the comparison.
     */
    public int compareValues(V v1, V v2)
    {
        if(v1 != null && v2 != null)
        {
            return direction == Direction.ASC ? compareTo(v1, v2) : compareTo(v2, v1);
        }

        if(v1 == null)
        {
            if(v2 == null)
            {
                return 0;
            }
            else
            {
                // d1 == null && d2 != null
                return 1;
            }
        }
        // d1 != null && d2 == null
        return -1;
    }

    /**
     * Compares two resources using values of the specified attribute:
     * <ul>
     * <li>If both values are defined, they are simply compared, but when sort direction is DESC,
     * comparison is reversed.</li>
     * <li>If both are undefined, resource ids are compared, thus producing stable sort in all
     * situations</li>
     * <li>If one of the values is undefined, and sort direction is ASC, resource with undefined
     * value is considered greater, and when direction is DESC, resource with undefined value is
     * considered lesser than then the resource with defined value.</li>
     * </ul>
     * 
     * @param res1 first resource.
     * @param res2 second resource.
     * @return the result of the comparison.
     */
    @Override
    public int compare(T res1, T res2)
    {
        V v1 = res1.get(attrDef);
        V v2 = res2.get(attrDef);
        int rel = compareValues(v1, v2);
        if(rel != 0)
        {
            return rel;
        }
        else
        {
            return (int)(res1.getId() - res2.getId());
        }
    }

    /**
     * Provide an instance of AttributeComparator for a given attribute defintition.
     * <p>
     * Attributes based on to the following java types are supported:
     * <ul>
     * <li>Boolean</li>
     * <li>Integer</li>
     * <li>Long</li>
     * <li>BigDecimal (coral-datatypes "number" attribute class)</li>
     * <li>Date</li>
     * <li>String</li>
     * </ul>
     * </p>
     * 
     * @param attrDef
     * @param locale
     * @param direction
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends Resource, V> Comparator<T> getInstance(
        final AttributeDefinition<V> attrDef, final Locale locale,
        final AttributeComparator.Direction direction)
    {
        Class<V> cl = attrDef.getAttributeClass().getJavaClass();

        if(cl.isAssignableFrom(Boolean.class))
        {
            return new AttributeComparator<T, Boolean>((AttributeDefinition<Boolean>)attrDef,
                            direction)
                {
                    @Override
                    protected int compareTo(Boolean v1, Boolean v2)
                    {
                        return v1.compareTo(v2);
                    }
                };
        }
        if(cl.isAssignableFrom(Integer.class))
        {
            return new AttributeComparator<T, Integer>((AttributeDefinition<Integer>)attrDef,
                            direction)
                {
                    @Override
                    protected int compareTo(Integer v1, Integer v2)
                    {
                        return v1 - v2;
                    }
                };
        }
        if(cl.isAssignableFrom(Long.class))
        {
            return new AttributeComparator<T, Long>((AttributeDefinition<Long>)attrDef, direction)
                {
                    @Override
                    protected int compareTo(Long v1, Long v2)
                    {
                        return (int)(v1 - v2);
                    }
                };
        }
        if(cl.isAssignableFrom(BigDecimal.class))
        {
            return new AttributeComparator<T, BigDecimal>((AttributeDefinition<BigDecimal>)attrDef,
                            direction)
                {
                    @Override
                    protected int compareTo(BigDecimal v1, BigDecimal v2)
                    {
                        return v1.compareTo(v2);
                    }
                };
        }
        if(cl.isAssignableFrom(Date.class))
        {
            return new AttributeComparator<T, Date>((AttributeDefinition<Date>)attrDef, direction)
                {
                    @Override
                    protected int compareTo(Date v1, Date v2)
                    {
                        return v1.compareTo(v2);
                    }
                };
        }
        if(cl.isAssignableFrom(String.class))
        {
            return new AttributeComparator<T, String>((AttributeDefinition<String>)attrDef,
                            direction)
                {
                    private Collator col = Collator.getInstance(locale);

                    @Override
                    protected int compareTo(String v1, String v2)
                    {
                        return col.compare(v1, v2);
                    }
                };
        }
        throw new IllegalArgumentException("unsupported attribute type " + cl.getName());
    }
}
