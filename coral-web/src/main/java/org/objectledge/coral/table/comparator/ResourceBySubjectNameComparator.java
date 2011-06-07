package org.objectledge.coral.table.comparator;

import java.util.Locale;

import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.Resource;
import org.objectledge.table.comparator.BaseStringComparator;

/**
 * This is a base comparator for comparing names of subjects related
 * to a resource.
 *
 * @author <a href="mailto:damian@caltha.pl">Damian Gajda</a>
 * @version $Id: ResourceBySubjectNameComparator.java,v 1.4 2008-06-05 16:37:59 rafal Exp $
 */
public abstract class ResourceBySubjectNameComparator<T extends Resource>
    extends BaseStringComparator<T>
{
    /**
     * Creates new ResourceBySubjectNameComparator instance.
     * 
     * @param locale the locale to use.
     */
    public ResourceBySubjectNameComparator(Locale locale)
    {
        super(locale);
    }

    /** 
     * This method must be implemented to provide subjects to be compared. 
     *
     * @param r the resource.
     * @return the subject corresponding to the resource.
     */
    protected abstract Subject getSubject(Resource r);

    /**
     * {@inheritDoc}
     */
    public int compare(Resource r1, Resource r2)
    {
        Subject s1 = getSubject(r1);
        Subject s2 = getSubject(r2);
        return compareStrings(s1.getName(), s2.getName());
    }
}
