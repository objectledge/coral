package org.objectledge.coral.table;

import java.util.Comparator;
import java.util.Date;

/**
 * This is a base comparator for comparing time values related to an object.
 *
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: TimeComparator.java,v 1.1 2004-03-23 11:44:30 pablo Exp $
 */
public abstract class TimeComparator
    implements Comparator
{
    /** Compares two objects using their date attributes. Dates may be null, the contract is:
     *  <ul>
     *  <li>If both dates are not null, they are simply compared.</li>
     *  <li>If both are null, dates are considered equal</li>
     *  <li>If one of the dates is null it is considered to be after the non null date</li>
     *  </ul>
     */
    public int compareDates(Date d1, Date d2)
    {
        
        if(d1 != null && d2 != null)
        {
            return d1.compareTo(d2);
        }
        
        if(d1 == null)
        {
            if(d2 == null) // dates are equal
            {
                return 0;
            }
            else // d1 is after d2
            {
                return 1;
            }
        }
        // if(d2 == null && d1 != null) // d1 is before d2
        return -1;
    }
}
