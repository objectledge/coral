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

import org.jmock.builder.Mock;
import org.jmock.builder.MockObjectTestCase;
import org.objectledge.coral.relation.RelationModification.AddOperation;
import org.objectledge.coral.relation.RelationModification.ClearOperation;
import org.objectledge.coral.relation.RelationModification.RemoveOperation;
import org.objectledge.coral.store.Resource;

/**
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: RelationModificationTest.java,v 1.1 2004-03-01 15:58:25 zwierzem Exp $
 */
public class RelationModificationTest  extends MockObjectTestCase
{
	Mock mockResource1;
	Mock mockResource2;
	
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(RelationModificationTest.class);
    }

	public void setUp()
	{
		mockResource1 = new Mock(Resource.class);
		mockResource2 = new Mock(Resource.class);
	}

	public void testRelationModification()
	{
		RelationModification modification = new RelationModification();
		mockResource1.expect(atLeastOnce()).method("getId").will(returnValue(1L));
		mockResource2.expect(atLeastOnce()).method("getId").will(returnValue(2L));
		modification.add((Resource) mockResource1.proxy(), (Resource) mockResource2.proxy());

		mockResource1.expect(atLeastOnce()).method("getId").will(returnValue(3L));
		mockResource2.expect(atLeastOnce()).method("getId").will(returnValue(4L));
		modification.add((Resource) mockResource1.proxy(), (Resource) mockResource2.proxy());

		mockResource1.expect(atLeastOnce()).method("getId").will(returnValue(1L));
		mockResource2.expect(atLeastOnce()).method("getId").will(returnValue(2L));
		modification.remove((Resource) mockResource1.proxy(), (Resource) mockResource2.proxy());

		mockResource1.expect(atLeastOnce()).method("getId").will(returnValue(1L));
		mockResource2.expect(atLeastOnce()).method("getId").will(returnValue(4L));
		Resource[] ress = new Resource[1];
		ress[0] = (Resource) mockResource2.proxy();
		modification.add((Resource) mockResource1.proxy(), ress);
		
		mockResource1.expect(atLeastOnce()).method("getId").will(returnValue(3L));
		mockResource2.expect(atLeastOnce()).method("getId").will(returnValue(5L));
		ress[0] = (Resource) mockResource1.proxy();
		modification.add(ress, (Resource) mockResource2.proxy());
		
		mockResource1.expect(atLeastOnce()).method("getId").will(returnValue(3L));
		modification.remove((Resource) mockResource1.proxy());
		
		mockResource1.expect(atLeastOnce()).method("getId").will(returnValue(4L));
		modification.removeInv((Resource) mockResource1.proxy());
		
		Visitor1 v1 = new Visitor1();
		modification.visit(v1);
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
}
