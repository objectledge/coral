package org.objectledge.coral.table.comparator;

import java.util.Locale;

import org.objectledge.coral.store.Resource;
import org.objectledge.table.comparator.BaseStringComparator;

/**
 * This is a comparator for comparing resource paths.
 *
 * @author <a href="mailto:damian@caltha.pl">Damian Gajda</a>
 * @version $Id: PathComparator.java,v 1.3 2005-02-14 17:26:31 pablo Exp $
 */
public class PathComparator
    extends BaseStringComparator
{
    /**
     * Comparator constructor.
     * 
     * @param locale the locale.
     */
    public PathComparator(Locale locale)
    {
        super(locale);
    }
    
    /**
     * {@inheritDoc}
     */
    public int compare(Object o1, Object o2)
    {
        if(!((o1 instanceof Resource && o2 instanceof Resource )))
        {
            return 0;
        }
        Resource r1 = (Resource)o1;
        Resource r2 = (Resource)o2;
        return compareStrings(r1.getPath(), r2.getPath());
    }
}
