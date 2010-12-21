package org.objectledge.coral.table.comparator;

import org.objectledge.coral.store.Resource;

/**
 * This comparator compares modification dates of resources.
 *
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: ModificationTimeComparator.java,v 1.3 2008-06-05 16:37:58 rafal Exp $
 */
public class ModificationTimeComparator
    extends TimeComparator<Resource>
{
    public ModificationTimeComparator()
    {
        // modification time is never null, so strategy is irrelevant here
        super(TimeComparator.SortNulls.LAST);
    }
  
    /**
     * {@inheritDoc}
     */
	public int compare(Resource r1, Resource r2)
	{
		return compareDates(r1.getModificationTime(), r2.getModificationTime());
	}
}
