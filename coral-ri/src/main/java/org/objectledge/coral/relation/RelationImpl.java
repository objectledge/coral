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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.objectledge.coral.BackendException;
import org.objectledge.coral.entity.AbstractEntity;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.Resource;
import org.objectledge.database.persistence.Persistence;

/**
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: RelationImpl.java,v 1.4 2004-02-23 12:26:59 zwierzem Exp $
 */
public class RelationImpl
extends AbstractEntity
implements Relation
{
	/** Store is used to retrieve resources. */
	private CoralStore store;
	
    /** Map r1 -&gt; set of r2. */
    private Map rel = new HashMap();

    /** Map r2 -&gt; set of r1. */
    private Map invRel = new HashMap();

    // initialization -----------------------------------------------------------------------------

    /**
     * Creates a relationship from provided definition.
     *
     * <p>An array of two element arrays is expected, each element containting
     * a single definition entry.</p>
     *
     * @param persistence the persistence system
     * @param store used to retrieve resources
     * @param name name of the relation
     * @param def the relationship definition
     */
    public RelationImpl(Persistence persistence, CoralStore store, String name, long[][] def)
    {
    	super(persistence, name);
    	
    	this.store = store;
    	
        Set set1 = new HashSet(def.length/2);
        Set set2 = new HashSet(def.length/2);

        for(int i=0; i<def.length; i++)
        {
            set1.add(new Long(def[i][0]));
            set2.add(new Long(def[i][1]));
        }

        rel = new HashMap((int)(set1.size()*1.5));
        invRel = new HashMap((int)(set2.size()*1.5));

        for(int i=0; i<def.length; i++)
        {
            Long r1k = new Long(def[i][0]);
            Long r2k = new Long(def[i][1]);

            set1 = maybeCreateSet(rel, r1k);
            set2 = maybeCreateSet(invRel, r2k);

            set1.add(r2k);
            set2.add(r1k);
        }
    }

    // public api ---------------------------------------------------------------------------------

	private ReverseRelation reverseRelation = new ReverseRelation(); 

    /**
     * {@inheritDoc}
     */
    public Relation getReverse()
    {
        return reverseRelation;
    }

	/**
	 * {@inheritDoc}
	 */
	public boolean isReverse()
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
    public long[] get(long id)
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

    // peristence api -----------------------------------------------------------------------------

	/** The key columns. */
	private static final String[] KEY_COLUMNS = { "relation_id" };

	/**
	 * {@inheritDoc}
	 */
	public String getTable()
	{
		return "arl_relation";
	}
    
	/**
	 * {@inheritDoc}
	 */
	public String[] getKeyColumns()
	{
		return KEY_COLUMNS;
	}           

    // implementation -----------------------------------------------------------------------------

    private static int initialSetCapacity = 128;

    /**
     * Returns a set for a given id key and relation, maybe creates it
     */
    private Set maybeCreateSet(Map relation, long id)
    {
        Long idk = new Long(id);
        return maybeCreateSet(relation, idk, initialSetCapacity);
    }

    /**
     * Returns a set for a given id key and relation, maybe creates it
     */
    private Set maybeCreateSet(Map relation, long id, int initialCapacity)
    {
        Long idk = new Long(id);
        return maybeCreateSet(relation, idk, initialCapacity);
    }

    /**
     * Returns a set for a given id key and relation, maybe creates it.
     */
    private Set maybeCreateSet(Map relation, Long idk)
    {
        return maybeCreateSet(relation, idk, initialSetCapacity);
    }

    /**
     * Returns a set for a given id key and relation, maybe creates it.
     */
    private Set maybeCreateSet(Map relation, Long idk, int initialCapacity)
    {
        Set set = (Set)relation.get(idk);
        if(set == null)
        {
            set = new HashSet(initialCapacity);
            relation.put(idk, set);
        }
        return set;
    }

	/**
	 * Returns resources referenced by a given resource in the relation.
	 */
	private Resource[] get(Map relation, Resource r)
	{
		Set set = (Set)relation.get(new Long(r.getId()));
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
	private long[] get(Map relation, long id)
	{
		Set set = (Set)relation.get(new Long(id));
		if(set != null)
		{
			long[] ids = new long[set.size()];
			int i = 0;
			for (Iterator iter = set.iterator(); iter.hasNext(); i++)
			{
				Long element = (Long) iter.next();
				ids[i] = element.longValue();
			}
			return ids;
		}
		else
		{
			return new long[0];
		}
	}


    /**
     * Returns an array of Resources with given identifiers.
     *
     * @param a indentifier array.
     * @return Resource array.
     */
    private Resource[] instantiate(Set a)
    {
        Resource[] res = new Resource[a.size()];
        try
        {
            int i = 0;
            for (Iterator iter = a.iterator(); iter.hasNext(); i++)
            {
                Long id = (Long) iter.next();
                res[i] = store.getResource(id.longValue());
            }
        }
        catch(EntityDoesNotExistException e)
        {
            throw new BackendException("inconsistent data", e);
        }
        return res;
    }

	/** Represents a reversed relation. */    
    private class ReverseRelation implements Relation
    {
        /**
         * {@inheritDoc}
         */
        public Relation getReverse()
        {
            return RelationImpl.this;
        }

        /**
         * {@inheritDoc}
         */
        public boolean isReverse()
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
        public long[] get(long id)
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
         * {@inheritDoc}
         */
        public String getName()
        {
			return RelationImpl.this.getName();
        }
    }
}
