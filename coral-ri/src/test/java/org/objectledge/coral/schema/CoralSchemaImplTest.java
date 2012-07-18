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
import java.util.HashMap;
import java.util.Map;

import org.jcontainer.dna.Logger;
import org.jmock.Mock;
import org.objectledge.collections.ImmutableHashSet;
import org.objectledge.collections.ImmutableSet;
import org.objectledge.coral.CoralCore;
import org.objectledge.coral.Instantiator;
import org.objectledge.coral.entity.CoralRegistry;
import org.objectledge.coral.event.AttributeClassChangeListener;
import org.objectledge.coral.event.AttributeDefinitionChangeListener;
import org.objectledge.coral.event.CoralEventHub;
import org.objectledge.coral.event.CoralEventRedirector;
import org.objectledge.coral.event.CoralEventWhiteboard;
import org.objectledge.coral.event.ResourceClassChangeListener;
import org.objectledge.database.Database;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.Persistent;
import org.objectledge.test.LedgeTestCase;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: CoralSchemaImplTest.java,v 1.15 2005-04-06 09:43:39 rafal Exp $
 */
public class CoralSchemaImplTest extends LedgeTestCase
{
    private Mock mockDatabase;
    private Database database;
    private Mock mockConnection;
    private Connection connection;
    private Mock mockPersistence;
    private Persistence persistence;
    private Mock mockInstantiator;
    private Instantiator instantiator;
    private Mock mockCoralRegistry;
    private CoralRegistry coralRegistry;
    private Mock mockCoralCore;
    private CoralCore coralCore;
    private Mock mockCoralEventHub;
    private CoralEventHub coralEventHub;
    private Mock mockLocalEventWhiteboard;
    private CoralEventWhiteboard localEventWhiteboard;
    private Mock mockOutboundEventWhiteboard;
    private CoralEventWhiteboard outboundEventWhiteboard;
    private Mock mockInboundEventWhiteboard;
    private CoralEventWhiteboard inboundEventWhiteboard;
    private CoralEventWhiteboard globalEventWhiteboard;
    private Mock mockLogger;
    private Logger logger;
    private CoralSchema coralSchema;

    private Mock mockAttributeClass;
    private AttributeClass attributeClass;
    private Mock mockAttributeHandler;
    private AttributeHandler attributeHandler;
    private Mock mockOtherAttributeHandler;
    private AttributeHandler otherAttributeHandler;
    private Mock mockAttributeDefinition;
    private AttributeDefinition attributeDefinition;
    private Mock mockResourceClass;
    private ResourceClass resourceClass;
    private Mock mockParentResourceClass;
    private ResourceClass parentResourceClass;
    private Mock mockChildResourceClass;
    private ResourceClass childResourceClass;
    private Mock mockResourceHandler;
    private ResourceHandler resourceHandler;
    private Mock mockOtherResourceHandler;
    private ResourceHandler otherResourceHandler;
    
    private interface OtherAttributeHandler
        extends AttributeHandler
    {
        // AttributeHandler derived type for mocking
    }

    private interface OtherResourceHandler
        extends ResourceHandler
    {
        // ResourceHandler derived type for mocking
    }
    
