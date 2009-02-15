package org.objectledge.coral.table.filter;

import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.Resource;
import org.objectledge.table.TableFilter;

/**
 * This is a filter for filtering resources upon related subjects.
 *
 * @author <a href="mailto:damian@caltha.pl">Damian Gajda</a>
 * @version $Id: SubjectFilter.java,v 1.2 2005-02-21 14:04:32 rafal Exp $
 */
public abstract class SubjectFilter
    implements TableFilter
{
    /** the accepted subject. */
    protected Subject filterSubject;

    /**
     * Creates new SubjectFilter instance.
     * 
     * @param filterSubject the accepted subject.
     */
    public SubjectFilter(Subject filterSubject)
    {
        this.filterSubject = filterSubject;
    }

    /** 
     * This method must be implemented to provide subject used to filter out resources. 
     *
     * @param r the resource.
     * @return subject to be used for filtering.
     */
    protected abstract Subject getSubject(Resource r);

    /**
     * {@inheritDoc}
     */
    public boolean accept(Object object)
    {
        if(!(object instanceof Resource))
        {
            return false;
        }
        Subject subject = getSubject((Resource)object);
        return (subject == filterSubject);
    }
}
