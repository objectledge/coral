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

import java.sql.SQLException;
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

import bak.pcj.IntIterator;
import bak.pcj.map.IntKeyMap;
import bak.pcj.map.IntKeyMapIterator;
import bak.pcj.map.IntKeyOpenHashMap;
import bak.pcj.set.IntOpenHashSet;
import bak.pcj.set.IntSet;
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
    private IntKeyMap rel = new IntKeyOpenHashMap();

    /** Map r2 -&gt; set of r1. */
    private IntKeyMap invRel = new IntKeyOpenHashMap();

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
        checkRange(id, idInv);
        IntSet set = (IntSet)rel.get((int)id);
        if(set != null)
        {
            return set.contains((int)idInv);
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
    public synchronized void setData(InputRecord record)
        throws SQLException
    {
        super.setData(record);
        long[] def = coralRelationManager.getRelationDefinition(this);

        IntSet set1 = makeIntSet(def.length / 4);
        IntSet set2 = makeIntSet(def.length / 4);

        for(int i = 0; i < def.length / 2; i++)
        {
            checkRange(def[2 * i], def[2 * i + 1]);
            set1.add((int)def[2 * i]);
            set2.add((int)def[2 * i + 1]);
        }

        rel = makeIntKeyMap((int)(set1.size() * 1.5));
        invRel = makeIntKeyMap((int)(set2.size() * 1.5));

        for(int i = 0; i < def.length / 2; i++)
        {
            long id1 = def[i * 2];
            long id2 = def[2 * i + 1];
            checkRange(id1, id2);

            set1 = maybeCreateSet(rel, id1);
            set2 = maybeCreateSet(invRel, id2);

            set1.add((int)id2);
            set2.add((int)id1);
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
        checkRange(id1, id2);
        IntSet set1 = (IntSet)rel.get((int)id1);
        IntSet set2 = (IntSet)invRel.get((int)id2);

        boolean p1 = false;
        if(set1 != null)
        {
            p1 = set1.remove((int)id2);
            if(set1.size() == 0)
            {
                rel.remove((int)id1);
            }
        }

        boolean p2 = false;
        if(set2 != null)
        {
            p2 = set2.remove((int)id1);
            if(set2.size() == 0)
            {
                invRel.remove((int)id2);
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
        checkRange(id1, id2);
        IntSet set1 = maybeCreateSet(rel, id1);
        IntSet set2 = maybeCreateSet(invRel, id2);

        boolean p1 = set1.add((int)id2);
        // -- this the moment in which the relationship is directed r1 -> r2
        boolean p2 = set2.add((int)id1);

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

    private static void checkRange(long... vals)
    {
        for(long val : vals)
        {
            if(val > Integer.MAX_VALUE || val < Integer.MIN_VALUE)
            {
                throw new IllegalArgumentException("value " + val + " is out of supported range");
            }
        }
    }

    private static int calcSetsSizeSum(IntKeyMap relation)
    {
        int totalSize = 0;
        for(IntKeyMapIterator i = relation.entries(); i.hasNext();)
        {
            i.next();
            IntSet relSet = (IntSet)i.getValue();
            totalSize += relSet.size();
        }
        return totalSize;
    }

    private static float getAvgMappingSize(IntKeyMap relation, int numPairs)
    {
        int numSets = relation.size();
        if(numSets != 0)
        {
            return (float)numPairs / (float)numSets;
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

    private static long[][] getPairs(IntKeyMap relation)
    {
        List<long[]> temp = new ArrayList<long[]>();
        for(IntKeyMapIterator i = relation.entries(); i.hasNext();)
        {
            i.next();
            IntSet tailSet = (IntSet)i.getValue();
            for(IntIterator j = tailSet.iterator(); j.hasNext();)
            {
                long[] pair = new long[2];
                pair[0] = (long)i.getKey();
                pair[1] = (long)j.next();
                temp.add(pair);
            }
        }
        Collections.sort(temp, PairComparator.INSTANCE);
        return temp.toArray(BLANK);
    }

    /**
     * Returns a set for a given id key and relation, maybe creates it.
     */
    private static IntSet maybeCreateSet(IntKeyMap relation, long idk)
    {
        return maybeCreateSet(relation, idk, initialSetCapacity);
    }

    /**
     * Returns a set for a given id key and relation, maybe creates it.
     */
    private static IntSet maybeCreateSet(IntKeyMap relation, long idk, int initialCapacity)
    {
        // checkRange(idk) was already performed by the caller
        IntSet set = (IntSet)relation.get((int)idk);
        if(set == null)
        {
            set = makeIntSet(initialCapacity);
            relation.put((int)idk, set);
        }
        return set;
    }

    /**
     * Returns resources referenced by a given resource in the relation.
     * 
     * @param store CoralStore
     */
    private static Resource[] get(IntKeyMap relation, Resource r, CoralStore store)
    {
        checkRange(r.getId());
        IntSet set = (IntSet)relation.get((int)r.getId());
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
    private static LongSet get(IntKeyMap relation, long id)
    {
        checkRange(id);
        IntSet set = (IntSet)relation.get((int)id);
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
    static private Resource[] instantiate(IntSet a, CoralStore store)
    {
        Resource[] res = new Resource[a.size()];
        try
        {
            int i = 0;
            for(IntIterator iter = a.iterator(); iter.hasNext(); i++)
            {
                long id = (long)iter.next();
                res[i] = store.getResource(id);
            }
        }
        catch(EntityDoesNotExistException e)
        {
            throw new BackendException("inconsistent data", e);
        }
        return res;
    }

    private static IntKeyMap makeIntKeyMap(int size)
    {
        if(size <= 0)
        {
            return new IntKeyOpenHashMap();
        }
        else
        {
            return new IntKeyOpenHashMap(size);
        }
    }

    private static IntSet makeIntSet(int size)
    {
        if(size <= 0)
        {
            return new IntOpenHashSet();
        }
        else
        {
            return new IntOpenHashSet(size);
        }
    }

    // relation invertion -------------------------------------------------------------------------

    /** Represents a reversed relation. */
    private class InvertedRelation
        implements Relation
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
            synchronized(RelationImpl.this)
            {
                return RelationImpl.get(invRel, r, store);
            }
        }

        /**
         * {@inheritDoc}
         */
        public LongSet get(long id)
        {
            synchronized(RelationImpl.this)
            {
                return RelationImpl.get(invRel, id);
            }
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
            synchronized(RelationImpl.this)
            {
                return RelationImpl.getAvgMappingSize(invRel, resourceIdPairsNum);
            }
        }

        public long[][] getPairs()
        {
            synchronized(RelationImpl.this)
            {
                return RelationImpl.getPairs(invRel);
            }
        }
    }
}
