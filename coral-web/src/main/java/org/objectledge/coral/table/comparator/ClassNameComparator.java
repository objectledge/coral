package org.objectledge.coral.table.comparator;

import java.util.Locale;

import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.table.comparator.BaseStringComparator;

/**
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: ClassNameComparator.java,v 1.3 2005-02-21 14:04:29 rafal Exp $ 
 */
public class ClassNameComparator extends BaseStringComparator
{
    /**
     * Creates new ClassNameComparator instance.
     * 
     * @param locale locale to use.
     */
    public ClassNameComparator(Locale locale)
    {
        super(locale);
    }

    /**
     * {@inheritDoc}
     */
    public int compare(Object o1, Object o2)
    {
        if (!((o1 instanceof ResourceClass && o2 instanceof ResourceClass)))
        {
            return 0;
        }
        ResourceClass rc1 = (ResourceClass)o1;
        ResourceClass rc2 = (ResourceClass)o2;
        return compareStrings(rc1.getName(), rc2.getName());
    }
}
