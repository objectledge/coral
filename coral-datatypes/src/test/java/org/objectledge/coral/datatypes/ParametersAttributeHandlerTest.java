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

import org.jcontainer.dna.Logger;
import org.jmock.builder.Mock;
import org.jmock.builder.MockObjectTestCase;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.AttributeHandler;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.database.Database;
import org.objectledge.parameters.DefaultParameters;
import org.objectledge.parameters.Parameters;
import org.objectledge.parameters.db.DBParameters;
import org.objectledge.parameters.db.DBParametersManager;

/**
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 *
 */
public class ParametersAttributeHandlerTest extends MockObjectTestCase
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

    private Mock mockConnection;
    private Connection connection;
    private Mock mockStatement;
    private Statement statement;
    private Mock mockPreparedStatement;
    private PreparedStatement preparedStatement;

    private Mock mockResultSet;
    private ResultSet resultSet;

    private Mock mockParametersManager;
    private DBParametersManager parametersManager;
    private Parameters parameters;
    private DBParameters dbParameters;
    private ParametersAttributeHandler handler;

    public void setUp() throws Exception
    {
        super.setUp();
        mockResultSet = new Mock(ResultSet.class);
        resultSet = (ResultSet)mockResultSet.proxy();
        mockStatement = new Mock(Statement.class);
        mockStatement.stub().method("executeQuery").will(returnValue(resultSet));
        statement = (Statement)mockStatement.proxy();
        mockPreparedStatement = new Mock(PreparedStatement.class);
        mockPreparedStatement.stub().method("executeBatch").will(returnValue(new int[0]));
        //mockPreparedStatement.stub().method("executeQuery").will(returnValue(resultSet));
        preparedStatement = (PreparedStatement)mockPreparedStatement.proxy();

        mockConnection = new Mock(Connection.class);
        mockConnection.stub().method("createStatement").will(returnValue(statement));
        mockConnection.stub().method("prepareStatement").will(returnValue(preparedStatement));
        mockConnection.stub().method("close");
        connection = (Connection)mockConnection.proxy();

        mockDatabase = new Mock(Database.class);
        mockDatabase.stub().method("getConnection").will(returnValue(connection));
        database = (Database)mockDatabase.proxy();
        mockLogger = new Mock(Logger.class);
        logger = (Logger)mockLogger.proxy();

        mockParametersManager = new Mock(DBParametersManager.class);
        parametersManager = (DBParametersManager)mockParametersManager.proxy();

        mockCoralStore = new Mock(CoralStore.class);
        coralStore = (CoralStore)mockCoralStore.proxy();
        mockCoralSchema = new Mock(CoralSchema.class);
        coralSchema = (CoralSchema)mockCoralSchema.proxy();
        mockCoralSecurity = new Mock(CoralSecurity.class);
        coralSecurity = (CoralSecurity)mockCoralSecurity.proxy();
        mockAttributeClass = new Mock(AttributeClass.class);
        attributeClass = (AttributeClass)mockAttributeClass.proxy();
        mockAttributeClass.stub().method("getJavaClass").will(returnValue(Parameters.class));
        mockAttributeClass.stub().method("getName").will(returnValue("parameters"));
/*
        mockAttributeClass.stub().method("getDbTable").will(returnValue("arl_attribute_parameters"));
        */
        handler = new ParametersAttributeHandler(database, coralStore, coralSecurity, coralSchema, attributeClass, parametersManager);

        parameters = new DefaultParameters();
        dbParameters = new DBParameters(parameters, 1, database, logger);

    }

    public void testAttributeHandlerBase()
    {
        assertNotNull(handler);
    }

    public void testCreate() throws Exception
    {
        mockParametersManager.expect(once()).method("createContainer").will(returnValue(dbParameters));
        handler.create(parameters, connection);
    }

    public void testUpdate() throws Exception
    {
        mockParametersManager.expect(once()).method("getParameters").will(returnValue(dbParameters));
        handler.update(1, parameters, connection);
    }
    
    public void testDelete() throws Exception
    {
        mockParametersManager.expect(once()).method("deleteParameters");
        handler.delete(1, connection);
    }
        

    public void testRetrieve() throws Exception
    {
        mockParametersManager.expect(once()).method("getParameters").will(returnValue(dbParameters));
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
        Parameters params = (Parameters)handler.toAttributeValue("@empty");
        assertEquals(parameters.getParameterNames().length, 0);
    }

    public void testToPrintableString()
    {
        assertEquals("", handler.toPrintableString(parameters));
    }

    public void testToExternalString()
    {
        try
        {
            handler.toExternalString(parameters);
            fail("should throw the exception");
        }
        catch (UnsupportedOperationException e)
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
    
    public void testShouldRetrieve()
    {
        assertEquals(true,handler.shouldRetrieveAfterCreate());
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
            handler.getResourceReferences(parameters);
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
            handler.clearResourceReferences(parameters);
            fail("should throw the exception");
        }
        catch (UnsupportedOperationException e)
        {
            //ok!
        }
    }
}
