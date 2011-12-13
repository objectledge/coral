package org.objectledge.coral.table.comparator;

import java.util.Locale;

import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.Resource;

/**
 * This comparator compares names of creator subjects.
 *
 * @author <a href="mailto:zwierzem@ngo.pl">Damian Gajda</a>
 * @version $Id: CreatorNameComparator.java,v 1.2 2005-02-21 14:04:29 rafal Exp $
 */
public class CreatorNameComparator<T extends Resource>
    extends ResourceBySubjectNameComparator<T>
{
    /**
     * Creates new CreatorNameComparator instance.
     * 
     * @param locale the locale to use.
     */
    public CreatorNameComparator(Locale locale)
    {
        super(locale);
    }

    /**
     * {@inheritDoc}
     */
    protected Subject getSubject(Resource r)
    {
        return r.getCreatedBy();
    }
}
