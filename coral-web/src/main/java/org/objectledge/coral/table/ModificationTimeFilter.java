package org.objectledge.coral.table;

import java.util.Date;

import org.objectledge.coral.store.Resource;

/**
 * This is a filter for filtering resources upon their modification time.
 *
 * @author <a href="mailto:zwierzem@ngo.pl">Damian Gajda</a>
 * @version $Id: ModificationTimeFilter.java,v 1.1 2004-03-23 11:44:30 pablo Exp $
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
