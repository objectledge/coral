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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jmock.Mock;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.entity.EntityExistsException;
import org.objectledge.coral.schema.CircularDependencyException;
import org.objectledge.coral.schema.ResourceClassFlags;
import org.objectledge.coral.schema.SchemaIntegrityException;
import org.objectledge.utils.LedgeTestCase;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: ResourceClassTest.java,v 1.4 2004-03-31 14:18:57 fil Exp $
 */
public class ResourceClassTest extends LedgeTestCase
{   
    private ResourceClass resourceClass;

    private Mock mockParentResourceClass;    
    private ResourceClass parentResourceClass;
    private Mock mockGrandParentResourceClass;    
    private ResourceClass grandParentResourceClass;
    private Mock mockAttribute1;
    private Attribute attribute1;
    private Mock mockAttribute2;
    private Attribute attribute2; 
    private Mock mockAttribute3;
    private Attribute attribute3; 

    public void setUp()
        throws Exception
    {
        resourceClass = new ResourceClass("RC", "org.objectledge.datatypes.Node", "node", 0);
        
        mockParentResourceClass = mock(ResourceClass.class, "mockParentResourceClass");
        mockParentResourceClass.stub().method("getName").will(returnValue("ParentRC"));
        parentResourceClass = (ResourceClass)mockParentResourceClass.proxy();
        mockGrandParentResourceClass = mock(ResourceClass.class, "mockGrandParentResourceClass");
        mockGrandParentResourceClass.stub().method("getName").will(returnValue("GrandParentRC"));
        mockGrandParentResourceClass.stub().method("compareTo").with(eq(parentResourceClass)).will(returnValue(-1));
        grandParentResourceClass = (ResourceClass)mockGrandParentResourceClass.proxy();
        mockAttribute1 = mock(Attribute.class, "mockAttribute1");
        mockAttribute1.stub().method("getName").will(returnValue("attribute1"));
        attribute1 = (Attribute)mockAttribute1.proxy();
        mockAttribute2 = mock(Attribute.class, "mockAttribute2");
        mockAttribute2.stub().method("getName").will(returnValue("attribute2"));
        mockAttribute2.stub().method("compareTo").with(same(attribute1)).will(returnValue(1));
        attribute2 = (Attribute)mockAttribute2.proxy();
        mockAttribute3 = mock(Attribute.class, "mockAttribute3");
        mockAttribute3.stub().method("getName").will(returnValue("attribute3"));
        mockAttribute3.stub().method("compareTo").with(same(attribute1)).will(returnValue(1));
        mockAttribute3.stub().method("compareTo").with(same(attribute2)).will(returnValue(1));
        attribute3 = (Attribute)mockAttribute3.proxy();
    }
    
    public void testGetPackageName()
    {
        assertEquals("org.objectledge.datatypes", resourceClass.getPackageName());
    }

    public void testGetImplClassName()
    {
        assertEquals("NodeImpl", resourceClass.getImplClassName());
    }

    public void testGetFQImplClassName()
    {
        assertEquals("org.objectledge.datatypes.NodeImpl", resourceClass.getFQImplClassName());
    }

    public void testGetInterfaceClassName()
    {
        assertEquals("Node", resourceClass.getInterfaceClassName());
    }

    public void testGetFQInterfaceClassName()
    {
        assertEquals("org.objectledge.datatypes.Node", resourceClass.getFQInterfaceClassName());
    }
    
    public void testSetJavaClassName()
    {
        resourceClass.setJavaClassName("Unqualified");
        assertEquals("", resourceClass.getPackageName());
        assertEquals("Unqualified", resourceClass.getInterfaceClassName());
        assertEquals("UnqualifiedImpl", resourceClass.getImplClassName());
        assertEquals("Unqualified", resourceClass.getFQInterfaceClassName());
        assertEquals("UnqualifiedImpl", resourceClass.getFQImplClassName());
        resourceClass.setJavaClassName("org.objectledge.datatypes.NodeImpl");
        assertEquals("Node", resourceClass.getInterfaceClassName());
        assertEquals("NodeImpl", resourceClass.getImplClassName());
        assertEquals("org.objectledge.datatypes.Node", resourceClass.getFQInterfaceClassName());
        assertEquals("org.objectledge.datatypes.NodeImpl", resourceClass.getFQImplClassName());
    }
    