    public void setUp()
        throws Exception
    {
        super.setUp();
        
        mockDatabase = mock(Database.class);
        database = (Database)mockDatabase.proxy();
        mockConnection = mock(Connection.class);
        connection = (Connection)mockConnection.proxy();
        mockPersistence = mock(Persistence.class);
        persistence = (Persistence)mockPersistence.proxy();
        mockPersistence.stubs().method("getDatabase").will(returnValue(database));        
        mockInstantiator = mock(Instantiator.class);
        instantiator = (Instantiator)mockInstantiator.proxy();
        mockCoralRegistry = mock(CoralRegistry.class);
        coralRegistry = (CoralRegistry)mockCoralRegistry.proxy();
        mockCoralCore = mock(CoralCore.class);
        coralCore = (CoralCore)mockCoralCore.proxy();
        mockCoralCore.stubs().method("getRegistry").will(returnValue(coralRegistry));
        mockCoralEventHub = mock(CoralEventHub.class);
        coralEventHub = (CoralEventHub)mockCoralEventHub.proxy();
        mockLocalEventWhiteboard = mock(CoralEventWhiteboard.class, "localEventWhiteboard");
        localEventWhiteboard = (CoralEventWhiteboard)mockLocalEventWhiteboard.proxy();
        mockOutboundEventWhiteboard = mock(CoralEventWhiteboard.class, "outboundEventWhiteboard");
        outboundEventWhiteboard = (CoralEventWhiteboard)mockOutboundEventWhiteboard.proxy();
        mockInboundEventWhiteboard = mock(CoralEventWhiteboard.class, "inboundEventWhiteboard");
        inboundEventWhiteboard = (CoralEventWhiteboard)mockInboundEventWhiteboard.proxy();
        mockLogger = mock(Logger.class);
        logger = (Logger)mockLogger.proxy();
        
        coralSchema = new CoralSchemaImpl(persistence, instantiator, coralCore, 
            coralEventHub, logger);
        
        mockDatabase.stubs().method("getConnection").will(returnValue(connection));
        globalEventWhiteboard = new CoralEventRedirector(inboundEventWhiteboard, 
            localEventWhiteboard, outboundEventWhiteboard);
        mockCoralEventHub.stubs().method("getLocal").will(returnValue(localEventWhiteboard));
        mockCoralEventHub.stubs().method("getOutbound").will(returnValue(outboundEventWhiteboard));
        mockCoralEventHub.stubs().method("getInbound").will(returnValue(inboundEventWhiteboard));
        mockCoralEventHub.stubs().method("getGlobal").will(returnValue(globalEventWhiteboard));
        
        mockAttributeClass = mock(AttributeClass.class);
        attributeClass = (AttributeClass)mockAttributeClass.proxy();
        mockAttributeHandler = mock(AttributeHandler.class);
        attributeHandler = (AttributeHandler)mockAttributeHandler.proxy();
        mockOtherAttributeHandler = mock(OtherAttributeHandler.class);
        otherAttributeHandler = (AttributeHandler)mockOtherAttributeHandler.proxy();
        mockAttributeClass.stubs().method("getHandler").will(returnValue(attributeHandler));
        mockAttributeDefinition = mock(AttributeDefinition.class);
        attributeDefinition = (AttributeDefinition)mockAttributeDefinition.proxy();
        mockResourceClass = mock(ResourceClass.class);
        resourceClass = (ResourceClass)mockResourceClass.proxy();
        mockParentResourceClass = mock(ResourceClass.class, "mockParentResourceClass");
        parentResourceClass = (ResourceClass)mockParentResourceClass.proxy();
        mockChildResourceClass = mock(ResourceClass.class, "mockChildResourceClass");
        childResourceClass = (ResourceClass)mockChildResourceClass.proxy();
        mockResourceHandler = mock(ResourceHandler.class);
        resourceHandler = (ResourceHandler)mockResourceHandler.proxy();
        mockOtherResourceHandler = mock(OtherResourceHandler.class);
        otherResourceHandler = (ResourceHandler)mockOtherResourceHandler.proxy();
    }
    
    public void testCreation()
    {
        // just run setUp()
    }
    
    // attribute classes /////////////////////////////////////////////////////////////////////////
    
    public void testGetAttributeClass()
    {
        ImmutableSet<AttributeClass<?>> ac = new ImmutableHashSet<AttributeClass<?>>();
        mockCoralRegistry.expects(once()).method("getAllAttributeClasses").will(returnValue(ac));
        assertSame(ac, coralSchema.getAllAttributeClasses());
    }
    
    public void testGetAttributeClassById()
        throws Exception
    {
        mockCoralRegistry.expects(once()).method("getAttributeClass").with(eq(1L)).will(returnValue(attributeClass));
        assertSame(attributeClass, coralSchema.getAttributeClass(1L));       
    }

    public void testGetAttributeClassByName()
        throws Exception
    {
        mockCoralRegistry.expects(once()).method("getAttributeClass").with(eq("<attribute class>")).will(returnValue(attributeClass));
        assertSame(attributeClass, coralSchema.getAttributeClass("<attribute class>"));       
    }
    
