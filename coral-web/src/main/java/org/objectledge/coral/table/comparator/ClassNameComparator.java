package org.objectledge.coral.table.comparator;

import java.util.Locale;

import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.table.comparator.BaseStringComparator;

/**
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: ClassNameComparator.java,v 1.2 2005-02-14 17:26:32 pablo Exp $ 
 */
public class ClassNameComparator extends BaseStringComparator
{
    public ClassNameComparator(Locale locale)
    {
        super(locale);
    }

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
