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
import org.objectledge.database.persistence.PersistenceException;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: AbstractEntityTestCase.java,v 1.1 2004-02-24 13:39:15 fil Exp $
 */
public class AbstractEntityTestCase extends MockObjectTestCase
{
    private static class BlueEntity
        extends AbstractEntity
    {
        private static String[] KEY_COLUMNS = { "blue_entity_id" };
        
        private static String TABLE = "blue_entity";
     
        public BlueEntity(Persistence persistence)
        {
            super(persistence);
        }

        public BlueEntity(Persistence persistence, String name)
        {
            super(persistence, name);
        }
        
        public String[] getKeyColumns()
        {
            return KEY_COLUMNS;
        }

        public String getTable()
        {
            return TABLE;
        }
    }

    private static class RedEntity
        extends AbstractEntity
    {
        private static String[] KEY_COLUMNS = { "red_entity_id" };
        
        private static String TABLE = "red_entity";
     
        public RedEntity(Persistence persistence)
        {
            super(persistence);
        }

        public RedEntity(Persistence persistence, String name)
        {
            super(persistence, name);
        }
        
        public String[] getKeyColumns()
        {
            return KEY_COLUMNS;
        }

        public String getTable()
        {
            return TABLE;
        }
    }
    
    private Mock mockPersistence;
    
    private Mock mockInputRecord;
    
    private Mock mockOutputRecord;
    
    public void setUp()
    {
        mockPersistence = new Mock(Persistence.class);
        mockInputRecord = new Mock(InputRecord.class);
        mockOutputRecord = new Mock(OutputRecord.class);
    }
    
    public void testCreation()
    {
        Entity test = new BlueEntity((Persistence)mockPersistence.proxy(), "<test>");
        assertEquals(-1L, test.getId());
        assertEquals("<test>", test.getName());
    }
    
    public void testHashCode()
    {
        Entity blue1 = new BlueEntity((Persistence)mockPersistence.proxy(), "<blue 1>");
        ((AbstractEntity)blue1).setId(1);
        Entity blue2 = new BlueEntity((Persistence)mockPersistence.proxy(), "<blue 2>");
        ((AbstractEntity)blue2).setId(2);
        Entity red1 = new RedEntity((Persistence)mockPersistence.proxy(), "<red 1>");
        ((AbstractEntity)red1).setId(1);
        Entity blue1copy = new BlueEntity((Persistence)mockPersistence.proxy(), "<blue 1>");
        ((AbstractEntity)blue1copy).setId(1);

        assertFalse(blue1.hashCode() == blue2.hashCode());
        assertFalse(blue1.hashCode() == red1.hashCode());
        assertTrue(blue1.hashCode() == blue1copy.hashCode());
    }
    
    public void testEquals()
    {
        Entity blue1 = new BlueEntity((Persistence)mockPersistence.proxy(), "<blue 1>");
        ((AbstractEntity)blue1).setId(1);
        Entity blue2 = new BlueEntity((Persistence)mockPersistence.proxy(), "<blue 2>");
        ((AbstractEntity)blue2).setId(2);
        Entity red1 = new RedEntity((Persistence)mockPersistence.proxy(), "<red 1>");
        ((AbstractEntity)red1).setId(1);
        Entity blue1copy = new BlueEntity((Persistence)mockPersistence.proxy(), "<blue 1>");
        ((AbstractEntity)blue1copy).setId(1);
    
        assertFalse(blue1.equals(blue2));
        assertFalse(blue1.equals(red1));
        assertTrue(blue1.equals(blue1copy));
        assertNotSame(blue1, blue1copy);
    }
    
    public void testToString()
    {
        Entity blue1 = new BlueEntity((Persistence)mockPersistence.proxy(), "<blue 1>");
        ((AbstractEntity)blue1).setId(1);
        String s = blue1.toString();
        int i1 = s.indexOf('@');
        int i2 = s.indexOf(':');
        assertTrue(i1 > 0);
        assertTrue(i1 > 0);
        String c = s.substring(0, i1);
        String obj = s.substring(i1+1, i2);
        String id = s.substring(i2+1);
        assertEquals(blue1.getClass().getName(), c);
        assertEquals(Integer.toString(System.identityHashCode(blue1), 16), obj);
        assertEquals(Integer.toString(blue1.getClass().hashCode() ^ 0x11111111, 16), id);
    }
    
    public void testSetters()
    {
        BlueEntity blue = new BlueEntity((Persistence)mockPersistence.proxy(), "<blue 1>");
        assertFalse(blue.getSaved());
        blue.setSaved(1);
        assertTrue(blue.getSaved());
        assertEquals(1L, blue.getId());
        assertEquals("<blue 1>", blue.getName());
        blue.setName("<blue 2>");
        assertEquals("<blue 2>", blue.getName());
        blue.setId(2L);
        assertEquals(2L, blue.getId());
    }
    
    public void testStoring() throws PersistenceException
    {
        BlueEntity blue = new BlueEntity((Persistence)mockPersistence.proxy(), "<blue 1>");
        mockOutputRecord.expect(once()).method("setLong").with(eq("blue_entity_id"), eq(-1L));
        mockOutputRecord.expect(once()).method("setString").with(eq("name"), eq("<blue 1>"));
        blue.getData((OutputRecord)mockOutputRecord.proxy());
    }
    
    public void testLoading() throws PersistenceException
    {
        BlueEntity blue = new BlueEntity((Persistence)mockPersistence.proxy());
        mockInputRecord.expect(once()).method("getLong").with(eq("blue_entity_id")).will(returnValue(1L));
        mockInputRecord.expect(once()).method("getString").with(eq("name")).will(returnValue("<blue 1>"));
        blue.setData((InputRecord)mockInputRecord.proxy());
        assertEquals(1L, blue.getId());
        assertEquals("<blue 1>", blue.getName());        
    }   
}