    public void testCreateAttributeClass()
        throws Exception
    {
        mockInstantiator.expects(once()).method("loadClass").with(eq("<java class>")).will(returnValue(Object.class));
        mockInstantiator.expects(once()).method("loadClass").with(eq("<handler class>")).will(returnValue(AttributeHandler.class));
        mockInstantiator.expects(once()).method("newInstance").with(eq(AttributeHandler.class), mapElement(AttributeClass.class, isA(AttributeClass.class))).will(returnValue(attributeHandler));
        mockInboundEventWhiteboard.expects(once()).method("addAttributeClassChangeListener").with(isA(AttributeClassChangeListener.class), isA(AttributeClass.class));
        mockCoralRegistry.expects(once()).method("addAttributeClass").with(and(isA(AttributeClass.class), isA(Persistent.class))).isVoid();
        AttributeClass realAttributeClass = coralSchema.createAttributeClass("<attribute class>", "<java class>", "<handler class>", "<db table>");
        assertEquals("<attribute class>", realAttributeClass.getName());
        assertEquals(Object.class, realAttributeClass.getJavaClass());
        assertSame(attributeHandler, realAttributeClass.getHandler());
        assertEquals("<db table>", realAttributeClass.getDbTable());
    }
    
    public void testDeleteAttributeClass()
        throws Exception
    {
        mockCoralRegistry.expects(once()).method("deleteAttributeClass").with(same(attributeClass));
        coralSchema.deleteAttributeClass(attributeClass);
    }

    public void testRenameAttributeClass()
        throws Exception
    {
        mockCoralRegistry.expects(once()).method("renameAttributeClass").with(same(attributeClass), eq("<new name>"));
        mockOutboundEventWhiteboard.expects(once()).method("fireAttributeClassChangeEvent").with(same(attributeClass));
        coralSchema.setName(attributeClass, "<new name>");
    }
    
    public void testAttributeClassSetJavaClass()
        throws Exception
    {
        mockInstantiator.expects(once()).method("loadClass").with(eq("<java class>")).will(returnValue(Object.class));
        mockInstantiator.expects(once()).method("loadClass").with(eq("<handler class>")).will(returnValue(AttributeHandler.class));
        mockInstantiator.expects(once()).method("newInstance").with(eq(AttributeHandler.class), mapElement(AttributeClass.class, isA(AttributeClass.class))).will(returnValue(attributeHandler));
        mockInboundEventWhiteboard.expects(once()).method("addAttributeClassChangeListener").with(isA(AttributeClassChangeListener.class), isA(AttributeClass.class));
        mockCoralRegistry.expects(once()).method("addAttributeClass").with(and(isA(AttributeClass.class), isA(Persistent.class))).isVoid();
        AttributeClass realAttributeClass = coralSchema.createAttributeClass("<attribute class>", "<java class>", "<handler class>", "<db table>");
        assertEquals(Object.class, realAttributeClass.getJavaClass());

        mockInstantiator.expects(once()).method("loadClass").with(eq("<new java class>")).will(returnValue(Number.class));
        mockPersistence.expects(once()).method("save").with(same(realAttributeClass));
        mockOutboundEventWhiteboard.expects(once()).method("fireAttributeClassChangeEvent").with(same(realAttributeClass));
        coralSchema.setJavaClass(realAttributeClass, "<new java class>");
        assertEquals(Number.class, realAttributeClass.getJavaClass());
    }

