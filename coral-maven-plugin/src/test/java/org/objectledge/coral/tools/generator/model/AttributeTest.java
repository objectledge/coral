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

import java.util.Date;

import org.jmock.Mock;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.store.Resource;
import org.objectledge.utils.LedgeTestCase;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: AttributeTest.java,v 1.1 2004-03-25 15:20:21 fil Exp $
 */
public class AttributeTest extends LedgeTestCase
{
    private Mock mockSchema;
    private Schema schema;
    private Mock mockAttributeClass;
    private AttributeClass attributeClass;

    private Attribute attribute;
    
    private Mock mockResourceClass;
    private ResourceClass resourceClass;
    
    public void setUp()
    {
        mockSchema = mock(Schema.class);
        schema = (Schema)mockSchema.proxy();
        mockAttributeClass = mock(AttributeClass.class);
        attributeClass = (AttributeClass)mockAttributeClass.proxy();
        
        attribute = new Attribute(schema, "attribute", attributeClass, null, 0);
        
        mockResourceClass = mock(ResourceClass.class);
        resourceClass = (ResourceClass)mockResourceClass.proxy();
    }
    
    public void testGetAttributeClass()
    {
        assertEquals(attributeClass, attribute.getAttributeClass());
    }
    
    public void testDomain()
    {
        assertNull(attribute.getDomain());
        attribute.setDomain("[A-Z]*");
        assertEquals("[A-Z]*", attribute.getDomain());
    }
    
    public void testJavaClassInteger()
        throws Exception
    {
        mockAttributeClass.stub().method("getJavaClassName").will(returnValue("java.lang.Integer"));
        mockAttributeClass.stub().method("getJavaClass").will(returnValue(Integer.class));
        assertTrue(attribute.isPrimitive());
        assertEquals("int", attribute.getJavaType());
        assertEquals("int", attribute.getFQJavaType());
    }

    public void testJavaClassString()
        throws Exception
    {
        mockAttributeClass.stub().method("getJavaClassName").will(returnValue("java.lang.String"));
        mockAttributeClass.stub().method("getJavaClass").will(returnValue(String.class));
        assertFalse(attribute.isPrimitive());
        assertEquals("String", attribute.getJavaType());
        assertEquals("java.lang.String", attribute.getFQJavaType());
    }
    
    public void testJavaClassDate()
        throws Exception
    {
        mockAttributeClass.stub().method("getJavaClassName").will(returnValue("java.util.Date"));
        mockAttributeClass.stub().method("getJavaClass").will(returnValue(Date.class));
        assertFalse(attribute.isPrimitive());
        assertEquals("Date", attribute.getJavaType());
        assertEquals("java.util.Date", attribute.getFQJavaType());
    }

    public void testJavaClassResource()
        throws Exception
    {
        mockAttributeClass.stub().method("getJavaClassName").
            will(returnValue("org.objectledge.coral.store.Resource"));
        mockAttributeClass.stub().method("getJavaClass").will(returnValue(Resource.class));
        assertFalse(attribute.isPrimitive());
        assertEquals("Resource", attribute.getJavaType());
        assertEquals("org.objectledge.coral.store.Resource", attribute.getFQJavaType());
    }        

    public void testJavaClassResourceWithDomain()
        throws Exception
    {
        attribute.setDomain("RC");
        mockSchema.stub().method("getResourceClass").with(eq("RC")).will(returnValue(resourceClass));
        mockAttributeClass.stub().method("getJavaClassName").will(returnValue("org.objectledge.coral.store.Resource"));
        mockAttributeClass.stub().method("getJavaClass").will(returnValue(Resource.class));
        mockResourceClass.stub().method("getFQInterfaceClassName").will(returnValue("org.objectledge.coral.datatypes.Node"));
        assertFalse(attribute.isPrimitive());
        assertEquals("Node", attribute.getJavaType());
        assertEquals("org.objectledge.coral.datatypes.Node", attribute.getFQJavaType());
    }
    
    public void testFlags()
    {
        assertEquals(0, attribute.getFlags());
        assertTrue(attribute.isConcrete());    
        assertFalse(attribute.isRequired());    
        assertFalse(attribute.isReadonly());

        attribute.setFlags(AttributeFlags.BUILTIN);
        assertFalse(attribute.isConcrete());    
        assertFalse(attribute.isRequired());    
        assertFalse(attribute.isReadonly());

        attribute.setFlags(AttributeFlags.SYNTHETIC);
        assertFalse(attribute.isConcrete());    
        assertFalse(attribute.isRequired());    
        assertFalse(attribute.isReadonly());

        attribute.setFlags(AttributeFlags.REQUIRED);
        assertTrue(attribute.isConcrete());    
        assertTrue(attribute.isRequired());    
        assertFalse(attribute.isReadonly());
            
        attribute.setFlags(AttributeFlags.READONLY);
        assertTrue(attribute.isConcrete());    
        assertFalse(attribute.isRequired());    
        assertTrue(attribute.isReadonly());
    }
}