    public void testGetDbTable()
    {
        assertEquals("node", resourceClass.getDbTable());
    }
    
    public void testSetDbTable()
    {
        resourceClass.setDbTable(null);
        assertNull(resourceClass.getDbTable());
        resourceClass.setDbTable("node2");
        assertEquals("node2", resourceClass.getDbTable());
    }
    
    public void testIsAbstract()
    {
        assertFalse(resourceClass.isAbstract());
        resourceClass.setFlags(ResourceClassFlags.ABSTRACT);
        assertTrue(resourceClass.isAbstract());
    }
    
    public void testDeclaredAttributes()
        throws Exception
    {
        assertTrue(resourceClass.getDeclaredAttributes().isEmpty());
        mockAttribute1.expect(once()).method("setDeclaringClass").with(same(resourceClass)).isVoid();
        resourceClass.addAttribute(attribute1);
        try
        {
            resourceClass.addAttribute(attribute1);
            fail("should throw");
        }
        catch(Exception e)
        {
            assertEquals(EntityExistsException.class, e.getClass());
        }
        assertSame(attribute1, resourceClass.getDeclaredAttribute("attribute1"));
        assertEquals(1, resourceClass.getDeclaredAttributes().size());
        assertSame(attribute1, resourceClass.getDeclaredAttributes().get(0));
        mockAttribute1.expect(once()).method("setDeclaringClass").with(NULL).isVoid();
        resourceClass.deleteAttribute(attribute1);
        assertTrue(resourceClass.getDeclaredAttributes().isEmpty());
        try
        {
            resourceClass.getDeclaredAttribute("attribute1");
            fail("should throw");
        }
        catch(Exception e)
        {
            assertEquals(EntityDoesNotExistException.class, e.getClass());
        }
        try
        {
            resourceClass.deleteAttribute(attribute1);
            fail("should throw");
        }
        catch(Exception e)
        {
            assertEquals(EntityDoesNotExistException.class, e.getClass());
        }
    }
    
    public void testAllAttributes()
        throws Exception
    {
        assertTrue(resourceClass.getAllAttributes().isEmpty());
        mockAttribute1.expect(once()).method("setDeclaringClass").with(same(resourceClass));
        resourceClass.addAttribute(attribute1);
        mockGrandParentResourceClass.stub().method("getAllParentClasses").will(returnValue(Collections.EMPTY_LIST));
        mockGrandParentResourceClass.stub().method("getDeclaredParentClasses").will(returnValue(Collections.EMPTY_LIST));
        mockGrandParentResourceClass.stub().method("getDeclaredAttributes").will(returnValue(Collections.singletonList(attribute2)));
        mockGrandParentResourceClass.stub().method("getDeclaredAttribute").with(eq("attribute2")).will(returnValue(attribute2));
        mockGrandParentResourceClass.stub().method("getDeclaredAttribute").with(eq("attribute3")).will(throwException(new EntityDoesNotExistException("not found")));
        mockParentResourceClass.stub().method("getAllParentClasses").will(returnValue(Collections.singletonList(grandParentResourceClass)));
        mockParentResourceClass.stub().method("getDeclaredParentClasses").will(returnValue(Collections.singletonList(grandParentResourceClass)));
        mockParentResourceClass.stub().method("getAllAttributes").will(returnValue(Collections.singletonList(attribute2)));
        mockParentResourceClass.stub().method("getDeclaredAttributes").will(returnValue(Collections.EMPTY_LIST));
        mockParentResourceClass.stub().method("getDeclaredAttribute").with(eq("attribute3")).will(throwException(new EntityDoesNotExistException("not found")));
        resourceClass.addParentClass(parentResourceClass);
        assertEquals(2, resourceClass.getAllAttributes().size());
        // entities are sorted alphabetically
        assertSame(attribute1, resourceClass.getAllAttributes().get(0));        
        assertSame(attribute2, resourceClass.getAllAttributes().get(1)); 
        assertSame(attribute1, resourceClass.getAttribute("attribute1"));       
        assertSame(attribute2, resourceClass.getAttribute("attribute2"));       
        try
        {
            resourceClass.getAttribute("attribute3");
            fail("should throw");
        }
        catch(Exception e)
        {
            assertEquals(EntityDoesNotExistException.class, e.getClass());
        }
        try
        {
            resourceClass.addAttribute(attribute2);
            fail("should throw");
        }
        catch(Exception e)
        {
            assertEquals(EntityExistsException.class, e.getClass());
        }
    }   
    
