package org.objectledge.coral.table.comparator;

import java.util.Date;

import org.objectledge.coral.store.Resource;

/**
 * This comparator compares creation times of resources.
 *
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: CreationTimeComparator.java,v 1.3 2008-06-05 16:37:58 rafal Exp $
 */
public class CreationTimeComparator<T extends Resource>
    extends TimeComparator<T>
{
    public CreationTimeComparator()
    {
        // creation time is never null, so direction is irrelevant here
        super(TimeComparator.Direction.ASC);
    }
    
    protected Date getDate(Resource res)
    {
        return res.getCreationTime();
    }
}
