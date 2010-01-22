package org.objectledge.coral.table.comparator;

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
    /**
     * {@inheritDoc}
     */
	public int compare(Resource r1, Resource r2)
	{
		return compareDates(r1.getCreationTime(), r2.getCreationTime());
	}
}
