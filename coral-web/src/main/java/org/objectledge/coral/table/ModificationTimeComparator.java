package org.objectledge.coral.table;

import org.objectledge.coral.store.Resource;

/**
 * This comparator compares modification dates of resources.
 *
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: ModificationTimeComparator.java,v 1.1 2004-03-23 11:44:30 pablo Exp $
 */
public class ModificationTimeComparator
    extends TimeComparator
{
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