    public void testAttributeClassSetHandlerClass()
        throws Exception
    {
        mockInstantiator.expects(once()).method("loadClass").with(eq("<java class>")).will(returnValue(Object.class));
        mockInstantiator.expects(once()).method("loadClass").with(eq("<handler class>")).will(returnValue(AttributeHandler.class));
        mockInstantiator.expects(once()).method("newInstance").with(eq(AttributeHandler.class), mapElement(AttributeClass.class, isA(AttributeClass.class))).will(returnValue(attributeHandler));
        mockInboundEventWhiteboard.expects(once()).method("addAttributeClassChangeListener").with(isA(AttributeClassChangeListener.class), isA(AttributeClass.class));
        mockCoralRegistry.expects(once()).method("addAttributeClass").with(and(isA(AttributeClass.class), isA(Persistent.class))).isVoid();
        AttributeClass realAttributeClass = coralSchema.createAttributeClass("<attribute class>", "<java class>", "<handler class>", "<db table>");
        assertEquals(Object.class, realAttributeClass.getJavaClass());

        mockInstantiator.expects(once()).method("loadClass").with(eq("<new handler class>")).will(returnValue(OtherAttributeHandler.class));
        mockInstantiator.expects(once()).method("newInstance").with(eq(OtherAttributeHandler.class), mapElement(AttributeClass.class, same(realAttributeClass))).will(returnValue(otherAttributeHandler));
        mockPersistence.expects(once()).method("save").with(same(realAttributeClass));
        mockOutboundEventWhiteboard.expects(once()).method("fireAttributeClassChangeEvent").with(same(realAttributeClass));
        coralSchema.setHandlerClass(realAttributeClass, "<new handler class>");
        assertSame(otherAttributeHandler, realAttributeClass.getHandler());
    }


    public void testAttributeClassSetDbTable()
        throws Exception
    {
        mockInstantiator.expects(once()).method("loadClass").with(eq("<java class>")).will(returnValue(Object.class));
        mockInstantiator.expects(once()).method("loadClass").with(eq("<handler class>")).will(returnValue(AttributeHandler.class));
        mockInstantiator.expects(once()).method("newInstance").with(eq(AttributeHandler.class), mapElement(AttributeClass.class, isA(AttributeClass.class))).will(returnValue(attributeHandler));
        mockInboundEventWhiteboard.expects(once()).method("addAttributeClassChangeListener").with(isA(AttributeClassChangeListener.class), isA(AttributeClass.class));
        mockCoralRegistry.expects(once()).method("addAttributeClass").with(and(isA(AttributeClass.class), isA(Persistent.class))).isVoid();
        AttributeClass realAttributeClass = coralSchema.createAttributeClass("<attribute class>", "<java class>", "<handler class>", "<db table>");
        assertEquals(Object.class, realAttributeClass.getJavaClass());

        mockPersistence.expects(once()).method("save").with(same(realAttributeClass));
        mockOutboundEventWhiteboard.expects(once()).method("fireAttributeClassChangeEvent").with(same(realAttributeClass));
        coralSchema.setDbTable(realAttributeClass, "<new db table>");
        assertEquals("<new db table>", realAttributeClass.getDbTable());
    }
    
    // attribute definitions /////////////////////////////////////////////////////////////////////
    
    public void testGetAttributeDefinition()
    {
        ImmutableSet<AttributeDefinition<?>> ad = new ImmutableHashSet<AttributeDefinition<?>>();
        mockCoralRegistry.expects(once()).method("getAllAttributeDefinitions").will(returnValue(ad));
        assertSame(ad, coralSchema.getAllAttributes());
    }
    
    public void testGetAttributeDefinitionById() 
        throws Exception
    {
        mockCoralRegistry.expects(once()).method("getAttributeDefinition").with(eq(1L)).will(returnValue(attributeDefinition));        
        assertSame(attributeDefinition, coralSchema.getAttribute(1L));
    }
    
    public void testCreateAttributeDefinition()
    {
        mockAttributeHandler.expects(once()).method("checkDomain").with(eq("<domain>"));
        mockInboundEventWhiteboard.expects(once()).method("addAttributeDefinitionChangeListener").with(isA(AttributeDefinitionChangeListener.class), isA(AttributeDefinition.class));
        AttributeDefinition realAttributeDefinition = coralSchema.createAttribute("<attribute>", attributeClass, "<domain>", 303);
        assertEquals("<attribute>", realAttributeDefinition.getName());
        assertEquals(attributeClass, realAttributeDefinition.getAttributeClass());
        assertEquals("<domain>", realAttributeDefinition.getDomain());
        assertEquals(303, realAttributeDefinition.getFlags());
    }
    