    public void testConcreteImplAttributes()
        throws Exception
    { 
        mockAttribute1.expect(once()).method("setDeclaringClass").with(same(resourceClass));
        resourceClass.addAttribute(attribute1);
        mockAttribute2.expect(once()).method("setDeclaringClass").with(same(resourceClass));
        resourceClass.addAttribute(attribute2);
        mockParentResourceClass.stub().method("getDeclaredParentClasses").will(returnValue(Collections.EMPTY_LIST));
        mockParentResourceClass.stub().method("getAllParentClasses").will(returnValue(Collections.EMPTY_LIST));
        mockParentResourceClass.stub().method("getDeclaredAttributes").will(returnValue(Collections.singletonList(attribute3)));
        mockParentResourceClass.stub().method("getAllAttributes").will(returnValue(Collections.singletonList(attribute3)));
        resourceClass.addParentClass(parentResourceClass);
        mockAttribute1.stub().method("isConcrete").will(returnValue(true));
        mockAttribute2.stub().method("isConcrete").will(returnValue(false));
        assertEquals(3, resourceClass.getAllAttributes().size());
        assertEquals(2, resourceClass.getDeclaredAttributes().size());
        assertEquals(1, resourceClass.getConcreteImplAttributes().size());
        assertSame(attribute1, resourceClass.getConcreteImplAttributes().get(0));
    }
    
    public void testConcreteAttributes()
        throws Exception
    { 
        mockAttribute1.expect(once()).method("setDeclaringClass").with(same(resourceClass));
        resourceClass.addAttribute(attribute1);
        mockAttribute2.expect(once()).method("setDeclaringClass").with(same(resourceClass));
        resourceClass.addAttribute(attribute2);
        mockAttribute3.expect(once()).method("setDeclaringClass").with(same(resourceClass));
        resourceClass.addAttribute(attribute3);
        mockAttribute1.stub().method("isConcrete").will(returnValue(true));
        mockAttribute2.stub().method("isConcrete").will(returnValue(true));
        mockAttribute3.stub().method("isConcrete").will(returnValue(false));
        mockAttribute1.stub().method("isRequired").will(returnValue(true));
        mockAttribute2.stub().method("isRequired").will(returnValue(false));
        mockAttribute3.stub().method("isRequired").will(returnValue(false));
        assertEquals(3, resourceClass.getDeclaredAttributes().size());
        assertEquals(2, resourceClass.getConcreteDeclaredAttributes().size());
        assertSame(attribute1, resourceClass.getConcreteDeclaredAttributes().get(0));        
        assertSame(attribute2, resourceClass.getConcreteDeclaredAttributes().get(1));        
        assertEquals(1, resourceClass.getConcreteRequiredAttributes().size());
        assertSame(attribute1, resourceClass.getConcreteRequiredAttributes().get(0));        
    }  
    
    public void testSortAttributes()
        throws Exception
    {  
        mockAttribute1.expect(once()).method("setDeclaringClass").with(same(resourceClass));
        resourceClass.addAttribute(attribute1);
        mockAttribute2.expect(once()).method("setDeclaringClass").with(same(resourceClass));
        resourceClass.addAttribute(attribute2);
        mockAttribute3.expect(once()).method("setDeclaringClass").with(same(resourceClass));
        resourceClass.addAttribute(attribute3);
        List order = new ArrayList();
        order.add("attribute3");
        order.add("attribute1");
        resourceClass.setAttributeOrder(order);
        assertEquals(3, resourceClass.getDeclaredAttributes().size());
        assertEquals(attribute3, resourceClass.getDeclaredAttributes().get(0));
        assertEquals(attribute1, resourceClass.getDeclaredAttributes().get(1));
        assertEquals(attribute2, resourceClass.getDeclaredAttributes().get(2));
    }
    
