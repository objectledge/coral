package org.objectledge.coral.table;

import java.util.Locale;

import org.objectledge.coral.security.Subject;

/**
 * This is a base comparator for comparing subjects by names.
 *
 * @author <a href="mailto:damian@caltha.pl">Damian Gajda</a>
 * @version $Id: SubjectNameComparator.java,v 1.1 2004-03-23 11:44:30 pablo Exp $
 */
public class SubjectNameComparator
    extends BaseStringComparator
{
    public SubjectNameComparator(Locale locale)
    {
        super(locale);
    }

    public int compare(Object o1, Object o2)
    {
        Subject s1 = (Subject)o1;
        Subject s2 = (Subject)o2;
        return compareStrings(s1.getName(), s2.getName());
    }
}
