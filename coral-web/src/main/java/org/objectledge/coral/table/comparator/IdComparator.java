package org.objectledge.coral.table.comparator;

import java.util.Comparator;

import org.objectledge.coral.store.Resource;

/**
 * This is a comparator for comparing resource ids.
 *
 * @author <a href="mailto:zwierzem@ngo.pl">Damian Gajda</a>
 * @version $Id: IdComparator.java,v 1.1 2004-04-22 12:56:23 zwierzem Exp $
 */
public class IdComparator
    implements Comparator
{
    public int compare(Object o1, Object o2)
    {
        if(!((o1 instanceof Resource && o2 instanceof Resource )))
        {
            return 0;
        }

        Resource r1 = (Resource)o1;
        Resource r2 = (Resource)o2;

        return (int)(r1.getId() - r2.getId());
    }
}
