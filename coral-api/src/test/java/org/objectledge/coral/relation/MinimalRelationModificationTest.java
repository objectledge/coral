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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jmock.Mock;
import org.objectledge.coral.store.Resource;
import org.objectledge.utils.LedgeTestCase;

/**
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: MinimalRelationModificationTest.java,v 1.4 2005-01-18 09:31:38 rafal Exp $
 */
public class MinimalRelationModificationTest extends LedgeTestCase
{
	private Mock mockResource1 = mock(Resource.class);
	private Mock mockResource2 = mock(Resource.class);
	
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(MinimalRelationModificationTest.class);
    }

    public void testMinimalRelationModification()
    {
    	MinimalRelationModification minMod = 
    		new MinimalRelationModification(getRelationModification(), new MockRelation(false));
		
		long[][] added = minMod.getAdded();
		// 3:4
		// 0:4
		assertEquals(added.length, 2);
		if(added[0][0] == 0) assertEquals(added[1][0], 3);
		if(added[0][0] == 3) assertEquals(added[1][0], 0);
		assertEquals(added[0][1], 4);
		assertEquals(added[1][1], 4);

		
		long[][] removed = minMod.getRemoved();
		// 1:6
		// 2:6
		// 3:6
		assertEquals(removed.length, 3);
		if(removed[0][0] == 1)
		{
			if(removed[1][0] == 2) assertEquals(removed[2][0], 3);
			if(removed[1][0] == 3) assertEquals(removed[2][0], 2);
    	} 
		if(removed[0][0] == 2)
		{
			if(removed[1][0] == 1) assertEquals(removed[2][0], 3);
			if(removed[1][0] == 3) assertEquals(removed[2][0], 1);
		} 
		if(removed[0][0] == 3)
		{
			if(removed[1][0] == 1) assertEquals(removed[2][0], 2);
			if(removed[1][0] == 2) assertEquals(removed[2][0], 1);
		}
		assertEquals(removed[0][1], 6);
		assertEquals(removed[1][1], 6);
		assertEquals(removed[2][1], 6);
    }

	public void testClear()
	{
		RelationModification modification = getRelationModification();
		
		// clear
		modification.clear();

		// add 3:4
		mockResource1.expects(once()).method("getIdObject").will(returnValue(new Long(3L)));
		mockResource2.expects(once()).method("getIdObject").will(returnValue(new Long(4L)));
		modification.add((Resource) mockResource1.proxy(), (Resource) mockResource2.proxy());

		// add 0:4
		mockResource1.expects(once()).method("getIdObject").will(returnValue(new Long(0L)));
		mockResource2.expects(once()).method("getIdObject").will(returnValue(new Long(4L)));
		modification.add((Resource) mockResource1.proxy(), (Resource) mockResource2.proxy());

		// add 1:5 - add existant but cleared
		mockResource1.expects(once()).method("getIdObject").will(returnValue(new Long(1L)));
		mockResource2.expects(once()).method("getIdObject").will(returnValue(new Long(5L)));
		modification.add((Resource) mockResource1.proxy(), (Resource) mockResource2.proxy());

		// rem 1:4 - remove existant (but cleared)
		mockResource1.expects(once()).method("getIdObject").will(returnValue(new Long(1L)));
		mockResource2.expects(once()).method("getIdObject").will(returnValue(new Long(4L)));
		modification.remove((Resource) mockResource1.proxy(), (Resource) mockResource2.proxy());

		// rem 3:4 - remove added after clear
		mockResource1.expects(once()).method("getIdObject").will(returnValue(new Long(3L)));
		mockResource2.expects(once()).method("getIdObject").will(returnValue(new Long(4L)));
		modification.remove((Resource) mockResource1.proxy(), (Resource) mockResource2.proxy());

		MinimalRelationModification minMod = 
			new MinimalRelationModification(modification, new MockRelation(false));
		
		assertTrue(minMod.getClear());
		
		long[][] added = minMod.getAdded();
		// 0:4
		// 1:5
		assertEquals(added.length, 2);
		if(added[0][0] == 0)
		{
			assertEquals(added[0][1], 4);
			assertEquals(added[1][0], 1);
			assertEquals(added[1][1], 5);
		}
		if(added[0][0] == 1)
		{
			assertEquals(added[0][1], 5);
			assertEquals(added[1][0], 0);
			assertEquals(added[1][1], 4);
		} 

		long[][] removed = minMod.getRemoved();
		// empty - was cleared
		assertEquals(removed.length, 0);
	}

    
	public void testMinimalRelationModificationInverted()
	{
		Relation relation = new MockRelation(true);
		MinimalRelationModification minMod = 
			new MinimalRelationModification(getRelationModification(), relation.getInverted());
		
		long[][] added = minMod.getAdded();
		// 3:4
		// 0:4
		assertEquals(added.length, 2);
		if(added[0][1] == 0) assertEquals(added[1][1], 3);
		if(added[0][1] == 3) assertEquals(added[1][1], 0);
		assertEquals(added[0][0], 4);
		assertEquals(added[1][0], 4);

		
		long[][] removed = minMod.getRemoved();
		// 1:6
		// 2:6
		// 3:6
		assertEquals(removed.length, 3);
		if(removed[0][1] == 1)
		{
			if(removed[1][1] == 2) assertEquals(removed[2][1], 3);
			if(removed[1][1] == 3) assertEquals(removed[2][1], 2);
		} 
		if(removed[0][1] == 2)
		{
			if(removed[1][1] == 1) assertEquals(removed[2][1], 3);
			if(removed[1][1] == 3) assertEquals(removed[2][1], 1);
		} 
		if(removed[0][1] == 3)
		{
			if(removed[1][1] == 1) assertEquals(removed[2][1], 2);
			if(removed[1][1] == 2) assertEquals(removed[2][1], 1);
		}
		assertEquals(removed[0][0], 6);
		assertEquals(removed[1][0], 6);
		assertEquals(removed[2][0], 6);
	}

	private RelationModification getRelationModification()
	{
		RelationModification modification = new RelationModification();
		
		// add 1:2
		mockResource1.expects(once()).method("getIdObject").will(returnValue(new Long(1L)));
		mockResource2.expects(once()).method("getIdObject").will(returnValue(new Long(2L)));
		modification.add((Resource) mockResource1.proxy(), (Resource) mockResource2.proxy());

		// add 3:6 - existing add
		mockResource1.expects(once()).method("getIdObject").will(returnValue(new Long(3L)));
		mockResource2.expects(once()).method("getIdObject").will(returnValue(new Long(6L)));
		modification.add((Resource) mockResource1.proxy(), (Resource) mockResource2.proxy());

		// add 2:4
		mockResource1.expects(once()).method("getIdObject").will(returnValue(new Long(2L)));
		mockResource2.expects(once()).method("getIdObject").will(returnValue(new Long(4L)));
		modification.add((Resource) mockResource1.proxy(), (Resource) mockResource2.proxy());

		// rem 1:2 - remove added
		mockResource1.expects(once()).method("getIdObject").will(returnValue(new Long(1L)));
		mockResource2.expects(once()).method("getIdObject").will(returnValue(new Long(2L)));
		modification.remove((Resource) mockResource1.proxy(), (Resource) mockResource2.proxy());

		// rem 1:8 - remove non existiant
		mockResource1.expects(once()).method("getIdObject").will(returnValue(new Long(1L)));
		mockResource2.expects(once()).method("getIdObject").will(returnValue(new Long(8L)));
		modification.remove((Resource) mockResource1.proxy(), (Resource) mockResource2.proxy());

		// add 3:[4]
		mockResource1.expects(once()).method("getIdObject").will(returnValue(new Long(3L)));
		mockResource2.expects(once()).method("getIdObject").will(returnValue(new Long(4L)));
		Resource[] ress = new Resource[1];
		ress[0] = (Resource) mockResource2.proxy();
		modification.add((Resource) mockResource1.proxy(), ress);

		// add [0]:4
		mockResource1.expects(once()).method("getIdObject").will(returnValue(new Long(0L)));
		mockResource2.expects(once()).method("getIdObject").will(returnValue(new Long(4L)));
		ress = new Resource[1];
		ress[0] = (Resource) mockResource1.proxy();
		modification.add(ress, (Resource) mockResource2.proxy());
		
        mockResource1.expects(once()).method("getIdObject").will(returnValue(new Long(0L)));
        mockResource2.expects(once()).method("getIdObject").will(returnValue(new Long(4L)));
		// add 0:4 - duplicate add
		modification.add((Resource) mockResource1.proxy(), (Resource) mockResource2.proxy());

		// rem 2:[] - remove many including added
		mockResource1.expects(once()).method("getIdObject").will(returnValue(new Long(2L)));
		modification.remove((Resource) mockResource1.proxy());

		// rem []:6
		mockResource1.expects(once()).method("getIdObject").will(returnValue(new Long(6L)));
		modification.removeInv((Resource) mockResource1.proxy());

		// add 2:5 - add removed
		mockResource1.expects(once()).method("getIdObject").will(returnValue(new Long(2L)));
		mockResource2.expects(once()).method("getIdObject").will(returnValue(new Long(5L)));
		modification.add((Resource) mockResource1.proxy(), (Resource) mockResource2.proxy());

		return modification;		
	}


    private class MockRelation implements Relation
    {
    	private Map rel = new HashMap();
		private Map invRel = new HashMap();
    	private Relation inverted = new InvertedRelation(); 
    	
    	public MockRelation(boolean invertData)
    	{
    		rel.put(new Long(1L), 
    		new HashSet(Arrays.asList(new Long[] { new Long(4L), new Long(5L), new Long(6L) })));
			rel.put(new Long(2L), 
			new HashSet(Arrays.asList(new Long[] { new Long(5L), new Long(6L) })));
			rel.put(new Long(3L), 
			new HashSet(Arrays.asList(new Long[] { new Long(6L) })));
    		
			invRel.put(new Long(4L), 
			new HashSet(Arrays.asList(new Long[] { new Long(1L) })));
			invRel.put(new Long(5L), 
			new HashSet(Arrays.asList(new Long[] { new Long(1L), new Long(2L) })));
			invRel.put(new Long(6L), 
			new HashSet(Arrays.asList(new Long[] { new Long(1L), new Long(2L), new Long(3L) })));
			
			if(invertData)
			{
				Map temp = rel;
				rel = invRel;
				invRel = temp;
			}
    	}
    	
        /**
         * {@inheritDoc}
         */
        public Relation getInverted()
        {
            return inverted;
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
            return null;
        }

        /**
         * {@inheritDoc}
         */
        public Set get(long id)
        {
            return getSet(rel, id);
        }

        /**
         * {@inheritDoc}
         */
        public boolean hasRef(Resource r, Resource rInv)
        {
            return false;
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
        public float getAvgMappingSize()
        {
            return 0F;
        }

        /**
         * {@inheritDoc}
         */
        public long getId()
        {
            return 0;
        }
        
        /**
         * Returns the numerical identifier of the entity as a Java object.
         * 
         * @return the numerical identifier of the entity as a Java object.
         */
        public Long getIdObject()
        {
            return new Long(0);
        }

        /**
         * Returns the numerical identifier of the entity as a string.
         * 
         * @return the numerical identifier of the entity as a string.
         */
        public String getIdString()
        {
            return "0";
        }
        
        /**
         * {@inheritDoc}
         */
        public String getName()
        {
            return null;
        }
        
        private Set getSet(Map relation, long id)
        {
			Set set = (Set) relation.get(new Long(id));
			if(set != null)
			{
				return Collections.unmodifiableSet(set);
			}
			else
			{
				return Collections.EMPTY_SET;
			}
        }
        
		/** Represents a reversed relation. */
		private class InvertedRelation implements Relation
		{
			/**
			 * {@inheritDoc}
			 */
			public Relation getInverted()
			{
				return MockRelation.this;
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
				return null;
			}

			/**
			 * {@inheritDoc}
			 */
			public Set get(long id)
			{
				return getSet(invRel, id);
			}

			/**
			 * {@inheritDoc}
			 */
			public boolean hasRef(Resource r, Resource rInv)
			{
				return false;
			}

			/**
			 * {@inheritDoc}
			 */
			public boolean hasRef(long id, long idInv)
			{
				return MockRelation.this.hasRef(idInv, id);
			}

			/**
			 * {@inheritDoc}
			 */
			public long getId()
			{
				return 0L;
			}

            /**
             * Returns the numerical identifier of the entity as a Java object.
             * 
             * @return the numerical identifier of the entity as a Java object.
             */
            public Long getIdObject()
            {
                return new Long(0L);
            }

            /**
             * Returns the numerical identifier of the entity as a string.
             * 
             * @return the numerical identifier of the entity as a string.
             */
            public String getIdString()
            {
                return "0";
            }
            
			/**
			 * {@inheritDoc}
			 */
			public String getName()
			{
				return null;
			}

			/**
			 * {@inheritDoc}
			 */
			public float getAvgMappingSize()
			{
				return 0F;
			}
		}
    }
}
