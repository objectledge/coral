package org.objectledge.coral.table;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

/**
 * This is a base comparator for string values related to a resource.
 * It provides localisation for string comparisons.
 *
 * @author <a href="mailto:zwierzem@ngo.pl">Damian Gajda</a>
 * @version $Id: BaseStringComparator.java,v 1.1 2004-03-23 11:44:30 pablo Exp $
 */
public abstract class BaseStringComparator
    implements Comparator
{
    protected Collator collator;
    
    public BaseStringComparator(Locale locale)
    {
        collator = Collator.getInstance(locale);
    }
    
    public int compareStrings(String s1, String s2)
    {
        return collator.compare(s1, s2);
    }
}
