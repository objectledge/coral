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
 * @version $Id: ResourceBySubjectNameComparator.java,v 1.3 2005-02-21 14:04:29 rafal Exp $
 */
public abstract class ResourceBySubjectNameComparator
    extends BaseStringComparator
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
    public int compare(Object o1, Object o2)
    {
        if(!((o1 instanceof Resource && o2 instanceof Resource )))
        {
            return 0;
        }
        Subject s1 = getSubject((Resource)o1);
        Subject s2 = getSubject((Resource)o2);
        return compareStrings(s1.getName(), s2.getName());
    }
}
