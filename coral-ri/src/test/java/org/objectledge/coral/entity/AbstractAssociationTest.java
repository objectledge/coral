// 
// Copyright (c) 2003, Caltha - Gajda, Krzewski, Mach, Potempski Sp.J. 
// All rights reserved. 
// 
// Redistribution and use in source and binary forms, with or without modification,  
// are permitted provided that the following conditions are met: 
//  
// * Redistributions of source code must retain the above copyright notice,  
//	 this list of conditions and the following disclaimer. 
// * Redistributions in binary form must reproduce the above copyright notice,  
//	 this list of conditions and the following disclaimer in the documentation  
//	 and/or other materials provided with the distribution. 
// * Neither the name of the Caltha - Gajda, Krzewski, Mach, Potempski Sp.J.  
//	 nor the names of its contributors may be used to endorse or promote products  
//	 derived from this software without specific prior written permission. 
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
package org.objectledge.coral.entity;

import org.jmock.builder.Mock;
import org.jmock.builder.MockObjectTestCase;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.OutputRecord;
import org.objectledge.database.persistence.Persistence;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: AbstractAssociationTest.java,v 1.1 2004-02-24 14:40:10 fil Exp $
 */
public class AbstractAssociationTest extends MockObjectTestCase
{
    private Mock mockPersistence;
    
    private Mock mockInputRecord;
    
    private Mock mockOutputRecord;
    
    private RedBlueEntityFactory factory;
    
    public void setUp()
    {
        mockPersistence = new Mock(Persistence.class);
        mockInputRecord = new Mock(InputRecord.class);
        mockOutputRecord = new Mock(OutputRecord.class);
        factory = new RedBlueEntityFactory((Persistence)mockPersistence.proxy());
    }
    
    public void testCreation()
    {
        RedEntity red1 = factory.getRed(1);
        BlueEntity blue2 = factory.getBlue(2);
        RedBlueAssociation as = new RedBlueAssociation((Persistence)mockPersistence.proxy(), factory, 
            red1, blue2);
        assertEquals(1, as.getRed().getId());    
        assertEquals(2, as.getBlue().getId());        
    }
    
    public void testHashCode()
    {
        RedEntity red1 = factory.getRed(1);
        BlueEntity blue1 = factory.getBlue(1);
        BlueEntity blue2 = factory.getBlue(2);
        RedBlueAssociation red1blue1 = new RedBlueAssociation((Persistence)mockPersistence.proxy(), factory, 
            red1, blue1);
        RedBlueAssociation red1blue2 = new RedBlueAssociation((Persistence)mockPersistence.proxy(), factory, 
            red1, blue2);
        RedBlueAssociation red1blue1copy = new RedBlueAssociation((Persistence)mockPersistence.proxy(), factory, 
            red1, blue1);
        assertFalse(red1blue1.hashCode() == red1blue2.hashCode());
        assertTrue(red1blue1.hashCode() == red1blue1copy.hashCode());                
    }
    
    public void testEquals()
    {
        RedEntity red1 = factory.getRed(1);
        BlueEntity blue1 = factory.getBlue(1);
        BlueEntity blue2 = factory.getBlue(2);
        RedBlueAssociation red1blue1 = new RedBlueAssociation((Persistence)mockPersistence.proxy(), factory, 
            red1, blue1);
        RedBlueAssociation red1blue2 = new RedBlueAssociation((Persistence)mockPersistence.proxy(), factory, 
            red1, blue2);
        RedBlueAssociation red1blue1copy = new RedBlueAssociation((Persistence)mockPersistence.proxy(), factory, 
            red1, blue1);
        assertFalse(red1blue1.equals(red1blue2));
        assertTrue(red1blue1.equals(red1blue1copy));
        assertNotSame(red1blue1, red1blue1copy);
        assertFalse(red1blue1.equals(null));
    }
    
    public void testStoring()
        throws Exception
    {
        RedEntity red1 = factory.getRed(1);
        BlueEntity blue1 = factory.getBlue(1);
        RedBlueAssociation red1blue1 = new RedBlueAssociation((Persistence)mockPersistence.proxy(), factory, 
            red1, blue1);
        mockOutputRecord.expect(once()).method("setLong").with(eq("blue_id"), eq(1L));
        mockOutputRecord.expect(once()).method("setLong").with(eq("red_id"), eq(1L));
        red1blue1.getData((OutputRecord)mockOutputRecord.proxy());
        red1blue1.getTable();
        red1blue1.getKeyColumns();
    }
    
    public void testLoading()
        throws Exception
    {
        RedBlueAssociation rb = new RedBlueAssociation((Persistence)mockPersistence.proxy(), factory);
        mockInputRecord.expect(once()).method("getLong").with(eq("blue_id")).will(returnValue(1L));
        mockInputRecord.expect(once()).method("getLong").with(eq("red_id")).will(returnValue(1L));
        rb.setData((InputRecord)mockInputRecord.proxy());
        assertEquals(1L, rb.getRed().getId());
        assertEquals(1L, rb.getBlue().getId());
    }
}
