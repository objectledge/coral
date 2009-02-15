package org.objectledge.coral.table.comparator;

import java.util.Locale;

import org.objectledge.coral.store.Resource;
import org.objectledge.table.comparator.BaseStringComparator;

/**
 * This is a comparator for comparing resource paths.
 *
 * @author <a href="mailto:damian@caltha.pl">Damian Gajda</a>
 * @version $Id: PathComparator.java,v 1.4 2008-06-05 16:37:58 rafal Exp $
 */
public class PathComparator
    extends BaseStringComparator<Resource>
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
    public int compare(Resource r1, Resource r2)
    {
        return compareStrings(r1.getPath(), r2.getPath());
    }
}
