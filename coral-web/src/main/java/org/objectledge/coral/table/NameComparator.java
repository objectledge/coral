package org.objectledge.coral.table;

import java.util.Locale;

import org.objectledge.coral.store.Resource;

/**
 * This is a comparator for comparing resource names.
 *
 * @author <a href="mailto:zwierzem@ngo.pl">Damian Gajda</a>
 * @version $Id: NameComparator.java,v 1.1 2004-03-23 11:44:30 pablo Exp $
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
        if(!((o1 instanceof Resource && o2 instanceof Resource )))
        {
            return 0;
        }

        Resource r1 = (Resource)o1;
        Resource r2 = (Resource)o2;

        return compareStrings(r1.getName(), r2.getName());
    }
}
