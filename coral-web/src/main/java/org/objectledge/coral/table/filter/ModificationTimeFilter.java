package org.objectledge.coral.table.filter;

import java.util.Date;

import org.objectledge.coral.store.Resource;

/**
 * This is a filter for filtering resources upon their modification time.
 *
 * @author <a href="mailto:zwierzem@ngo.pl">Damian Gajda</a>
 * @version $Id: ModificationTimeFilter.java,v 1.1 2004-04-22 12:56:24 zwierzem Exp $
 */
public class ModificationTimeFilter
    extends TimeFilter
{
    public ModificationTimeFilter(Date start, Date end)
    {
        super(start, end);
    }

    protected Date getDate(Resource r)
    {
        return r.getModificationTime();
    }
}
