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
import org.objectledge.coral.CoralCore;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.OutputRecord;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.PersistenceException;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: ResourceClassInheritanceImplTest.java,v 1.2 2004-03-05 11:52:15 fil Exp $
 */
public class ResourceClassInheritanceImplTest extends MockObjectTestCase
{
    private Mock mockPersistence;
    private Persistence persistence;
    private Mock mockInputRecord;
    private InputRecord inputRecord;
    private Mock mockOutputRecord;
    private OutputRecord outputRecord;
    private Mock mockCoralSchema;
    private CoralSchema coralSchema;
    private Mock mockCoralCore;
    private CoralCore coralCore;
    private Mock mockParentClass;
    private ResourceClass parentClass;    
    private Mock mockChildClass;
    private ResourceClass childClass;
    private Mock mockChildClass2;
    private ResourceClass childClass2;
    
    public void setUp()
    {
        mockPersistence = new Mock(Persistence.class);
        persistence = (Persistence)mockPersistence.proxy();
        mockInputRecord = new Mock(InputRecord.class);
        inputRecord = (InputRecord)mockInputRecord.proxy();
        mockOutputRecord = new Mock(OutputRecord.class);
        outputRecord = (OutputRecord)mockOutputRecord.proxy();
        mockCoralSchema = new Mock(CoralSchema.class);
        coralSchema = (CoralSchema)mockCoralSchema.proxy();
        mockCoralCore = new Mock(CoralCore.class);
        coralCore = (CoralCore)mockCoralCore.proxy();
        mockCoralCore.stub().method("getSchema").will(returnValue(coralSchema));
        mockParentClass = new Mock(ResourceClass.class, "parent");
        parentClass = (ResourceClass)mockParentClass.proxy();
        mockChildClass = new Mock(ResourceClass.class, "child");
        childClass = (ResourceClass)mockChildClass.proxy();
        mockChildClass2 = new Mock(ResourceClass.class, "child 2");
        childClass2 = (ResourceClass)mockChildClass2.proxy();
    }
    
    public void testCreation()
    {
        ResourceClassInheritanceImpl record = new ResourceClassInheritanceImpl(coralCore, 
            parentClass, childClass);
        assertSame(parentClass, record.getParent());    
        assertSame(childClass, record.getChild());    
    }
    
    public void testHashCode()
    {
        ResourceClassInheritanceImpl inheritance = new ResourceClassInheritanceImpl(coralCore, 
            parentClass, childClass);
        ResourceClassInheritanceImpl inheritance2 = new ResourceClassInheritanceImpl(coralCore, 
            parentClass, childClass2);
        ResourceClassInheritanceImpl inheritanceCopy = new ResourceClassInheritanceImpl(coralCore, 
            parentClass, childClass);
        mockParentClass.expect(atLeastOnce()).method("getId").will(returnValue(1L));
        mockChildClass.expect(atLeastOnce()).method("getId").will(returnValue(2L));
        mockChildClass2.expect(atLeastOnce()).method("getId").will(returnValue(3L));
        assertFalse(inheritance.hashCode() == inheritance2.hashCode());
        assertTrue(inheritance.hashCode() == inheritanceCopy.hashCode());                
    }
    
    public void testEquals()
    {
        ResourceClassInheritanceImpl inheritance = new ResourceClassInheritanceImpl(coralCore,  
            parentClass, childClass);
        ResourceClassInheritanceImpl inheritance2 = new ResourceClassInheritanceImpl(coralCore, 
            parentClass, childClass2);
        ResourceClassInheritanceImpl inheritanceCopy = new ResourceClassInheritanceImpl(coralCore, 
            parentClass, childClass);
        assertFalse(inheritance.equals(inheritance2));
        assertTrue(inheritance.equals(inheritanceCopy));
        assertNotSame(inheritance, inheritanceCopy);
        assertFalse(inheritance.equals(null));
    }
    
    public void testStoring()
        throws Exception
    {
        ResourceClassInheritanceImpl inheritance = new ResourceClassInheritanceImpl(coralCore, 
            parentClass, childClass);
        mockParentClass.expect(once()).method("getId").will(returnValue(1L));
        mockChildClass.expect(once()).method("getId").will(returnValue(2L));
        mockOutputRecord.expect(once()).method("setLong").with(eq("parent"), eq(1L));
        mockOutputRecord.expect(once()).method("setLong").with(eq("child"), eq(2L));
        inheritance.getData(outputRecord);
        inheritance.getTable();
        inheritance.getKeyColumns();
    }
    
    public void testLoading()
        throws Exception
    {
        ResourceClassInheritanceImpl rb = new ResourceClassInheritanceImpl(coralCore);
        mockInputRecord.expect(once()).method("getLong").with(eq("parent")).will(returnValue(1L));
        mockInputRecord.expect(once()).method("getLong").with(eq("child")).will(returnValue(2L));
        mockCoralSchema.expect(once()).method("getResourceClass").with(eq(1L)).will(returnValue(parentClass));
        mockCoralSchema.expect(once()).method("getResourceClass").with(eq(2L)).will(returnValue(childClass));
        rb.setData(inputRecord);
        assertSame(parentClass, rb.getParent());
        assertSame(childClass, rb.getChild());
    }

    public void testLoadingMissingParent()
        throws Exception
    {
        ResourceClassInheritanceImpl rb = new ResourceClassInheritanceImpl(coralCore);
        mockInputRecord.expect(once()).method("getLong").with(eq("parent")).will(returnValue(1L));
        mockCoralSchema.expect(once()).method("getResourceClass").with(eq(1L)).will(throwException(new EntityDoesNotExistException("missing resource class")));
        try
        {
            rb.setData(inputRecord);
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
        ResourceClassInheritanceImpl rb = new ResourceClassInheritanceImpl(coralCore);
        mockInputRecord.expect(once()).method("getLong").with(eq("parent")).will(returnValue(1L));
        mockInputRecord.expect(once()).method("getLong").with(eq("child")).will(returnValue(2L));
        mockCoralSchema.expect(once()).method("getResourceClass").with(eq(1L)).will(returnValue(parentClass));
        mockCoralSchema.expect(once()).method("getResourceClass").with(eq(2L)).will(throwException(new EntityDoesNotExistException("missing resource class")));
        try
        {
            rb.setData(inputRecord);
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