    public void testRenameAttributeDefinition()
        throws Exception
    {
        mockAttributeDefinition.stubs().method("getDeclaringClass").will(returnValue(null));
        mockAttributeDefinition.stubs().method("getName").will(returnValue("<old name>"));
        mockCoralRegistry.expects(once()).method("renameAttributeDefinition").with(same(attributeDefinition), eq("<new name>"));
        mockOutboundEventWhiteboard.expects(once()).method("fireAttributeDefinitionChangeEvent").with(same(attributeDefinition));
        mockLocalEventWhiteboard.expects(once()).method("fireAttributeDefinitionChangeEvent").with(same(attributeDefinition));
        coralSchema.setName(attributeDefinition, "<new name>");
    }
    
    public void testAttributeDefinitionSetFlags()
        throws Exception
    {
        mockAttributeHandler.expects(once()).method("checkDomain").with(eq("<domain>"));
        mockInboundEventWhiteboard.expects(once()).method("addAttributeDefinitionChangeListener").with(isA(AttributeDefinitionChangeListener.class), isA(AttributeDefinition.class));
        AttributeDefinition realAttributeDefinition = coralSchema.createAttribute("<attribute>", attributeClass, "<domain>", 303);
        assertEquals(303, realAttributeDefinition.getFlags());
        
        mockPersistence.expects(once()).method("save").with(same(realAttributeDefinition));
        mockOutboundEventWhiteboard.expects(once()).method("fireAttributeDefinitionChangeEvent").with(same(realAttributeDefinition));
        mockLocalEventWhiteboard.expects(once()).method("fireAttributeDefinitionChangeEvent").with(same(realAttributeDefinition));
        coralSchema.setFlags(realAttributeDefinition, 121);
        assertEquals(121, realAttributeDefinition.getFlags());
    }

    public void testAttributeDefinitionSetDomain()
        throws Exception
    {
        mockAttributeHandler.expects(once()).method("checkDomain").with(eq("<domain>"));
        mockInboundEventWhiteboard.expects(once()).method("addAttributeDefinitionChangeListener").with(isA(AttributeDefinitionChangeListener.class), isA(AttributeDefinition.class));
        AttributeDefinition realAttributeDefinition = coralSchema.createAttribute("<attribute>", attributeClass, "<domain>", 303);
        assertEquals("<domain>", realAttributeDefinition.getDomain());
        
        mockAttributeHandler.expects(once()).method("checkDomain").with(eq("<new domain>"));
        mockPersistence.expects(once()).method("save").with(same(realAttributeDefinition));
        mockOutboundEventWhiteboard.expects(once()).method("fireAttributeDefinitionChangeEvent").with(same(realAttributeDefinition));
        mockLocalEventWhiteboard.expects(once()).method("fireAttributeDefinitionChangeEvent").with(same(realAttributeDefinition));
        coralSchema.setDomain(realAttributeDefinition, "<new domain>");
        assertEquals("<new domain>", realAttributeDefinition.getDomain());
    }
    
    // resource classess ////////////////////////////////////////////////////////////////////////
    
    public void testGetResourceClass()
    {
        ImmutableSet<ResourceClass<?>> rc = new ImmutableHashSet<ResourceClass<?>>();
        mockCoralRegistry.expects(once()).method("getAllResourceClasses").will(returnValue(rc));
        assertSame(rc, coralSchema.getAllResourceClasses());
    }
    
    public void testGetResourceById()
        throws Exception
    {
        mockCoralRegistry.expects(once()).method("getResourceClass").with(eq(1L)).will(returnValue(resourceClass));
        assertSame(resourceClass, coralSchema.getResourceClass(1L));
    }   

    public void testGetResourceByName()
        throws Exception
    {
        mockCoralRegistry.expects(once()).method("getResourceClass").with(eq("<resource class>")).will(returnValue(resourceClass));
        assertSame(resourceClass, coralSchema.getResourceClass("<resource class>"));
    }   

