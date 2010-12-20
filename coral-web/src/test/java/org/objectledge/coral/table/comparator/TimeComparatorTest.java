package org.objectledge.coral.table.comparator;

import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;

import junit.framework.TestCase;

public class TimeComparatorTest extends TestCase
{
    public static class DateObject
    {
        private final Date date;

        public DateObject(Date date)
        {
            this.date = date;
        }

        public static final Comparator<DateObject> COMPARATOR = new TimeComparator<DateObject>()
            {
                @Override
                public int compare(DateObject o1, DateObject o2)
                {
                    return compareDates(o1.date, o2.date);
                }
            };
    }
    
    public void testCompareBothNotNull()
    {
        DateObject d1 = new DateObject(new GregorianCalendar(2010,12,20).getTime());
        DateObject d2 = new DateObject(new GregorianCalendar(2010,12,21).getTime());
        assertTrue(DateObject.COMPARATOR.compare(d1, d2) < 0);
    }

    public void testCompareBothNull()
    {
        DateObject d1 = new DateObject(null);
        DateObject d2 = new DateObject(null);
        assertTrue(DateObject.COMPARATOR.compare(d1, d2) == 0);
    }

    public void testCompareNullAndNotNull()
    {
        DateObject d1 = new DateObject(new GregorianCalendar(2010,12,20).getTime());
        DateObject d2 = new DateObject(null);
        assertTrue(DateObject.COMPARATOR.compare(d1, d2) < 0);
    }

}
