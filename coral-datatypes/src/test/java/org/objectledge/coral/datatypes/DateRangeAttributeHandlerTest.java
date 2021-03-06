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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;

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
public class DateRangeAttributeHandlerTest extends LedgeTestCase
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

    private AttributeClass<DateRange> attributeClass;

    private Mock mockConnection;
    private Connection connection;
    private Mock mockStatement;
    private Statement statement;
    private Mock mockPreparedStatement;
    private PreparedStatement preparedStatement;
    private Mock mockResultSet;
    private ResultSet resultSet;
    private DateRange range;
    private java.sql.Timestamp rangeStart;
    private java.sql.Timestamp rangeEnd;
    private DateRangeAttributeHandler handler;

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
        attributeClass = (AttributeClass<DateRange>)mockAttributeClass.proxy();
        mockAttributeClass.stubs().method("getJavaClass").will(returnValue(DateRange.class));
        mockAttributeClass.stubs().method("getName").will(returnValue("date_range"));
        mockAttributeClass.stubs().method("getDbTable").will(returnValue("coral_attribute_date_range"));
        handler = new DateRangeAttributeHandler(database, coralStore, coralSecurity, coralSchema, attributeClass);
        mockStatement = mock(Statement.class);
        statement = (Statement)mockStatement.proxy();
        mockPreparedStatement = mock(PreparedStatement.class);
        preparedStatement = (PreparedStatement)mockPreparedStatement.proxy();
        mockConnection = mock(Connection.class);
        mockConnection.stubs().method("createStatement").will(returnValue(statement));
        connection = (Connection)mockConnection.proxy();
        mockResultSet = mock(ResultSet.class);
        resultSet = (ResultSet)mockResultSet.proxy();
        mockStatement.stubs().method("executeQuery").will(returnValue(resultSet));
        Date d1 = new Date();
        Date d2 = new Date(d1.getTime()+60000);
        range = new DateRange(d1, d2);
        rangeStart = new java.sql.Timestamp(d1.getTime());
        rangeEnd = new java.sql.Timestamp(d2.getTime());
        mockResultSet.stubs().method("close").isVoid();
        mockStatement.stubs().method("close").isVoid();
    }

    public void testAttributeHandlerBase()
    {
        assertNotNull(handler);
    }

    public void testCreate() throws Exception
    {
        String stmt = "INSERT INTO " + "coral_attribute_date_range" + 
            "(data_key, start_date, end_date) VALUES (?, ?, ?)";        
        mockConnection.expects(once()).method("prepareStatement").with(eq(stmt)).will(returnValue(preparedStatement));
        mockPreparedStatement.expects(once()).method("setLong").with(eq(1), eq(1L)).isVoid();
        mockPreparedStatement.expects(once()).method("setTimestamp").with(eq(2), eq(rangeStart)).isVoid();        
        mockPreparedStatement.expects(once()).method("setTimestamp").with(eq(3), eq(rangeEnd)).isVoid();        
        mockPreparedStatement.expects(once()).method("execute").will(returnValue(true));
        mockPreparedStatement.expects(once()).method("close").isVoid();
        handler.create(range, connection);
    }

    public void testUpdate() throws Exception
    {
        mockResultSet.expects(once()).method("next").will(returnValue(true));
        mockStatement.expects(once()).method("close").isVoid();
        String stmt = "UPDATE coral_attribute_date_range SET"+
            " start_date = ?, end_date = ? WHERE data_key = ?";
        mockConnection.expects(atLeastOnce()).method("prepareStatement").with(eq(stmt)).will(returnValue(preparedStatement));
        mockPreparedStatement.expects(once()).method("setTimestamp").with(eq(1), eq(rangeStart)).isVoid();        
        mockPreparedStatement.expects(once()).method("setTimestamp").with(eq(2), eq(rangeEnd)).isVoid();        
        mockPreparedStatement.expects(once()).method("setLong").with(eq(3), eq(1L)).isVoid();
        mockPreparedStatement.expects(once()).method("execute").will(returnValue(true));
        mockPreparedStatement.expects(atLeastOnce()).method("close").isVoid();
        handler.update(1, range, connection);
        mockResultSet.expects(once()).method("next").will(returnValue(false));
        try
        {
            handler.update(1, range, connection);
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
        Timestamp now = new Timestamp(new Date().getTime());
        mockResultSet.expects(once()).method("getTimestamp").will(returnValue(now));
        mockResultSet.expects(once()).method("getTimestamp").will(returnValue(now));
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
            AttributeHandler.CONDITION_NONE,
            handler.getSupportedConditions());
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
        /*
        assertEquals(range.getStart(), 
        ((DateRange)handler.toAttributeValue(range.getStart().toString()+ " :: "+
        range.getEnd().toString())).getStart());
        */
    }

    public void testToPrintableString()
    {
        assertEquals(range.getStart().toString()+ " :: "+
        range.getEnd().toString(), handler.toPrintableString(range));
    }

    public void testToExternalString()
    {
        try
        {
            handler.toExternalString(range);
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
