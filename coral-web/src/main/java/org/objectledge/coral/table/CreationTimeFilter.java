package org.objectledge.coral.table;

import java.util.Date;

import org.objectledge.coral.store.Resource;

/**
 * This is a filter for filtering resources upon their creation time.
 *
 * @author <a href="mailto:zwierzem@ngo.pl">Damian Gajda</a>
 * @version $Id: CreationTimeFilter.java,v 1.1 2004-03-23 11:44:30 pablo Exp $
 */
public class CreationTimeFilter
    extends TimeFilter
{
    public CreationTimeFilter(Date start, Date end)
    {
        super(start, end);
    }

    protected Date getDate(Resource r)
    {
        return r.getCreationTime();
    }
}
