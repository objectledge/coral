package org.objectledge.coral.table.comparator;

import java.util.Comparator;
import java.util.Date;

import org.objectledge.coral.store.Resource;

/**
 * This is a base comparator for comparing time values related to an object.
 *
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: TimeComparator.java,v 1.3 2008-06-05 16:37:58 rafal Exp $
 */
public abstract class TimeComparator<T extends Resource>
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
     *  <li>If one of the dates is null, result depends on the strategy selected in the constructor</li>
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
    
    /**
     * Returns date attribute of the resource that is the sort criterion.
     * 
     * @param resource
     * @return
     */
    protected abstract Date getSortCriterionDate(T resource);
    
    /**
     * Compare resources by date, when dates compare resource ids in order to stabilize sort order.
     * 
     * @param res1 a resource
     * @param res2 a resource
     * @return an integer specifying resource relationship.
     */
    @Override
    public int compare(T res1, T res2)
    {
        Date d1 = getSortCriterionDate(res1);
        Date d2 = getSortCriterionDate(res2);
        int rel = compareDates(d1, d2);
        if(rel != 0)
        {
            return rel; 
        }
        else
        {
            return (int)(res1.getId() - res2.getId());
        }
    }
}