    public void testParentClasses()
        throws Exception
    {
        assertTrue(resourceClass.getDeclaredParentClasses().isEmpty());
        assertTrue(resourceClass.getAllParentClasses().isEmpty());
        mockParentResourceClass.stub().method("getAllAttributes").will(returnValue(Collections.EMPTY_LIST));
        mockParentResourceClass.stub().method("getDeclaredParentClasses").will(returnValue(Collections.EMPTY_LIST));
        mockParentResourceClass.stub().method("getAllParentClasses").will(returnValue(Collections.EMPTY_LIST));
        resourceClass.addParentClass(parentResourceClass);
        assertEquals(1, resourceClass.getDeclaredParentClasses().size());
        assertEquals(1, resourceClass.getAllParentClasses().size());
        resourceClass.deleteParentClass(parentResourceClass);
        assertTrue(resourceClass.getDeclaredParentClasses().isEmpty());
        assertTrue(resourceClass.getAllParentClasses().isEmpty());
        
        mockAttribute1.expect(once()).method("setDeclaringClass").with(same(resourceClass));
        mockAttribute1.stub().method("getDeclaringClass").will(returnValue(resourceClass));
        mockAttribute1.stub().method("getName").will(returnValue("attribute"));
        resourceClass.addAttribute(attribute1);
        mockAttribute2.stub().method("getDeclaringClass").will(returnValue(grandParentResourceClass));
        mockAttribute2.stub().method("getName").will(returnValue("attribute"));
        mockGrandParentResourceClass.stub().method("getAllAttributes").will(returnValue(Collections.singletonList(attribute2)));
        mockGrandParentResourceClass.stub().method("getDeclaredParentClasses").will(returnValue(Collections.EMPTY_LIST));
        mockGrandParentResourceClass.stub().method("getAllParentClasses").will(returnValue(Collections.EMPTY_LIST));
        try
        {
            resourceClass.addParentClass(grandParentResourceClass);
            fail("should throw");
        }
        catch(Exception e)
        {
            assertEquals(SchemaIntegrityException.class, e.getClass());
        }
    }
    
    public void testParentClassesCircularity()
    {
        mockParentResourceClass.stub().method("getAllAttributes").will(returnValue(Collections.EMPTY_LIST));
        mockParentResourceClass.stub().method("getDeclaredParentClasses").will(returnValue(Collections.singletonList(resourceClass)));
        mockParentResourceClass.stub().method("getAllParentClasses").will(returnValue(Collections.singletonList(resourceClass)));
        try
        {
            resourceClass.addParentClass(parentResourceClass);
            fail("should throw");
        }
        catch(Exception e)
        {
            assertEquals(CircularDependencyException.class, e.getClass());
        }
    }
    
    public void testImplParentClass()
        throws Exception
    {
        try
        {
            resourceClass.getImplParentClass();
            fail("should throw");
        }
        catch(Exception e)
        {
            assertEquals("primary wrapper generation not supported", e.getMessage());
        }
        try
        {
            resourceClass.setImplParentClass(parentResourceClass);
            fail("should throw");
        }
        catch(Exception e)
        {
            assertEquals("ParentRC is not a direct parent class of RC", e.getMessage());
        }
        mockParentResourceClass.stub().method("getDeclaredParentClasses").will(returnValue(Collections.EMPTY_LIST));
        mockParentResourceClass.stub().method("getAllParentClasses").will(returnValue(Collections.EMPTY_LIST));
        mockParentResourceClass.stub().method("getDeclaredAttributes").will(returnValue(Collections.EMPTY_LIST));
        mockParentResourceClass.stub().method("getAllAttributes").will(returnValue(Collections.EMPTY_LIST));
        resourceClass.addParentClass(parentResourceClass);
        assertEquals(parentResourceClass, resourceClass.getImplParentClass());
        mockGrandParentResourceClass.stub().method("getDeclaredParentClasses").will(returnValue(Collections.EMPTY_LIST));
        mockGrandParentResourceClass.stub().method("getAllParentClasses").will(returnValue(Collections.EMPTY_LIST));
        mockGrandParentResourceClass.stub().method("getDeclaredAttributes").will(returnValue(Collections.EMPTY_LIST));
        mockGrandParentResourceClass.stub().method("getAllAttributes").will(returnValue(Collections.EMPTY_LIST));
        resourceClass.addParentClass(grandParentResourceClass);
        
        assertEquals(grandParentResourceClass, resourceClass.getImplParentClass());
        
        resourceClass.setImplParentClass(parentResourceClass);
        assertEquals(parentResourceClass, resourceClass.getImplParentClass());                
    }
}
