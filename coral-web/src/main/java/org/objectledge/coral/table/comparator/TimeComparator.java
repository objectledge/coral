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
    public enum Direction 
    {
        ASC,
        DESC
    };
    
    private final Direction direction;
    
    /**
     * Creates a new comparator instance with specified sort direction.
     * 
     * @param direction
     */
    public TimeComparator(Direction direction)
    {
        this.direction = direction;
    }
    
    /**
     * Returns the sort direction of this comparator instance.
     * 
     * @return the sort direction of this comparator instance.
     */
    public Direction getDirection()
    {
        return direction;
    }
     
    /**
     * Compares two objects using their date attributes. Dates may be null, the contract is:
     * <ul>
     * <li>If both dates are not null, they are simply compared, but when sort direction is DESC,
     * comparison is reversed.</li>
     * <li>If both are null, dates are considered equal</li>
     * <li>If one of the dates is null, and sort direction is ASC, null date is considered greater,
     * and when direction is DESC, null date is considered lesser than then non-null date.</li>
     * </ul>
     * 
     * @param d1 first date.
     * @param d2 second date.
     * @return the result of the comparison.
     */
    public int compareDates(Date d1, Date d2)
    {        
        if(d1 != null && d2 != null)
        {
            return direction == Direction.ASC ? d1.compareTo(d2) : d2.compareTo(d1);
        }
        
        if(d1 == null)
        {
            if(d2 == null) 
            {
                return 0;
            }
            else 
            {
                // d1 == null && d2 != null
                return 1;
            }
        }
        // d1 != null && d2 == null
        return -1;
    }
    
    /**
     * Returns date attribute of the resource that is the sort criterion.
     * 
     * @param resource
     * @return
     */
    protected abstract Date getDate(T resource);
    
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
        Date d1 = getDate(res1);
        Date d2 = getDate(res2);
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
