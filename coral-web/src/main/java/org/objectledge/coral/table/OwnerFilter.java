package org.objectledge.coral.table;

import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.Resource;

/**
 * This is a filter for filtering resources upon their owner subject.
 *
 * @author <a href="mailto:zwierzem@ngo.pl">Damian Gajda</a>
 * @version $Id: OwnerFilter.java,v 1.1 2004-03-23 11:44:30 pablo Exp $
 */
public class OwnerFilter
    extends SubjectFilter
{
    public OwnerFilter(Subject ownerSubject)
    {
        super(ownerSubject);
    }

    protected Subject getSubject(Resource r)
    {
        return r.getOwner();
    }
}
