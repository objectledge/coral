package org.objectledge.coral.table.comparator;

import java.util.Locale;

import org.objectledge.coral.entity.Entity;
import org.objectledge.table.comparator.BaseStringComparator;

/**
 * This is a comparator for comparing coral entities names.
 *
 * @author <a href="mailto:zwierzem@ngo.pl">Damian Gajda</a>
 * @version $Id: NameComparator.java,v 1.3 2005-02-14 17:26:32 pablo Exp $
 */
public class NameComparator
    extends BaseStringComparator
{
    public NameComparator(Locale locale)
    {
        super(locale);
    }
    
    public int compare(Object o1, Object o2)
    {
        if(!((o1 instanceof Entity && o2 instanceof Entity )))
        {
            return 0;
        }

        Entity r1 = (Entity)o1;
        Entity r2 = (Entity)o2;

        return compareStrings(r1.getName(), r2.getName());
    }
}
