package org.objectledge.coral.table.filter;

import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.Resource;

/**
 * This is a filter for filtering resources upon their owner subject.
 *
 * @author <a href="mailto:zwierzem@ngo.pl">Damian Gajda</a>
 * @version $Id: OwnerFilter.java,v 1.2 2005-02-21 14:04:32 rafal Exp $
 */
public class OwnerFilter
    extends SubjectFilter
{
    /**
     * Creates new OwnerFilter instance.
     * 
     * @param ownerSubject the requested subject.
     */
    public OwnerFilter(Subject ownerSubject)
    {
        super(ownerSubject);
    }

    /**
     * {@inheritDoc}
     */
    protected Subject getSubject(Resource r)
    {
        return r.getOwner();
    }
}
