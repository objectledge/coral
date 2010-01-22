package org.objectledge.coral.table.comparator;

import java.util.Comparator;

import org.objectledge.coral.entity.Entity;

/**
 * This is a comparator for comparing entities ids.
 *
 * @author <a href="mailto:zwierzem@ngo.pl">Damian Gajda</a>
 * @version $Id: IdComparator.java,v 1.4 2008-06-05 16:37:58 rafal Exp $
 */
public class IdComparator<T extends Entity>
    implements Comparator<T>
{
    /**
     * {@inheritDoc}
     */
    public int compare(T e1, T e2)
    {
        return (int)(e1.getId() - e2.getId());
    }
}
