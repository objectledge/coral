package org.objectledge.coral.table.filter;

import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.table.filter.*;

/**
 * This is a filter for filtering resources upon their owner subject.
 *
 * @author <a href="mailto:zwierzem@ngo.pl">Damian Gajda</a>
 * @version $Id: CreatorFilter.java,v 1.1 2004-04-22 12:56:24 zwierzem Exp $
 */
public class CreatorFilter
    extends SubjectFilter
{
    public CreatorFilter(Subject creatorSubject)
    {
        super(creatorSubject);
    }

    protected Subject getSubject(Resource r)
    {
        return r.getCreatedBy();
    }
}
