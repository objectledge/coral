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
import org.objectledge.coral.BackendException;
import org.objectledge.coral.CoralInstantiationException;
import org.objectledge.coral.Instantiator;
import org.objectledge.coral.entity.CoralRegistry;
import org.objectledge.coral.event.CoralEventHub;
import org.objectledge.coral.event.CoralEventWhiteboard;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.OutputRecord;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.PersistenceException;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: ResourceClassImplTest.java,v 1.1 2004-02-25 16:05:51 fil Exp $
 */
public class ResourceClassImplTest extends MockObjectTestCase
{
    private Mock mockPersistence;   
    private Persistence persistence;
    private Mock mockInputRecord;
    private InputRecord inputRecord;
    private Mock mockOutputRecord;
    private OutputRecord outputRecord;
    private Mock mockInstantiator;
    private Instantiator instantiator;
    private Mock mockCoralEventHub;
    private CoralEventHub coralEventHub;
    private Mock mockCoralEventWhiteboard;
    private CoralEventWhiteboard coralEventWhiteboard;
    private Mock mockCoralRegistry;
    private CoralRegistry coralRegistry;
    private Mock mockResourceHandler;
    private ResourceHandler resourceHandler;

    public void setUp()
    {
        mockPersistence = new Mock(Persistence.class);
        persistence = (Persistence)mockPersistence.proxy();
        mockInputRecord = new Mock(InputRecord.class);
        inputRecord = (InputRecord)mockInputRecord.proxy();
        mockOutputRecord = new Mock(OutputRecord.class);
        outputRecord = (OutputRecord)mockOutputRecord.proxy();
        mockInstantiator = new Mock(Instantiator.class);
        instantiator = (Instantiator)mockInstantiator.proxy();
        mockCoralEventHub = new Mock(CoralEventHub.class);
        coralEventHub = (CoralEventHub)mockCoralEventHub.proxy();
        mockCoralEventWhiteboard = new Mock(CoralEventWhiteboard.class);
        coralEventWhiteboard = (CoralEventWhiteboard)mockCoralEventWhiteboard.proxy();
        mockCoralRegistry = new Mock(CoralRegistry.class);
        coralRegistry = (CoralRegistry)mockCoralRegistry.proxy();
        mockResourceHandler = new Mock(ResourceHandler.class);
        resourceHandler = (ResourceHandler)mockResourceHandler.proxy();
    }
    
    private ResourceClassImpl createResourceClass(String dbTable)
        throws JavaClassException
    {
        mockInstantiator.expect(once()).method("loadClass").with(eq("<java class>")).will(returnValue(Object.class));
        mockInstantiator.expect(once()).method("loadClass").with(eq("<handler class>")).will(returnValue(ResourceHandler.class));
        mockInstantiator.expect(once()).method("newInstance").with(eq(ResourceHandler.class)).will(returnValue(resourceHandler));
        mockCoralEventHub.expect(once()).method("getInbound").will(returnValue(coralEventWhiteboard));
        mockCoralEventWhiteboard.expect(once()).method("addResourceClassChangeListener");
        return new ResourceClassImpl(persistence, instantiator, coralEventHub,
            coralRegistry, "<resource class>", "<java class>", "<handler class>", dbTable, 303);
    }
    
    // basics ///////////////////////////////////////////////////////////////////////////////////
    
    public void testCreation()
        throws Exception
    {
        ResourceClassImpl rc = createResourceClass("<db table>");
        assertEquals(-1, rc.getId());
        assertEquals("<resource class>", rc.getName());
        assertEquals(Object.class, rc.getJavaClass());
        assertEquals("<java class>", rc.getJavaClassName());
        mockResourceHandler.expect(once()).method("hashCode").will(returnValue(0));
        rc.getHandler().hashCode();
        assertEquals("<db table>", rc.getDbTable());
        assertEquals(303, rc.getFlags());
    }
        
