package org.objectledge.coral.table.comparator;

import java.util.Locale;

import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.Resource;

/**
 * This comparator compares names of owner subjects.
 *
 * @author <a href="mailto:zwierzem@ngo.pl">Damian Gajda</a>
 * @version $Id: OwnerNameComparator.java,v 1.1 2004-04-22 12:56:24 zwierzem Exp $
 */
public class OwnerNameComparator
    extends ResourceBySubjectNameComparator
{
    public OwnerNameComparator(Locale locale)
    {
        super(locale);
    }

    protected Subject getSubject(Resource r)
    {
        return r.getOwner();
    }
}
