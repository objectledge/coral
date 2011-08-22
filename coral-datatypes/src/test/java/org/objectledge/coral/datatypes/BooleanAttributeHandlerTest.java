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
//derived from this software without specific prior written permission. 
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

import org.jmock.Mock;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.AttributeHandler;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.database.Database;
import org.objectledge.test.LedgeTestCase;

/**
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 *
 */
public class BooleanAttributeHandlerTest extends LedgeTestCase
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
    private Mock mockResultSet;
    private ResultSet resultSet;

    private BooleanAttributeHandler booleanHandler;

    public void setUp() throws Exception
    {
        super.setUp();
        mockDatabase = mock(Database.class);
        mockDatabase.stubs().method("getNextId").will(returnValue(1L));
        database = (Database)mockDatabase.proxy();

        mockCoralStore = mock(CoralStore.class);
        coralStore = (CoralStore)mockCoralStore.proxy();
        mockCoralSchema = mock(CoralSchema.class);
        coralSchema = (CoralSchema)mockCoralSchema.proxy();
        mockCoralSecurity = mock(CoralSecurity.class);
        coralSecurity = (CoralSecurity)mockCoralSecurity.proxy();
        mockAttributeClass = mock(AttributeClass.class);
        attributeClass = (AttributeClass)mockAttributeClass.proxy();
        mockAttributeClass.stubs().method("getJavaClass").will(returnValue(Boolean.class));
        mockAttributeClass.stubs().method("getName").will(returnValue("boolean"));
        mockAttributeClass.stubs().method("getDbTable").will(returnValue("coral_attribute_boolean"));
        booleanHandler = new BooleanAttributeHandler(database, coralStore, coralSecurity, coralSchema, attributeClass);
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
    }

    public void testAttributeHandlerBase()
    {
        assertNotNull(booleanHandler);
    }

    public void testDelete() throws Exception
    {
        //mockResultSet.expects(once()).method("next").will(returnValue(true));
        String stmt = "DELETE FROM coral_attribute_boolean WHERE data_key = 1";
        mockStatement.expects(once()).method("execute").with(eq(stmt)).will(returnValue(true));
        booleanHandler.delete(1, connection);
    }


    public void testCreate() throws Exception
    {
        String stmt = "INSERT INTO " + "coral_attribute_boolean" + "(data_key, data) VALUES (" + 1 + ", " + 1 + ")";
        mockStatement.expects(once()).method("execute").with(eq(stmt)).will(returnValue(true));
        booleanHandler.create(new Boolean(true), connection);
        String stmt2 = "INSERT INTO coral_attribute_boolean(data_key, data) VALUES (1, 0)";
        mockStatement.expects(once()).method("execute").with(eq(stmt2)).will(returnValue(true));
        booleanHandler.create(new Boolean(false), connection);
    }

    public void testUpdate() throws Exception
    {
        mockResultSet.expects(once()).method("next").will(returnValue(true));
        String stmt2 = "UPDATE coral_attribute_boolean SET data = 1 WHERE data_key = 1";
        mockStatement.expects(once()).method("execute").with(eq(stmt2)).will(returnValue(true));
        booleanHandler.update(1,Boolean.TRUE,connection);
        mockResultSet.expects(once()).method("next").will(returnValue(true));
        stmt2 = "UPDATE coral_attribute_boolean SET data = 0 WHERE data_key = 1";
        mockStatement.expects(once()).method("execute").with(eq(stmt2)).will(returnValue(true));
        booleanHandler.update(1,Boolean.FALSE,connection);
        mockResultSet.expects(once()).method("next").will(returnValue(false));
        try
        {
            booleanHandler.update(1,Boolean.FALSE,connection);
            fail("should throw the excpetion");
        }
        catch(EntityDoesNotExistException e)
        {
            //ok!
        }
    }

    public void testRetrieve() throws Exception
    {
        mockResultSet.expects(once()).method("next").will(returnValue(true));
        mockResultSet.expects(once()).method("getBoolean").will(returnValue(false));
        booleanHandler.retrieve(1, connection);
        mockResultSet.expects(once()).method("next").will(returnValue(false));
        try
        {
            booleanHandler.retrieve(1, connection);
            fail("should throw the exception");
        }
        catch (EntityDoesNotExistException e)
        {
            //ok!
        }
    }

    public void testGetSupportedConditions()
    {
        assertEquals(AttributeHandler.CONDITION_EQUALITY, booleanHandler.getSupportedConditions());
    }

    public void testGetComparator()
    {
        Object comp = booleanHandler.getComparator();
        assertNotNull(comp);
    }

    public void testSupportsExternalString()
    {
        assertEquals(true, booleanHandler.supportsExternalString());
    }

    public void testToAttributeValue()
    {
        assertEquals(Boolean.TRUE, booleanHandler.toAttributeValue("true"));
        assertEquals(Boolean.FALSE, booleanHandler.toAttributeValue("false"));
        try
        {
            booleanHandler.toAttributeValue("foo");
            fail("should throw the exception");
        }
        catch(IllegalArgumentException e)
        {
            //ok!
        }
        try
        {
            booleanHandler.toAttributeValue(null);
            fail("should throw the exception");
        }
        catch(IllegalArgumentException e)
        {
            //ok!
        }        
    }

    public void testToPrintableString()
    {
        assertEquals("true", booleanHandler.toPrintableString(Boolean.TRUE));
        assertEquals("false", booleanHandler.toPrintableString(Boolean.FALSE));
    }

    public void testToExternalString()
    {
        assertEquals("1", booleanHandler.toExternalString(Boolean.TRUE));
        assertEquals("0", booleanHandler.toExternalString(Boolean.FALSE));
    }

    public void testCheckDomainString()
    {
        try
        {
            booleanHandler.checkDomain("");
            fail("should throw the exception");
        }
        catch (IllegalArgumentException e)
        {
            //ok!
        }
    }

    public void testIsComposite()
    {
        assertEquals(false, booleanHandler.isComposite());
    }

    public void testContainsResourceReferences()
    {
        assertEquals(false, booleanHandler.containsResourceReferences());
    }

    public void testGetResourceReferences()
    {
        try
        {
            booleanHandler.getResourceReferences(null);
            fail("should throw the exception");
        }
        catch(UnsupportedOperationException e)
        {
            //ok!
        }
    }

    public void testClearResourceReferences()
    {
        try
        {
            booleanHandler.clearResourceReferences(null);
            fail("should throw the exception");
        }
        catch(UnsupportedOperationException e)
        {
            //ok!
        }        
    }
    
    public void testOtherMethods()
    {
        assertEquals(false, booleanHandler.shouldRetrieveAfterCreate());
        //assertEquals(AttributeHandler.CONDITION_NONE, booleanHandler.getSupportedConditions());
        
    }
}
