package org.objectledge.coral.datatypes;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;

import org.objectledge.coral.BackendException;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.Resource;

/**
 * Represents a one-to-many relationship among Resources, or a plain ordered
 * list of resources. 
 *
 * <p>This implementation allows only Resource objects to be stored, and keeps
 * only identifiers of the conatained resources, leaving cache management to
 * the StoreService</p>  
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: ResourceList.java,v 1.4 2005-01-17 11:20:23 rafal Exp $
 */
public class ResourceList
    extends AbstractList
{
    // constants /////////////////////////////////////////////////////////////

    /** Initial list capacity. */
    public static final int CAPACITY_INITIAL = 10;
    
    /** List capacity increment. */
    public static final int CAPACITY_INCREMENT = 10;

    // instance variables ////////////////////////////////////////////////////

    /** The coral store. */
    protected CoralStore coralStore;

    /** The resource ids. */
	protected long[] ids = new long[CAPACITY_INITIAL];

    /** Current list size. */
	protected int size = 0;
    
    /** Current list capacity. */
	protected int capacity = CAPACITY_INITIAL;

    /**
     * Creates an empty list.
     * 
     * @param coralStore the coral store.
     */
    public ResourceList(CoralStore coralStore)
    {
        super();
        this.coralStore = coralStore;
    }

    /**
     * Creates a list of resources.
     *
     * <p>The collection should contain Resource or Long objects.</p>
     *
     * @param coralStore the coral store.
     * @param elements the collection of resources or identifiers.
     */
    public ResourceList(CoralStore coralStore, Collection elements)
    {
        this(coralStore);
        Iterator i = elements.iterator();
        size = elements.size();
        capacity = size+CAPACITY_INCREMENT;
        ids = new long[capacity];
        int position = 0;
        while(i.hasNext())
        {
            Object v = i.next();
            if(v instanceof Resource)
            {
                ids[position++] = ((Resource)v).getId();
            }
            else if(v instanceof Long)
            {
                ids[position++] = ((Long)v).longValue();
            }
            else
            {
                throw new ClassCastException(v.getClass().getName());
            }
        }
    }

    // package private ///////////////////////////////////////////////////////

    /**
     * Provides direct acceess to the data, used by {@link
     * ResourceListAttributeHandler}.
     */
    long[] getIds()
    {
        return ids;    
    }

    // List implementation ///////////////////////////////////////////////////

    /**
     * Get the size.
     * 
     * @return the size.
     */
    public int size()
    {
        return size;
    }

    /**
     * {@inheritDoc}
     */
    public Object get(int index)
        throws IndexOutOfBoundsException
    {
        if(index < 0 || index >= size)
        {
            throw new IndexOutOfBoundsException();
        }
        try
        {
            return coralStore.getResource(ids[index]);
        }
        catch(EntityDoesNotExistException e)
        {
            throw new BackendException("resource #"+ids[index]+" dissappeared", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void add(int index, Object object)
        throws IndexOutOfBoundsException, ClassCastException
    {
        if(index < 0 || index > size)
        {
            throw new IndexOutOfBoundsException();
        }
        long id;
        if(object instanceof Resource)
        {
            id = ((Resource)object).getId();
        }
        else if(object instanceof Long)
        {
            id = ((Long)object).longValue();
        }
        else
        {
            throw new ClassCastException(object.getClass().getName());
        }

        if(size+1 <= capacity && index == size)
        {
            ids[index] = id;
            size++;
            modCount++;
            return;
        }
        
        long[] newIds = new long[ids.length];
        if(size+1 > capacity)
        {
            capacity += CAPACITY_INCREMENT;
            newIds = new long[capacity];
        }
        
        if(index != size)
        {
            System.arraycopy(ids,0,newIds,0,index);
            System.arraycopy(ids,index,newIds,index+1,size-index);
        }
        else
        {
            System.arraycopy(ids,0,newIds,0,size);
        }
        newIds[index] = id;
        ids = newIds;
        size++;
        modCount++;
    }

    /**
     * {@inheritDoc}
     */    
    public Object set(int index, Object object)
        throws IndexOutOfBoundsException, ClassCastException
    {
        if(index < 0 || index >= size)
        {
            throw new IndexOutOfBoundsException();
        }
        long id;
        if(object instanceof Resource)
        {
            id = ((Resource)object).getId();
        }
        else if(object instanceof Long)
        {
            id = ((Long)object).longValue();
        }
        else
        {
            throw new ClassCastException(object.getClass().getName());
        }
        Object old;
        try
        {
            old = coralStore.getResource(ids[index]);
        }
        catch(EntityDoesNotExistException e)
        {
            throw new BackendException("resource #"+ids[index]+" dissappeared", e);
        }
        ids[index] = id;
        return old;
    }

    /**
     * {@inheritDoc}
     */
    public Object remove(int index)
        throws IndexOutOfBoundsException
    {
        if(index < 0 || index >= size)
        {
            throw new IndexOutOfBoundsException();
        }
        Object old;
        try
        {
            old = coralStore.getResource(ids[index]);
        }
        catch(EntityDoesNotExistException e)
        {
            throw new BackendException("resource #"+ids[index]+" dissappeared", e);
        }
        long[] newIds = ids;
        if(size-1 <= capacity/2)
        {
            capacity = capacity/2+CAPACITY_INCREMENT;
            newIds = new long[capacity];
            System.arraycopy(ids,0,newIds,0,index);
        }
        if(index < size-1)
        {
            System.arraycopy(ids,index+1,newIds,index,size-index-1);
        }
        ids = newIds;
        modCount++;
        size--;
        return old;
    }
    
    /**
     * Remove a range of entries.
     * 
     * @param fromIndex range start (inclusive)
     * @param toIndex range end (exclusive)
     */
    protected void removeRange(int fromIndex, int toIndex)
    {
        if(fromIndex < 0 || fromIndex > size ||
           toIndex < 0 || toIndex > size ||
           fromIndex > toIndex)
        {
            throw new IndexOutOfBoundsException();
        }
        if(fromIndex == toIndex)
        {
            return;
        }
        long[] newIds = ids;
        if(size-(toIndex-fromIndex) <= capacity/2)
        {
            capacity = capacity/2+CAPACITY_INCREMENT;
            newIds = new long[capacity];
            System.arraycopy(ids,0,newIds,0,fromIndex);
        }
        if(toIndex < size-1)
        {
            System.arraycopy(ids,toIndex,newIds,fromIndex,size-toIndex);
        }
        size = size-(toIndex-fromIndex);
        modCount++;
    }
    
    /**
     * Simple implemenation that prints out member ids.
     * 
     * @return string representation of the list.
     */
    public String toString()
    {
        StringBuffer buff = new StringBuffer();
        buff.append('[');
        for(int i = 0; i < size; i++)
        {
            buff.append(ids[i]);
            if(i < size - 1)
            {
                buff.append(", ");
            }
        }
        buff.append(']');
        return buff.toString();
    }
}
