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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jcontainer.dna.Logger;
import org.jmock.Mock;
import org.objectledge.coral.tools.generator.model.ResourceClass;
import org.objectledge.filesystem.FileSystem;
import org.objectledge.templating.Template;
import org.objectledge.templating.Templating;
import org.objectledge.test.LedgeTestCase;

/**
 * 
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: GeneratorComponentTest.java,v 1.11 2004-12-23 06:44:04 rafal Exp $
 */
public class GeneratorComponentTest
    extends LedgeTestCase
{
    private Mock mockFileSystem;
    private FileSystem fileSystem;
    private Mock mockTemplating;
    private Templating templating;
    private Mock mockRMLModelLoader;
    private RMLModelLoader rmlModelLoader;

    private GeneratorComponent generatorComponent; 
    
    private Mock mockInterfaceTemplate;
    private Template interfaceTemplate;
    private Mock mockGenericImplTemplate;
    private Template genericImplTemplate;
    private Mock mockSQLTemplate;
    private Template sqlTemplate;
    private Mock mockReader1;
    private Reader reader1;
    private Mock mockReader2;
    private Reader reader2;
    private Mock mockResourceClass;
    private ResourceClass resourceClass;

    private Mock mockLogger;

    private Logger logger;
    
    private FileSystem testFileSystem; 

    @Override
    public void setUp()
        throws Exception
    {
        mockFileSystem = mock(FileSystem.class);
        fileSystem = (FileSystem)mockFileSystem.proxy();
        mockTemplating = mock(Templating.class);
        templating = (Templating)mockTemplating.proxy();
        mockRMLModelLoader = mock(RMLModelLoader.class);
        rmlModelLoader = (RMLModelLoader)mockRMLModelLoader.proxy();
        
        mockInterfaceTemplate = mock(Template.class, "mockInterfaceTemplate");
        interfaceTemplate = (Template)mockInterfaceTemplate.proxy();
        mockGenericImplTemplate = mock(Template.class, "mockGenericImplTemplate");
        genericImplTemplate = (Template)mockGenericImplTemplate.proxy();
        mockSQLTemplate = mock(Template.class, "mockSQLTemplate");
        sqlTemplate = (Template)mockSQLTemplate.proxy();
        
        mockReader1 = mock(Reader.class, "mockReader1");
        reader1 = (Reader)mockReader1.proxy();
        mockReader2 = mock(Reader.class, "mockReader2");
        reader2 = (Reader)mockReader2.proxy();
        
        mockResourceClass = mock(ResourceClass.class);
        resourceClass = (ResourceClass)mockResourceClass.proxy();
        
        mockLogger = mock(Logger.class);
        logger = (Logger)mockLogger.proxy();
        mockLogger.stubs().method("info").with(ANYTHING).isVoid();
        mockLogger.stubs().method("debug").with(ANYTHING).isVoid();

        mockFileSystem.stubs().method("exists").with(eq("LICENSE.txt")).will(returnValue(true));
        mockFileSystem.stubs().method("read").with(eq("LICENSE.txt"),eq("UTF-8")).will(returnValue("//license"));
        mockTemplating.stubs().method("getTemplate").with(eq("org/objectledge/coral/tools/generator/Interface")).will(returnValue(interfaceTemplate));
        mockTemplating.stubs().method("getTemplate").with(eq("org/objectledge/coral/tools/generator/Implementation")).will(returnValue(genericImplTemplate));
        mockTemplating.stubs().method("getTemplate").with(eq("org/objectledge/coral/tools/generator/SQL")).will(returnValue(sqlTemplate));
        generatorComponent = new GeneratorComponent("UTF-8", "src/main/rml/files.lst",
            "src/main/java", "java.,javax.,org.objectledge.", "*", "", "LICENSE.txt", null, null,
            null, null, fileSystem, templating, rmlModelLoader, logger);
            
        testFileSystem = FileSystem.getStandardFileSystem("src/test/resources/generator");
    }
    
    public void testReadNormal()
        throws Exception
    {
        provideFile("Custom1.java");
        Map<String, List<String>> hints = new HashMap<String, List<String>>();
        String custom = generatorComponent.read("Custom1.java", hints);
        assertEquals(
            "    // @order a,b,c\n"+
            "    // @import java.util.Date\n"+
            "    // @import java.lang.reflect.Method\n"+
            "    // @extends fred\n"+
            "    // user defined\n", custom);
        List<String> orderHint = hints.get("order");
        List<String> importHint = hints.get("import");
        List<String> extendsHint = hints.get("extends");
        assertEquals("[a, b, c]", orderHint.toString());
        assertEquals("[java.util.Date, java.lang.reflect.Method]", importHint.toString());
        assertEquals("[fred]", extendsHint.toString());        
    }
    
    public void testReadUnknown()
        throws Exception
    {
        provideFile("Custom2.java");
        Map<String, List<String>> hints = new HashMap<String, List<String>>();
        try
        {
            generatorComponent.read("Custom2.java", hints);
            fail();
        }
        catch(Exception e)
        {
            assertEquals("unknown hint @orber in Custom2.java at line 8", e.getMessage());
        }
    }

    public void testReadMalformed()
        throws Exception
    {
        provideFile("Custom3.java");
        Map<String, List<String>> hints = new HashMap<String, List<String>>();
        try
        {
            generatorComponent.read("Custom3.java", hints);
            fail();
        }
        catch(Exception e)
        {
            assertEquals("malformed hint @order in Custom3.java at line 8 - value expected", e.getMessage());
        }
    }
    
    public void testReadMissing()
        throws Exception
    {
        mockFileSystem.stubs().method("exists").with(eq("Custom4.java")).will(returnValue(false));
        Map<String, List<String>> hints = new HashMap<String, List<String>>();
        String custom = generatorComponent.read("Custom4.java", hints);
        assertEquals("", custom);
    }
    
    public void testWriteMissing()
        throws Exception
    {
        mockFileSystem.stubs().method("exists").with(eq("foo/Out.java")).will(returnValue(false));
        mockFileSystem.expects(once()).method("mkdirs").with(eq("/foo")).isVoid();
        mockFileSystem.expects(once()).method("write").with(eq("foo/Out.java"), eq("content"), eq("UTF-8")).isVoid();
        assertTrue(generatorComponent.write("foo/Out.java", "content"));
    }

    public void testWriteChanged()
        throws Exception
    {
        mockFileSystem.stubs().method("exists").with(eq("foo/Out.java")).will(returnValue(true));
        mockFileSystem.expects(once()).method("read").with(eq("foo/Out.java"), eq("UTF-8")).will(returnValue("other content"));
        mockFileSystem.expects(once()).method("write").with(eq("foo/Out.java"), eq("content"), eq("UTF-8")).isVoid();
        assertTrue(generatorComponent.write("foo/Out.java", "content"));
    }

    public void testWriteNotChanged()
        throws Exception
    {
        mockFileSystem.stubs().method("exists").with(eq("foo/Out.java")).will(returnValue(true));
        mockFileSystem.expects(once()).method("read").with(eq("foo/Out.java"), eq("UTF-8")).will(returnValue("content"));
        assertFalse(generatorComponent.write("foo/Out.java", "content"));
    }
    
    public void testLoadSources()
        throws Exception
    {
        provideFile("sources.lst");
        provideFile("masterSources.lst");
        provideFile("masterSchema.rml", reader1);
        provideFile("schema.rml", reader2);
        mockRMLModelLoader.expects(once()).method("load").with(same(reader1)).isVoid().id("c1");
        mockRMLModelLoader.expects(once()).method("load").with(same(reader2)).isVoid().id("c2");
        generatorComponent.loadSources("sources.lst");
    }
    
    public void testLoadSourcesMissing()
    {
        dontProvideFile("sourcesMissing.lst");
        try
        {
            generatorComponent.loadSources("sourcesMissing.lst");
            fail();
        }
        catch(Exception e)
        {
            assertEquals("missing listing file sourcesMissing.lst", e.getMessage());
        }
    }

    public void testLoadSourcesMissingRml()
        throws Exception
    {
        provideFile("sourcesMissingRml.lst");
        dontProvideFile("missingSchema.rml");
        try
        {
            generatorComponent.loadSources("sourcesMissingRml.lst");
            fail();
        }
        catch(Exception e)
        {
            assertEquals("missing source file missingSchema.rml in sourcesMissingRml.lst" +
                    " at line 3", e.getMessage());
        }
    }

    public void testLoadSourcesMissingInclude()
        throws Exception
    {
        provideFile("sourcesMissingInclude.lst");
        dontProvideFile("missingMasterSources.lst");
        try
        {
            generatorComponent.loadSources("sourcesMissingInclude.lst");
            fail();
        }
        catch(Exception e)
        {
            assertEquals("missing include file missingMasterSources.lst in " +
                    "sourcesMissingInclude.lst at line 3", e.getMessage());
        }
    }
    
    public void testMatches()
    {
        List<String> prefices = new ArrayList<String>(2);
        prefices.add("org.objectledge.coral.test");
        prefices.add("org.objectledge.coral.test.*");
        assertTrue(generatorComponent.matches("org.objectledge.coral.test", prefices));
        assertTrue(generatorComponent.matches("org.objectledge.coral.test.sub", prefices));
        assertFalse(generatorComponent.matches("org.objectledge.coral.testother", prefices));
        assertFalse(generatorComponent.matches("org.objectledge.coral.other", prefices));
    }
    
    public void testResolvePrimaryParentClass()
    {
        mockResourceClass.expects(once()).method("getHandlerClassName").will(returnValue("org.objectledge.datatypes.GenericResourceHandler"));
        assertEquals("org.objectledge.datatypes.GenericResource", generatorComponent.resolvePrimaryParentClass(resourceClass));
        mockResourceClass.expects(once()).method("getHandlerClassName").will(returnValue("org.objectledge.datatypes.PersistentResourceHandler"));
        assertEquals("org.objectledge.datatypes.PersistentResource", generatorComponent.resolvePrimaryParentClass(resourceClass));
    }

    // implementation ///////////////////////////////////////////////////////////////////////////
    
    private void provideFile(String path)
        throws Exception
    {
        mockFileSystem.stubs().method("exists").with(eq(path)).will(returnValue(true));
        mockFileSystem.stubs().method("getReader").with(eq(path), eq("UTF-8")).
            will(returnValue(testFileSystem.getReader(path, "UTF-8")));        
    }

    private void provideFile(String path, Reader reader)
        throws Exception
    {
        mockFileSystem.stubs().method("exists").with(eq(path)).will(returnValue(true));
        mockFileSystem.stubs().method("getReader").with(eq(path), eq("UTF-8")).
            will(returnValue(reader));        
    }
    
    private void dontProvideFile(String path)
    {
        mockFileSystem.stubs().method("exists").with(eq(path)).will(returnValue(false));        
    }
}
