package org.objectledge.coral.table.comparator;

import java.util.Comparator;

import org.objectledge.coral.entity.Entity;

/**
 * This is a comparator for comparing entities ids.
 *
 * @author <a href="mailto:zwierzem@ngo.pl">Damian Gajda</a>
 * @version $Id: IdComparator.java,v 1.3 2005-02-21 14:04:29 rafal Exp $
 */
public class IdComparator
    implements Comparator
{
    /**
     * {@inheritDoc}
     */
    public int compare(Object o1, Object o2)
    {
        if(!((o1 instanceof Entity && o2 instanceof Entity )))
        {
            return 0;
        }

        Entity r1 = (Entity)o1;
        Entity r2 = (Entity)o2;

        return (int)(r1.getId() - r2.getId());
    }
}
