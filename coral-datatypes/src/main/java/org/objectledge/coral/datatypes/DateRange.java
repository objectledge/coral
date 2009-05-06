package org.objectledge.coral.datatypes;

import java.util.Date;

/**
 * Represents a date range.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: DateRange.java,v 1.2 2004-05-06 13:01:53 pablo Exp $
 */
public class DateRange
{
    // Member objects ////////////////////////////////////////////////////////

    /** The value */
    private Date start;

    private Date end;
    
    // modification tracking - Date objects are mutable, which I believe is a design flaw in JDK
    
    private Date origStart;
    
    private Date origEnd;

    // Initialization ///////////////////////////////////////////////////////
    
    /**
     * Constructs a Date attribute.
     *
     * @param start the start date.
     * @param end the end date.
     */
    public DateRange(Date start, Date end)
    {
        this.start = start;
        this.end = end;
        clearModified();
    }
    
    // Public interface //////////////////////////////////////////////////////

    /**
     * Returns the start date of the range.
     *
     * @return the start date of the range.
     */
    public Date getStart()
    {
        return start;
    }    

    /**
     * Returns the end date of the range.
     *
     * @return the end date of the range.
     */
    public Date getEnd()
    {
        return end;
    }    
    
    /**
     * Returns <code>true</code> if the range contains the specified date.
     *
     * @param date the date to check.
     * @return <code>true</code> if the range contains the specified date.
     */
    public boolean contains(Date date)
    {
        return (date.compareTo(start) >= 0) && (date.compareTo(end) <= 0);
    }

    /**
     * Returns the value of the attribue as a printable string.
     *
     * @return the value of the attribue as a printable string.
     */
    public String toString()
    {
        return "["+start.toString()+","+end.toString()+"]";
    }
    
    // package protected methods for AttributeHandler implementation
    
    boolean isModified()
    {
        return !(start.equals(origStart) && end.equals(origEnd));
    }
    
    void clearModified() 
    {
        origStart = new Date(start.getTime());
        origEnd = new Date(end.getTime());
    }
}
