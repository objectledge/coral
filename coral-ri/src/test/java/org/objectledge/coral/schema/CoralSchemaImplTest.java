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
import org.jmock.Constraint;
import org.jmock.builder.Mock;
import org.jmock.builder.MockObjectTestCase;
import org.objectledge.coral.CoralCore;
import org.objectledge.coral.Instantiator;
import org.objectledge.coral.entity.CoralRegistry;
import org.objectledge.coral.event.AttributeClassChangeListener;
import org.objectledge.coral.event.AttributeDefinitionChangeListener;
import org.objectledge.coral.event.CoralEventHub;
import org.objectledge.coral.event.CoralEventWhiteboard;
import org.objectledge.coral.event.ResourceClassChangeListener;
import org.objectledge.database.Database;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.Persistent;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: CoralSchemaImplTest.java,v 1.8 2004-03-12 09:15:18 fil Exp $
 */
public class CoralSchemaImplTest extends MockObjectTestCase
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
    
    private interface OtherAttributeHandler extends AttributeHandler { }
    private interface OtherResourceHandler extends ResourceHandler { }
    
    public void setUp()
        throws Exception
    {
        super.setUp();
        
        mockDatabase = new Mock(Database.class);
        database = (Database)mockDatabase.proxy();
        mockConnection = new Mock(Connection.class);
        connection = (Connection)mockConnection.proxy();
        mockPersistence = new Mock(Persistence.class);
        persistence = (Persistence)mockPersistence.proxy();
        mockPersistence.stub().method("getDatabase").will(returnValue(database));        
        mockInstantiator = new Mock(Instantiator.class);
        instantiator = (Instantiator)mockInstantiator.proxy();
        mockCoralRegistry = new Mock(CoralRegistry.class);
        coralRegistry = (CoralRegistry)mockCoralRegistry.proxy();
        mockCoralCore = new Mock(CoralCore.class);
        coralCore = (CoralCore)mockCoralCore.proxy();
        mockCoralCore.stub().method("getRegistry").will(returnValue(coralRegistry));
        mockCoralEventHub = new Mock(CoralEventHub.class);
        coralEventHub = (CoralEventHub)mockCoralEventHub.proxy();
        mockLocalEventWhiteboard = new Mock(CoralEventWhiteboard.class, "localEventWhiteboard");
        localEventWhiteboard = (CoralEventWhiteboard)mockLocalEventWhiteboard.proxy();
        mockOutboundEventWhiteboard = new Mock(CoralEventWhiteboard.class, "outboundEventWhiteboard");
        outboundEventWhiteboard = (CoralEventWhiteboard)mockOutboundEventWhiteboard.proxy();
        mockInboundEventWhiteboard = new Mock(CoralEventWhiteboard.class, "inboundEventWhiteboard");
        inboundEventWhiteboard = (CoralEventWhiteboard)mockInboundEventWhiteboard.proxy();
        mockLogger = new Mock(Logger.class);
        logger = (Logger)mockLogger.proxy();
        
        coralSchema = new CoralSchemaImpl(persistence, instantiator, coralCore, 
            coralEventHub, logger);
        
        mockDatabase.stub().method("getConnection").will(returnValue(connection));
        mockCoralEventHub.stub().method("getLocal").will(returnValue(localEventWhiteboard));
        mockCoralEventHub.stub().method("getOutbound").will(returnValue(outboundEventWhiteboard));
        mockCoralEventHub.stub().method("getInbound").will(returnValue(inboundEventWhiteboard));
        
        mockAttributeClass = new Mock(AttributeClass.class);
        attributeClass = (AttributeClass)mockAttributeClass.proxy();
        mockAttributeHandler = new Mock(AttributeHandler.class);
        attributeHandler = (AttributeHandler)mockAttributeHandler.proxy();
        mockOtherAttributeHandler = new Mock(OtherAttributeHandler.class);
        otherAttributeHandler = (AttributeHandler)mockOtherAttributeHandler.proxy();
        mockAttributeClass.stub().method("getHandler").will(returnValue(attributeHandler));
        mockAttributeDefinition = new Mock(AttributeDefinition.class);
        attributeDefinition = (AttributeDefinition)mockAttributeDefinition.proxy();
        mockResourceClass = new Mock(ResourceClass.class);
        resourceClass = (ResourceClass)mockResourceClass.proxy();
        mockParentResourceClass = new Mock(ResourceClass.class, "mockParentResourceClass");
        parentResourceClass = (ResourceClass)mockParentResourceClass.proxy();
        mockChildResourceClass = new Mock(ResourceClass.class, "mockChildResourceClass");
        childResourceClass = (ResourceClass)mockChildResourceClass.proxy();
        mockResourceHandler = new Mock(ResourceHandler.class);
        resourceHandler = (ResourceHandler)mockResourceHandler.proxy();
        mockOtherResourceHandler = new Mock(OtherResourceHandler.class);
        otherResourceHandler = (ResourceHandler)mockOtherResourceHandler.proxy();
    }
    
    public void testCreation()
    {
    }
    
    // attribute classes /////////////////////////////////////////////////////////////////////////
    
    public void testGetAttributeClass()
    {
        AttributeClass[] ac = new AttributeClass[0];
        mockCoralRegistry.expect(once()).method("getAttributeClass").will(returnValue(ac));
        assertSame(ac, coralSchema.getAttributeClass());
    }
    
    public void testGetAttributeClassById()
        throws Exception
    {
        mockCoralRegistry.expect(once()).method("getAttributeClass").with(eq(1L)).will(returnValue(attributeClass));
        assertSame(attributeClass, coralSchema.getAttributeClass(1L));       
    }

    public void testGetAttributeClassByName()
        throws Exception
    {
        mockCoralRegistry.expect(once()).method("getAttributeClass").with(eq("<attribute class>")).will(returnValue(attributeClass));
        assertSame(attributeClass, coralSchema.getAttributeClass("<attribute class>"));       
    }
    
    public void testCreateAttributeClass()
        throws Exception
    {
        mockInstantiator.expect(once()).method("loadClass").with(eq("<java class>")).will(returnValue(Object.class));
        mockInstantiator.expect(once()).method("loadClass").with(eq("<handler class>")).will(returnValue(AttributeHandler.class));
        mockInstantiator.expect(once()).method("newInstance").with(eq(AttributeHandler.class), mapElement(AttributeClass.class, isA(AttributeClass.class))).will(returnValue(attributeHandler));
        mockInboundEventWhiteboard.expect(once()).method("addAttributeClassChangeListener").with(isA(AttributeClassChangeListener.class), isA(AttributeClass.class));
        mockCoralRegistry.expect(once()).method("addAttributeClass").with(and(isA(AttributeClass.class), isA(Persistent.class))).will(returnValue(attributeClass));
        AttributeClass realAttributeClass = coralSchema.createAttributeClass("<attribute class>", "<java class>", "<handler class>", "<db table>");
        assertEquals("<attribute class>", realAttributeClass.getName());
        assertEquals(Object.class, realAttributeClass.getJavaClass());
        assertSame(attributeHandler, realAttributeClass.getHandler());
        assertEquals("<db table>", realAttributeClass.getDbTable());
    }
    
    public void testDeleteAttributeClass()
        throws Exception
    {
        mockCoralRegistry.expect(once()).method("deleteAttributeClass").with(same(attributeClass));
        coralSchema.deleteAttributeClass(attributeClass);
    }

    public void testRenameAttributeClass()
        throws Exception
    {
        mockCoralRegistry.expect(once()).method("renameAttributeClass").with(same(attributeClass), eq("<new name>"));
        mockOutboundEventWhiteboard.expect(once()).method("fireAttributeClassChangeEvent").with(same(attributeClass));
        coralSchema.setName(attributeClass, "<new name>");
    }
    
    public void testAttributeClassSetJavaClass()
        throws Exception
    {
        mockInstantiator.expect(once()).method("loadClass").with(eq("<java class>")).will(returnValue(Object.class));
        mockInstantiator.expect(once()).method("loadClass").with(eq("<handler class>")).will(returnValue(AttributeHandler.class));
        mockInstantiator.expect(once()).method("newInstance").with(eq(AttributeHandler.class), mapElement(AttributeClass.class, isA(AttributeClass.class))).will(returnValue(attributeHandler));
        mockInboundEventWhiteboard.expect(once()).method("addAttributeClassChangeListener").with(isA(AttributeClassChangeListener.class), isA(AttributeClass.class));
        mockCoralRegistry.expect(once()).method("addAttributeClass").with(and(isA(AttributeClass.class), isA(Persistent.class))).will(returnValue(attributeClass));
        AttributeClass realAttributeClass = coralSchema.createAttributeClass("<attribute class>", "<java class>", "<handler class>", "<db table>");
        assertEquals(Object.class, realAttributeClass.getJavaClass());

        mockInstantiator.expect(once()).method("loadClass").with(eq("<new java class>")).will(returnValue(Number.class));
        mockPersistence.expect(once()).method("save").with(same(realAttributeClass));
        mockOutboundEventWhiteboard.expect(once()).method("fireAttributeClassChangeEvent").with(same(realAttributeClass));
        coralSchema.setJavaClass(realAttributeClass, "<new java class>");
        assertEquals(Number.class, realAttributeClass.getJavaClass());
    }

    public void testAttributeClassSetHandlerClass()
        throws Exception
    {
        mockInstantiator.expect(once()).method("loadClass").with(eq("<java class>")).will(returnValue(Object.class));
        mockInstantiator.expect(once()).method("loadClass").with(eq("<handler class>")).will(returnValue(AttributeHandler.class));
        mockInstantiator.expect(once()).method("newInstance").with(eq(AttributeHandler.class), mapElement(AttributeClass.class, isA(AttributeClass.class))).will(returnValue(attributeHandler));
        mockInboundEventWhiteboard.expect(once()).method("addAttributeClassChangeListener").with(isA(AttributeClassChangeListener.class), isA(AttributeClass.class));
        mockCoralRegistry.expect(once()).method("addAttributeClass").with(and(isA(AttributeClass.class), isA(Persistent.class))).will(returnValue(attributeClass));
        AttributeClass realAttributeClass = coralSchema.createAttributeClass("<attribute class>", "<java class>", "<handler class>", "<db table>");
        assertEquals(Object.class, realAttributeClass.getJavaClass());

        mockInstantiator.expect(once()).method("loadClass").with(eq("<new handler class>")).will(returnValue(OtherAttributeHandler.class));
        mockInstantiator.expect(once()).method("newInstance").with(eq(OtherAttributeHandler.class), mapElement(AttributeClass.class, same(realAttributeClass))).will(returnValue(otherAttributeHandler));
        mockPersistence.expect(once()).method("save").with(same(realAttributeClass));
        mockOutboundEventWhiteboard.expect(once()).method("fireAttributeClassChangeEvent").with(same(realAttributeClass));
        coralSchema.setHandlerClass(realAttributeClass, "<new handler class>");
        assertSame(otherAttributeHandler, realAttributeClass.getHandler());
    }


    public void testAttributeClassSetDbTable()
        throws Exception
    {
        mockInstantiator.expect(once()).method("loadClass").with(eq("<java class>")).will(returnValue(Object.class));
        mockInstantiator.expect(once()).method("loadClass").with(eq("<handler class>")).will(returnValue(AttributeHandler.class));
        mockInstantiator.expect(once()).method("newInstance").with(eq(AttributeHandler.class), mapElement(AttributeClass.class, isA(AttributeClass.class))).will(returnValue(attributeHandler));
        mockInboundEventWhiteboard.expect(once()).method("addAttributeClassChangeListener").with(isA(AttributeClassChangeListener.class), isA(AttributeClass.class));
        mockCoralRegistry.expect(once()).method("addAttributeClass").with(and(isA(AttributeClass.class), isA(Persistent.class))).will(returnValue(attributeClass));
        AttributeClass realAttributeClass = coralSchema.createAttributeClass("<attribute class>", "<java class>", "<handler class>", "<db table>");
        assertEquals(Object.class, realAttributeClass.getJavaClass());

        mockPersistence.expect(once()).method("save").with(same(realAttributeClass));
        mockOutboundEventWhiteboard.expect(once()).method("fireAttributeClassChangeEvent").with(same(realAttributeClass));
        coralSchema.setDbTable(realAttributeClass, "<new db table>");
        assertEquals("<new db table>", realAttributeClass.getDbTable());
    }
    
    // attribute definitions /////////////////////////////////////////////////////////////////////
    
    public void testGetAttributeDefinition()
    {
        AttributeDefinition[] ad = new AttributeDefinition[0];
        mockCoralRegistry.expect(once()).method("getAttributeDefinition").will(returnValue(ad));
        assertSame(ad, coralSchema.getAttribute());
    }
    
    public void testGetAttributeDefinitionById() 
        throws Exception
    {
        mockCoralRegistry.expect(once()).method("getAttributeDefinition").with(eq(1L)).will(returnValue(attributeDefinition));        
        assertSame(attributeDefinition, coralSchema.getAttribute(1L));
    }
    
    public void testCreateAttributeDefinition()
    {
        mockAttributeHandler.expect(once()).method("checkDomain").with(eq("<domain>"));
        mockInboundEventWhiteboard.expect(once()).method("addAttributeDefinitionChangeListener").with(isA(AttributeDefinitionChangeListener.class), isA(AttributeDefinition.class));
        AttributeDefinition realAttributeDefinition = coralSchema.createAttribute("<attribute>", attributeClass, "<domain>", 303);
        assertEquals("<attribute>", realAttributeDefinition.getName());
        assertEquals(attributeClass, realAttributeDefinition.getAttributeClass());
        assertEquals("<domain>", realAttributeDefinition.getDomain());
        assertEquals(303, realAttributeDefinition.getFlags());
    }
    
    public void testRenameAttributeDefinition()
        throws Exception
    {
        mockAttributeDefinition.stub().method("getDeclaringClass").will(returnValue(null));
        mockAttributeDefinition.stub().method("getName").will(returnValue("<old name>"));
        mockCoralRegistry.expect(once()).method("renameAttributeDefinition").with(same(attributeDefinition), eq("<new name>"));
        mockOutboundEventWhiteboard.expect(once()).method("fireAttributeDefinitionChangeEvent").with(same(attributeDefinition));
        coralSchema.setName(attributeDefinition, "<new name>");
    }
    
    public void testAttributeDefinitionSetFlags()
        throws Exception
    {
        mockAttributeHandler.expect(once()).method("checkDomain").with(eq("<domain>"));
        mockInboundEventWhiteboard.expect(once()).method("addAttributeDefinitionChangeListener").with(isA(AttributeDefinitionChangeListener.class), isA(AttributeDefinition.class));
        AttributeDefinition realAttributeDefinition = coralSchema.createAttribute("<attribute>", attributeClass, "<domain>", 303);
        assertEquals(303, realAttributeDefinition.getFlags());
        
        mockPersistence.expect(once()).method("save").with(same(realAttributeDefinition));
        mockOutboundEventWhiteboard.expect(once()).method("fireAttributeDefinitionChangeEvent").with(same(realAttributeDefinition));
        coralSchema.setFlags(realAttributeDefinition, 121);
        assertEquals(121, realAttributeDefinition.getFlags());
    }

    public void testAttributeDefinitionSetDomain()
        throws Exception
    {
        mockAttributeHandler.expect(once()).method("checkDomain").with(eq("<domain>"));
        mockInboundEventWhiteboard.expect(once()).method("addAttributeDefinitionChangeListener").with(isA(AttributeDefinitionChangeListener.class), isA(AttributeDefinition.class));
        AttributeDefinition realAttributeDefinition = coralSchema.createAttribute("<attribute>", attributeClass, "<domain>", 303);
        assertEquals("<domain>", realAttributeDefinition.getDomain());
        
        mockAttributeHandler.expect(once()).method("checkDomain").with(eq("<new domain>"));
        mockPersistence.expect(once()).method("save").with(same(realAttributeDefinition));
        mockOutboundEventWhiteboard.expect(once()).method("fireAttributeDefinitionChangeEvent").with(same(realAttributeDefinition));
        coralSchema.setDomain(realAttributeDefinition, "<new domain>");
        assertEquals("<new domain>", realAttributeDefinition.getDomain());
    }
    
    // resource classess ////////////////////////////////////////////////////////////////////////
    
    public void testGetResourceClass()
    {
        ResourceClass[] rc = new ResourceClass[0];
        mockCoralRegistry.expect(once()).method("getResourceClass").will(returnValue(rc));
        assertSame(rc, coralSchema.getResourceClass());
    }
    
    public void testGetResourceById()
        throws Exception
    {
        mockCoralRegistry.expect(once()).method("getResourceClass").with(eq(1L)).will(returnValue(resourceClass));
        assertSame(resourceClass, coralSchema.getResourceClass(1L));
    }   

    public void testGetResourceByName()
        throws Exception
    {
        mockCoralRegistry.expect(once()).method("getResourceClass").with(eq("<resource class>")).will(returnValue(resourceClass));
        assertSame(resourceClass, coralSchema.getResourceClass("<resource class>"));
    }   

    public void testCreateResourceClass()
        throws Exception
    {
        mockInstantiator.expect(once()).method("loadClass").with(eq("<java class>")).will(returnValue(Object.class));
        mockInstantiator.expect(once()).method("loadClass").with(eq("<handler class>")).will(returnValue(ResourceHandler.class));
        mockInstantiator.expect(once()).method("newInstance").with(eq(ResourceHandler.class), mapElement(ResourceClass.class, isA(ResourceClass.class))).will(returnValue(resourceHandler));
        mockInboundEventWhiteboard.expect(once()).method("addResourceClassChangeListener").with(isA(ResourceClassChangeListener.class), isA(ResourceClass.class));
        mockCoralRegistry.expect(once()).method("addResourceClass").with(and(isA(ResourceClass.class), isA(Persistent.class))).will(returnValue(resourceClass));
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
        mockCoralRegistry.expect(once()).method("deleteResourceClass").with(same(resourceClass));
        coralSchema.deleteResourceClass(resourceClass);
    }
    
    public void testRenameResourceClass()
        throws Exception
    {
        mockCoralRegistry.expect(once()).method("renameResourceClass").with(same(resourceClass), eq("<new name>"));
        mockOutboundEventWhiteboard.expect(once()).method("fireResourceClassChangeEvent").with(same(resourceClass));
        coralSchema.setName(resourceClass, "<new name>");
    }

    public void testResourceClassSetJavaClass()
        throws Exception
    {
        mockInstantiator.expect(once()).method("loadClass").with(eq("<java class>")).will(returnValue(Object.class));
        mockInstantiator.expect(once()).method("loadClass").with(eq("<handler class>")).will(returnValue(ResourceHandler.class));
        mockInstantiator.expect(once()).method("newInstance").with(eq(ResourceHandler.class), mapElement(ResourceClass.class, isA(ResourceClass.class))).will(returnValue(resourceHandler));
        mockInboundEventWhiteboard.expect(once()).method("addResourceClassChangeListener").with(isA(ResourceClassChangeListener.class), isA(ResourceClass.class));
        mockCoralRegistry.expect(once()).method("addResourceClass").with(and(isA(ResourceClass.class), isA(Persistent.class))).will(returnValue(resourceClass));
        ResourceClass realResourceClass = coralSchema.createResourceClass("<attribute class>", "<java class>", "<handler class>", "<db table>", 303);
        assertEquals(Object.class, realResourceClass.getJavaClass());

        mockInstantiator.expect(once()).method("loadClass").with(eq("<new java class>")).will(returnValue(Number.class));
        mockPersistence.expect(once()).method("save").with(same(realResourceClass));
        mockOutboundEventWhiteboard.expect(once()).method("fireResourceClassChangeEvent").with(same(realResourceClass));
        coralSchema.setJavaClass(realResourceClass, "<new java class>");
        assertEquals(Number.class, realResourceClass.getJavaClass());        
    }

    public void testResourceClassSetHandlerClass()
        throws Exception
    {
        mockInstantiator.expect(once()).method("loadClass").with(eq("<java class>")).will(returnValue(Object.class));
        mockInstantiator.expect(once()).method("loadClass").with(eq("<handler class>")).will(returnValue(ResourceHandler.class));
        mockInstantiator.expect(once()).method("newInstance").with(eq(ResourceHandler.class), mapElement(ResourceClass.class, isA(ResourceClass.class))).will(returnValue(resourceHandler));
        mockInboundEventWhiteboard.expect(once()).method("addResourceClassChangeListener").with(isA(ResourceClassChangeListener.class), isA(ResourceClass.class));
        mockCoralRegistry.expect(once()).method("addResourceClass").with(and(isA(ResourceClass.class), isA(Persistent.class))).will(returnValue(resourceClass));
        ResourceClass realResourceClass = coralSchema.createResourceClass("<attribute class>", "<java class>", "<handler class>", "<db table>", 303);
        assertEquals(Object.class, realResourceClass.getJavaClass());

        mockInstantiator.expect(once()).method("loadClass").with(eq("<new handler class>")).will(returnValue(OtherResourceHandler.class));
        mockInstantiator.expect(once()).method("newInstance").with(eq(OtherResourceHandler.class), mapElement(ResourceClass.class, same(realResourceClass))).will(returnValue(otherResourceHandler));
        mockPersistence.expect(once()).method("save").with(same(realResourceClass));
        mockOutboundEventWhiteboard.expect(once()).method("fireResourceClassChangeEvent").with(same(realResourceClass));
        coralSchema.setHandlerClass(realResourceClass, "<new handler class>");
        assertSame(otherResourceHandler, realResourceClass.getHandler());
    }

    public void testResourceClassSetFlags()
        throws Exception
    {
        mockInstantiator.expect(once()).method("loadClass").with(eq("<java class>")).will(returnValue(Object.class));
        mockInstantiator.expect(once()).method("loadClass").with(eq("<handler class>")).will(returnValue(ResourceHandler.class));
        mockInstantiator.expect(once()).method("newInstance").with(eq(ResourceHandler.class), mapElement(ResourceClass.class, isA(ResourceClass.class))).will(returnValue(resourceHandler));
        mockInboundEventWhiteboard.expect(once()).method("addResourceClassChangeListener").with(isA(ResourceClassChangeListener.class), isA(ResourceClass.class));
        mockCoralRegistry.expect(once()).method("addResourceClass").with(and(isA(ResourceClass.class), isA(Persistent.class))).will(returnValue(resourceClass));
        ResourceClass realResourceClass = coralSchema.createResourceClass("<attribute class>", "<java class>", "<handler class>", "<db table>", 303);
        assertEquals(303, realResourceClass.getFlags());
        
        mockPersistence.expect(once()).method("save").with(same(realResourceClass));
        mockOutboundEventWhiteboard.expect(once()).method("fireResourceClassChangeEvent").with(same(realResourceClass));
        coralSchema.setFlags(realResourceClass, 121);
        assertEquals(121, realResourceClass.getFlags());        
    }
    
    public void testAddAttribute()
        throws Exception
    {
        mockAttributeHandler.expect(once()).method("checkDomain").with(eq("<domain>"));
        mockInboundEventWhiteboard.expect(once()).method("addAttributeDefinitionChangeListener").with(isA(AttributeDefinitionChangeListener.class), isA(AttributeDefinition.class));
        AttributeDefinition realAttributeDefinition = coralSchema.createAttribute("<attribute>", attributeClass, "<domain>", 303);

        Object value = new Object();
        mockResourceClass.stub().method("getHandler").will(returnValue(resourceHandler));
        mockResourceClass.expect(once()).method("hasAttribute").with(eq("<attribute>")).will(returnValue(false));
        mockResourceClass.expect(once()).method("getChildClasses").will(returnValue(new ResourceClass[0]));
        mockDatabase.expect(once()).method("beginTransaction").will(returnValue(true));
        mockCoralRegistry.expect(once()).method("addAttributeDefinition").with(same(realAttributeDefinition));
        // TODO ???
        mockLocalEventWhiteboard.expect(once()).method("fireResourceClassAttributesChangeEvent").with(same(realAttributeDefinition), eq(true));
        mockResourceHandler.expect(once()).method("addAttribute").with(same(realAttributeDefinition), same(value), same(connection));
        mockDatabase.expect(once()).method("commitTransaction").with(eq(true));
        mockOutboundEventWhiteboard.expect(once()).method("fireResourceClassAttributesChangeEvent").with(same(realAttributeDefinition), eq(true));        
        mockConnection.expect(once()).method("close");
        coralSchema.addAttribute(resourceClass, realAttributeDefinition, value);
        assertEquals(resourceClass, realAttributeDefinition.getDeclaringClass());
    }
    
    public void testDeleteAttribute()
        throws Exception
    {
        mockResourceClass.stub().method("getHandler").will(returnValue(resourceHandler));
        mockAttributeDefinition.stub().method("getDeclaringClass").will(returnValue(resourceClass));
        mockDatabase.expect(once()).method("beginTransaction").will(returnValue(true));
        // TODO ???
        mockLocalEventWhiteboard.expect(once()).method("fireResourceClassAttributesChangeEvent").with(same(attributeDefinition), eq(false));
        mockResourceHandler.expect(once()).method("deleteAttribute").with(same(attributeDefinition), same(connection));
        mockCoralRegistry.expect(once()).method("deleteAttributeDefinition").with(same(attributeDefinition));
        mockDatabase.expect(once()).method("commitTransaction").with(eq(true));
        mockOutboundEventWhiteboard.expect(once()).method("fireResourceClassAttributesChangeEvent").with(same(attributeDefinition), eq(false));        
        mockConnection.expect(once()).method("close");
        coralSchema.deleteAttribute(resourceClass, attributeDefinition);
    }
    
    public void testAddParentClass()
        throws Exception
    {
        Map values = new HashMap();
        
        mockParentResourceClass.stub().method("isParent").with(same(childResourceClass)).will(returnValue(false));
        mockChildResourceClass.stub().method("isParent").with(same(parentResourceClass)).will(returnValue(false));
        mockParentResourceClass.stub().method("getFlags").will(returnValue(0));
        mockParentResourceClass.stub().method("getAllAttributes").will(returnValue(new AttributeDefinition[0]));
        mockParentResourceClass.stub().method("getId").will(returnValue(1L));
        mockChildResourceClass.stub().method("getId").will(returnValue(2L));
        mockChildResourceClass.stub().method("getHandler").will(returnValue(resourceHandler));
        mockDatabase.expect(once()).method("beginTransaction").will(returnValue(true));
        ResourceClassInheritance rci = new ResourceClassInheritanceImpl(coralCore, parentResourceClass, childResourceClass);
        mockCoralRegistry.expect(once()).method("addResourceClassInheritance").with(eq(rci));
        // TODO ???
        mockLocalEventWhiteboard.expect(once()).method("fireResourceClassInheritanceChangeEvent").with(eq(rci), eq(true));
        mockResourceHandler.expect(once()).method("addParentClass").with(same(parentResourceClass), same(values), same(connection));
        mockDatabase.expect(once()).method("commitTransaction").with(eq(true));
        mockOutboundEventWhiteboard.expect(once()).method("fireResourceClassInheritanceChangeEvent").with(eq(rci), eq(true));
        mockConnection.expect(once()).method("close");       
        coralSchema.addParentClass(childResourceClass, parentResourceClass, values);
    }
    
    public void testDeleteParentClass()
    {
        mockParentResourceClass.stub().method("isParent").with(same(childResourceClass)).will(returnValue(true));
        mockChildResourceClass.stub().method("getHandler").will(returnValue(resourceHandler));
        mockDatabase.expect(once()).method("beginTransaction").will(returnValue(true));
        ResourceClassInheritance rci = new ResourceClassInheritanceImpl(coralCore, parentResourceClass, childResourceClass);
        // TODO ???
        mockLocalEventWhiteboard.expect(once()).method("fireResourceClassInheritanceChangeEvent").with(eq(rci), eq(false));
        mockResourceHandler.expect(once()).method("deleteParentClass").with(same(parentResourceClass), same(connection));
        mockCoralRegistry.expect(once()).method("deleteResourceClassInheritance").with(eq(rci));
        mockDatabase.expect(once()).method("commitTransaction").with(eq(true));
        mockOutboundEventWhiteboard.expect(once()).method("fireResourceClassInheritanceChangeEvent").with(eq(rci), eq(false));
        mockConnection.expect(once()).method("close");       
        coralSchema.deleteParentClass(childResourceClass, parentResourceClass);
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////////
    
    private Constraint mapElement(Object key, Constraint c)
    {
        return new MapElement(key, c);
    }
}
