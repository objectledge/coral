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
package org.objectledge.coral.schema;

import org.jmock.builder.Mock;
import org.jmock.builder.MockObjectTestCase;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.OutputRecord;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.PersistenceException;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: ResourceClassInheritanceImplTest.java,v 1.1 2004-02-25 12:08:53 fil Exp $
 */
public class ResourceClassInheritanceImplTest extends MockObjectTestCase
{
    private Mock mockPersistence;
    
    private Mock mockInputRecord;
    
    private Mock mockOutputRecord;
    
    private Mock mockCoralSchema;
    
    private Mock mockParentClass;
    
    private Mock mockChildClass;

    private Mock mockChildClass2;
    
    private ResourceClass parent;
    
    private ResourceClass child;
    
    private ResourceClass child2;
    
    public void setUp()
    {
        mockPersistence = new Mock(Persistence.class);
        mockInputRecord = new Mock(InputRecord.class);
        mockOutputRecord = new Mock(OutputRecord.class);
        mockCoralSchema = new Mock(CoralSchema.class);
        mockParentClass = new Mock(ResourceClass.class, "parent");
        parent = (ResourceClass)mockParentClass.proxy();
        mockChildClass = new Mock(ResourceClass.class, "child");
        child = (ResourceClass)mockChildClass.proxy();
        mockChildClass2 = new Mock(ResourceClass.class, "child 2");
        child2 = (ResourceClass)mockChildClass2.proxy();
    }
    
    public void testCreation()
    {
        ResourceClassInheritanceImpl record = new ResourceClassInheritanceImpl((CoralSchema)mockCoralSchema.proxy(), 
            parent, child);
        assertSame(parent, record.getParent());    
        assertSame(child, record.getChild());    
    }
    
    public void testHashCode()
    {
        ResourceClassInheritanceImpl inheritance = new ResourceClassInheritanceImpl((CoralSchema)mockCoralSchema.proxy(), 
            parent, child);
        ResourceClassInheritanceImpl inheritance2 = new ResourceClassInheritanceImpl((CoralSchema)mockCoralSchema.proxy(), 
            parent, child2);
        ResourceClassInheritanceImpl inheritanceCopy = new ResourceClassInheritanceImpl((CoralSchema)mockCoralSchema.proxy(), 
            parent, child);
        mockParentClass.expect(atLeastOnce()).method("getId").will(returnValue(1L));
        mockChildClass.expect(atLeastOnce()).method("getId").will(returnValue(2L));
        mockChildClass2.expect(atLeastOnce()).method("getId").will(returnValue(3L));
        assertFalse(inheritance.hashCode() == inheritance2.hashCode());
        assertTrue(inheritance.hashCode() == inheritanceCopy.hashCode());                
    }
    
    public void testEquals()
    {
        ResourceClassInheritanceImpl inheritance = new ResourceClassInheritanceImpl((CoralSchema)mockCoralSchema.proxy(),  
            parent, child);
        ResourceClassInheritanceImpl inheritance2 = new ResourceClassInheritanceImpl((CoralSchema)mockCoralSchema.proxy(), 
            parent, child2);
        ResourceClassInheritanceImpl inheritanceCopy = new ResourceClassInheritanceImpl((CoralSchema)mockCoralSchema.proxy(), 
            parent, child);
        assertFalse(inheritance.equals(inheritance2));
        assertTrue(inheritance.equals(inheritanceCopy));
        assertNotSame(inheritance, inheritanceCopy);
        assertFalse(inheritance.equals(null));
    }
    
    public void testStoring()
        throws Exception
    {
        ResourceClassInheritanceImpl inheritance = new ResourceClassInheritanceImpl((CoralSchema)mockCoralSchema.proxy(), 
            parent, child);
        mockParentClass.expect(once()).method("getId").will(returnValue(1L));
        mockChildClass.expect(once()).method("getId").will(returnValue(2L));
        mockOutputRecord.expect(once()).method("setLong").with(eq("parent"), eq(1L));
        mockOutputRecord.expect(once()).method("setLong").with(eq("child"), eq(2L));
        inheritance.getData((OutputRecord)mockOutputRecord.proxy());
        inheritance.getTable();
        inheritance.getKeyColumns();
    }
    
    public void testLoading()
        throws Exception
    {
        ResourceClassInheritanceImpl rb = new ResourceClassInheritanceImpl((CoralSchema)mockCoralSchema.proxy());
        mockInputRecord.expect(once()).method("getLong").with(eq("parent")).will(returnValue(1L));
        mockInputRecord.expect(once()).method("getLong").with(eq("child")).will(returnValue(2L));
        mockCoralSchema.expect(once()).method("getResourceClass").with(eq(1L)).will(returnValue(parent));
        mockCoralSchema.expect(once()).method("getResourceClass").with(eq(2L)).will(returnValue(child));
        rb.setData((InputRecord)mockInputRecord.proxy());
        assertSame(parent, rb.getParent());
        assertSame(child, rb.getChild());
    }

    public void testLoadingMissingParent()
        throws Exception
    {
        ResourceClassInheritanceImpl rb = new ResourceClassInheritanceImpl((CoralSchema)mockCoralSchema.proxy());
        mockInputRecord.expect(once()).method("getLong").with(eq("parent")).will(returnValue(1L));
        mockCoralSchema.expect(once()).method("getResourceClass").with(eq(1L)).will(throwException(new EntityDoesNotExistException("missing resource class")));
        try
        {
            rb.setData((InputRecord)mockInputRecord.proxy());
            fail("exception expected");
        }
        catch(Exception e)
        {
            assertEquals(PersistenceException.class, e.getClass());
            assertEquals(EntityDoesNotExistException.class, e.getCause().getClass());
            assertEquals("missing resource class", e.getCause().getMessage());
        }
    }

    public void testLoadingMissingChild()
        throws Exception
    {
        ResourceClassInheritanceImpl rb = new ResourceClassInheritanceImpl((CoralSchema)mockCoralSchema.proxy());
        mockInputRecord.expect(once()).method("getLong").with(eq("parent")).will(returnValue(1L));
        mockInputRecord.expect(once()).method("getLong").with(eq("child")).will(returnValue(2L));
        mockCoralSchema.expect(once()).method("getResourceClass").with(eq(1L)).will(returnValue(parent));
        mockCoralSchema.expect(once()).method("getResourceClass").with(eq(2L)).will(throwException(new EntityDoesNotExistException("missing resource class")));
        try
        {
            rb.setData((InputRecord)mockInputRecord.proxy());
            fail("exception expected");
        }
        catch(Exception e)
        {
            assertEquals(PersistenceException.class, e.getClass());
            assertEquals(EntityDoesNotExistException.class, e.getCause().getClass());
            assertEquals("missing resource class", e.getCause().getMessage());
        }
    }
}
