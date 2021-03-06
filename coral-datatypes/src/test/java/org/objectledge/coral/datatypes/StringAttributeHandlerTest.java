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
public class StringAttributeHandlerTest extends LedgeTestCase
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

    private AttributeClass<String> attributeClass;

    private Mock mockConnection;
    private Connection connection;
    private Mock mockStatement;
    private Statement statement;
    private Mock mockResultSet;
    private ResultSet resultSet;

    private StringAttributeHandler handler;

    private String longString;

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
        attributeClass = (AttributeClass<String>)mockAttributeClass.proxy();
        mockAttributeClass.stubs().method("getJavaClass").will(returnValue(String.class));
        mockAttributeClass.stubs().method("getName").will(returnValue("string"));
        mockAttributeClass.stubs().method("getDbTable").will(returnValue("coral_attribute_string"));
        handler = new StringAttributeHandler(database, coralStore, coralSecurity, coralSchema, attributeClass);
        mockStatement = mock(Statement.class);
        statement = (Statement)mockStatement.proxy();
        mockConnection = mock(Connection.class);
        mockConnection.stubs().method("createStatement").will(returnValue(statement));
        connection = (Connection)mockConnection.proxy();
        mockResultSet = mock(ResultSet.class);
        resultSet = (ResultSet)mockResultSet.proxy();
        mockStatement.stubs().method("executeQuery").will(returnValue(resultSet));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 26; i++)
        {
            sb.append("1234567890");
        }
        longString = sb.toString();
        mockResultSet.stubs().method("close").isVoid();
        mockStatement.stubs().method("close").isVoid();
    }

    public void testAttributeHandlerBase()
    {
        assertNotNull(handler);
    }

    public void testCreate() throws Exception
    {
        String stmt = "INSERT INTO " + "coral_attribute_string" + "(data_key, data) VALUES (1, '1000')";
        mockStatement.expects(once()).method("execute").with(eq(stmt)).will(returnValue(true));
        handler.create("1000", connection);
        String stmt2 = "INSERT INTO coral_attribute_string(data_key, data) VALUES (1, '1000')";
        mockStatement.expects(once()).method("execute").with(eq(stmt2)).will(returnValue(true));
        handler.create("1000", connection);
        try
        {
            handler.create(longString, connection);
            fail("should throw the excpetion");
        }
        catch(IllegalArgumentException e)
        {
            //ok!
        }
    }

    public void testUpdate() throws Exception
    {
        mockResultSet.expects(once()).method("next").will(returnValue(true));
        String stmt2 = "UPDATE coral_attribute_string SET data = '1000' WHERE data_key = 1";
        mockStatement.expects(once()).method("execute").with(eq(stmt2)).will(returnValue(true));
        handler.update(1, "1000", connection);
        mockResultSet.expects(once()).method("next").will(returnValue(false));
        try
        {
            handler.update(1, "1000", connection);
            fail("should throw the excpetion");
        }
        catch (EntityDoesNotExistException e)
        {
            //ok!
        }
        try
        {
            handler.update(1, longString, connection);
            fail("should throw the excpetion");
        }
        catch(IllegalArgumentException e)
        {
            //ok!
        }
    }

    public void testRetrieveCreate() throws Exception
    {
        mockResultSet.expects(once()).method("next").will(returnValue(true));
        mockResultSet.expects(once()).method("getString").will(returnValue("1000"));
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
        assertEquals(
            AttributeHandler.CONDITION_EQUALITY | AttributeHandler.CONDITION_COMPARISON | AttributeHandler.CONDITION_APPROXIMATION,
            handler.getSupportedConditions());
    }

    public void testGetComparator()
    {
        Object comp = handler.getComparator();
        assertNotNull(comp);
    }

    public void testSupportsExternalString()
    {
        assertEquals(true, handler.supportsExternalString());
    }

    public void testToAttributeValue()
    {
        assertEquals("1000", handler.toAttributeValue("1000"));
    }

    public void testToPrintableString()
    {
        assertEquals("1000", handler.toPrintableString("1000"));
    }

    public void testToExternalString()
    {
        assertEquals("'1000'", handler.toExternalString("1000"));
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
        assertEquals(false, handler.isComposite());
    }

    public void testContainsResourceReferences()
    {
        assertEquals(false, handler.containsResourceReferences());
    }

    public void testGetResourceReferences()
    {
        try
        {
            handler.getResourceReferences(null);
            fail("should throw the exception");
        }
        catch (UnsupportedOperationException e)
        {
            //ok!
        }
    }

    public void testClearResourceReferences()
    {
        try
        {
            handler.clearResourceReferences(null);
            fail("should throw the exception");
        }
        catch (UnsupportedOperationException e)
        {
            //ok!
        }
    }
}
