package org.objectledge.coral.table.filter;

import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.Resource;

/**
 * This is a filter for filtering resources upon their owner subject.
 *
 * @author <a href="mailto:zwierzem@ngo.pl">Damian Gajda</a>
 * @version $Id: ModifierFilter.java,v 1.1 2004-04-22 12:56:24 zwierzem Exp $
 */
public class ModifierFilter
    extends SubjectFilter
{
    public ModifierFilter(Subject modifierSubject)
    {
        super(modifierSubject);
    }

    protected Subject getSubject(Resource r)
    {
        return r.getModifiedBy();
    }
}
