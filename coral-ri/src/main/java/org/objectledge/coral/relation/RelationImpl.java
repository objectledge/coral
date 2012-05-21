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
import java.util.List;

import org.objectledge.coral.BackendException;
import org.objectledge.coral.entity.AbstractEntity;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.util.PrimitiveCollections;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.PersistenceException;

import bak.pcj.LongIterator;
import bak.pcj.map.LongKeyMap;
import bak.pcj.map.LongKeyMapIterator;
import bak.pcj.map.LongKeyOpenHashMap;
import bak.pcj.set.LongOpenHashSet;
import bak.pcj.set.LongSet;

/**
 * An implementation of the Relation interface.
 * 
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: RelationImpl.java,v 1.31 2007-06-03 00:06:26 rafal Exp $
 */
public class RelationImpl
extends AbstractEntity
implements Relation
{
    // implementation -----------------------------------------------------------------------------
    
    private static int initialSetCapacity = 128;

    private static final long[][] BLANK = new long[0][];

    /** Store is used to retrieve resources. */
    private CoralStore store;
    
    /** RelationManager is used to retrieve relation definitions. */
    private CoralRelationManager coralRelationManager;

    /** Map r1 -&gt; set of r2. */
    private LongKeyMap rel = new LongKeyOpenHashMap();
    /** Map r2 -&gt; set of r1. */
    private LongKeyMap invRel = new LongKeyOpenHashMap();
	/** Number of unique resource pairs. */
	private int resourceIdPairsNum = 0;

    // initialization -----------------------------------------------------------------------------

    private InvertedRelation invertedRelation = new InvertedRelation();

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
    public synchronized Resource[] get(Resource r)
    {
        return get(rel, r, store);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized LongSet get(long id)
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
    public synchronized boolean hasRef(long id, long idInv)
    {
        LongSet set = (LongSet)rel.get(id);
        if(set != null)
        {
            return set.contains(idInv);
        }
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    public synchronized int size()
    {
        return calcSetsSizeSum(rel);
    }

	/**
	 * {@inheritDoc}
	 */
	public synchronized float getAvgMappingSize()
	{
		return getAvgMappingSize(rel, resourceIdPairsNum);
	}

    // persistence api ----------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public String getTable()
    {
        return "coral_relation";
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

    /** The key columns. */
    private static final String[] KEY_COLUMNS = { "relation_id" };

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
	public synchronized void setData(InputRecord record) throws PersistenceException
	{
		super.setData(record);
		long[] def = coralRelationManager.getRelationDefinition(this);
        
        LongSet set1 = makeLongSet(def.length / 4);
        LongSet set2 = makeLongSet(def.length / 4);

        for(int i=0; i<def.length / 2; i++)
        {
            set1.add(def[2 * i]);
            set2.add(def[2 * i + 1]);
        }

        rel = makeLongKeyMap((int)(set1.size()*1.5));
        invRel = makeLongKeyMap((int)(set2.size()*1.5));

        for(int i=0; i<def.length / 2; i++)
        {
            long id1 = def[i * 2];
            long id2 = def[2 * i + 1];

            set1 = maybeCreateSet(rel, id1);
            set2 = maybeCreateSet(invRel, id2);

            set1.add(id2);
            set2.add(id1);
        }
        this.resourceIdPairsNum = calcSetsSizeSum(rel);
    }

    public synchronized long[][] getPairs()
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
        LongSet set1 = (LongSet) rel.get(id1);
        LongSet set2 = (LongSet) invRel.get(id2);

        boolean p1 = false;
        if(set1 != null)
        {
            p1 = set1.remove(id2);
            if(set1.size() == 0)
            {
				rel.remove(id1);
            }
        }

        boolean p2 = false;
        if(set2 != null)
        {
            p2 = set2.remove(id1);
			if(set2.size() == 0)
			{
				invRel.remove(id2);
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
        LongSet set1 = maybeCreateSet(rel, id1);
        LongSet set2 = maybeCreateSet(invRel, id2);

        boolean p1 = set1.add(id2);
        // -- this the moment in which the relationship is directed r1 -> r2
		boolean p2 = set2.add(id1);

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

    private static int calcSetsSizeSum(LongKeyMap relation)
    {
    	int totalSize = 0;
    	for(LongKeyMapIterator i = relation.entries(); i.hasNext(); )
    	{
    		i.next();
    		LongSet relSet = (LongSet) i.getValue();
    		totalSize += relSet.size();
    	}
    	return totalSize;
    }

    private static float getAvgMappingSize(LongKeyMap relation, int numPairs)
    {
    	int numSets = relation.size();
    	if(numSets != 0)
    	{
    		return (float) numPairs / (float) numSets;
    	}
    	else
    	{
    		if(numPairs != 0)
    		{
    			throw new IllegalStateException("inconsistent state");
    		}
    		return 0F;
    	}
    }

    private static long[][] getPairs(LongKeyMap relation)
    {
        List<long[]> temp = new ArrayList<long[]>();
        for(LongKeyMapIterator i = relation.entries(); i.hasNext(); )
        {
        	i.next();
        	LongSet tailSet = (LongSet)i.getValue();
        	for(LongIterator j = tailSet.iterator(); j.hasNext(); )
        	{
                long[] pair = new long[2];
                pair[0] = i.getKey();
                pair[1] = j.next();
                temp.add(pair);
        	}
        }
        Collections.sort(temp, PairComparator.INSTANCE);
        return temp.toArray(BLANK);
    }

    /**
     * Returns a set for a given id key and relation, maybe creates it.
     */
    private static LongSet maybeCreateSet(LongKeyMap relation, long idk)
    {
        return maybeCreateSet(relation, idk, initialSetCapacity);
    }

    /**
     * Returns a set for a given id key and relation, maybe creates it.
     */
    private static LongSet maybeCreateSet(LongKeyMap relation, long idk, int initialCapacity)
    {
        LongSet set = (LongSet) relation.get(idk);
        if(set == null)
        {
            set = makeLongSet(initialCapacity);
            relation.put(idk, set);
        }
        return set;
    }

    /**
     * Returns resources referenced by a given resource in the relation.
     * @param store CoralStore
     */
    private static Resource[] get(LongKeyMap relation, Resource r, CoralStore store)
    {
        LongSet set = (LongSet)relation.get(r.getId());
        if(set != null)
        {
            return instantiate(set, store);
        }
        else
        {
            return new Resource[0];
        }
    }

    /**
     * Return an array of ids contained in the map under given id.
     */
    private static LongSet get(LongKeyMap relation, long id)
    {
        LongSet set = (LongSet) relation.get(id);
        if(set != null)
        {
        	return PrimitiveCollections.unmodifiableLongSet(set);
        }
        else
        {
            return PrimitiveCollections.EMPTY_LONG_SET;
        }
    }

    /**
     * Returns an array of Resources with given identifiers.
     *
     * @param a indentifier array.
     * @param store CoralStore
     * @return Resource array.
     */
    static private Resource[] instantiate(LongSet a, CoralStore store)
    {
        Resource[] res = new Resource[a.size()];
        try
        {
            int i = 0;
            for (LongIterator iter = a.iterator(); iter.hasNext(); i++)
            {
                long id = iter.next();
                res[i] = store.getResource(id);
            }
        }
        catch(EntityDoesNotExistException e)
        {
            throw new BackendException("inconsistent data", e);
        }
        return res;
    }
    
	
	private static LongKeyMap makeLongKeyMap(int size)
	{
		if(size <= 0)
		{
			return new LongKeyOpenHashMap();
		}
		else
		{
			return new LongKeyOpenHashMap(size);
		}
	}
	
	private static LongSet makeLongSet(int size)
	{
		if(size <= 0)
		{
			return new LongOpenHashSet();
		}
		else
		{
			return new LongOpenHashSet(size);
		}		
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
        public synchronized Resource[] get(Resource r)
        {
            return RelationImpl.get(invRel, r, store);
        }

        /**
         * {@inheritDoc}
         */
        public synchronized LongSet get(long id)
        {
            return RelationImpl.get(invRel, id);
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
		public synchronized float getAvgMappingSize()
		{
			return RelationImpl.getAvgMappingSize(invRel, resourceIdPairsNum);
		}
        
        public synchronized long[][] getPairs()
        {
            return RelationImpl.getPairs(invRel);
        }
    }
}