    public void testMissingHandlerClass()
        throws Exception
    {
        mockInstantiator.expect(once()).method("loadClass").with(eq("<java class>")).willReturn(Object.class);
        mockInstantiator.expect(once()).method("loadClass").with(eq("<handler class>")).willThrow(new ClassNotFoundException("<handler class>"));
        try
        {
            new ResourceClassImpl(persistence, instantiator, coralEventHub,
                coralRegistry, "<resource class>", "<java class>", "<handler class>", "<db table>", 303);
            fail("should throw an exception");        
        }
        catch(Exception e)
        {
            assertEquals(JavaClassException.class, e.getClass());
            assertEquals(ClassNotFoundException.class, e.getCause().getClass());
            assertEquals("<handler class>", e.getMessage());
        }
    }

    public void testUninstantiableHandlerClass()
        throws Exception
    {
        mockInstantiator.expect(once()).method("loadClass").with(eq("<java class>")).willReturn(Object.class);
        mockInstantiator.expect(once()).method("loadClass").with(eq("<handler class>")).willReturn(ResourceHandler.class);
        mockInstantiator.expect(once()).method("newInstance").with(eq(ResourceHandler.class)).willThrow(new CoralInstantiationException("<handler class>", new Exception("unavailable")));
        try
        {
            new ResourceClassImpl(persistence, instantiator, coralEventHub,
                coralRegistry, "<resource class>", "<java class>", "<handler class>", "<db table>", 303);
            fail("should throw an exception");        
        }
        catch(Exception e)
        {
            assertEquals(JavaClassException.class, e.getClass());
            assertEquals(Exception.class, e.getCause().getClass());
            assertEquals("unavailable", e.getCause().getMessage());
        }
    }
    
    public void testNotImplementingHandlerClass()
        throws Exception
    {
        mockInstantiator.expect(once()).method("loadClass").with(eq("<java class>")).willReturn(Object.class);
        mockInstantiator.expect(once()).method("loadClass").with(eq("<handler class>")).willReturn(ResourceHandler.class);
        mockInstantiator.expect(once()).method("newInstance").with(eq(ResourceHandler.class)).willReturn(new Object());
        try
        {        
            new ResourceClassImpl(persistence, instantiator, coralEventHub,
                coralRegistry, "<resource class>", "<java class>", "<handler class>", "<db table>", 303);
            fail("should throw an exception");        
        }
        catch(Exception e)
        {
            assertEquals(JavaClassException.class, e.getClass());
            assertEquals(ClassCastException.class, e.getCause().getClass());
            assertEquals("<handler class> does not implement ResourceHandler interface", e.getMessage());
        }
    }    
    
    public void testMiscSetters()
        throws Exception
    {
        ResourceClassImpl rc = createResourceClass("<db table>");
        assertEquals("<resource class>", rc.getName());
        rc.setName("<other resource class>");
        assertEquals("<other resource class>", rc.getName());
        assertEquals(Object.class, rc.getJavaClass());
        mockInstantiator.expect(once()).method("loadClass").with(eq("<missing class>")).will(throwException(new ClassNotFoundException("<missing class>")));
        try
        {
            rc.setJavaClass("<missing class>");
            fail("exception expected");
        }
        catch(Exception e)
        {
            assertEquals(JavaClassException.class, e.getClass());
            assertEquals(ClassNotFoundException.class, e.getCause().getClass());
        }
        try
        {
            rc.getJavaClass();
        }
        catch(Exception e)
        {
            assertEquals(BackendException.class, e.getClass());
            assertEquals("implementation class <missing class> is missing or has linkage problems", e.getMessage());
        }
    }

    // persistence //////////////////////////////////////////////////////////////////////////////
    
