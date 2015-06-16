package org.objectledge.coral.util;

import bak.pcj.LongCollection;
import bak.pcj.LongIterator;
import bak.pcj.set.LongOpenHashSet;
import bak.pcj.set.LongSet;

public class PrimitiveCollections
{
    public static LongSet EMPTY_LONG_SET = new LongOpenHashSet(new long[] {});

    public static LongSet singletonLongSet(long l)
    {
        LongSet s = new LongOpenHashSet(1);
        s.add(l);
        return s;
    }

    public static LongSet unmodifiableLongSet(final LongSet s)
    {
        return new LongSet()
            {
                public boolean add(long arg0)
                {
                    throw new UnsupportedOperationException();
                }

                public boolean addAll(LongCollection arg0)
                {
                    throw new UnsupportedOperationException();
                }

                public void clear()
                {
                    throw new UnsupportedOperationException();
                }

                public boolean contains(long arg0)
                {
                    return s.contains(arg0);
                }

                public boolean containsAll(LongCollection arg0)
                {
                    return s.containsAll(arg0);
                }

                public boolean isEmpty()
                {
                    return s.isEmpty();
                }

                @Override
                public LongIterator iterator()
                {
                    final LongIterator i = s.iterator();
                    return new LongIterator()
                        {

                            @Override
                            public boolean hasNext()
                            {
                                return i.hasNext();
                            }

                            @Override
                            public long next()
                            {
                                return i.next();
                            }

                            @Override
                            public void remove()
                            {
                                throw new UnsupportedOperationException();
                            }
                        };
                }

                @Override
                public boolean remove(long arg0)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean removeAll(LongCollection arg0)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean retainAll(LongCollection arg0)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public int size()
                {
                    return s.size();
                }

                @Override
                public long[] toArray()
                {
                    return s.toArray();
                }

                @Override
                public long[] toArray(long[] arg0)
                {
                    return s.toArray(arg0);
                }

                @Override
                public void trimToSize()
                {
                    throw new UnsupportedOperationException();
                }

                public int hashCode()
                {
                    return s.hashCode();
                }

                public boolean equals(Object obj)
                {
                    if(obj instanceof LongSet)
                    {
                        return equal(this, (LongSet)obj);
                    }
                    else
                    {
                        return false;
                    }
                }
            };
    }
    
    private static boolean equal(LongSet thisSet, LongSet thatSet)
    {
        if(thisSet.size() != thatSet.size())
        {
            return false;
        }
        LongIterator thisIter = thisSet.iterator();
        LongIterator thatIter = thatSet.iterator();        
        while(thisIter.hasNext() && thatIter.hasNext())
        {
            if(thisIter.next() != thatIter.next())
            {
                return false;
            }
        }
        return true;
    }
}
