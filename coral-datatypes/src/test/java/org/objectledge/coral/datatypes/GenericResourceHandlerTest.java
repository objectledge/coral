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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jcontainer.dna.Logger;
import org.jmock.Mock;
import org.objectledge.cache.CacheFactory;
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
import org.objectledge.test.LedgeTestCase;

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
    private Mock mockCacheFactory;
    private CacheFactory cacheFactory;
    
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

    private ResourceClass<Node> resourceClass;
    private Mock mockAttributeClass;

    private AttributeClass<?> attributeClass;
    private Mock mockAttributeHandler;

    private AttributeHandler<?> attributeHandler;
    
    
    private Mock mockAttributeDefinition;

    private AttributeDefinition<?> attributeDefinition;

    private Mock mockConnection;
    private Connection connection;
    private Mock mockStatement;
    private Statement statement;

    private Mock mockPreparedStatement;

    private PreparedStatement preparedStatement;
    private Mock mockResultSet;
    private ResultSet resultSet;
    
    // not mock  
    private GenericResourceHandler<Node> handler;
    private NodeImpl node;
    
    public void setUp() throws Exception
    {
        super.setUp();
        mockDatabase = mock(Database.class);
        database = (Database)mockDatabase.proxy();
        mockLogger = mock(Logger.class);
        logger = (Logger)mockLogger.proxy();
        mockCacheFactory = mock(CacheFactory.class);
        mockCacheFactory.stubs().method("registerForPeriodicExpunge").isVoid();
        cacheFactory = (CacheFactory)mockCacheFactory.proxy();

        mockSubject = mock(Subject.class);
        subject = (Subject)mockSubject.proxy();
        
        mockAttributeHandler = mock(AttributeHandler.class);
        mockAttributeHandler.stubs().method("toAttributeValue").will(returnValue("foo"));
        mockAttributeHandler.stubs().method("checkDomain");
        mockAttributeHandler.stubs().method("shouldRetrieveAfterCreate").will(returnValue(false));
        mockAttributeHandler.stubs().method("create").will(returnValue(1L));
        mockAttributeHandler.stubs().method("delete");
        attributeHandler = (AttributeHandler<?>)mockAttributeHandler.proxy();
        
        mockAttributeClass = mock(AttributeClass.class);
        mockAttributeClass.stubs().method("getHandler").will(returnValue(attributeHandler));
        attributeClass = (AttributeClass<?>)mockAttributeClass.proxy();
        
        mockAttributeDefinition = mock(AttributeDefinition.class);
        mockAttributeDefinition.stubs().method("getFlags").will(returnValue(1));
        mockAttributeDefinition.stubs().method("getName").will(returnValue("description"));
        mockAttributeDefinition.stubs().method("getAttributeClass").will(returnValue(attributeClass));
        mockAttributeDefinition.stubs().method("getDomain").will(returnValue(""));
        mockAttributeDefinition.stubs().method("getId").will(returnValue(1L));
        mockAttributeDefinition.stubs().method("getIdObject").will(returnValue(new Long(1L)));
        mockAttributeDefinition.stubs().method("getIdString").will(returnValue("1"));
        attributeDefinition = (AttributeDefinition<?>)mockAttributeDefinition.proxy();
        
        mockCoralStore = mock(CoralStore.class);
        coralStore = (CoralStore)mockCoralStore.proxy();
        mockCoralSecurity = mock(CoralSecurity.class);
        mockCoralSecurity.stubs().method("getSubject").will(returnValue(subject));
        
        coralSecurity = (CoralSecurity)mockCoralSecurity.proxy();
        mockResourceClass = mock(ResourceClass.class);
        mockResourceClass.stubs().method("getJavaClass").will(returnValue(NodeImpl.class));
        mockResourceClass.stubs().method("getName").will(returnValue("coral.Node"));
        mockResourceClass.stubs().method("getAttribute").will(returnValue(attributeDefinition));
        mockResourceClass.stubs().method("getInheritance").will(returnValue(new ResourceClassInheritance[0]));
        mockResourceClass.stubs().method("getParentClasses").will(returnValue(new ResourceClass[0]));
        mockResourceClass.stubs().method("getDirectParentClasses").will(returnValue(Collections.EMPTY_SET));
        mockResourceClass.stubs().method("getDeclaredAttributes").will(returnValue(new AttributeDefinition[]{attributeDefinition}));
        mockResourceClass.stubs().method("getAllAttributes").will(returnValue(new AttributeDefinition[]{attributeDefinition}));
        mockResourceClass.stubs().method("getMaxAttributeIndex").will(returnValue(new Integer(0)));
        mockResourceClass.stubs().method("getAttributeIndex").will(returnValue(new Integer(0)));
        resourceClass = (ResourceClass<Node>)mockResourceClass.proxy();
        
        mockCoralSchema = mock(CoralSchema.class);
        mockCoralSchema.stubs().method("getResourceClass").will(returnValue(resourceClass));
        mockCoralSchema.stubs().method("getAttribute").with(eq(1L)).will(returnValue(attributeDefinition));
        coralSchema = (CoralSchema)mockCoralSchema.proxy();
        
        node = new NodeImpl();
        
        mockInstantiator = mock(Instantiator.class);
        mockInstantiator.stubs().method("newInstance").will(returnValue(node));
        instantiator = (Instantiator)mockInstantiator.proxy();
        
        mockResource = mock(Resource.class);
        mockResource.stubs().method("getResourceClass").will(returnValue(resourceClass));
        mockResource.stubs().method("getIdObject").will(returnValue(new Long(1L)));
        mockResource.stubs().method("getIdString").will(returnValue("1"));
        mockResource.stubs().method("getId").will(returnValue(1L));
        mockResource.stubs().method("update").isVoid();
        resource = (Resource)mockResource.proxy();

        mockResultSet = mock(ResultSet.class);
        mockResultSet.stubs().method("close");
        resultSet = (ResultSet)mockResultSet.proxy();
        
        mockStatement = mock(Statement.class);
        mockStatement.stubs().method("close");
        mockStatement.stubs().method("execute").will(returnValue(true));
        statement = (Statement)mockStatement.proxy();

        mockPreparedStatement = mock(PreparedStatement.class);
        mockPreparedStatement.stubs().method("close");
        mockPreparedStatement.stubs().method("execute").will(returnValue(true));
        mockPreparedStatement.stubs().method("addBatch").isVoid();
        mockPreparedStatement.stubs().method("executeBatch").will(returnValue(new int[0]));
        mockPreparedStatement.stubs().method("executeQuery").will(returnValue(resultSet));
        mockPreparedStatement.stubs().method("setLong").isVoid();
        preparedStatement = (PreparedStatement)mockPreparedStatement.proxy();

        mockConnection = mock(Connection.class);
        mockConnection.stubs().method("createStatement").will(returnValue(statement));
        mockConnection.stubs().method("prepareStatement").will(returnValue(preparedStatement));
        connection = (Connection)mockConnection.proxy();              
                
        
        handler = new GenericResourceHandler<Node>(coralSchema, resourceClass, database,
            instantiator, cacheFactory, logger);
        mockResourceClass.stubs().method("getHandler").will(returnValue(handler));
                                             
    }
    
    
    public void testGenericResourceHandler()
    {
        assertNotNull(handler);
    }

    public void testCreateAndDelete()
        throws Exception
    {
        String stmt = "INSERT INTO coral_generic_resource (resource_id, attribute_definition_id, data_key) VALUES (?, ?, ?)";
        Map<AttributeDefinition<?>, String> attributes = new HashMap<AttributeDefinition<?>, String>();
        attributes.put(attributeDefinition, "foo");
        mockAttributeHandler.expects(once()).method("create").with(eq("foo"),ANYTHING).will(returnValue(1L));
        mockAttributeHandler.expects(atLeastOnce()).method("isModified").with(eq("foo")).will(returnValue(false));
        // mockStatement.expects(once()).method("execute").with(eq(stmt)).will(returnValue(true));
        Node newResource = (Node)handler.create(resource, attributes, connection);
        //assertEquals(newResource.getDescription(),"foo");
        handler.update(newResource, connection);
        //newResource.setDescription("bar");
        //mockAttributeHandler.expects(once()).method("update").with(eq(1L),eq("foo"),ANYTHING).isVoid();
        handler.update(newResource, connection);
        mockAttributeHandler.expects(once()).method("delete").with(eq(newResource.getId()),ANYTHING).isVoid();
        stmt = "DELETE FROM coral_generic_resource WHERE  resource_id = 1";
        // mockStatement.expects(once()).method("execute").with(eq(stmt)).will(returnValue(true));
        /*
         * mockConnection.expects(once()).method("prepareStatement").with(eq(stmt))
         * .will(returnValue(true));
         * mockPreparedStatement.expects(once()).method("setLong").with(eq(1), eq(1L));
         * mockPreparedStatement.expects(once()).method("setLong").with(eq(2), eq(1L));
         * mockPreparedStatement.expects(once()).method("setLong").with(eq(3), eq(1L));
         * mockPreparedStatement.expects(once()).method("addBatch").isVoid();
         * mockPreparedStatement.expects(once()).method("executeBatch").will(returnValue(new
         * int[0]));
         */
        
        
        
        handler.delete(newResource, connection);
    }

    public void testDelete()
        throws Exception
    {
        Map<AttributeDefinition<?>, String> attrs = new HashMap<AttributeDefinition<?>, String>();
        attrs.put(attributeDefinition, "");
        Resource n = handler.create(resource, attrs, connection);
        handler.delete(n, connection);
    }

    /*
     * Test for Resource retrieve(Resource, Connection)
     */
    public void testRetrieveResourceConnection()
        throws Exception
    {
        String stmt = "SELECT attribute_definition_id, data_key FROM coral_generic_resource WHERE resource_id = 1";
        //mockResultSet.expects(once()).method("next").will(returnValue(true));
        //mockResultSet.expects(once()).method("getLong").will(returnValue(1L));
        //mockResultSet.expects(once()).method("getLong").will(returnValue(1L));
        mockResultSet.expects(once()).method("next").will(returnValue(false));
        //mockCoralSchema.expects(once()).method("getAttribute").will(returnValue(attributeClass));
        
        // mockStatement.expects(once()).method("executeQuery").with(eq(stmt)).will(returnValue(resultSet));
                
        handler.retrieve(resource, connection, null);
    }


    // TODO implement tests below http://objectledge.org/jira/browse/CORAL-71

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
