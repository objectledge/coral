// 
//Copyright (c) 2003, Caltha - Gajda, Krzewski, Mach, Potempski Sp.J. 
//All rights reserved. 
//   
//Redistribution and use in source and binary forms, with or without modification,  
//are permitted provided that the following conditions are met: 
//   
//* Redistributions of source code must retain the above copyright notice,  
//this list of conditions and the following disclaimer. 
//* Redistributions in binary form must reproduce the above copyright notice,  
//this list of conditions and the following disclaimer in the documentation  
//and/or other materials provided with the distribution. 
//* Neither the name of the Caltha - Gajda, Krzewski, Mach, Potempski Sp.J.  
//nor the names of its contributors may be used to endorse or promote products  
//derived from this software without specific prior written resource. 
// 
//THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"  
//AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED  
//WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
//IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,  
//INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,  
//BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
//OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,  
//WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)  
//ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE  
//POSSIBILITY OF SUCH DAMAGE. 
//
package org.objectledge.coral.datatypes;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.jcontainer.dna.Logger;
import org.jmock.Mock;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeHandler;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.Resource;
import org.objectledge.database.Database;
import org.objectledge.utils.LedgeTestCase;

/**
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 *
 */
public class ResourceAttributeHandlerTest extends LedgeTestCase
{
    private Mock mockDatabase;
    private Database database;
    private Mock mockLogger;
    private Logger logger;
    private Mock mockCoralStore;
    private CoralStore coralStore;
    private Mock mockCoralSecurity;
    private CoralSecurity coralSecurity;
    private Mock mockCoralSchema;
    private CoralSchema coralSchema;
    private Mock mockAttributeClass;
    private AttributeClass attributeClass;
    private Mock mockResourceClass;
    private ResourceClass resourceClass;
    private Mock mockAttributeDefinition;
    private AttributeDefinition attributeDefinition;
    private Mock mockConnection;
    private Connection connection;
    private Mock mockStatement;
    private Statement statement;
    private Mock mockResultSet;
    private ResultSet resultSet;

    private Mock mockResource;
    private Resource resource;

    private ResourceAttributeHandler handler;
    
    private NodeImpl node;

    public void setUp() throws Exception
    {
        super.setUp();
        mockDatabase = mock(Database.class);
        mockDatabase.stubs().method("getNextId").will(returnValue(1L));
        database = (Database)mockDatabase.proxy();
        mockLogger = mock(Logger.class);
        logger = (Logger)mockLogger.proxy();
        mockResource = mock(Resource.class);
        mockResource.stubs().method("getIdObject").will(returnValue(new Long(1L)));
        mockResource.stubs().method("getIdString").will(returnValue("1"));
        mockResource.stubs().method("getName").will(returnValue("foo"));
        mockResource.stubs().method("getName").will(returnValue("foo"));
        mockResource.stubs().method("getPath").will(returnValue("/foo"));
        resource = (Resource)mockResource.proxy();
        
        mockAttributeDefinition = mock(AttributeDefinition.class);
        attributeDefinition = (AttributeDefinition)mockAttributeDefinition.proxy();
        
        mockResourceClass = mock(ResourceClass.class);
        mockResourceClass.stubs().method("getJavaClass").will(returnValue(NodeImpl.class));
        mockResourceClass.stubs().method("getName").will(returnValue("coral.Node"));
        mockResourceClass.stubs().method("getAttribute").will(returnValue(attributeDefinition));
               
        resourceClass = (ResourceClass)mockResourceClass.proxy();
        mockCoralStore = mock(CoralStore.class);
        mockCoralStore.stubs().method("getResourceByPath").with(eq("/foo")).will(returnValue(new Resource[]{resource}));
        mockCoralStore.stubs().method("getResource").with(eq(2L)).will(throwException(new EntityDoesNotExistException("")));
        mockCoralStore.stubs().method("getResourceByPath").with(not(eq("/foo"))).will(returnValue(new Resource[0]));
        mockCoralStore.stubs().method("getResource").with(eq(1L)).will(returnValue(resource));
        /*
        mockCoralStore.stubs().method("getResource").with(eq("foo")).will(returnValue(new Resource[]{resource}));
        mockCoralStore.stubs().method("getResource").with(eq(1L)).will(returnValue(resource));
        mockCoralStore.stubs().method("getResource").with(eq(2L)).will(throwException(new EntityDoesNotExistException("foo")));
        mockCoralStore.stubs().method("getResource").with(eq("bar")).will(throwException(new IllegalArgumentException("foo")));
        mockCoralStore.stubs().method("getResource").with(eq("foo0")).will(returnValue(new Resource[0]));
        mockCoralStore.stubs().method("getResource").with(eq("foo2")).will(returnValue(new Resource[2]));
        */
        coralStore = (CoralStore)mockCoralStore.proxy();
        mockCoralSchema = mock(CoralSchema.class);
        mockCoralSchema.stubs().method("getResourceClass").will(returnValue(resourceClass));
        coralSchema = (CoralSchema)mockCoralSchema.proxy();
        mockCoralSecurity = mock(CoralSecurity.class);
        coralSecurity = (CoralSecurity)mockCoralSecurity.proxy();
        mockAttributeClass = mock(AttributeClass.class);
        attributeClass = (AttributeClass)mockAttributeClass.proxy();
        mockAttributeClass.stubs().method("getJavaClass").will(returnValue(Resource.class));
        mockAttributeClass.stubs().method("getName").will(returnValue("resource"));
        mockAttributeClass.stubs().method("getDbTable").will(returnValue("coral_attribute_resource"));
        handler = new ResourceAttributeHandler(database, coralStore, coralSecurity, coralSchema, attributeClass);
        mockStatement = mock(Statement.class);
        statement = (Statement)mockStatement.proxy();
        mockConnection = mock(Connection.class);
        mockConnection.stubs().method("createStatement").will(returnValue(statement));
        connection = (Connection)mockConnection.proxy();
        mockResultSet = mock(ResultSet.class);
        resultSet = (ResultSet)mockResultSet.proxy();
        mockStatement.stubs().method("executeQuery").will(returnValue(resultSet));
        mockResultSet.stubs().method("close").isVoid();
        mockStatement.stubs().method("close").isVoid();
        
        node = new NodeImpl(coralSchema, database, logger);
    }

