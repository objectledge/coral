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
import java.util.HashMap;
import java.util.Map;

import org.jcontainer.dna.Logger;
import org.jmock.Mock;
import org.objectledge.coral.Instantiator;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeHandler;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.schema.ResourceClassInheritance;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.Resource;
import org.objectledge.database.Database;
import org.objectledge.utils.LedgeTestCase;

/**
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class GenericResourceHandlerTest extends LedgeTestCase
{
    private Mock mockDatabase;
    private Database database;
    private Mock mockLogger;
    private Logger logger;
    
    
    private Mock mockSubject;
    private Subject subject;

    private Mock mockResource;
    private Resource resource;

    private Mock mockCoralStore;
    private CoralStore coralStore;
    private Mock mockCoralSecurity;
    private CoralSecurity coralSecurity;
    private Mock mockCoralSchema;
    private CoralSchema coralSchema;
    private Mock mockInstantiator;
    private Instantiator instantiator;
    private Mock mockResourceClass;
    private ResourceClass resourceClass;
    private Mock mockAttributeClass;
    private AttributeClass attributeClass;
    private Mock mockAttributeHandler;
    private AttributeHandler attributeHandler;
    
    
    private Mock mockAttributeDefinition;
    private AttributeDefinition attributeDefinition;

    private Mock mockConnection;
    private Connection connection;
    private Mock mockStatement;
    private Statement statement;
    private Mock mockResultSet;
    private ResultSet resultSet;
    // not mock  
    private GenericResourceHandler handler;
    private NodeResourceImpl node;
    
    public void setUp() throws Exception
    {
        super.setUp();
        mockDatabase = mock(Database.class);
        database = (Database)mockDatabase.proxy();
        mockLogger = mock(Logger.class);
        logger = (Logger)mockLogger.proxy();

        mockSubject = mock(Subject.class);
        subject = (Subject)mockSubject.proxy();
        
        mockAttributeHandler = mock(AttributeHandler.class);
        mockAttributeHandler.stub().method("toAttributeValue").will(returnValue("foo"));
        mockAttributeHandler.stub().method("checkDomain");
        mockAttributeHandler.stub().method("shouldRetrieveAfterCreate").will(returnValue(false));
        attributeHandler = (AttributeHandler)mockAttributeHandler.proxy();        
        
        mockAttributeClass = mock(AttributeClass.class);
        mockAttributeClass.stub().method("getHandler").will(returnValue(attributeHandler));
        attributeClass = (AttributeClass)mockAttributeClass.proxy();        
        
        mockAttributeDefinition = mock(AttributeDefinition.class);
        mockAttributeDefinition.stub().method("getFlags").will(returnValue(1));
        mockAttributeDefinition.stub().method("getName").will(returnValue("description"));
        mockAttributeDefinition.stub().method("getAttributeClass").will(returnValue(attributeClass));
        mockAttributeDefinition.stub().method("getDomain").will(returnValue(""));
        mockAttributeDefinition.stub().method("getId").will(returnValue(1L));
        attributeDefinition = (AttributeDefinition)mockAttributeDefinition.proxy();
        
        mockCoralStore = mock(CoralStore.class);
        coralStore = (CoralStore)mockCoralStore.proxy();
        mockCoralSecurity = mock(CoralSecurity.class);
        mockCoralSecurity.stub().method("getSubject").will(returnValue(subject));
        
        coralSecurity = (CoralSecurity)mockCoralSecurity.proxy();
        mockResourceClass = mock(ResourceClass.class);
        mockResourceClass.stub().method("getJavaClass").will(returnValue(NodeResourceImpl.class));
        mockResourceClass.stub().method("getName").will(returnValue("node"));
        mockResourceClass.stub().method("getAttribute").will(returnValue(attributeDefinition));
        mockResourceClass.stub().method("getInheritance").will(returnValue(new ResourceClassInheritance[0]));
        mockResourceClass.stub().method("getDeclaredAttributes").will(returnValue(new AttributeDefinition[]{attributeDefinition}));
        resourceClass = (ResourceClass)mockResourceClass.proxy();
        
        mockCoralSchema = mock(CoralSchema.class);
        mockCoralSchema.stub().method("getResourceClass").will(returnValue(resourceClass));
        mockCoralSchema.stub().method("getAttribute").with(eq(1L)).will(returnValue(attributeDefinition));
        coralSchema = (CoralSchema)mockCoralSchema.proxy();
        
        node = new NodeResourceImpl(coralSchema, database, logger);
        
        mockInstantiator = mock(Instantiator.class);
        mockInstantiator.stub().method("newInstance").will(returnValue(node));
        instantiator = (Instantiator)mockInstantiator.proxy();
        
        mockResource = mock(Resource.class);
        mockResource.stub().method("getResourceClass").will(returnValue(resourceClass));
        mockResource.stub().method("getId").will(returnValue(1L));
        resource = (Resource)mockResource.proxy();

        mockResultSet = mock(ResultSet.class);
        mockResultSet.stub().method("close");
        resultSet = (ResultSet)mockResultSet.proxy();
        
        mockStatement = mock(Statement.class);
        mockStatement.stub().method("close");
        statement = (Statement)mockStatement.proxy();
        mockConnection = mock(Connection.class);
        mockConnection.stub().method("createStatement").will(returnValue(statement));
        connection = (Connection)mockConnection.proxy();              
                
        
        handler = new GenericResourceHandler(coralSchema, coralSecurity, instantiator, resourceClass);
                                             
    }
    
    
    public void testGenericResourceHandler()
    {
        assertNotNull(handler);
    }

    public void testCreateAndDelete()
        throws Exception
    {
        String stmt = "INSERT INTO coral_generic_resource (resource_id, attribute_definition_id, data_key) VALUES (1, 1, 1)";
        Map attributes = new HashMap();
        attributes.put(attributeDefinition, "foo");
        mockAttributeHandler.expect(once()).method("create").with(eq("foo"),ANYTHING).will(returnValue(1L));
        mockStatement.expect(once()).method("execute").with(eq(stmt)).will(returnValue(true));
        NodeResource newResource = (NodeResource)handler.create(resource, attributes, connection);
        assertEquals(newResource.getDescription(),"foo");
        handler.update(newResource, connection);
        newResource.setDescription("bar");
        mockAttributeHandler.expect(once()).method("update").with(eq(1L),eq("foo"),ANYTHING).will(returnValue(1L));
        handler.update(newResource, connection);
        mockAttributeHandler.expect(once()).method("delete").with(eq(newResource.getId()),ANYTHING).will(returnValue(1L));
        stmt = "DELETE FROM coral_generic_resource WHERE  resource_id = 1 AND attribute_definition_id = 1";
        mockStatement.expect(once()).method("execute").with(eq(stmt)).will(returnValue(true));
        
        
        
        
        handler.delete(newResource, connection);
    }

    public void testDelete()
        throws Exception
    {
        handler.delete(node, connection);
    }

    /*
     * Test for Resource retrieve(Resource, Connection)
     */
    public void testRetrieveResourceConnection()
        throws Exception
    {
        String stmt = "SELECT attribute_definition_id, data_key FROM coral_generic_resource WHERE resource_id = 1";
        //mockResultSet.expect(once()).method("next").will(returnValue(true));
        mockResultSet.expect(once()).method("getLong").will(returnValue(1L));
        mockResultSet.expect(once()).method("getLong").will(returnValue(1L));
        mockResultSet.expect(once()).method("next").will(returnValue(false));
        //mockCoralSchema.expect(once()).method("getAttribute").will(returnValue())
        
        mockStatement.expect(once()).method("executeQuery").with(eq(stmt)).will(returnValue(resultSet));
                
        handler.retrieve(resource, connection);
    }

    /*
     * Test for Resource retrieve(Resource, Connection, Map)
     */
    public void testRetrieveResourceConnectionMap()
    {
    }

    /*
     * Test for void revert(Resource, Connection)
     */
    public void testRevertResourceConnection()
    {
        
    }

    /*
     * Test for void revert(Resource, Connection, Map)
     */
    public void testRevertResourceConnectionMap()
    {
    }

    public void testAddAttribute()
    {
    }

    public void testDeleteAttribute()
    {
    }

    public void testAddParentClass()
    {
    }

    public void testDeleteParentClass()
    {
    }

    /*
     * Test for Map getDataKeys(Resource, Connection)
     */
    public void testGetDataKeysResourceConnection()
    {
    }

    /*
     * Test for Map getDataKeys(Connection)
     */
    public void testGetDataKeysConnection()
    {
    }

}
