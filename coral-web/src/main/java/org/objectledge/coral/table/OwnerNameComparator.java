package org.objectledge.coral.table;

import java.util.Locale;

import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.Resource;

/**
 * This comparator compares names of owner subjects.
 *
 * @author <a href="mailto:zwierzem@ngo.pl">Damian Gajda</a>
 * @version $Id: OwnerNameComparator.java,v 1.1 2004-03-23 11:44:30 pablo Exp $
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
