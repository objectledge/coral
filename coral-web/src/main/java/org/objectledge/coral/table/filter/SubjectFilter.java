package org.objectledge.coral.table.filter;

import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.Resource;
import org.objectledge.table.TableFilter;

/**
 * This is a filter for filtering resources upon related subjects.
 *
 * @author <a href="mailto:damian@caltha.pl">Damian Gajda</a>
 * @version $Id: SubjectFilter.java,v 1.1 2004-04-22 12:56:24 zwierzem Exp $
 */
public abstract class SubjectFilter
    implements TableFilter
{
    protected Subject filterSubject;

    public SubjectFilter(Subject filterSubject)
    {
        this.filterSubject = filterSubject;
    }

    /** This method must be implemented to provide subject used to filter out resources. */
    protected abstract Subject getSubject(Resource r);

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
