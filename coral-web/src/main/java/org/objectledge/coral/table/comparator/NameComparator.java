package org.objectledge.coral.table.comparator;

import java.util.Locale;

import org.objectledge.coral.entity.Entity;
import org.objectledge.table.comparator.BaseStringComparator;

/**
 * This is a comparator for comparing coral entities names.
 *
 * @author <a href="mailto:zwierzem@ngo.pl">Damian Gajda</a>
 * @version $Id: NameComparator.java,v 1.5 2008-06-05 16:37:59 rafal Exp $
 */
public class NameComparator<T extends Entity>
    extends BaseStringComparator<T>
{
    /**
     * Creates new NameComparator instance.
     * 
     * @param locale the locale to use.
     */
    public NameComparator(Locale locale)
    {
        super(locale);
    }

    /**
     * {@inheritDoc}
     */
    public int compare(T e1, T e2)
    {
        return compareStrings(e1.getName(), e2.getName());
    }
}
