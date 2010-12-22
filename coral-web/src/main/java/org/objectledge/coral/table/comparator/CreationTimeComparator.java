package org.objectledge.coral.table.comparator;

import java.util.Date;

import org.objectledge.coral.store.Resource;

/**
 * This comparator compares creation times of resources.
 *
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: CreationTimeComparator.java,v 1.3 2008-06-05 16:37:58 rafal Exp $
 */
public class CreationTimeComparator
    extends TimeComparator<Resource>
{
    public CreationTimeComparator()
    {
        // creation time is never null, so strategy is irrelevant here
        super(TimeComparator.SortNulls.LAST);
    }
    
    protected Date getSortCriterionDate(Resource res)
    {
        return res.getCreationTime();
    }
}
