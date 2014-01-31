package org.objectledge.coral.table.comparator;

import java.util.Comparator;
import java.util.Locale;

import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.store.Resource;
import org.objectledge.table.comparator.Direction;
import org.objectledge.table.comparator.PropertyBasedComparator;

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
    extends PropertyBasedComparator<T, V>
{
    public AttributeComparator(AttributeDefinition<V> attrDef, Direction direction)
    {
        super(new AttributeAccessor<T, V>(attrDef), direction);
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
    public static <T extends Resource, V> Comparator<T> getInstance(
        final AttributeDefinition<V> attrDef, final Locale locale, final Direction direction)
    {
        return PropertyBasedComparator.getInstance(new AttributeAccessor<T, V>(attrDef), locale,
            direction);
    }

    private static class AttributeAccessor<AT extends Resource, AV>
        implements PropertyAccessor<AT, AV>
    {
        private final AttributeDefinition<AV> attrDef;

        public AttributeAccessor(AttributeDefinition<AV> attrDef)
        {
            this.attrDef = attrDef;
        }

        @Override
        public Class<AV> getType()
        {
            return attrDef.getAttributeClass().getJavaClass();
        }

        @Override
        public AV getValue(AT o)
        {
            return o.get(attrDef);
        }
    }
}
