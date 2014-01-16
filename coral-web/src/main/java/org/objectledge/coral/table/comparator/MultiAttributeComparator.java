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
    implements Comparator<T>
{
    private final Comparator<T>[] comp;

    /**
     * Create a MultiAttributeComparator instance.
     * 
     * @param rClass a resource class.
     * @param locale locale used for comparing String values.
     * @param direction sort direction.
     * @param attrNames names of the attributes.
     * @throws UnknownAttributeException when an illegal attribute name is provided in attrNames
     * @throws IllegalArgumentException when one of the requested attributes has an unsupported
     *         type, see
     *         {@link AttributeComparator#getInstance(org.objectledge.coral.schema.AttributeDefinition, Locale, org.objectledge.coral.table.comparator.AttributeComparator.Direction)}
     */
    @SuppressWarnings("unchecked")
    public MultiAttributeComparator(ResourceClass<T> rClass, Locale locale,
        AttributeComparator.Direction direction, String... attrNames)
    {
        comp = new Comparator[attrNames.length];
        for(int i = 0; i < attrNames.length; i++)
        {
            comp[i] = AttributeComparator.getInstance(rClass.getAttribute(attrNames[i]), locale,
                direction);
        }
    }

    @Override
    public int compare(T o1, T o2)
    {
        for(int i = 0; i < comp.length; i++)
        {
            int d = comp[i].compare(o1, o2);
            if(d != 0)
            {
                return d;
            }
        }
        return 0;
    }
}