    public void testAttributeHandlerBase()
    {
        assertNotNull(handler);
    }

    public void testCreate() throws Exception
    {
        String stmt = "INSERT INTO " + "coral_attribute_resource" + "(data_key, ref) VALUES (1, " + resource.getIdString() + ")";
        mockStatement.expects(once()).method("execute").with(eq(stmt)).will(returnValue(true));
        handler.create(resource, connection);
    }

    public void testUpdate() throws Exception
    {
        mockResultSet.expects(once()).method("next").will(returnValue(true));
        String stmt2 = "UPDATE coral_attribute_resource SET ref = " + resource.getIdString() + " WHERE data_key = 1";
        mockStatement.expects(once()).method("execute").with(eq(stmt2)).will(returnValue(true));
        handler.update(1, resource, connection);
        mockResultSet.expects(once()).method("next").will(returnValue(false));
        try
        {
            handler.update(1, resource, connection);
            fail("should throw the excpetion");
        }
        catch (EntityDoesNotExistException e)
        {
            //ok!
        }
    }

    public void testRetrieveCreate() throws Exception
    {
        mockResultSet.expects(once()).method("next").will(returnValue(true));
        mockResultSet.expects(once()).method("getLong").will(returnValue(1L));
        handler.retrieve(1, connection);
        mockResultSet.expects(once()).method("next").will(returnValue(false));
        try
        {
            handler.retrieve(1, connection);
            fail("should throw the exception");
        }
        catch (EntityDoesNotExistException e)
        {
            //ok!
        }
    }

    public void testGetSupportedConditions()
    {
        assertEquals(AttributeHandler.CONDITION_EQUALITY, handler.getSupportedConditions());
    }

    public void testGetComparator()
    {
        Object comp = handler.getComparator();
        assertNotNull(comp);
    }

    public void testSupportsExternalString()
    {
        assertEquals(false, handler.supportsExternalString());
    }

    public void testToAttributeValue()
    {
        assertEquals(resource, handler.toAttributeValue("/foo"));
        assertEquals(resource, handler.toAttributeValue("1"));
        try
        {
            handler.toAttributeValue("bar");
            fail("should throw the exception");
        }
        catch (IllegalArgumentException e)
        {
            //ok!
        }
        try
        {
            handler.toAttributeValue("2");
            fail("should throw the exception");
        }
        catch (IllegalArgumentException e)
        {
            //ok!
        }
        try
        {
            handler.toAttributeValue("foo0");
            fail("should throw the exception");
        }
        catch (IllegalArgumentException e)
        {
            //ok!
        }
        try
        {
            handler.toAttributeValue("foo2");
            fail("should throw the exception");
        }
        catch (IllegalArgumentException e)
        {
            //ok!
        }        
    }

    public void testToPrintableString()
    {
        assertEquals("/foo", handler.toPrintableString(resource));
    }

    public void testToExternalString()
    {
        assertEquals(resource.getIdString(), handler.toExternalString(resource));
    }

    public void testCheckDomainString()
    {
        try
        {
            handler.checkDomain("");
            //ok!
        }
        catch (IllegalArgumentException e)
        {
            fail("should throw the exception");
        }
    }

    public void testCheckDomainStringObject()
    {
        handler.checkDomain("", node);
    }

    public void testIsComposite()
    {
        assertEquals(false, handler.isComposite());
    }

    public void testContainsResourceReferences()
    {
        assertEquals(true, handler.containsResourceReferences());
    }

    public void testGetResourceReferences()
    {
        assertEquals(0, handler.getResourceReferences(null).length);
        assertEquals(1, handler.getResourceReferences(resource).length);
    }

    public void testClearResourceReferences()
    {
        assertEquals(true, handler.clearResourceReferences(null));
    }
}
