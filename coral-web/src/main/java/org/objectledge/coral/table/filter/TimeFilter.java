package org.objectledge.coral.table.filter;

import java.util.Date;

import org.objectledge.coral.store.Resource;
import org.objectledge.table.TableFilter;

/**
 * This is a base filter for filtering based on time values related
 * to a resource. It introduces different filtering strategies based upon
 * contstructor parameters.
 *
 * @author <a href="mailto:zwierzem@ngo.pl">Damian Gajda</a>
 * @version $Id: TimeFilter.java,v 1.2 2005-02-21 14:04:32 rafal Exp $
 */
public abstract class TimeFilter
    implements TableFilter
{
    /** the start of the accepted range. */
    protected Date start;
    
    /** the end of the accepted range. */
    protected Date end;

    /** the strategy to be used. */
    protected DateRangeCheckStrategy strategy;

    /** Constructs a filter for date values. Different start and end parameters
     * make the filter choose a different filtering strategy.
     * 
     * @param start the accepted range start.
     * @param end the accepted range end.
     * 
     * @see TimeFilter.BetweenStartEnd
     * @see TimeFilter.NotBetweenStartEnd
     * @see TimeFilter.AfterStart
     * @see TimeFilter.BeforeEnd
     * @see TimeFilter.PassAll
     */
    public TimeFilter(Date start, Date end)
    {
        this.start = start;
        this.end = end;

        if(start != null && end != null)
        {
            if(start.before(end))
            {
                strategy = new BetweenStartEnd();
            }
            else
            {
                strategy = new NotBetweenStartEnd();
            }
        }
        else if(start != null)
        {
            strategy = new AfterStart();
        }
        else if(end != null)
        {
            strategy = new BeforeEnd();
        }
        else
        {
            strategy = new PassAll();
        }
    }

    /** 
     * This method must be implemented to provide date used to filter out resources. 
     *
     * @param r the resource.
     * @return date to be used for filtering.
     */
    protected abstract Date getDate(Resource r);

    /**
     * {@inheritDoc}
     */
    public boolean accept(Object object)
    {
        if(!(object instanceof Resource))
        {
            return false;
        }

        Date d = getDate((Resource)object);
		
		if(d != null)
		{
			return strategy.check(d);
		}
		else
		{
			return acceptNullDate();
		}
    }

    /**
     * A hint from the child classes if the null dates should be accepted.
     * 
     * @return <code>true</code> if the null dates should be accepted.
     */
	protected boolean acceptNullDate()
	{
		return true;
	}

    /**
     * This is an internal interface for different time range filtering
     * strategies.
     */
    public interface DateRangeCheckStrategy
    {
        /** 
         * Returns <code>true</code> if a given date object fits filtering strategy. 
         * 
         * @param d the date.
         * @return <code>true</code> if a given date object fits filtering strategy.
         */
        public boolean check(Date d);
    }

    /** <code>start &lt; end =&gt; start &lt; d &lt; end</code> */
    public class BetweenStartEnd
        implements DateRangeCheckStrategy
    {
        /**
         * {@inheritDoc}
         */
        public boolean check(Date d)
        {
            if(start.before(d) && end.after(d))
            {
                return true;
            }
            return false;
        }
    }

    /** <code>start &gt; end =&gt; d &lt; start || end &lt; d</code> */
    public class NotBetweenStartEnd
        implements DateRangeCheckStrategy
    {
        /**
         * {@inheritDoc}
         */
        public boolean check(Date d)
        {
            if(end.after(d) || start.before(d))
            {
                return true;
            }
            return false;
        }
    }

    /** <code>start, null =&gt; start &lt; d</code> */
    public class AfterStart
        implements DateRangeCheckStrategy
    {
        /**
         * {@inheritDoc}
         */
        public boolean check(Date d)
        {
            if(start.before(d))
            {
                return true;
            }
            return false;
        }
    }

    /** <code>null, end =&gt; d &lt; end</code> */
    public class BeforeEnd
        implements DateRangeCheckStrategy
    {
        /**
         * {@inheritDoc}
         */
        public boolean check(Date d)
        {
            if(end.after(d))
            {
                return true;
            }
            return false;
        }
    }

    /** <code>null, null</code> pass all dates */
    public class PassAll
        implements DateRangeCheckStrategy
    {
        /**
         * {@inheritDoc}
         */
        public boolean check(Date d)
        {
            return true;
        }
    }
}