    public void testCreateResourceClass()
        throws Exception
    {
        mockInstantiator.expects(once()).method("loadClass").with(eq("<java class>")).will(returnValue(Object.class));
        mockInstantiator.expects(once()).method("loadClass").with(eq("<handler class>")).will(returnValue(ResourceHandler.class));
        mockInstantiator.expects(once()).method("newInstance").with(eq(ResourceHandler.class), mapElement(ResourceClass.class, isA(ResourceClass.class))).will(returnValue(resourceHandler));
        mockInboundEventWhiteboard.expects(once()).method("addResourceClassChangeListener").with(isA(ResourceClassChangeListener.class), isA(ResourceClass.class));
        mockCoralRegistry.expects(once()).method("addResourceClass").with(and(isA(ResourceClass.class), isA(Persistent.class))).isVoid();
        ResourceClass realResourceClass = coralSchema.createResourceClass("<attribute class>", "<java class>", "<handler class>", "<db table>", 303);
        assertEquals("<attribute class>", realResourceClass.getName());
        assertEquals(Object.class, realResourceClass.getJavaClass());
        assertSame(resourceHandler, realResourceClass.getHandler());
        assertEquals("<db table>", realResourceClass.getDbTable());
        assertEquals(303, realResourceClass.getFlags());
    }
    
    public void testDeleteResourceClass()
        throws Exception
    {
        mockCoralRegistry.expects(once()).method("deleteResourceClass").with(same(resourceClass));
        coralSchema.deleteResourceClass(resourceClass);
    }
    
    public void testRenameResourceClass()
        throws Exception
    {
        mockCoralRegistry.expects(once()).method("renameResourceClass").with(same(resourceClass), eq("<new name>"));
        mockOutboundEventWhiteboard.expects(once()).method("fireResourceClassChangeEvent").with(same(resourceClass));
        coralSchema.setName(resourceClass, "<new name>");
    }

    public void testResourceClassSetJavaClass()
        throws Exception
    {
        mockInstantiator.expects(once()).method("loadClass").with(eq("<java class>")).will(returnValue(Object.class));
        mockInstantiator.expects(once()).method("loadClass").with(eq("<handler class>")).will(returnValue(ResourceHandler.class));
        mockInstantiator.expects(once()).method("newInstance").with(eq(ResourceHandler.class), mapElement(ResourceClass.class, isA(ResourceClass.class))).will(returnValue(resourceHandler));
        mockInboundEventWhiteboard.expects(once()).method("addResourceClassChangeListener").with(isA(ResourceClassChangeListener.class), isA(ResourceClass.class));
        mockCoralRegistry.expects(once()).method("addResourceClass").with(and(isA(ResourceClass.class), isA(Persistent.class))).isVoid();
        ResourceClass realResourceClass = coralSchema.createResourceClass("<attribute class>", "<java class>", "<handler class>", "<db table>", 303);
        assertEquals(Object.class, realResourceClass.getJavaClass());

        mockInstantiator.expects(once()).method("loadClass").with(eq("<new java class>")).will(returnValue(Number.class));
        mockPersistence.expects(once()).method("save").with(same(realResourceClass));
        mockOutboundEventWhiteboard.expects(once()).method("fireResourceClassChangeEvent").with(same(realResourceClass));
        coralSchema.setJavaClass(realResourceClass, "<new java class>");
        assertEquals(Number.class, realResourceClass.getJavaClass());        
    }