    public void testStoring()
        throws Exception
    {
        ResourceClassImpl rc = createResourceClass("<db table>");
        mockOutputRecord.expect(once()).method("setLong").with(eq("resource_class_id"),eq(-1L));
        mockOutputRecord.expect(once()).method("setString").with(eq("name"),eq("<resource class>"));
        mockOutputRecord.expect(once()).method("setString").with(eq("java_class_name"),eq("<java class>"));
        mockOutputRecord.expect(once()).method("setString").with(eq("handler_class_name"),eq("<handler class>"));
        mockOutputRecord.expect(once()).method("setString").with(eq("db_table"),eq("<db table>"));
        mockOutputRecord.expect(once()).method("setInteger").with(eq("flags"),eq(303));
        rc.getData(outputRecord);
        assertEquals("arl_resource_class", rc.getTable());
    }

    public void testStoringNullDbTable()
        throws Exception
    {
        ResourceClassImpl rc = createResourceClass(null);
        mockOutputRecord.expect(once()).method("setLong").with(eq("resource_class_id"),eq(-1L));
        mockOutputRecord.expect(once()).method("setString").with(eq("name"),eq("<resource class>"));
        mockOutputRecord.expect(once()).method("setString").with(eq("java_class_name"),eq("<java class>"));
        mockOutputRecord.expect(once()).method("setString").with(eq("handler_class_name"),eq("<handler class>"));
        mockOutputRecord.expect(once()).method("setNull").with(eq("db_table"));
        mockOutputRecord.expect(once()).method("setInteger").with(eq("flags"),eq(303));
        rc.getData(outputRecord);
        assertEquals("arl_resource_class", rc.getTable());
    }
    
    public void testLoading()
        throws Exception
    {
        ResourceClassImpl rc = new ResourceClassImpl(persistence, instantiator, coralEventHub, coralRegistry);
        mockInputRecord.expect(once()).method("getLong").with(eq("resource_class_id")).will(returnValue(-1L));
        mockInputRecord.expect(once()).method("getString").with(eq("name")).will(returnValue("<resource class>"));
        mockInputRecord.expect(once()).method("getString").with(eq("java_class_name")).will(returnValue("<java class>"));
        mockInstantiator.expect(once()).method("loadClass").with(eq("<java class>")).will(returnValue(Object.class));
        mockInputRecord.expect(once()).method("getString").with(eq("handler_class_name")).will(returnValue("<handler class>"));
        mockInstantiator.expect(once()).method("loadClass").with(eq("<handler class>")).will(returnValue(ResourceHandler.class));
        mockInstantiator.expect(once()).method("newInstance").with(eq(ResourceHandler.class)).will(returnValue(resourceHandler));
        mockInputRecord.expect(once()).method("isNull").with(eq("db_table")).will(returnValue(false));
        mockInputRecord.expect(once()).method("getString").with(eq("db_table")).will(returnValue("<db table>"));
        mockInputRecord.expect(once()).method("getInteger").with(eq("flags")).will(returnValue(303));    
        mockCoralEventHub.expect(once()).method("getInbound").will(returnValue(coralEventWhiteboard));
        mockCoralEventWhiteboard.expect(once()).method("addResourceClassChangeListener");        
        rc.setData(inputRecord);
        assertEquals(-1, rc.getId());
        assertEquals("<resource class>", rc.getName());
        assertEquals(Object.class, rc.getJavaClass());
        mockResourceHandler.expect(once()).method("hashCode").will(returnValue(0));
        rc.getHandler().hashCode();
        assertEquals("<db table>", rc.getDbTable());
        assertEquals(303, rc.getFlags());
    }

