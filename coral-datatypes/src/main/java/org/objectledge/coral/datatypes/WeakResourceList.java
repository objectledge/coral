package org.objectledge.coral.datatypes;

import java.util.Collection;

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
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: WeakResourceList.java,v 1.1 2004-03-02 09:51:01 pablo Exp $
 */
public class WeakResourceList
    extends ResourceList
{
    /**
     * Creates an empty list.
     */
    public WeakResourceList(CoralStore coralStore)
    {
        super(coralStore);
    }
    
	/**
	 * Creates a list of resources.
	 *
	 * <p>The collection should contain Resource or Long objects.</p>
	 *
	 * @param elements the collection of resources or identifiers.
	 */
	public WeakResourceList(CoralStore coralStore, Collection elements)
	{
		super(coralStore, elements);
	}

    
    // List implementation ///////////////////////////////////////////////////
    
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
            return null;
        }
    }
    
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
        Object old = null;
        try
        {
            old = coralStore.getResource(ids[index]);
        }
        catch(EntityDoesNotExistException e)
        {
            //do nothing...
        }
        ids[index] = id;
        return old;
    }

    public Object remove(int index)
        throws IndexOutOfBoundsException
    {
        if(index < 0 || index >= size)
        {
            throw new IndexOutOfBoundsException();
        }
        Object old = null;
        try
        {
            old = coralStore.getResource(ids[index]);
        }
        catch(EntityDoesNotExistException e)
        {
            // do nothing ...
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
}
