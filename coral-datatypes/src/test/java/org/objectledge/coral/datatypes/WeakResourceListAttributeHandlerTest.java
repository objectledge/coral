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
//derived from this software without specific prior written weak_resource_list. 
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

import org.jmock.builder.Mock;
import org.jmock.builder.MockObjectTestCase;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.AttributeHandler;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.Resource;
import org.objectledge.database.Database;

/**
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 *
 */
public class WeakResourceListAttributeHandlerTest extends MockObjectTestCase
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
    private WeakResourceList resourceList;

    private WeakResourceListAttributeHandler handler;

    public void setUp() throws Exception
    {
        super.setUp();
        mockDatabase = new Mock(Database.class);
        mockDatabase.stub().method("getNextId").will(returnValue(1L));
        database = (Database)mockDatabase.proxy();

        mockCoralStore = new Mock(CoralStore.class);
        mockCoralStore.stub().method("getResource").will(returnValue(resource));
        coralStore = (CoralStore)mockCoralStore.proxy();
        mockCoralSchema = new Mock(CoralSchema.class);
        coralSchema = (CoralSchema)mockCoralSchema.proxy();
        mockCoralSecurity = new Mock(CoralSecurity.class);
        coralSecurity = (CoralSecurity)mockCoralSecurity.proxy();
        mockAttributeClass = new Mock(AttributeClass.class);
        attributeClass = (AttributeClass)mockAttributeClass.proxy();
        mockAttributeClass.stub().method("getJavaClass").will(returnValue(WeakResourceList.class));
        mockAttributeClass.stub().method("getName").will(returnValue("weak_resource_list"));
        mockAttributeClass.stub().method("getDbTable").will(returnValue("arl_attribute_weak_resource_list"));
        handler = new WeakResourceListAttributeHandler(database, coralStore, coralSecurity, coralSchema, attributeClass);
        mockResultSet = new Mock(ResultSet.class);
        resultSet = (ResultSet)mockResultSet.proxy();
        mockStatement = new Mock(Statement.class);
        mockStatement.stub().method("executeQuery").will(returnValue(resultSet));
        statement = (Statement)mockStatement.proxy();
        mockPreparedStatement = new Mock(PreparedStatement.class);
        //mockPreparedStatement.stub().method("executeQuery").will(returnValue(resultSet));
        preparedStatement = (PreparedStatement)mockPreparedStatement.proxy();
        
        mockConnection = new Mock(Connection.class);
        mockConnection.stub().method("createStatement").will(returnValue(statement));
        connection = (Connection)mockConnection.proxy();
        
        mockResource = new Mock(Resource.class);
        mockResource.stub().method("getId").will(returnValue(1L));
        mockResource.stub().method("getName").will(returnValue("foo"));
        mockResource.stub().method("getPath").will(returnValue("/foo"));
        resource = (Resource)mockResource.proxy();
        
        ArrayList list = new ArrayList();
        list.add(resource);
        resourceList = new WeakResourceList(coralStore, list);
    }

    public void testAttributeHandlerBase()
    {
        assertNotNull(handler);
    }

    public void testCreate() throws Exception
    {
        String stmt = "INSERT INTO " + "arl_attribute_weak_resource_list" + "(data_key, pos, ref) VALUES (1, ?, ?)";
        mockConnection.expect(once()).method("prepareStatement").with(eq(stmt)).will(returnValue(preparedStatement));
        mockPreparedStatement.expect(once()).method("setInt");
        mockPreparedStatement.expect(once()).method("setLong");
        mockPreparedStatement.expect(once()).method("addBatch");
        mockPreparedStatement.expect(once()).method("executeBatch").will(returnValue(new int[]{1}));
        
        //mockStatement.expect(once()).method("execute").with(eq(stmt)).will(returnValue(true));
        handler.create(resourceList, connection);
    }

    public void testUpdate() throws Exception
    {
        String stmt = "INSERT INTO " + "arl_attribute_weak_resource_list" + "(data_key, pos, ref) VALUES (1, ?, ?)";
        String deleteStmt = "DELETE FROM arl_attribute_weak_resource_list WHERE data_key = 1";
        mockStatement.expect(once()).method("execute").with(eq(deleteStmt)).will(returnValue(true));
        mockConnection.expect(once()).method("prepareStatement").with(eq(stmt)).will(returnValue(preparedStatement));
        mockPreparedStatement.expect(once()).method("setInt");
        mockPreparedStatement.expect(once()).method("setLong");
        mockPreparedStatement.expect(once()).method("addBatch");
        mockPreparedStatement.expect(once()).method("executeBatch").will(returnValue(new int[]{1}));
        handler.update(1, resourceList, connection);
    }

    public void testRetrieveCreate() throws Exception
    {
        mockResultSet.expect(once()).method("next").will(returnValue(false));
        //mockResultSet.expect(once()).method("getLong").will(returnValue(1L));
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
        assertEquals(false, handler.containsResourceReferences());
    }

    public void testGetResourceReferences()
    {
        assertEquals(0, handler.getResourceReferences(resourceList).length);
    }

    public void testClearResourceReferences()
    {
        assertEquals(false, handler.clearResourceReferences(resourceList));
    }
}