    public void testLoadingNullDbTable()
        throws Exception
    {
        ResourceClassImpl rc = new ResourceClassImpl(persistence, instantiator, coralEventHub, coralRegistry);
        mockInputRecord.expect(once()).method("getLong").with(eq("resource_class_id")).will(returnValue(-1L));
        mockInputRecord.expect(once()).method("getString").with(eq("name")).will(returnValue("<resource class>"));
        mockInputRecord.expect(once()).method("getString").with(eq("java_class_name")).will(returnValue("<java class>"));
        mockInstantiator.expect(once()).method("loadClass").with(eq("<java class>")).will(returnValue(Object.class));
        mockInputRecord.expect(once()).method("getString").with(eq("handler_class_name")).will(returnValue("<handler class>"));
        mockInstantiator.expect(once()).method("loadClass").with(eq("<handler class>")).will(returnValue(ResourceHandler.class));
        mockInstantiator.expect(once()).method("newInstance").with(eq(ResourceHandler.class)).will(returnValue(resourceHandler));
        mockInputRecord.expect(once()).method("isNull").with(eq("db_table")).will(returnValue(true));
        mockInputRecord.expect(once()).method("getInteger").with(eq("flags")).will(returnValue(303));    
        mockCoralEventHub.expect(once()).method("getInbound").will(returnValue(coralEventWhiteboard));
        mockCoralEventWhiteboard.expect(once()).method("addResourceClassChangeListener");        
        rc.setData(inputRecord);
        assertEquals(-1, rc.getId());
        assertEquals("<resource class>", rc.getName());
        assertEquals(Object.class, rc.getJavaClass());
        mockResourceHandler.expect(once()).method("hashCode").will(returnValue(0));
        rc.getHandler().hashCode();
        assertNull(rc.getDbTable());
        assertEquals(303, rc.getFlags());
    }
    
    public void testLoadingJavaClassException()
    {
        ResourceClassImpl rc = new ResourceClassImpl(persistence, instantiator, coralEventHub, coralRegistry);
        mockInputRecord.expect(once()).method("getLong").with(eq("resource_class_id")).will(returnValue(-1L));
        mockInputRecord.expect(once()).method("getString").with(eq("name")).will(returnValue("<resource class>"));
        mockInputRecord.expect(once()).method("getString").with(eq("java_class_name")).will(returnValue("<java class>"));
        mockInstantiator.expect(once()).method("loadClass").with(eq("<java class>")).will(throwException(new ClassNotFoundException("<java class>")));
        try
        {
            rc.setData(inputRecord);
            fail("should throw exception");
        }
        catch(Exception e)
        {
            assertEquals(PersistenceException.class, e.getClass());
            assertEquals(JavaClassException.class, e.getCause().getClass());
            assertEquals(ClassNotFoundException.class, e.getCause().getCause().getClass());
        }
    }
    
    // events ////////////////////////////////////////////////////////////////////////////////////
    
    public void testResourceClassChanged()
        throws Exception
    {
        ResourceClassImpl rc = createResourceClass("<db table>");
        mockPersistence.expect(once()).method("revert").with(same(rc));
        rc.resourceClassChanged(rc);
    }

    public void testResourceClassAttributesChanged()
    {
        // TODO implement test
    }
    
    public void testResourceClassInheritanceChanged()
    {
        // TODO implement test
    }
    
    public void testPermissionAssociationChanged()
    {
        // TODO implement test
    }
    
    // use cases //////////////////////////////////////////////////////////////////////////
    
    public void testAttributeAdded()
    {
        // TODO implement test
    }

    public void testAttributeRemoved()
    {
        // TODO implement test
    }

    public void testParentAdded()
    {
        // TODO implement test
    }

    public void testParentRemoved()
    {
        // TODO implement test
    }

    public void testAttributeAddedToParent()
    {
        // TODO implement test
    }

    public void testAttributeRevemovedFromParent()
    {
        // TODO implement test
    }

    public void testParentAddedToParent()
    {
        // TODO implement test
    }

    public void testParentRevemovedFromParent()
    {
        // TODO implement test
    }

    public void testChildAdded()
    {
        // TODO implement test
    }

    public void testChildRemoved()
    {
        // TODO implement test
    }

    public void testPermissionAdded()
    {
        // TODO implement test
    }

    public void testPermissionRemoved()
    {
        // TODO implement test
    }

    public void testPermissionAddedToParent()
    {
        // TODO implement test
    }

    public void testPermissionRemovedFromParent()
    {
        // TODO implement test
    }
}
