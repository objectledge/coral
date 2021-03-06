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
package org.objectledge.coral.tools.generator;

import java.io.Reader;

import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.ResourceClassFlags;
import org.objectledge.coral.tools.generator.model.Attribute;
import org.objectledge.coral.tools.generator.model.AttributeClass;
import org.objectledge.coral.tools.generator.model.ResourceClass;
import org.objectledge.coral.tools.generator.model.Schema;
import org.objectledge.filesystem.FileSystem;
import org.objectledge.test.LedgeTestCase;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: RMLModelLoaderTest.java,v 1.10 2004-04-29 15:54:04 fil Exp $
 */
public class RMLModelLoaderTest
    extends LedgeTestCase
{
    private Schema schema;
    private RMLModelLoader modelLoader;

    public void setUp()
    {
        schema = new Schema();
        modelLoader = new RMLModelLoader(schema);
    }

    public void testCreateAttributeClass()
        throws Exception
    {
        modelLoader.execute("CREATE ATTRIBUTE CLASS ac1 JAVA CLASS java.lang.Integer HANDLER CLASS hc1;");
        AttributeClass ac1 = schema.getAttributeClass("ac1");
        assertEquals("java.lang.Integer", ac1.getJavaClassName());
    }
    
    public void testDeleteAttributeClass()
        throws Exception
    {
        modelLoader.execute("CREATE ATTRIBUTE CLASS ac1 JAVA CLASS java.lang.Integer HANDLER CLASS hc1;");
        AttributeClass ac1 = schema.getAttributeClass("ac1");
        assertNotNull(ac1);
        modelLoader.execute("DELETE ATTRIBUTE CLASS ac1;");
        assertTrue(schema.getAttributeClasses().isEmpty());
    }

    public void testAlterAttributeClassSetName()
        throws Exception
    {
        modelLoader.execute("CREATE ATTRIBUTE CLASS ac1 JAVA CLASS java.lang.Integer HANDLER CLASS hc1;");
        AttributeClass ac1 = schema.getAttributeClass("ac1");
        assertNotNull(ac1);
        modelLoader.execute("ALTER ATTRIBUTE CLASS ac1 SET NAME ac2;");
        AttributeClass ac2 = schema.getAttributeClass("ac2");
        assertNotNull(ac2);
        assertSame(ac1, ac2);
    }

    public void testAlterAttributeClassSetJavaClass()
        throws Exception
    {
        modelLoader.execute("CREATE ATTRIBUTE CLASS ac1 JAVA CLASS java.lang.Integer HANDLER CLASS hc1;");
        AttributeClass ac1 = schema.getAttributeClass("ac1");
        assertNotNull(ac1);
        modelLoader.execute("ALTER ATTRIBUTE CLASS ac1 SET JAVA CLASS java.lang.String;");
        assertEquals("java.lang.String", ac1.getJavaClassName());
    }

    public void testCreateResourceClass()
        throws Exception
    {
        modelLoader.execute("CREATE RESOURCE CLASS rc1 JAVA CLASS org.objectledge.coral.datatypes.Node HANDLER CLASS hc1;");
        ResourceClass rc1 = schema.getResourceClass("rc1");
        assertEquals("org.objectledge.coral.datatypes.Node", rc1.getFQInterfaceClassName());
        assertEquals("org.objectledge.coral.datatypes.NodeImpl", rc1.getFQImplClassName());
        assertEquals(null, rc1.getDbTable());
    }
    
    // create with db table
    // create with flags
    // crate with superclasses
    // crate with attributes
    
    public void testDeleteResourceClass()
        throws Exception
    {
        modelLoader.execute("CREATE RESOURCE CLASS rc1 JAVA CLASS org.objectledge.coral.datatypes.Node HANDLER CLASS hc1;");
        ResourceClass rc1 = schema.getResourceClass("rc1");
        assertNotNull(rc1);
        modelLoader.execute("DELETE RESOURCE CLASS rc1;");
        assertTrue(schema.getResourceClasses().isEmpty());
    }
    
    public void testAlterResourceClassAddAttribute()
        throws Exception
    {
        modelLoader.execute("CREATE ATTRIBUTE CLASS ac1 JAVA CLASS java.lang.Integer HANDLER CLASS hc1;");
        modelLoader.execute("CREATE RESOURCE CLASS rc1 JAVA CLASS org.objectledge.coral.datatypes.Node HANDLER CLASS hc1;");
        AttributeClass ac1 = schema.getAttributeClass("ac1");
        ResourceClass rc1 = schema.getResourceClass("rc1");
        assertTrue(rc1.getDeclaredAttributes().isEmpty());
        modelLoader.execute("ALTER RESOURCE CLASS rc1 ADD ATTRIBUTE ac1 a1;");
        Attribute a1 = rc1.getDeclaredAttribute("a1");
        assertSame(rc1, a1.getDeclaringClass());
        assertSame(ac1, a1.getAttributeClass());
    }

    public void testAlterResourceClassDeleteAttribute()
        throws Exception
    {
        modelLoader.execute("CREATE ATTRIBUTE CLASS ac1 JAVA CLASS java.lang.Integer HANDLER CLASS hc1;");
        modelLoader.execute("CREATE RESOURCE CLASS rc1 JAVA CLASS org.objectledge.coral.datatypes.Node HANDLER CLASS hc1 ATTRIBUTES (ac1 a1);");
        AttributeClass ac1 = schema.getAttributeClass("ac1");
        ResourceClass rc1 = schema.getResourceClass("rc1");
        Attribute a1 = rc1.getDeclaredAttribute("a1");
        assertSame(ac1, a1.getAttributeClass());
        modelLoader.execute("ALTER RESOURCE CLASS rc1 DELETE ATTRIBUTE a1;");
        assertTrue(rc1.getDeclaredAttributes().isEmpty());
    }
    
    public void testAlterResourceClassAddParentClass()
        throws Exception
    {
        modelLoader.execute("CREATE RESOURCE CLASS rc1 JAVA CLASS org.objectledge.coral.datatypes.Node HANDLER CLASS hc1;");
        modelLoader.execute("CREATE RESOURCE CLASS rc2 JAVA CLASS org.objectledge.coral.datatypes.Node2 HANDLER CLASS hc1;");
        ResourceClass rc1 = schema.getResourceClass("rc1");
        ResourceClass rc2 = schema.getResourceClass("rc2");
        assertTrue(rc1.getDeclaredParentClasses().isEmpty());
        modelLoader.execute("ALTER RESOURCE CLASS rc2 ADD SUPERCLASS rc1;");
        assertSame(rc1, rc2.getDeclaredParentClasses().get(0));                
    }
    
    public void testAlterResourceClassDeleteParentClass()
        throws Exception
    {
        modelLoader.execute("CREATE RESOURCE CLASS rc1 JAVA CLASS org.objectledge.coral.datatypes.Node HANDLER CLASS hc1;");
        modelLoader.execute("CREATE RESOURCE CLASS rc2 JAVA CLASS org.objectledge.coral.datatypes.Node2 HANDLER CLASS hc1 SUPERCLASSES ( rc1 );");        
        ResourceClass rc1 = schema.getResourceClass("rc1");
        ResourceClass rc2 = schema.getResourceClass("rc2");
        assertSame(rc1, rc2.getDeclaredParentClasses().get(0));                
        modelLoader.execute("ALTER RESOURCE CLASS rc2 DELETE SUPERCLASS rc1;");
        assertTrue(rc1.getDeclaredParentClasses().isEmpty());
    }
    
    public void testAlterResourceClassSetName()
        throws Exception
    {
        modelLoader.execute("CREATE RESOURCE CLASS rc1 JAVA CLASS org.objectledge.coral.datatypes.Node HANDLER CLASS hc1 DB TABLE dt1;");
        ResourceClass rc1 = schema.getResourceClass("rc1");
        assertNotNull(rc1);
        modelLoader.execute("ALTER RESOURCE CLASS rc1 SET NAME rc2;");
        ResourceClass rc2 = schema.getResourceClass("rc2");
        assertNotNull(rc2);
        assertSame(rc1, rc2);
    }
    
    public void testAlterResourceClassSetJavaClass()
        throws Exception
    {    
        modelLoader.execute("CREATE RESOURCE CLASS rc1 JAVA CLASS org.objectledge.coral.datatypes.Node HANDLER CLASS hc1 DB TABLE dt1;");
        ResourceClass rc1 = schema.getResourceClass("rc1");
        assertEquals("org.objectledge.coral.datatypes.Node", rc1.getFQInterfaceClassName());
        assertEquals("org.objectledge.coral.datatypes.NodeImpl", rc1.getFQImplClassName());
        modelLoader.execute("ALTER RESOURCE CLASS rc1 SET JAVA CLASS org.objectledge.coral.datatypes.Node2;");
        assertEquals("org.objectledge.coral.datatypes.Node2", rc1.getFQInterfaceClassName());
        assertEquals("org.objectledge.coral.datatypes.Node2Impl", rc1.getFQImplClassName());
    }
    
    public void testAlterResourceClassSetDbTable()
        throws Exception
    {
        modelLoader.execute("CREATE RESOURCE CLASS rc1 JAVA CLASS org.objectledge.coral.datatypes.Node HANDLER CLASS hc1 DB TABLE dt1;");
        ResourceClass rc1 = schema.getResourceClass("rc1");
        assertEquals("dt1", rc1.getDbTable());
        modelLoader.execute("ALTER RESOURCE CLASS rc1 SET DB TABLE dt2;");
    }

    public void testAlterResourceClassSetFlags()
        throws Exception
    {
        modelLoader.execute("CREATE RESOURCE CLASS rc1 JAVA CLASS org.objectledge.coral.datatypes.Node HANDLER CLASS hc1 DB TABLE dt1;");
        ResourceClass rc1 = schema.getResourceClass("rc1");
        assertEquals(0, rc1.getFlags());
        modelLoader.execute("ALTER RESOURCE CLASS rc1 SET FLAGS ABSTRACT;");
        assertEquals(ResourceClassFlags.ABSTRACT, rc1.getFlags());
    }
    
    public void testAlterResourceClassAlterAttributeSetName()
        throws Exception
    {
        modelLoader.execute("CREATE ATTRIBUTE CLASS ac1 JAVA CLASS java.lang.Integer HANDLER CLASS hc1;");
        modelLoader.execute("CREATE RESOURCE CLASS rc1 JAVA CLASS org.objectledge.coral.datatypes.Node HANDLER CLASS hc1 ATTRIBUTES (ac1 a1);");
        AttributeClass ac1 = schema.getAttributeClass("ac1");
        ResourceClass rc1 = schema.getResourceClass("rc1");
        Attribute a1 = rc1.getDeclaredAttribute("a1");
        assertSame(ac1, a1.getAttributeClass());
        modelLoader.execute("ALTER RESOURCE CLASS rc1 ALTER ATTRIBUTE a1 SET NAME a2;");
        Attribute a2 = rc1.getDeclaredAttribute("a2");
        assertSame(ac1, a2.getAttributeClass());
    }

    public void testAlterResourceClassAlterAttributeSetDomain()
        throws Exception
    {
        modelLoader.execute("CREATE ATTRIBUTE CLASS ac1 JAVA CLASS java.lang.Integer HANDLER CLASS hc1;");
        modelLoader.execute("CREATE RESOURCE CLASS rc1 JAVA CLASS org.objectledge.coral.datatypes.Node HANDLER CLASS hc1 ATTRIBUTES (ac1 a1);");
        ResourceClass rc1 = schema.getResourceClass("rc1");
        Attribute a1 = rc1.getDeclaredAttribute("a1");
        assertNull(a1.getDomain());
        modelLoader.execute("ALTER RESOURCE CLASS rc1 ALTER ATTRIBUTE a1 SET DOMAIN '[A-Z]*';");
        assertEquals("[A-Z]*", a1.getDomain());
    }

    public void testAlterResourceClassAlterAttributeSetFlags()
        throws Exception
    {
        modelLoader.execute("CREATE ATTRIBUTE CLASS ac1 JAVA CLASS java.lang.Integer HANDLER CLASS hc1;");
        modelLoader.execute("CREATE RESOURCE CLASS rc1 JAVA CLASS org.objectledge.coral.datatypes.Node HANDLER CLASS hc1 ATTRIBUTES (ac1 a1);");
        ResourceClass rc1 = schema.getResourceClass("rc1");
        Attribute a1 = rc1.getDeclaredAttribute("a1");
        assertEquals(0, a1.getFlags());
        modelLoader.execute("ALTER RESOURCE CLASS rc1 ALTER ATTRIBUTE a1 SET FLAGS REQUIRED;");
        assertEquals(AttributeFlags.REQUIRED, a1.getFlags());
    }
    
    // border cases
    
    public void testNumericIds()
    {
        try
        {
            modelLoader.execute("ALTER RESOURCE CLASS 1 SET NAME foo;");
            fail("should throw");
        }
        catch(Exception e)
        {
            assertEquals("there were exceptions running the script", e.getMessage());
            assertEquals("numeric ids are not supported", e.getCause().getMessage());
        }
        try
        {
            modelLoader.execute("ALTER ATTRIBUTE CLASS 1 SET NAME foo;");
            fail("should throw");
        }
        catch(Exception e)
        {
            assertEquals("there were exceptions running the script", e.getMessage());
            assertEquals("numeric ids are not supported", e.getCause().getMessage());
        }
    }
    
    // datatypes initial
    
    public void testDatatypesInitial()
        throws Exception
    {
        FileSystem fs = getFileSystem();
        Reader r = fs.getReader("rml/coral/CoralDatatypesInitial.rml", "UTF-8");
        if(r == null)
        {
            throw new Exception("rml/coral/CoralDatatypesInitial.rml not found in the classpath");
        }
        modelLoader.load(r);
        ResourceClass rc = schema.getResourceClass("coral.Node");
        assertEquals("long", rc.getAttribute("id").getAttributeClass().getName());
        assertEquals("string", rc.getAttribute("name").getAttributeClass().getName());
    }
}
