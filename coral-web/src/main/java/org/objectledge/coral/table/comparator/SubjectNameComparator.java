package org.objectledge.coral.table.comparator;

import java.util.Locale;

import org.objectledge.coral.security.Subject;
import org.objectledge.table.comparator.BaseStringComparator;

/**
 * This is a base comparator for comparing subjects by names.
 *
 * @author <a href="mailto:damian@caltha.pl">Damian Gajda</a>
 * @version $Id: SubjectNameComparator.java,v 1.3 2005-02-21 14:04:29 rafal Exp $
 */
public class SubjectNameComparator
    extends BaseStringComparator
{
    /**
     * Creates new SubjectNameComparator instance.
     * 
     * @param locale the locale to use.
     */
    public SubjectNameComparator(Locale locale)
    {
        super(locale);
    }

    /**
     * {@inheritDoc}
     */
    public int compare(Object o1, Object o2)
    {
        Subject s1 = (Subject)o1;
        Subject s2 = (Subject)o2;
        return compareStrings(s1.getName(), s2.getName());
    }
}
