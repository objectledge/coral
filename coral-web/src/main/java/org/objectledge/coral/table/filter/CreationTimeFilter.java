package org.objectledge.coral.table.filter;

import java.util.Date;

import org.objectledge.coral.store.Resource;

/**
 * This is a filter for filtering resources upon their creation time.
 *
 * @author <a href="mailto:zwierzem@ngo.pl">Damian Gajda</a>
 * @version $Id: CreationTimeFilter.java,v 1.2 2005-02-21 14:04:32 rafal Exp $
 */
public class CreationTimeFilter
    extends TimeFilter
{
    /**
     * Creates new CreationTimeFilter instance.
     * 
     * @param start the start date.
     * @param end the end date.
     */
    public CreationTimeFilter(Date start, Date end)
    {
        super(start, end);
    }

    /**
     * {@inheritDoc}
     */
    protected Date getDate(Resource r)
    {
        return r.getCreationTime();
    }
}
