package org.objectledge.coral.table.comparator;

import org.objectledge.coral.store.Resource;

/**
 * This comparator compares modification dates of resources.
 *
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: ModificationTimeComparator.java,v 1.2 2005-02-21 14:04:29 rafal Exp $
 */
public class ModificationTimeComparator
    extends TimeComparator
{
    /**
     * {@inheritDoc}
     */
	public int compare(Object o1, Object o2)
	{
		if(!((o1 instanceof Resource && o2 instanceof Resource )))
		{
			return 0;
		}

		Resource r1 = (Resource)o1;
		Resource r2 = (Resource)o2;

		return compareDates(r1.getModificationTime(), r2.getModificationTime());
	}
}
