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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectledge.coral.BackendException;
import org.objectledge.coral.entity.AbstractEntity;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.Resource;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.PersistenceException;

/**
 * An implementation of the Relation interface.
 * 
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: RelationImpl.java,v 1.29 2006-03-03 13:52:26 rafal Exp $
 */
public class RelationImpl
extends AbstractEntity
implements Relation
{
    /** Store is used to retrieve resources. */
    private CoralStore store;
    
    /** RelationManager is used to retrieve relation definitions. */
    private CoralRelationManager coralRelationManager;

    /** Map r1 -&gt; set of r2. */
    private Map<Long, Set<Long>> rel = new HashMap<Long, Set<Long>>();
    /** Map r2 -&gt; set of r1. */
    private Map<Long, Set<Long>> invRel = new HashMap<Long, Set<Long>>();
	/** Number of unique resource pairs. */
	private int resourceIdPairsNum = 0;

    // initialization -----------------------------------------------------------------------------

    /**
     * Creates a relationship from provided definition.
     *
     * @param persistence the persistence system
     * @param store used to retrieve resources
     * @param coralRelationManager used to retrieve relation definitions
     */
    public RelationImpl(Persistence persistence, CoralStore store, 
        CoralRelationManager coralRelationManager)
    {
        super(persistence);

        this.store = store;
        this.coralRelationManager = coralRelationManager;
    }

    /**
     * Creates a relationship from provided definition.
     *
     * @param persistence the persistence system
     * @param store used to retrieve resources
     * @param coralRelationManager used to retrieve relation definitions
     * @param name name of the relation
     */
    public RelationImpl(Persistence persistence, CoralStore store, 
        CoralRelationManager coralRelationManager, String name)
    {
        super(persistence, name);

        this.store = store;
        this.coralRelationManager = coralRelationManager;
    }

    // public api ---------------------------------------------------------------------------------

    private InvertedRelation invertedRelation = new InvertedRelation();

    /**
     * {@inheritDoc}
     */
    public Relation getInverted()
    {
        return invertedRelation;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isInverted()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public Resource[] get(Resource r)
    {
        return get(rel, r);
    }

    /**
     * {@inheritDoc}
     */
    public Set<Long> get(long id)
    {
        return get(rel, id);
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasRef(Resource r, Resource rInv)
    {
        return hasRef(r.getId(), rInv.getId());
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasRef(long id, long idInv)
    {
        Set set = (Set)rel.get(new Long(id));
        if(set != null)
        {
            return set.contains(new Long(idInv));
        }
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    public int size()
    {
        return calcSetsSizeSum(rel);
    }

	/**
	 * {@inheritDoc}
	 */
	public float getAvgMappingSize()
	{
		return getAvgMappingSize(rel);
	}

	float getAvgMappingSize(Map relation)
	{
		int numSets = relation.keySet().size();
		if(numSets != 0)
		{
			return (float) resourceIdPairsNum / (float) numSets;
		}
		else
		{
			if(resourceIdPairsNum != 0)
			{
				throw new IllegalStateException("inconsistent state");
			}
			return 0F;
		}
	}

    // persistence api ----------------------------------------------------------------------------

    /** The key columns. */
    private static final String[] KEY_COLUMNS = { "relation_id" };

    /**
     * {@inheritDoc}
     */
    public String getTable()
    {
        return "coral_relation";
    }

    /**
     * {@inheritDoc}
     */
    public String[] getKeyColumns()
    {
        return KEY_COLUMNS;
    }

	/**
	 * {@inheritDoc}
	 */
	public void setData(InputRecord record) throws PersistenceException
	{
		super.setData(record);
		long[] def = coralRelationManager.getRelationDefinition(this);
        
        
        Set<Long> set1 = new HashSet(def.length/4);
        Set<Long> set2 = new HashSet(def.length/4);

        for(int i=0; i<def.length / 2; i++)
        {
            set1.add(new Long(def[2 * i]));
            set2.add(new Long(def[2 * i + 1]));
        }

        rel = new HashMap<Long, Set<Long>>((int)(set1.size()*1.5));
        invRel = new HashMap<Long, Set<Long>>((int)(set2.size()*1.5));

        for(int i=0; i<def.length / 2; i++)
        {
            Long r1k = new Long(def[i * 2]);
            Long r2k = new Long(def[2 * i + 1]);

            set1 = maybeCreateSet(rel, r1k);
            set2 = maybeCreateSet(invRel, r2k);

            set1.add(r2k);
            set2.add(r1k);
        }
        this.resourceIdPairsNum = calcSetsSizeSum(rel);
    }

    /**
	 * Returns a name of a database table used for storing relations contents.
	 * 
	 * @return name of a database table.  
	 */
	String getDataTable()
	{
		return "coral_relation_data";
	}
    
	private int calcSetsSizeSum(Map<Long, Set<Long>> relation)
	{
		int totalSize = 0;
		for (Iterator<Set<Long>> iter = relation.values().iterator(); iter.hasNext();)
		{
            Set<Long> relSet = iter.next();
			totalSize += relSet.size();
		}
		return totalSize;
	}
    
    private static final long[][] BLANK = new long[0][];
    
    private long[][] getPairs(Map<Long, Set<Long>> relation)
    {
        List<long[]> temp = new ArrayList<long[]>();
        for(long head : relation.keySet())
        {
            for(long tail : relation.get(head))
            {
                long[] pair = new long[2];
                pair[0] = head;
                pair[1] = tail;
                temp.add(pair);
            }
        }
        return temp.toArray(BLANK);
    }
    
    public long[][] getPairs()
    {
        return getPairs(rel);
    }

    // implementation api -------------------------------------------------------------------------

    /**
     * Clears the contents of the relation.
     */
	synchronized void clear()
    {
        rel.clear();
        invRel.clear();
		resourceIdPairsNum = 0;
    }

    /**
     * Removes a pair of elements.
     *
     * @param id1 left side of thr relation
     * @param id2 right side of thr relation
     */
	synchronized void remove(long id1, long id2)
    {
        Long r1k = new Long(id1);
        Long r2k = new Long(id2);

        Set<Long> set1 = rel.get(r1k);
        Set<Long> set2 = invRel.get(r2k);

        boolean p1 = false;
        if(set1 != null)
        {
            p1 = set1.remove(r2k);
            if(set1.size() == 0)
            {
				rel.remove(r1k);
            }
        }

        boolean p2 = false;
        if(set2 != null)
        {
            p2 = set2.remove(r1k);
			if(set2.size() == 0)
			{
				invRel.remove(r2k);
			}
        }

		if(p1 && p2)
		{
			resourceIdPairsNum -= 1F;
		}

        if(p1 != p2)
        {
            throw new IllegalStateException("inconsistent state");
        }
    }

    /**
     * Add an ordered pair to the relationship's definition.
     *
     * @param id1 the first element of the pair.
     * @param id2 the second element of the pair.
     */
    synchronized void add(long id1, long id2)
    {
        Long r1k = new Long(id1);
        Long r2k = new Long(id2);

        Set<Long> set1 = maybeCreateSet(rel, r1k);
        Set<Long> set2 = maybeCreateSet(invRel, r2k);

        boolean p1 = set1.add(r2k);
        // -- this the moment in which the relationship is directed r1 -> r2
		boolean p2 = set2.add(r1k);

		if(p1 && p2)
		{
			resourceIdPairsNum += 1F;
		}

		if(p1 != p2)
		{
			throw new IllegalStateException("inconsistent state");
		}
    }

    // implementation -----------------------------------------------------------------------------

    private static int initialSetCapacity = 128;

    /**
     * Returns a set for a given id key and relation, maybe creates it.
     */
    private Set<Long> maybeCreateSet(Map<Long, Set<Long>> relation, Long idk)
    {
        return maybeCreateSet(relation, idk, initialSetCapacity);
    }

    /**
     * Returns a set for a given id key and relation, maybe creates it.
     */
    private Set<Long> maybeCreateSet(Map<Long, Set<Long>> relation, Long idk, int initialCapacity)
    {
        Set<Long> set = relation.get(idk);
        if(set == null)
        {
            set = new HashSet<Long>(initialCapacity);
            relation.put(idk, set);
        }
        return set;
    }

    /**
     * Returns resources referenced by a given resource in the relation.
     */
    Resource[] get(Map<Long, Set<Long>> relation, Resource r)
    {
        Set<Long> set = relation.get(r.getIdObject());
        if(set != null)
        {
            return instantiate(set);
        }
        else
        {
            return new Resource[0];
        }
    }

    /**
     * Return an array of ids contained in the map under given id.
     */
    Set<Long> get(Map<Long, Set<Long>> relation, long id)
    {
        Set<Long> set = relation.get(new Long(id));
        if(set != null)
        {
        	return Collections.unmodifiableSet(set);
        }
        else
        {
            return (Set<Long>) Collections.EMPTY_SET;
        }
    }

    /**
     * Returns an array of Resources with given identifiers.
     *
     * @param a indentifier array.
     * @return Resource array.
     */
    private Resource[] instantiate(Set<Long> a)
    {
        Resource[] res = new Resource[a.size()];
        try
        {
            int i = 0;
            for (Iterator<Long> iter = a.iterator(); iter.hasNext(); i++)
            {
                Long id = iter.next();
                res[i] = store.getResource(id.longValue());
            }
        }
        catch(EntityDoesNotExistException e)
        {
            throw new BackendException("inconsistent data", e);
        }
        return res;
    }

	// relation invertion -------------------------------------------------------------------------

    /** Represents a reversed relation. */
    private class InvertedRelation implements Relation
    {
        /**
         * {@inheritDoc}
         */
        public Relation getInverted()
        {
            return RelationImpl.this;
        }

        /**
         * {@inheritDoc}
         */
        public boolean isInverted()
        {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        public Resource[] get(Resource r)
        {
            return RelationImpl.this.get(invRel, r);
        }

        /**
         * {@inheritDoc}
         */
        public Set<Long> get(long id)
        {
            return RelationImpl.this.get(invRel, id);
        }

        /**
         * {@inheritDoc}
         */
        public boolean hasRef(Resource r, Resource rInv)
        {
            return RelationImpl.this.hasRef(rInv, r);
        }

        /**
         * {@inheritDoc}
         */
        public boolean hasRef(long id, long idInv)
        {
            return RelationImpl.this.hasRef(idInv, id);
        }

        /**
         * {@inheritDoc}
         */
        public long getId()
        {
            return RelationImpl.this.getId();
        }

        /**
         * Returns the numerical identifier of the entity as a Java object.
         * 
         * @return the numerical identifier of the entity as a Java object.
         */
        public Long getIdObject()
        {
            return RelationImpl.this.getIdObject();
        }

        /**
         * Returns the numerical identifier of the entity as a string.
         * 
         * @return the numerical identifier of the entity as a string.
         */
        public String getIdString()
        {
            return RelationImpl.this.getIdString();
        }
        
        /**
         * {@inheritDoc}
         */
        public String getName()
        {
            return RelationImpl.this.getName();
        }
        
        /**
         * {@inheritDoc}
         */
        public int size()
        {
            return RelationImpl.this.size();
        }

		/**
		 * {@inheritDoc}
		 */
		public float getAvgMappingSize()
		{
			return RelationImpl.this.getAvgMappingSize(invRel);
		}
        
        public long[][] getPairs()
        {
            return RelationImpl.this.getPairs(invRel);
        }
    }
}
