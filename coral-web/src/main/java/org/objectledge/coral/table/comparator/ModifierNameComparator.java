package org.objectledge.coral.table.comparator;

import java.util.Locale;

import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.Resource;

/**
 * This comparator compares names of modifier subjects.
 *
 * @author <a href="mailto:zwierzem@ngo.pl">Damian Gajda</a>
 * @version $Id: ModifierNameComparator.java,v 1.1 2004-04-22 12:56:24 zwierzem Exp $
 */
public class ModifierNameComparator
    extends ResourceBySubjectNameComparator
{
    public ModifierNameComparator(Locale locale)
    {
        super(locale);
    }

    protected Subject getSubject(Resource r)
    {
        return r.getModifiedBy();
    }
}
