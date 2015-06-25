package org.objectledge.coral.util;

import bak.pcj.IntIterator;
import bak.pcj.LongCollection;
import bak.pcj.LongIterator;
import bak.pcj.set.IntSet;
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

    public static LongSet unmodifiableLongSet(final IntSet intSet)
    {
        return new LongSet()
            {
                private void checkRange(long val)
                {
                    if(val > Integer.MAX_VALUE || val < Integer.MIN_VALUE)
                    {
                        throw new IllegalArgumentException("value " + val
                            + " is out of supported range");
                    }
                }

                @Override
                public boolean add(long arg0)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean addAll(LongCollection arg0)
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void clear()
                {
                    throw new UnsupportedOperationException();
                }

                @Override
                public boolean contains(long val)
                {
                    checkRange(val);
                    return intSet.contains((int)val);
                }

                @Override
                public boolean containsAll(LongCollection coll)
                {
                    LongIterator i = coll.iterator();
                    while(i.hasNext())
                    {
                        if(!contains(i.next()))
                        {
                            return false;
                        }
                    }
                    return true;
                }

                @Override
                public boolean isEmpty()
                {
                    return intSet.isEmpty();
                }

                @Override
                public LongIterator iterator()
                {
                    final IntIterator i = intSet.iterator();
                    return new LongIterator()
                        {
                            @Override
                            public void remove()
                            {
                                throw new UnsupportedOperationException();
                            }

                            @Override
                            public long next()
                            {
                                return (long)i.next();
                            }

                            @Override
                            public boolean hasNext()
                            {
                                return i.hasNext();
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
                    return intSet.size();
                }

                @Override
                public long[] toArray()
                {
                    return toArray(new long[intSet.size()]);
                }

                @Override
                public long[] toArray(long[] arr)
                {
                    if(arr.length != intSet.size())
                    {
                        arr = new long[intSet.size()];
                    }
                    int j = 0;
                    for(IntIterator i = intSet.iterator(); i.hasNext();)
                    {
                        arr[j++] = (long)i.next();
                    }
                    return arr;
                }

                @Override
                public void trimToSize()
                {
                    throw new UnsupportedOperationException();
                }

                public int hashCode()
                {
                    return intSet.hashCode();
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