    public void testResourceClassSetHandlerClass()
        throws Exception
    {
        mockInstantiator.expects(once()).method("loadClass").with(eq("<java class>")).will(returnValue(Object.class));
        mockInstantiator.expects(once()).method("loadClass").with(eq("<handler class>")).will(returnValue(ResourceHandler.class));
        mockInstantiator.expects(once()).method("newInstance").with(eq(ResourceHandler.class), mapElement(ResourceClass.class, isA(ResourceClass.class))).will(returnValue(resourceHandler));
        mockInboundEventWhiteboard.expects(once()).method("addResourceClassChangeListener").with(isA(ResourceClassChangeListener.class), isA(ResourceClass.class));
        mockCoralRegistry.expects(once()).method("addResourceClass").with(and(isA(ResourceClass.class), isA(Persistent.class))).isVoid();
        ResourceClass realResourceClass = coralSchema.createResourceClass("<attribute class>", "<java class>", "<handler class>", "<db table>", 303);
        assertEquals(Object.class, realResourceClass.getJavaClass());

        mockInstantiator.expects(once()).method("loadClass").with(eq("<new handler class>")).will(returnValue(OtherResourceHandler.class));
        mockInstantiator.expects(once()).method("newInstance").with(eq(OtherResourceHandler.class), mapElement(ResourceClass.class, same(realResourceClass))).will(returnValue(otherResourceHandler));
        mockPersistence.expects(once()).method("save").with(same(realResourceClass));
        mockOutboundEventWhiteboard.expects(once()).method("fireResourceClassChangeEvent").with(same(realResourceClass));
        coralSchema.setHandlerClass(realResourceClass, "<new handler class>");
        assertSame(otherResourceHandler, realResourceClass.getHandler());
    }

    public void testResourceClassSetFlags()
        throws Exception
    {
        mockInstantiator.expects(once()).method("loadClass").with(eq("<java class>")).will(returnValue(Object.class));
        mockInstantiator.expects(once()).method("loadClass").with(eq("<handler class>")).will(returnValue(ResourceHandler.class));
        mockInstantiator.expects(once()).method("newInstance").with(eq(ResourceHandler.class), mapElement(ResourceClass.class, isA(ResourceClass.class))).will(returnValue(resourceHandler));
        mockInboundEventWhiteboard.expects(once()).method("addResourceClassChangeListener").with(isA(ResourceClassChangeListener.class), isA(ResourceClass.class));
        mockCoralRegistry.expects(once()).method("addResourceClass").with(and(isA(ResourceClass.class), isA(Persistent.class))).isVoid();
        ResourceClass realResourceClass = coralSchema.createResourceClass("<attribute class>", "<java class>", "<handler class>", "<db table>", 303);
        assertEquals(303, realResourceClass.getFlags());
        
        mockPersistence.expects(once()).method("save").with(same(realResourceClass));
        mockOutboundEventWhiteboard.expects(once()).method("fireResourceClassChangeEvent").with(same(realResourceClass));
        coralSchema.setFlags(realResourceClass, 121);
        assertEquals(121, realResourceClass.getFlags());        
    }
    
    public void testAddAttribute()
        throws Exception
    {
        mockAttributeHandler.expects(once()).method("checkDomain").with(eq("<domain>"));
        mockInboundEventWhiteboard.expects(once()).method("addAttributeDefinitionChangeListener").with(isA(AttributeDefinitionChangeListener.class), isA(AttributeDefinition.class));
        AttributeDefinition realAttributeDefinition = coralSchema.createAttribute("<attribute>", attributeClass, "<domain>", 303);

        Object value = new Object();
        mockResourceClass.stubs().method("getHandler").will(returnValue(resourceHandler));
        mockResourceClass.expects(once()).method("hasAttribute").with(eq("<attribute>")).will(returnValue(false));
        mockResourceClass.expects(once()).method("getChildClasses").will(returnValue(new ResourceClass[0]));
        mockDatabase.expects(once()).method("beginTransaction").will(returnValue(true));
        mockCoralRegistry.expects(once()).method("addAttributeDefinition").with(same(realAttributeDefinition));
        // TODO ???
        mockLocalEventWhiteboard.expects(once()).method("fireResourceClassAttributesChangeEvent").with(same(realAttributeDefinition), eq(true));
        mockResourceHandler.expects(once()).method("addAttribute").with(same(realAttributeDefinition), same(value), same(connection));
        mockDatabase.expects(once()).method("commitTransaction").with(eq(true));
        mockOutboundEventWhiteboard.expects(once()).method("fireResourceClassAttributesChangeEvent").with(same(realAttributeDefinition), eq(true));        
        mockConnection.expects(once()).method("close");
        coralSchema.addAttribute(resourceClass, realAttributeDefinition, value);
        assertEquals(resourceClass, realAttributeDefinition.getDeclaringClass());
    }
    
