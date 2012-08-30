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

import java.sql.Connection;

import org.jcontainer.dna.Logger;
import org.jmock.Mock;
import org.objectledge.coral.BackendException;
import org.objectledge.coral.CoralCore;
import org.objectledge.coral.InstantiationException;
import org.objectledge.coral.Instantiator;
import org.objectledge.coral.entity.CoralRegistry;
import org.objectledge.coral.event.CoralEventHub;
import org.objectledge.coral.event.CoralEventWhiteboard;
import org.objectledge.coral.store.Resource;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.OutputRecord;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.test.LedgeTestCase;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: ResourceClassImplTest.java,v 1.14 2005-02-10 17:47:13 rafal Exp $
 */
public class ResourceClassImplTest extends LedgeTestCase
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
    private Mock mockCoralCore;
    private CoralCore coralCore;
    private Mock mockResourceHandler;
    private ResourceHandler resourceHandler;
    private Mock mockResource;
    private Resource resource;
    private Mock mockConnection;
    private Connection connection;
    private Mock mockLogger;
    private Logger logger;
    
    public void setUp()
    {
        mockPersistence = mock(Persistence.class);
        persistence = (Persistence)mockPersistence.proxy();
        mockInputRecord = mock(InputRecord.class);
        inputRecord = (InputRecord)mockInputRecord.proxy();
        mockOutputRecord = mock(OutputRecord.class);
        outputRecord = (OutputRecord)mockOutputRecord.proxy();
        mockInstantiator = mock(Instantiator.class);
        instantiator = (Instantiator)mockInstantiator.proxy();
        mockCoralEventHub = mock(CoralEventHub.class);
        coralEventHub = (CoralEventHub)mockCoralEventHub.proxy();
        mockCoralEventWhiteboard = mock(CoralEventWhiteboard.class);
        coralEventWhiteboard = (CoralEventWhiteboard)mockCoralEventWhiteboard.proxy();
        mockCoralRegistry = mock(CoralRegistry.class);
        coralRegistry = (CoralRegistry)mockCoralRegistry.proxy();
        mockCoralCore = mock(CoralCore.class);
        coralCore = (CoralCore)mockCoralCore.proxy();
        mockCoralCore.stubs().method("getRegistry").will(returnValue(coralRegistry));
        mockResourceHandler = mock(ResourceHandler.class);
        resourceHandler = (ResourceHandler)mockResourceHandler.proxy();
        mockResource = mock(Resource.class);
        resource = (Resource)mockResource.proxy();
        mockConnection = mock(Connection.class);
        connection = (Connection)mockConnection.proxy();
        mockLogger = mock(Logger.class);
        logger = (Logger)mockLogger.proxy();
    }
    
    private ResourceClassImpl createResourceClass(String dbTable)
        throws JavaClassException
    {
        mockInstantiator.expects(once()).method("loadClass").with(eq("<java class>")).will(returnValue(Object.class));
        mockInstantiator.expects(once()).method("loadClass").with(eq("<handler class>")).will(returnValue(ResourceHandler.class));
        mockInstantiator.expects(once()).method("newInstance").with(eq(ResourceHandler.class), mapElement(ResourceClass.class, isA(ResourceClass.class))).will(returnValue(resourceHandler));
        mockCoralEventHub.expects(once()).method("getInbound").will(returnValue(coralEventWhiteboard));
        mockCoralEventWhiteboard.expects(once()).method("addResourceClassChangeListener");
        return new ResourceClassImpl(persistence, instantiator, coralEventHub,
            coralCore, "<resource class>", "<java class>", "<handler class>", dbTable, 303);
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
        mockResourceHandler.expects(once()).method("retrieve").with(eq(resource), eq(connection), NULL).isVoid();
        rc.getHandler().retrieve(resource, connection, null);
        assertEquals("<db table>", rc.getDbTable());
        assertEquals(303, rc.getFlags());
    }
        
    public void testMissingHandlerClass()
        throws Exception
    {
        mockInstantiator.expects(once()).method("loadClass").with(eq("<handler class>")).will(throwException(new ClassNotFoundException("<handler class>")));
        try
        {
            new ResourceClassImpl(persistence, instantiator, coralEventHub,
                coralCore, "<resource class>", "<java class>", "<handler class>", "<db table>", 303);
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
        mockInstantiator.expects(once()).method("loadClass").with(eq("<handler class>")).will(returnValue(ResourceHandler.class));
        mockInstantiator.expects(once()).method("newInstance").with(eq(ResourceHandler.class), mapElement(ResourceClass.class, isA(ResourceClass.class))).will(throwException(new InstantiationException("<handler class>", new Exception("unavailable"))));
        try
        {
            new ResourceClassImpl(persistence, instantiator, coralEventHub,
                coralCore, "<resource class>", "<java class>", "<handler class>", "<db table>", 303);
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
        mockInstantiator.expects(once()).method("loadClass").with(eq("<handler class>")).will(returnValue(ResourceHandler.class));
        mockInstantiator.expects(once()).method("newInstance").with(eq(ResourceHandler.class), mapElement(ResourceClass.class, isA(ResourceClass.class))).will(returnValue(new Object()));
        try
        {        
            new ResourceClassImpl(persistence, instantiator, coralEventHub,
                coralCore, "<resource class>", "<java class>", "<handler class>", "<db table>", 303);
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
        rc.setClassName("<other resource class>");
        assertEquals("<other resource class>", rc.getName());
        assertEquals(Object.class, rc.getJavaClass());
        mockInstantiator.expects(once()).method("loadClass").with(eq("<missing class>")).will(throwException(new ClassNotFoundException("<missing class>")));
        try
        {
            rc.setJavaClass("<missing class>", false);
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
        mockOutputRecord.expects(once()).method("setString").with(eq("name"),eq("<resource class>"));
        mockOutputRecord.expects(once()).method("setString").with(eq("java_class_name"),eq("<java class>"));
        mockOutputRecord.expects(once()).method("setString").with(eq("handler_class_name"),eq("<handler class>"));
        mockOutputRecord.expects(once()).method("setString").with(eq("db_table_name"),eq("<db table>"));
        mockOutputRecord.expects(once()).method("setInteger").with(eq("flags"),eq(303));
        rc.getData(outputRecord);
        assertEquals("coral_resource_class", rc.getTable());
    }

    public void testStoringNullDbTable()
        throws Exception
    {
        ResourceClassImpl rc = createResourceClass(null);
        mockOutputRecord.expects(once()).method("setString").with(eq("name"),eq("<resource class>"));
        mockOutputRecord.expects(once()).method("setString").with(eq("java_class_name"),eq("<java class>"));
        mockOutputRecord.expects(once()).method("setString").with(eq("handler_class_name"),eq("<handler class>"));
        mockOutputRecord.expects(once()).method("setNull").with(eq("db_table_name"));
        mockOutputRecord.expects(once()).method("setInteger").with(eq("flags"),eq(303));
        rc.getData(outputRecord);
        assertEquals("coral_resource_class", rc.getTable());
    }
    
    public void testLoading()
        throws Exception
    {
        ResourceClassImpl rc = new ResourceClassImpl(persistence, instantiator, coralEventHub, coralCore);
        mockInputRecord.expects(once()).method("getLong").with(eq("resource_class_id")).will(returnValue(-1L));
        mockInputRecord.expects(once()).method("getString").with(eq("name")).will(returnValue("<resource class>"));
        mockInputRecord.expects(once()).method("getString").with(eq("java_class_name")).will(returnValue("<java class>"));
        mockInstantiator.expects(once()).method("loadClass").with(eq("<java class>")).will(returnValue(Object.class));
        mockInputRecord.expects(once()).method("getString").with(eq("handler_class_name")).will(returnValue("<handler class>"));
        mockInstantiator.expects(once()).method("loadClass").with(eq("<handler class>")).will(returnValue(ResourceHandler.class));
        mockInstantiator.expects(once()).method("newInstance").with(eq(ResourceHandler.class), mapElement(ResourceClass.class, same(rc))).will(returnValue(resourceHandler));
        mockInputRecord.expects(once()).method("isNull").with(eq("db_table_name")).will(returnValue(false));
        mockInputRecord.expects(once()).method("getString").with(eq("db_table_name")).will(returnValue("<db table>"));
        mockInputRecord.expects(once()).method("getInteger").with(eq("flags")).will(returnValue(303));    
        mockCoralEventHub.expects(once()).method("getInbound").will(returnValue(coralEventWhiteboard));
        mockCoralEventWhiteboard.expects(once()).method("addResourceClassChangeListener");        
        rc.setData(inputRecord);
        assertEquals(-1, rc.getId());
        assertEquals("<resource class>", rc.getName());
        assertEquals(Object.class, rc.getJavaClass());
        mockResourceHandler.expects(once()).method("retrieve").with(eq(resource), eq(connection), NULL).isVoid();
        rc.getHandler().retrieve(resource, connection, null);
        assertEquals("<db table>", rc.getDbTable());
        assertEquals(303, rc.getFlags());
    }

    public void testLoadingNullDbTable()
        throws Exception
    {
        ResourceClassImpl rc = new ResourceClassImpl(persistence, instantiator, coralEventHub, coralCore);
        mockInputRecord.expects(once()).method("getLong").with(eq("resource_class_id")).will(returnValue(-1L));
        mockInputRecord.expects(once()).method("getString").with(eq("name")).will(returnValue("<resource class>"));
        mockInputRecord.expects(once()).method("getString").with(eq("java_class_name")).will(returnValue("<java class>"));
        mockInstantiator.expects(once()).method("loadClass").with(eq("<java class>")).will(returnValue(Object.class));
        mockInputRecord.expects(once()).method("getString").with(eq("handler_class_name")).will(returnValue("<handler class>"));
        mockInstantiator.expects(once()).method("loadClass").with(eq("<handler class>")).will(returnValue(ResourceHandler.class));
        mockInstantiator.expects(once()).method("newInstance").with(eq(ResourceHandler.class), mapElement(ResourceClass.class, isA(ResourceClass.class))).will(returnValue(resourceHandler));
        mockInputRecord.expects(once()).method("isNull").with(eq("db_table_name")).will(returnValue(true));
        mockInputRecord.expects(once()).method("getInteger").with(eq("flags")).will(returnValue(303));    
        mockCoralEventHub.expects(once()).method("getInbound").will(returnValue(coralEventWhiteboard));
        mockCoralEventWhiteboard.expects(once()).method("addResourceClassChangeListener");        
        rc.setData(inputRecord);
        assertEquals(-1, rc.getId());
        assertEquals("<resource class>", rc.getName());
        assertEquals(Object.class, rc.getJavaClass());
        mockResourceHandler.expects(once()).method("retrieve").with(eq(resource), eq(connection), NULL).isVoid();
        rc.getHandler().retrieve(resource, connection, null);
        assertNull(rc.getDbTable());
        assertEquals(303, rc.getFlags());
    }
    
    public void testLoadingJavaClassException() throws Exception
    {
        ResourceClassImpl rc = new ResourceClassImpl(persistence, instantiator, coralEventHub, coralCore);
        mockInputRecord.expects(once()).method("getLong").with(eq("resource_class_id")).will(returnValue(-1L));
        mockInputRecord.expects(once()).method("getString").with(eq("name")).will(returnValue("<resource class>"));
        mockInputRecord.expects(once()).method("getString").with(eq("handler_class_name")).will(returnValue("<handler class>"));
        mockInstantiator.expects(once()).method("loadClass").with(eq("<handler class>")).will(returnValue(ResourceHandler.class));
        mockInstantiator.expects(once()).method("newInstance").with(eq(ResourceHandler.class), mapElement(ResourceClass.class, isA(ResourceClass.class))).will(returnValue(resourceHandler));
        mockResourceHandler.expects(once()).method("getFallbackResourceImplClass").withNoArguments().will(returnValue(Resource.class));
        mockInputRecord.expects(once()).method("getString").with(eq("java_class_name")).will(returnValue("<java class>"));
        mockInstantiator.expects(once()).method("loadClass").with(eq("<java class>")).will(throwException(new ClassNotFoundException("<java class>")));
        mockCoralCore.expects(once()).method("getLog").withNoArguments().will(returnValue(logger));
        mockLogger.expects(once()).method("warn").withAnyArguments().isVoid();
        mockInputRecord.expects(once()).method("isNull").with(eq("db_table_name")).will(returnValue(true));
        mockInputRecord.expects(once()).method("getInteger").with(eq("flags")).will(returnValue(303));  
        mockCoralEventHub.expects(once()).method("getInbound").will(returnValue(coralEventWhiteboard));
        mockCoralEventWhiteboard.expects(once()).method("addResourceClassChangeListener").withAnyArguments().isVoid();      
        rc.setData(inputRecord);
        assertEquals(Resource.class, rc.getJavaClass());
    }
    
    // events ////////////////////////////////////////////////////////////////////////////////////
    
    public void testResourceClassChanged()
        throws Exception
    {
        ResourceClassImpl rc = createResourceClass("<db table>");
        mockPersistence.expects(once()).method("revert").with(same(rc));
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
    
    // ( ) resource classes
    // [ ] attribute definitions
    // < > permissions
    
    // <1>-(1)-[1]
    private void setupFixture1()
    {
        // TODO implement setup
    }
    
    private void assertFixture1()
    {
        // TODO implement
    }

    //         [1]
    // <1>-(1)<
    //         [2]
    //
    // extends 1
    private void setupFixture2()
    {
        // TODO implement setup
    }
    
    private void assertFixture2()
    {
        // TODO implement check
    }

    //     (2)-[3]
    //      |
    // <1>-(1)-[2]
    //
    // extends 1
    private void setupFixture3()
    {
        // TODO implement setup
    }
    
    private void assertFixture3()
    {
        // TODO implement check
    }
    
    //         [3]        
    //     (2)<
    //      |  [4]
    //      |
    // <1>-(1)-[2]
    //
    // extends 3
    private void setupFixture4()
    {
        // TODO implement setup
    }
    
    private void assertFixture4()
    {
        // TODO implement check
    }

    //     (3)-[5]
    //      |
    //     (2)-[3]
    //      |
    // <1>-(1)-[2]
    //
    // extends 3
    private void setupFixture5()
    {
        // TODO implement setup
    }
    
    private void assertFixture5()
    {
        // TODO implement check
    }

    // <1>-(1)-[2]
    //      |
    //     (4)
    //
    // extends 1
    private void setupFixture6()
    {
        // TODO implement setup
    }
    
    private void assertFixture6()
    {
        // TODO implement check
    }

    // <1>
    //    >(1)-[1]
    // <2>
    //
    // extends 1
    private void setupFixture7()
    {
        // TODO implement setup
    }
    
    private void assertFixture7()
    {
        // TODO implement check
    }

    // <3>-(2)-[3]
    //      |
    // <1>-(1)-[2]
    //
    // extends 3
    private void setupFixture8()
    {
        // TODO implement setup
    }
    
    private void assertFixture8()
    {
        // TODO implement check
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////

    // case 1, fixture 1 -> 2        
    public void testAttributeAdded()
    {
        // TODO implement test
    }

    // case 2, fixture 2 -> 1
    public void testAttributeRemoved()
    {
        // TODO implement test
    }

    // case 3, fixture 1 -> 3
    public void testParentAdded()
    {
        // TODO implement test
    }

    // case 4, fixture 3 -> 1
    public void testParentRemoved()
    {
        // TODO implement test
    }

    // case 5, fixtutre 3 -> 4
    public void testAttributeAddedToParent()
    {
        // TODO implement test
    }

    // case 6, fixture 4 -> 3
    public void testAttributeRevemovedFromParent()
    {
        // TODO implement test
    }

    // case 7, fixture 3 -> 5
    public void testParentAddedToParent()
    {
        // TODO implement test
    }

    // case 8, fixture 5 -> 3
    public void testParentRevemovedFromParent()
    {
        // TODO implement test
    }

    // case 9, fixture 1 -> 6
    public void testChildAdded()
    {
        // TODO implement test
    }

    // case 10, fixture 6 -> 1
    public void testChildRemoved()
    {
        // TODO implement test
    }

    // case 11, fixture 1 -> 7
    public void testPermissionAdded()
    {
        // TODO implement test
    }

    // case 12, fixture 7 -> 1
    public void testPermissionRemoved()
    {
        // TODO implement test
    }

    // case 13, fixture 3 -> 8
    public void testPermissionAddedToParent()
    {
        // TODO implement test
    }

    // case 14, fixture 8 -> 3
    public void testPermissionRemovedFromParent()
    {
        // TODO implement test
    }
}
