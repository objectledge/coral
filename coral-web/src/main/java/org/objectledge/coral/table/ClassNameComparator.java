package org.objectledge.coral.table;

import java.util.Locale;

import org.objectledge.coral.schema.ResourceClass;

/**
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: ClassNameComparator.java,v 1.1 2004-03-23 11:44:30 pablo Exp $ 
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
