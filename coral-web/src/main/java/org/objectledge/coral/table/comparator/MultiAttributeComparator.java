package org.objectledge.coral.table.comparator;

import java.util.Comparator;
import java.util.Locale;

import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.schema.UnknownAttributeException;
import org.objectledge.coral.store.Resource;

/**
 * Compare resources using multiple attributes.
 * 
 * @author rafal.krzewski@caltha.pl
 * @param <T>
 */
public class MultiAttributeComparator<T extends Resource>
    extends CompositeComparator<T>
{
    /**
     * Create a MultiAttributeComparator instance.
     * 
     * @param rClass a resource class.
     * @param locale locale used for comparing String values.
     * @param directions sort directions for the attributes.
     * @param attrNames names of the attributes.
     * @throws UnknownAttributeException when an illegal attribute name is provided in attrNames
     * @throws IllegalArgumentException when number of provided attribute names and sort directions
     *         differ, or one of the requested attributes has an unsupported type, see
     *         {@link AttributeComparator#getInstance(org.objectledge.coral.schema.AttributeDefinition, org.objectledge.coral.table.comparator.AttributeComparator.Direction, Locale)}
     */
    public MultiAttributeComparator(ResourceClass<T> rClass, Locale locale, Direction[] directions,
        String[] attrNames)
    {
        super(buildComparators(rClass, locale, directions, attrNames));
    }

    private static <T extends Resource> Comparator<T>[] buildComparators(ResourceClass<T> rClass,
        Locale locale, Direction[] directions, String[] attrNames)
    {
        if(directions.length != attrNames.length)
        {
            throw new IllegalArgumentException("direction and attrName counts are not equal");
        }
        @SuppressWarnings("unchecked")
        Comparator<T>[] comparators = new Comparator[attrNames.length];
        for(int i = 0; i < attrNames.length; i++)
        {
            comparators[i] = AttributeComparator.getInstance(rClass.getAttribute(attrNames[i]),
                locale, directions[i]);
        }
        return comparators;
    }
}
