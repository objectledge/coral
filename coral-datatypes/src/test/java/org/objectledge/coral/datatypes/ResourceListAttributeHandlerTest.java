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
//derived from this software without specific prior written resource_list. 
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.jmock.Mock;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.AttributeHandler;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.Resource;
import org.objectledge.database.Database;
import org.objectledge.utils.LedgeTestCase;

/**
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 *
 */
public class ResourceListAttributeHandlerTest extends LedgeTestCase
{
    private Mock mockDatabase;
    private Database database;
    private Mock mockCoralStore;
    private CoralStore coralStore;
    private Mock mockCoralSecurity;
    private CoralSecurity coralSecurity;
    private Mock mockCoralSchema;
    private CoralSchema coralSchema;
    private Mock mockAttributeClass;
    private AttributeClass attributeClass;

    private Mock mockConnection;
    private Connection connection;
    private Mock mockStatement;
    private Statement statement;
    private Mock mockPreparedStatement;
    private PreparedStatement preparedStatement;
    
    private Mock mockResultSet;
    private ResultSet resultSet;

    private Mock mockResource;
    private Resource resource;
    private ResourceList resourceList;

    private ResourceListAttributeHandler handler;

    public void setUp() throws Exception
    {
        super.setUp();
        mockDatabase = mock(Database.class);
        mockDatabase.stubs().method("getNextId").will(returnValue(1L));
        database = (Database)mockDatabase.proxy();

        mockCoralStore = mock(CoralStore.class);
        mockCoralStore.stubs().method("getResource").will(returnValue(resource));
        coralStore = (CoralStore)mockCoralStore.proxy();
        mockCoralSchema = mock(CoralSchema.class);
        coralSchema = (CoralSchema)mockCoralSchema.proxy();
        mockCoralSecurity = mock(CoralSecurity.class);
        coralSecurity = (CoralSecurity)mockCoralSecurity.proxy();
        mockAttributeClass = mock(AttributeClass.class);
        attributeClass = (AttributeClass)mockAttributeClass.proxy();
        mockAttributeClass.stubs().method("getJavaClass").will(returnValue(ResourceList.class));
        mockAttributeClass.stubs().method("getName").will(returnValue("resource_list"));
        mockAttributeClass.stubs().method("getDbTable").will(returnValue("arl_attribute_resource_list"));
        handler = new ResourceListAttributeHandler(database, coralStore, coralSecurity, coralSchema, attributeClass);
        mockResultSet = mock(ResultSet.class);
        resultSet = (ResultSet)mockResultSet.proxy();
        mockStatement = mock(Statement.class);
        mockStatement.stubs().method("executeQuery").will(returnValue(resultSet));
        statement = (Statement)mockStatement.proxy();
        mockPreparedStatement = mock(PreparedStatement.class);
        //mockPreparedStatement.stubs().method("executeQuery").will(returnValue(resultSet));
        preparedStatement = (PreparedStatement)mockPreparedStatement.proxy();
        
        mockConnection = mock(Connection.class);
        mockConnection.stubs().method("createStatement").will(returnValue(statement));
        connection = (Connection)mockConnection.proxy();
        
        mockResource = mock(Resource.class);
        mockResource.stubs().method("getId").will(returnValue(1L));
        mockResource.stubs().method("getName").will(returnValue("foo"));
        mockResource.stubs().method("getPath").will(returnValue("/foo"));
        resource = (Resource)mockResource.proxy();
        
        ArrayList list = new ArrayList();
        list.add(resource);
        resourceList = new ResourceList(coralStore, list);
    }

    public void testAttributeHandlerBase()
    {
        assertNotNull(handler);
    }

    public void testCreate() throws Exception
    {
        String stmt = "INSERT INTO " + "arl_attribute_resource_list" + "(data_key, pos, ref) VALUES (1, ?, ?)";
        mockConnection.expects(once()).method("prepareStatement").with(eq(stmt)).will(returnValue(preparedStatement));
        mockPreparedStatement.expects(once()).method("setInt");
        mockPreparedStatement.expects(once()).method("setLong");
        mockPreparedStatement.expects(once()).method("addBatch");
        mockPreparedStatement.expects(once()).method("executeBatch").will(returnValue(new int[]{1}));
        
        //mockStatement.expects(once()).method("execute").with(eq(stmt)).will(returnValue(true));
        handler.create(resourceList, connection);
    }

    public void testUpdate() throws Exception
    {
        String stmt = "INSERT INTO " + "arl_attribute_resource_list" + "(data_key, pos, ref) VALUES (1, ?, ?)";
        String deleteStmt = "DELETE FROM arl_attribute_resource_list WHERE data_key = 1";
        mockStatement.expects(once()).method("execute").with(eq(deleteStmt)).will(returnValue(true));
        mockConnection.expects(once()).method("prepareStatement").with(eq(stmt)).will(returnValue(preparedStatement));
        mockPreparedStatement.expects(once()).method("setInt");
        mockPreparedStatement.expects(once()).method("setLong");
        mockPreparedStatement.expects(once()).method("addBatch");
        mockPreparedStatement.expects(once()).method("executeBatch").will(returnValue(new int[]{1}));
        handler.update(1, resourceList, connection);
    }

    public void testRetrieveCreate() throws Exception
    {
        mockResultSet.expects(once()).method("next").will(returnValue(false));
        //mockResultSet.expects(once()).method("getLong").will(returnValue(1L));
        handler.retrieve(1, connection);
    }

    public void testGetSupportedConditions()
    {
        assertEquals(AttributeHandler.CONDITION_NONE, handler.getSupportedConditions());
    }

    public void testGetComparator()
    {
        Object comp = handler.getComparator();
        assertNull(comp);
    }

    public void testSupportsExternalString()
    {
        assertEquals(false, handler.supportsExternalString());
    }

    public void testToAttributeValue()
    {
        try
        {
            handler.toAttributeValue("bar");
            fail("should throw the exception");
        }
        catch (IllegalArgumentException e)
        {
            //ok!
        }
    }

    public void testToPrintableString()
    {
        assertEquals("",handler.toPrintableString(resourceList));
    }

    public void testToExternalString()
    {
        try
        {
            handler.toExternalString(resourceList);
            fail("should throw the exception");
        }
        catch(UnsupportedOperationException e)
        {
           //ok!
        }        
    }

    public void testCheckDomainString()
    {
        try
        {
            handler.checkDomain("");
            fail("should throw the exception");
        }
        catch (IllegalArgumentException e)
        {
            //ok!
        }
    }

    public void testCheckDomainStringObject()
    {
        try
        {
            handler.checkDomain("", "");
            fail("should throw the exception");
        }
        catch (IllegalArgumentException e)
        {
            //ok!
        }

    }

    public void testIsComposite()
    {
        assertEquals(true, handler.isComposite());
    }

    public void testContainsResourceReferences()
    {
        assertEquals(true, handler.containsResourceReferences());
    }

    public void testGetResourceReferences()
    {
        assertEquals(1, handler.getResourceReferences(resourceList).length);
    }

    public void testClearResourceReferences()
    {
        assertEquals(false, handler.clearResourceReferences(resourceList));
    }
}
