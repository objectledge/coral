package org.objectledge.coral.table.filter;

import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.Resource;

/**
 * This is a filter for filtering resources upon their owner subject.
 *
 * @author <a href="mailto:zwierzem@ngo.pl">Damian Gajda</a>
 * @version $Id: ModifierFilter.java,v 1.2 2005-02-21 14:04:32 rafal Exp $
 */
public class ModifierFilter
    extends SubjectFilter
{
    /**
     * Creates new ModifierFilter instance.
     * 
     * @param modifierSubject the requested subject.
     */
    public ModifierFilter(Subject modifierSubject)
    {
        super(modifierSubject);
    }

    /**
     * {@inheritDoc}
     */
    protected Subject getSubject(Resource r)
    {
        return r.getModifiedBy();
    }
}
