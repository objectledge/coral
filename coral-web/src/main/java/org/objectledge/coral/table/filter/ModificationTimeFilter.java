package org.objectledge.coral.table.filter;

import java.util.Date;

import org.objectledge.coral.store.Resource;

/**
 * This is a filter for filtering resources upon their modification time.
 *
 * @author <a href="mailto:zwierzem@ngo.pl">Damian Gajda</a>
 * @version $Id: ModificationTimeFilter.java,v 1.2 2005-02-21 14:04:32 rafal Exp $
 */
public class ModificationTimeFilter
    extends TimeFilter
{
    /**
     * Creates new ModificationTimeFilter instance.
     * 
     * @param start the start date.
     * @param end the end date.
     */
    public ModificationTimeFilter(Date start, Date end)
    {
        super(start, end);
    }

    /**
     * {@inheritDoc}
     */
    protected Date getDate(Resource r)
    {
        return r.getModificationTime();
    }
}
