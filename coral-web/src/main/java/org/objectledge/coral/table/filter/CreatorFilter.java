package org.objectledge.coral.table.filter;

import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.Resource;

/**
 * This is a filter for filtering resources upon their owner subject.
 *
 * @author <a href="mailto:zwierzem@ngo.pl">Damian Gajda</a>
 * @version $Id: CreatorFilter.java,v 1.2 2004-05-06 13:49:38 pablo Exp $
 */
public class CreatorFilter
    extends SubjectFilter
{
    /**
     * The filter constructor.
     * 
     * @param creatorSubject the creator.
     */
    public CreatorFilter(Subject creatorSubject)
    {
        super(creatorSubject);
    }

    /**
     * {@inheritDoc}
     */
    protected Subject getSubject(Resource r)
    {
        return r.getCreatedBy();
    }
}
