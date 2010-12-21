package org.objectledge.coral.table.comparator;

import java.util.Date;
import java.util.GregorianCalendar;

import junit.framework.TestCase;

public class TimeComparatorTest
    extends TestCase
{
    public static class DateObject
    {
        private final Date date;

        public DateObject(Date date)
        {
            this.date = date;
        }

        public static class Comparator
            extends TimeComparator<DateObject>
        {

            public Comparator(TimeComparator.SortNulls strategy)
            {
                super(strategy);
            }

            @Override
            public int compare(DateObject o1, DateObject o2)
            {
                return compareDates(o1.date, o2.date);
            }
        };
    }

    public void testCompareBothNotNull()
    {
        DateObject d1 = new DateObject(new GregorianCalendar(2010, 12, 20).getTime());
        DateObject d2 = new DateObject(new GregorianCalendar(2010, 12, 21).getTime());
        DateObject.Comparator c1 = new DateObject.Comparator(TimeComparator.SortNulls.LAST); 
        assertTrue(c1.compare(d1, d2) < 0);
        DateObject.Comparator c2 = new DateObject.Comparator(TimeComparator.SortNulls.FIRST); 
        assertTrue(c2.compare(d1, d2) < 0);

    }

    public void testCompareBothNull()
    {
        DateObject d1 = new DateObject(null);
        DateObject d2 = new DateObject(null);
        DateObject.Comparator c1 = new DateObject.Comparator(TimeComparator.SortNulls.LAST); 
        assertTrue(c1.compare(d1, d2) == 0);
        DateObject.Comparator c2 = new DateObject.Comparator(TimeComparator.SortNulls.FIRST); 
        assertTrue(c2.compare(d1, d2) == 0);
    }

    public void testCompareNullAndNotNullNullsLast()
    {
        DateObject d1 = new DateObject(new GregorianCalendar(2010, 12, 20).getTime());
        DateObject d2 = new DateObject(null);
        DateObject.Comparator c = new DateObject.Comparator(TimeComparator.SortNulls.LAST); 
        assertTrue(c.compare(d1, d2) < 0);
    }

    public void testCompareNullAndNotNullNullsFirst()
    {
        DateObject d1 = new DateObject(new GregorianCalendar(2010, 12, 20).getTime());
        DateObject d2 = new DateObject(null);
        DateObject.Comparator c = new DateObject.Comparator(TimeComparator.SortNulls.FIRST); 
        assertTrue(c.compare(d1, d2) > 0);
    }
}
