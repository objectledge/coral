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
import org.objectledge.coral.security.Permission;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.database.Database;
import org.objectledge.test.LedgeTestCase;

/**
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 *
 */
public class PermissionAttributeHandlerTest extends LedgeTestCase
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

    private AttributeClass<Permission> attributeClass;

    private Mock mockConnection;
    private Connection connection;
    private Mock mockStatement;
    private Statement statement;
    private Mock mockResultSet;
    private ResultSet resultSet;

    private Mock mockPermission;
    private Permission permission;

    private PermissionAttributeHandler handler;

    public void setUp() throws Exception
    {
        super.setUp();
        mockDatabase = mock(Database.class);
        mockDatabase.stubs().method("getNextId").will(returnValue(1L));
        database = (Database)mockDatabase.proxy();
        mockPermission = mock(Permission.class);
        mockPermission.stubs().method("getIdObject").will(returnValue(new Long(1L)));
        mockPermission.stubs().method("getIdString").will(returnValue("1"));
        mockPermission.stubs().method("getName").will(returnValue("foo"));
        permission = (Permission)mockPermission.proxy();

        mockCoralStore = mock(CoralStore.class);
        coralStore = (CoralStore)mockCoralStore.proxy();
        mockCoralSchema = mock(CoralSchema.class);
        coralSchema = (CoralSchema)mockCoralSchema.proxy();
        mockCoralSecurity = mock(CoralSecurity.class);
        mockCoralSecurity.stubs().method("getPermission").with(eq("foo")).will(returnValue(new Permission[]{permission}));
        mockCoralSecurity.stubs().method("getPermission").with(eq(1L)).will(returnValue(permission));
        mockCoralSecurity.stubs().method("getPermission").with(eq("bar")).will(throwException(new IllegalArgumentException("foo")));
        mockCoralSecurity.stubs().method("getPermission").with(eq(2L)).will(throwException(new EntityDoesNotExistException("foo")));
        mockCoralSecurity.stubs().method("getPermission").with(eq("foo0")).will(returnValue(new Permission[3]));
        mockCoralSecurity.stubs().method("getPermission").with(eq("foo2")).will(returnValue(new Permission[0]));
        coralSecurity = (CoralSecurity)mockCoralSecurity.proxy();
        mockAttributeClass = mock(AttributeClass.class);
        attributeClass = (AttributeClass<Permission>)mockAttributeClass.proxy();
        mockAttributeClass.stubs().method("getJavaClass").will(returnValue(Permission.class));
        mockAttributeClass.stubs().method("getName").will(returnValue("permission"));
        mockAttributeClass.stubs().method("getDbTable").will(returnValue("coral_attribute_permission"));
        handler = new PermissionAttributeHandler(database, coralStore, coralSecurity, coralSchema, attributeClass);
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
        assertNotNull(handler);
    }

    public void testCreate() throws Exception
    {
        String stmt = "INSERT INTO " + "coral_attribute_permission" + "(data_key, ref) VALUES (1, " + permission.getIdString() + ")";
        mockStatement.expects(once()).method("execute").with(eq(stmt)).will(returnValue(true));
        handler.create(permission, connection);
    }

    public void testUpdate() throws Exception
    {
        mockResultSet.expects(once()).method("next").will(returnValue(true));
        String stmt2 = "UPDATE coral_attribute_permission SET ref = " + permission.getIdString() + " WHERE data_key = 1";
        mockStatement.expects(once()).method("execute").with(eq(stmt2)).will(returnValue(true));
        handler.update(1, permission, connection);
        mockResultSet.expects(once()).method("next").will(returnValue(false));
        try
        {
            handler.update(1, permission, connection);
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
        assertEquals(permission, handler.toAttributeValue("foo"));
        assertEquals(permission, handler.toAttributeValue("1"));
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
        assertEquals("foo", handler.toPrintableString(permission));
    }

    public void testToExternalString()
    {
        assertEquals(permission.getIdString(), handler.toExternalString(permission));
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
