package org.objectledge.coral.table.comparator;

import java.util.Locale;

import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.Resource;

/**
 * This comparator compares names of creator subjects.
 *
 * @author <a href="mailto:zwierzem@ngo.pl">Damian Gajda</a>
 * @version $Id: CreatorNameComparator.java,v 1.1 2004-04-22 12:56:24 zwierzem Exp $
 */
public class CreatorNameComparator
    extends ResourceBySubjectNameComparator
{
    public CreatorNameComparator(Locale locale)
    {
        super(locale);
    }

    protected Subject getSubject(Resource r)
    {
        return r.getCreatedBy();
    }
}
