// 
// Copyright (c) 2003,2004 , Caltha - Gajda, Krzewski, Mach, Potempski Sp.J. 
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
package org.objectledge.coral.tools.generator.model;

import org.objectledge.utils.LedgeTestCase;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: EntityTest.java,v 1.1 2004-03-24 14:40:06 fil Exp $
 */
public class EntityTest extends LedgeTestCase
{
    private Entity entityA;
    private Entity entityB;
    private Entity entityAcopy;
    private OtherEntity otherEntity;
    
    public void setUp()
    {
        entityA = new Entity("A", 0);
        entityB = new Entity("B", 0);
        entityAcopy = new Entity("A", 0);
        otherEntity = new OtherEntity("A", 0);
    }
    
    public void testCompareTo()
    {
        assertTrue(entityA.compareTo(entityB) < 0);
        assertTrue(entityB.compareTo(entityA) > 0);
        assertTrue(entityA.compareTo(entityA) == 0);
        assertTrue(entityA.compareTo(entityAcopy) == 0);
        try
        {
            entityA.compareTo(otherEntity);
            fail("should throw exception");
        }
        catch(Exception e)
        {
            assertEquals(ClassCastException.class, e.getClass());
        }
    }
    
    public void testEquals()
    {
        assertTrue(entityA.equals(entityA));
        assertTrue(entityA.equals(entityAcopy));
        assertFalse(entityA.equals(entityB));
        try
        {
            entityA.equals(otherEntity);
            fail("should throw exception");
        }
        catch(Exception e)
        {
            assertEquals(ClassCastException.class, e.getClass());
        }
    }
    
    public void testHashCode()
    {
        assertTrue(entityA.hashCode() == entityAcopy.hashCode());
        assertFalse(entityA.hashCode() == entityB.hashCode());
    }
    
    public void testToString()
    {
        assertTrue(entityA.toString().equals(entityAcopy.toString()));
        assertFalse(entityA.toString().equals(entityB.toString()));
    }
    
    public void testFlags()
    {
        assertEquals(0, entityA.getFlags());
        entityA.setFlags(1 | 4);
        assertEquals(1 | 4, entityA.getFlags());
        assertTrue(entityA.hasFlags(1));
        assertTrue(entityA.hasFlags(4));
        assertTrue(entityA.hasFlags(1 | 4));
        assertFalse(entityA.hasFlags(2));
        assertFalse(entityA.hasFlags(1 | 2));
        assertFalse(entityA.hasFlags(2 | 4));
    }
    
    public void testName()
    {
        assertTrue(entityAcopy.equals(entityA));
        assertFalse(entityAcopy.equals(entityB));
        entityAcopy.setName("B");
        assertTrue(entityAcopy.equals(entityB));
        assertFalse(entityAcopy.equals(entityA));        
    }
    
    private static class OtherEntity extends Entity
    {
        public OtherEntity(String name, int flags)
        {
            super(name, flags);
        }
    }
}
