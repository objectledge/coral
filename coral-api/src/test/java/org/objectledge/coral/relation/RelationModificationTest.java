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

import org.jmock.Mock;
import org.objectledge.coral.relation.RelationModification.AddOperation;
import org.objectledge.coral.relation.RelationModification.ClearOperation;
import org.objectledge.coral.relation.RelationModification.RemoveOperation;
import org.objectledge.coral.store.Resource;
import org.objectledge.utils.LedgeTestCase;

/**
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: RelationModificationTest.java,v 1.4 2004-05-28 10:04:04 fil Exp $
 */
public class RelationModificationTest  extends LedgeTestCase
{
	private Mock mockResource1;
	private Mock mockResource2;
	
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(RelationModificationTest.class);
    }

	public void setUp()
	{
		mockResource1 = mock(Resource.class);
		mockResource2 = mock(Resource.class);
	}

	public void testRelationModification()
	{
		RelationModification modification = new RelationModification();
		mockResource1.expects(atLeastOnce()).method("getId").will(returnValue(1L));
		mockResource2.expects(atLeastOnce()).method("getId").will(returnValue(2L));
		modification.add((Resource) mockResource1.proxy(), (Resource) mockResource2.proxy());

		mockResource1.expects(atLeastOnce()).method("getId").will(returnValue(3L));
		mockResource2.expects(atLeastOnce()).method("getId").will(returnValue(4L));
		modification.add((Resource) mockResource1.proxy(), (Resource) mockResource2.proxy());

		mockResource1.expects(atLeastOnce()).method("getId").will(returnValue(1L));
		mockResource2.expects(atLeastOnce()).method("getId").will(returnValue(2L));
		modification.remove((Resource) mockResource1.proxy(), (Resource) mockResource2.proxy());

		mockResource1.expects(atLeastOnce()).method("getId").will(returnValue(1L));
		mockResource2.expects(atLeastOnce()).method("getId").will(returnValue(4L));
		Resource[] ress = new Resource[1];
		ress[0] = (Resource) mockResource2.proxy();
		modification.add((Resource) mockResource1.proxy(), ress);
		
		mockResource1.expects(atLeastOnce()).method("getId").will(returnValue(3L));
		mockResource2.expects(atLeastOnce()).method("getId").will(returnValue(5L));
		ress[0] = (Resource) mockResource1.proxy();
		modification.add(ress, (Resource) mockResource2.proxy());
		
		mockResource1.expects(atLeastOnce()).method("getId").will(returnValue(3L));
		modification.remove((Resource) mockResource1.proxy());
		
		mockResource1.expects(atLeastOnce()).method("getId").will(returnValue(4L));
		modification.removeInv((Resource) mockResource1.proxy());
		
		Visitor1 v1 = new Visitor1();
		modification.accept(v1);
		
		modification.clear();
		Visitor2 v2 = new Visitor2();
		modification.accept(v2);
		
		modification.reset();
		Visitor3 v3 = new Visitor3();
		modification.accept(v3);
	}

	public void testModificationOperation()
	{
		RelationModification.AddOperation addOper1 =
			new RelationModification.AddOperation(new Long(1L), new Long(2L));
		RelationModification.AddOperation addOper2 =
			new RelationModification.AddOperation(new Long(1L), new Long(2L));
		assertEquals(addOper1, addOper2);
		assertEquals(addOper1.hashCode(), addOper2.hashCode());

		RelationModification.ClearOperation clearOper1 = new RelationModification.ClearOperation();
		RelationModification.ClearOperation  clearOper2 = new RelationModification.ClearOperation();
		assertEquals(clearOper1, clearOper2);
		assertEquals(clearOper1.hashCode(), clearOper2.hashCode());

		RelationModification.RemoveOperation remOper1 =
			new RelationModification.RemoveOperation(new Long(1L), new Long(2L));
		RelationModification.RemoveOperation remOper2 =
			new RelationModification.RemoveOperation(new Long(1L), new Long(2L));
		assertEquals(remOper1, remOper2);
		assertEquals(remOper1.hashCode(), remOper2.hashCode());
		
		assertTrue(remOper1.equals(addOper2));
		
		remOper1 = new RelationModification.RemoveOperation(new Long(1L), null);
		remOper2 = new RelationModification.RemoveOperation(new Long(1L), null);
		assertEquals(remOper1, remOper2);
		assertEquals(remOper1.hashCode(), remOper2.hashCode());

		remOper2 = new RelationModification.RemoveOperation(null, new Long(1L));
		assertFalse(remOper1.equals(remOper2));
		assertFalse(remOper1.hashCode() == remOper2.hashCode());
		
		try
		{
			remOper2 = new RelationModification.RemoveOperation(null, null);
			fail("exception expected");
		}
		catch(IllegalArgumentException e)
		{
			// ok!
		}
	}
	
	private class Visitor1 implements RelationModification.ModificationOperationVisitor
	{
		private int count = 0; 
		
        /**
         * {@inheritDoc}
         */
        public void visit(ClearOperation oper)
        {
			count++;
        	fail("should not be called");
        }

        /**
         * {@inheritDoc}
         */
        public void visit(AddOperation oper)
        {
			count++;
			switch(count)
			{
				case 1: assertEquals(oper.getId1(), 1L);            
						assertEquals(oper.getId2(), 2L);
						break;
				case 2: assertEquals(oper.getId1(), 3L);            
						assertEquals(oper.getId2(), 4L);
						break;
				case 4: assertEquals(oper.getId1(), 1L);            
						assertEquals(oper.getId2(), 4L);
						break;
				case 5: assertEquals(oper.getId1(), 3L);            
						assertEquals(oper.getId2(), 5L);
						break;
				default: fail("too many add operations");
			}
        }

        /**
         * {@inheritDoc}
         */
        public void visit(RemoveOperation oper)
        {
			count++;
			switch(count)
			{
				case 3: assertEquals(oper.getId1(), 1L);            
						assertEquals(oper.getId2(), 2L);
						
						oper.invert();
						assertEquals(oper.getId1(), 2L);            
						assertEquals(oper.getId2(), 1L);
						break;
				case 6: assertEquals(oper.getId1(), 3L);            
						assertFalse(oper.hasId2());
						break;
				case 7: assertFalse(oper.hasId1());            
						assertEquals(oper.getId2(), 4L);
						break;
				default: fail("too many remove operations");
			}
        }
	}

	private class Visitor2 implements RelationModification.ModificationOperationVisitor
	{
		private int count = 0; 
		
		/**
		 * {@inheritDoc}
		 */
		public void visit(ClearOperation oper)
		{
			count++;
			assertEquals(count, 1);
		}

		/**
		 * {@inheritDoc}
		 */
		public void visit(AddOperation oper)
		{
			count++;
			fail("should not be called");
		}

		/**
		 * {@inheritDoc}
		 */
		public void visit(RemoveOperation oper)
		{
			count++;
			fail("should not be called");
		}
	}


	private class Visitor3 implements RelationModification.ModificationOperationVisitor
	{
		private int count = 0; 
		
		/**
		 * {@inheritDoc}
		 */
		public void visit(ClearOperation oper)
		{
			count++;
			fail("should not be called");
		}

		/**
		 * {@inheritDoc}
		 */
		public void visit(AddOperation oper)
		{
			count++;
			fail("should not be called");
		}

		/**
		 * {@inheritDoc}
		 */
		public void visit(RemoveOperation oper)
		{
			count++;
			fail("should not be called");
		}
	}
}
