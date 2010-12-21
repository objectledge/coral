package org.objectledge.coral.table.comparator;

import java.util.Comparator;
import java.util.Date;

/**
 * This is a base comparator for comparing time values related to an object.
 *
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: TimeComparator.java,v 1.3 2008-06-05 16:37:58 rafal Exp $
 */
public abstract class TimeComparator<T>
    implements Comparator<T>
{    
    public enum SortNulls 
    {
        FIRST,
        LAST
    };
    
    private final SortNulls nullSortStrategy;
    
    /**
     * Creates a new comparator instance with specified null sorting strategy.
     * 
     * @param sortNulls
     */
    public TimeComparator(SortNulls sortNulls)
    {
        this.nullSortStrategy = sortNulls;
    }
     
    /** Compares two objects using their date attributes. Dates may be null, the contract is:
     *  <ul>
     *  <li>If both dates are not null, they are simply compared.</li>
     *  <li>If both are null, dates are considered equal</li>
     *  <li>If one of the dates is null it is considered to be after the non null date</li>
     *  </ul>
     * @param d1 first date.
     * @param d2 second date.
     * @return the result of the comparison.
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
            else 
            {
                return nullSortStrategy == SortNulls.LAST ? 1 : -1;
            }
        }
        return nullSortStrategy == SortNulls.LAST ? -1 : 1;
    }
}
