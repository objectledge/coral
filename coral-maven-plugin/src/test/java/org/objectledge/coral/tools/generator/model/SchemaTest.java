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

import org.jmock.Mock;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.entity.EntityExistsException;
import org.objectledge.utils.LedgeTestCase;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: SchemaTest.java,v 1.3 2004-03-24 15:45:36 fil Exp $
 */
public class SchemaTest extends LedgeTestCase
{
    private Schema schema;
    private Mock mockResourceClassRC1;
    private ResourceClass resourceClassRC1;
    private Mock mockAttributeClassAC1;
    private AttributeClass attributeClassAC1;
    
    public void setUp()
    {
        schema = new Schema();
        mockResourceClassRC1 = mock(ResourceClass.class, "RC1");
        mockResourceClassRC1.stub().method("getName").will(returnValue("RC1"));
        resourceClassRC1 = (ResourceClass)mockResourceClassRC1.proxy();
        mockAttributeClassAC1 = mock(AttributeClass.class, "RC1");
        mockAttributeClassAC1.stub().method("getName").will(returnValue("AC1"));
        attributeClassAC1 = (AttributeClass)mockAttributeClassAC1.proxy();
    }
    
    public void testGetResourceClassNonexistent()
        throws Exception
    {
        try
        {
            schema.getResourceClass("RC1");
            fail("should throw exception");
        }
        catch(Exception e)
        {
            assertEquals(EntityDoesNotExistException.class, e.getClass());
        }        
    }
    
    public void testAddResourceClass()
        throws Exception
    {
        assertTrue(schema.getResourceClasses().isEmpty());
        schema.addResourceClass(resourceClassRC1);
        assertFalse(schema.getResourceClasses().isEmpty());
        assertSame(resourceClassRC1, schema.getResourceClass("RC1"));        
    }
    
    public void testAddResourceClassDuplicate()
        throws Exception
    {
        schema.addResourceClass(resourceClassRC1);
        try
        {
            schema.addResourceClass(resourceClassRC1);
            fail("should throw exception");
        }
        catch(Exception e)
        {
            assertEquals(EntityExistsException.class, e.getClass());
        }
    }
    
    public void testDeleteResourceClass()
        throws Exception
    {
        schema.addResourceClass(resourceClassRC1);
        assertSame(resourceClassRC1, schema.getResourceClass("RC1"));
        schema.deleteResourceClass(resourceClassRC1);        
        assertTrue(schema.getResourceClasses().isEmpty());
    }

    public void testDeleteResourceClassNonExistent()
        throws Exception
    {
        try
        {
            schema.deleteResourceClass(resourceClassRC1);        
        }
        catch(Exception e)
        {
            assertEquals(EntityDoesNotExistException.class, e.getClass());
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    public void testGetAttributeClassNonexistent()
        throws Exception
    {
        try
        {
            schema.getAttributeClass("AC1");
            fail("should throw exception");
        }
        catch(Exception e)
        {
            assertEquals(EntityDoesNotExistException.class, e.getClass());
        }        
    }
    
    public void testAddAttributeClass()
        throws Exception
    {
        assertTrue(schema.getAttributeClasses().isEmpty());
        schema.addAttributeClass(attributeClassAC1);
        assertFalse(schema.getAttributeClasses().isEmpty());
        assertSame(attributeClassAC1, schema.getAttributeClass("AC1"));        
    }
    
    public void testAddAttributeClassDuplicate()
        throws Exception
    {
        schema.addAttributeClass(attributeClassAC1);
        try
        {
            schema.addAttributeClass(attributeClassAC1);
            fail("should throw exception");
        }
        catch(Exception e)
        {
            assertEquals(EntityExistsException.class, e.getClass());
        }
    }
    
    public void testDeleteAttributeClass()
        throws Exception
    {
        schema.addAttributeClass(attributeClassAC1);
        assertSame(attributeClassAC1, schema.getAttributeClass("AC1"));
        schema.deleteAttributeClass(attributeClassAC1);        
        assertTrue(schema.getAttributeClasses().isEmpty());
    }

    public void testDeleteAttributeClassNonExistent()
        throws Exception
    {
        try
        {
            schema.deleteAttributeClass(attributeClassAC1);        
        }
        catch(Exception e)
        {
            assertEquals(EntityDoesNotExistException.class, e.getClass());
        }
    }    
}
