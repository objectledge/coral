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
import org.jmock.builder.Mock;
import org.jmock.builder.MockObjectTestCase;
import org.objectledge.coral.Instantiator;
import org.objectledge.coral.entity.CoralRegistry;
import org.objectledge.coral.event.AttributeClassChangeListener;
import org.objectledge.coral.event.CoralEventHub;
import org.objectledge.coral.event.CoralEventWhiteboard;
import org.objectledge.database.Database;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.Persistent;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: CoralSchemaImplTest.java,v 1.1 2004-03-02 11:08:40 fil Exp $
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
    private Mock mockResourceClass;
    private ResourceClass resourceClass;
    
    private interface OtherAttributeHandler extends AttributeHandler { }
    
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
        mockInstantiator = new Mock(Instantiator.class);
        instantiator = (Instantiator)mockInstantiator.proxy();
        mockCoralRegistry = new Mock(CoralRegistry.class);
        coralRegistry = (CoralRegistry)mockCoralRegistry.proxy();
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
        
        coralSchema = new CoralSchemaImpl(database, persistence, instantiator, coralRegistry, 
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
    }
    
    public void testCreation()
    {
    }
    
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
    
    public void testCreateAttributClass()
        throws Exception
    {
        mockInstantiator.expect(once()).method("loadClass").with(eq("<java class>")).will(returnValue(Object.class));
        mockInstantiator.expect(once()).method("loadClass").with(eq("<handler class>")).will(returnValue(AttributeHandler.class));
        mockInstantiator.expect(once()).method("newInstance").with(eq(AttributeHandler.class)).will(returnValue(attributeHandler));
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
        mockInstantiator.expect(once()).method("newInstance").with(eq(AttributeHandler.class)).will(returnValue(attributeHandler));
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
        mockInstantiator.expect(once()).method("newInstance").with(eq(AttributeHandler.class)).will(returnValue(attributeHandler));
        mockInboundEventWhiteboard.expect(once()).method("addAttributeClassChangeListener").with(isA(AttributeClassChangeListener.class), isA(AttributeClass.class));
        mockCoralRegistry.expect(once()).method("addAttributeClass").with(and(isA(AttributeClass.class), isA(Persistent.class))).will(returnValue(attributeClass));
        AttributeClass realAttributeClass = coralSchema.createAttributeClass("<attribute class>", "<java class>", "<handler class>", "<db table>");
        assertEquals(Object.class, realAttributeClass.getJavaClass());

        mockInstantiator.expect(once()).method("loadClass").with(eq("<new handler class>")).will(returnValue(OtherAttributeHandler.class));
        mockInstantiator.expect(once()).method("newInstance").with(eq(OtherAttributeHandler.class)).will(returnValue(otherAttributeHandler));
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
        mockInstantiator.expect(once()).method("newInstance").with(eq(AttributeHandler.class)).will(returnValue(attributeHandler));
        mockInboundEventWhiteboard.expect(once()).method("addAttributeClassChangeListener").with(isA(AttributeClassChangeListener.class), isA(AttributeClass.class));
        mockCoralRegistry.expect(once()).method("addAttributeClass").with(and(isA(AttributeClass.class), isA(Persistent.class))).will(returnValue(attributeClass));
        AttributeClass realAttributeClass = coralSchema.createAttributeClass("<attribute class>", "<java class>", "<handler class>", "<db table>");
        assertEquals(Object.class, realAttributeClass.getJavaClass());

        mockPersistence.expect(once()).method("save").with(same(realAttributeClass));
        mockOutboundEventWhiteboard.expect(once()).method("fireAttributeClassChangeEvent").with(same(realAttributeClass));
        coralSchema.setDbTable(realAttributeClass, "<new db table>");
        assertEquals("<new db table>", realAttributeClass.getDbTable());
    }
}
