//
// Copyright (c) 2003, 2004, Caltha - Gajda, Krzewski, Mach, Potempski Sp.J.
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without modification,
// are permitted provided that the following conditions are met:
//
// * Redistributions of source code must retain the above copyright notice,
// this list of conditions and the following disclaimer.
// * Redistributions in binary form must reproduce the above copyright notice,
// this list of conditions and the following disclaimer in the documentation
// and/or other materials provided with the distribution.
// * Neither the name of the Caltha - Gajda, Krzewski, Mach, Potempski Sp.J.
// nor the names of its contributors may be used to endorse or promote products
// derived from this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
// IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
// INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
// BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
// OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
// WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.
//
package org.objectledge.coral.relation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.objectledge.coral.store.Resource;

/**
 * A class representing a batch of {@link Relation} modifications.
 *
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: RelationModification.java,v 1.1 2004-02-20 09:15:48 zwierzem Exp $
 */
public class RelationModification
{
    /** Flag which tells if relation has been cleared. */
    private boolean cleared = false;
    /** Set of added relations. */
    private Set added = new HashSet(128);
    /** Set of deleted relations. */
    private Set deleted = new HashSet(128);

    // basic api ----------------------------------------------------------------------------------

	/**
	 * Returns the <code>cleared</code> flag for this <code>RelationModification</code>.
	 * 
	 * @return <code>true</code> if relation should be cleared.
	 */
	public boolean isCleared()
	{
		return cleared;
	}

	/**
	 * Returns an array of deleted id pairs.
	 * 
	 * @return deleted id pairs
	 */
    public long[][] getDeleted()
    {
        return setToArray(deleted);
    }

	/**
	 * Returns an array of added id pairs.
	 * 
	 * @return added id pairs
	 */
    public long[][] getAdded()
    {
        return setToArray(added);
    }

	/**
	 * Resets this <code>RelationModification</code> state - as if no modifications were
	 * performed.
	 */
	public synchronized void reset()
	{
		cleared = false;
		added.clear();
		deleted.clear();
	}

    // modification -------------------------------------------------------------------------------

    /**
     * Add a ordered pair to the relationship's definition.
     *
     * @param r1 the first element of the pair.
     * @param r2 the second element of the pair.
     */
    public synchronized void put(Resource r1, Resource r2)
    {
        add(new Long(r1.getId()), new Long(r2.getId()));
    }

    /**
     * Add a number of pairs to the relationship's definition.
     *
     * @param r1 the first element of the pairs.
     * @param ress second elements of the pairs.
     */
    public synchronized void put(Resource r1, Resource[] ress)
    {
        Long id1 = new Long(r1.getId());
        for (int i = 0; i < ress.length; i++)
        {
            add(id1, new Long(ress[i].getId()));
        }
    }

    /**
     * Add a number of pairs to the relationship's definition.
     *
     * @param ress first elements of the pairs.
     * @param r2 the second element of the pairs.
     */
    public synchronized void put(Resource[] ress, Resource r2)
    {
        Long id2 = new Long(r2.getId());
        for (int i = 0; i < ress.length; i++)
        {
            add(new Long(ress[i].getId()), id2);
        }
    }

    /**
     * Delete a ordered pair from the relationship's definition.
     *
     * @param r1 the first element of the pair.
     * @param r2 the second element of the pair.
     */
    public synchronized void remove(Resource r1, Resource r2)
    {
        rem(new Long(r1.getId()), new Long(r2.getId()));
    }

    /**
     * Delete all pairs where r is the first element of the pair from relationship's definition.
     *
     * TODO: Remove all added with resource on the left side.
     * TODO: How do I track removed on one side???
     * @param r the resource
     */
    public synchronized void remove(Resource r)
    {
    	
        rem(new Long(r.getId()), null);
    }

    /**
     * Delete all pairs where r is the second element of the pair from relationship's definition.
     *
     * TODO: Remove all added with resource on the right side.
     * TODO: How do I track removed on one side???
     * @param r the resource
     */
    public synchronized void removeInv(Resource r)
    {    	
        rem(null, new Long(r.getId()));
    }

    /**
     * Removes all references defined for the relation.
     */
    public synchronized void clear()
    {
        this.added.clear();
        this.deleted.clear();
        cleared = true;
    }

    // implementation -----------------------------------------------------------------------------

    private void add(Long id1, Long id2)
    {
        IdPair pair = new IdPair(id1, id2);
        if(deleted.contains(pair))
        {
            deleted.remove(pair);
        }
        else
        {
            added.add(pair);
        }
    }

    private void rem(Long id1, Long id2)
    {
        IdPair pair = new IdPair(id1, id2);
        if(added.contains(pair))
        {
            added.remove(pair);
        }
        else
        {
            deleted.add(pair);
        }
    }

    /**
     * A class representing an id pair - a relation between resources.
     */
    private class IdPair
    {
        private int hashCode;
        private long id1 = -1L;
        private long id2 = -1L;

        public IdPair(Long id1, Long id2)
        {
            if(id1 == null && id2 == null)
            {
                throw new IllegalArgumentException("both params cannot be null");
            }

            if(id1 != null && id2 != null)
            {
                hashCode = (id1.hashCode()) ^ (id2.hashCode());
                this.id1 = id1.longValue();
                this.id2 = id2.longValue();
            }
            else if(id1 != null)
            {
                hashCode = id1.hashCode();
                this.id1 = id1.longValue();
            }
            else
            {
                hashCode = id2.hashCode();
                this.id2 = id2.longValue();
            }
        }

        public int hashCode()
        {
            return hashCode;
        }

        public boolean equals(Object o)
        {
            if(o instanceof IdPair)
            {
                IdPair lp = (IdPair)o;
                return (this.id1 == lp.id1) && (this.id2 == lp.id2);
            }
            return false;
        }

        /**
         * Returns the first id in the pair.
         * @return the id value
         */
        public long getId1()
        {
            return id1;
        }

        /**
         * Returns the second id in the pair.
         * @return the id value
         */
        public long getId2()
        {
            return id2;
        }
    }

    private long[][] setToArray(Set set)
    {
        ArrayList list = new ArrayList(set.size());
        for (Iterator iter = set.iterator(); iter.hasNext();)
        {
            IdPair pair = (IdPair) iter.next();
            long[] entry = new long[] { pair.getId1(), pair.getId2() };
            list.add(entry);
        }
        long[][] result = new long[list.size()][];
        list.toArray(result);
        return result;
    }
}