    public void testDeleteAttribute()
        throws Exception
    {
        mockResourceClass.stubs().method("getHandler").will(returnValue(resourceHandler));
        mockAttributeDefinition.stubs().method("getDeclaringClass").will(returnValue(resourceClass));
        mockDatabase.stubs().method("setTransactionTimeout").isVoid();
        mockDatabase.expects(once()).method("beginTransaction").will(returnValue(true));
        // TODO ???
        mockLocalEventWhiteboard.expects(once()).method("fireResourceClassAttributesChangeEvent").with(same(attributeDefinition), eq(false));
        mockResourceHandler.expects(once()).method("deleteAttribute").with(same(attributeDefinition), same(connection));
        mockCoralRegistry.expects(once()).method("deleteAttributeDefinition").with(same(attributeDefinition));
        mockDatabase.expects(once()).method("commitTransaction").with(eq(true));
        mockOutboundEventWhiteboard.expects(once()).method("fireResourceClassAttributesChangeEvent").with(same(attributeDefinition), eq(false));        
        mockConnection.expects(once()).method("close");
        coralSchema.deleteAttribute(resourceClass, attributeDefinition);
    }
    
    public void testAddParentClass()
        throws Exception
    {
        Map values = new HashMap();
        
        mockParentResourceClass.stubs().method("isParent").with(same(childResourceClass)).will(returnValue(false));
        mockChildResourceClass.stubs().method("isParent").with(same(parentResourceClass)).will(returnValue(false));
        mockParentResourceClass.stubs().method("getFlags").will(returnValue(0));
        mockParentResourceClass.stubs().method("getAllAttributes").will(returnValue(new AttributeDefinition[0]));
        mockParentResourceClass.stubs().method("getIdObject").will(returnValue(new Long(1L)));
        mockChildResourceClass.stubs().method("getIdObject").will(returnValue(new Long(2L)));
        mockChildResourceClass.stubs().method("getHandler").will(returnValue(resourceHandler));
        mockDatabase.expects(once()).method("beginTransaction").will(returnValue(true));
        ResourceClassInheritance rci = new ResourceClassInheritanceImpl(coralCore, parentResourceClass, childResourceClass);
        mockCoralRegistry.expects(once()).method("addResourceClassInheritance").with(eq(rci));
        // TODO ???
        mockLocalEventWhiteboard.expects(once()).method("fireResourceClassInheritanceChangeEvent").with(eq(rci), eq(true));
        mockResourceHandler.expects(once()).method("addParentClass").with(same(parentResourceClass), same(values), same(connection));
        mockDatabase.expects(once()).method("commitTransaction").with(eq(true));
        mockOutboundEventWhiteboard.expects(once()).method("fireResourceClassInheritanceChangeEvent").with(eq(rci), eq(true));
        mockConnection.expects(once()).method("close");       
        coralSchema.addParentClass(childResourceClass, parentResourceClass, values);
    }
    
    public void testDeleteParentClass()
    {
        mockParentResourceClass.stubs().method("isParent").with(same(childResourceClass)).will(returnValue(true));
        mockChildResourceClass.stubs().method("getHandler").will(returnValue(resourceHandler));
        mockDatabase.expects(once()).method("beginTransaction").will(returnValue(true));
        ResourceClassInheritance rci = new ResourceClassInheritanceImpl(coralCore, parentResourceClass, childResourceClass);
        // TODO ???
        mockLocalEventWhiteboard.expects(once()).method("fireResourceClassInheritanceChangeEvent").with(eq(rci), eq(false));
        mockResourceHandler.expects(once()).method("deleteParentClass").with(same(parentResourceClass), same(connection));
        mockCoralRegistry.expects(once()).method("deleteResourceClassInheritance").with(eq(rci));
        mockDatabase.expects(once()).method("commitTransaction").with(eq(true));
        mockOutboundEventWhiteboard.expects(once()).method("fireResourceClassInheritanceChangeEvent").with(eq(rci), eq(false));
        mockConnection.expects(once()).method("close");       
        coralSchema.deleteParentClass(childResourceClass, parentResourceClass);
    }
}
