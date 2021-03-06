package org.objectledge.coral.table.comparator;

import java.util.Date;
import java.util.GregorianCalendar;

import junit.framework.TestCase;

import org.objectledge.coral.store.ResourceImpl;

public class TimeComparatorTest
    extends TestCase
{
    public static class FooResource
    extends ResourceImpl
    {
        private final Date date;

        public FooResource(Date date)
        {
            super(null, null, null);
            this.date = date;
        }

        public static class Comparator
            extends TimeComparator<FooResource>
        {

            public Comparator(TimeComparator.Direction strategy)
            {
                super(strategy);
            }

            protected Date getDate(FooResource res)
            {
                return res.date;
            }
        };
    }

    public void testCompareBothNotNull()
    {
        FooResource d1 = new FooResource(new GregorianCalendar(2010, 12, 20).getTime());
        FooResource d2 = new FooResource(new GregorianCalendar(2010, 12, 21).getTime());
        FooResource.Comparator c1 = new FooResource.Comparator(TimeComparator.Direction.ASC); 
        assertTrue(c1.compare(d1, d2) < 0);
        assertTrue(c1.compare(d2, d1) > 0);
        FooResource.Comparator c2 = new FooResource.Comparator(TimeComparator.Direction.DESC); 
        assertTrue(c2.compare(d1, d2) > 0);
        assertTrue(c2.compare(d2, d1) < 0);
    }

    public void testCompareBothNull()
    {
        FooResource d1 = new FooResource(null);
        FooResource d2 = new FooResource(null);
        FooResource.Comparator c1 = new FooResource.Comparator(TimeComparator.Direction.ASC); 
        assertTrue(c1.compare(d1, d2) == 0);
        FooResource.Comparator c2 = new FooResource.Comparator(TimeComparator.Direction.DESC); 
        assertTrue(c2.compare(d1, d2) == 0);
    }

    public void testCompareNullAndNotNull()
    {
        FooResource d1 = new FooResource(new GregorianCalendar(2010, 12, 20).getTime());
        FooResource d2 = new FooResource(null);
        FooResource.Comparator c1 = new FooResource.Comparator(TimeComparator.Direction.ASC); 
        assertTrue(c1.compare(d1, d2) < 0);
        assertTrue(c1.compare(d2, d1) > 0);
        FooResource.Comparator c2 = new FooResource.Comparator(TimeComparator.Direction.DESC); 
        assertTrue(c2.compare(d1, d2) < 0);
        assertTrue(c2.compare(d2, d1) > 0);
    }
}
