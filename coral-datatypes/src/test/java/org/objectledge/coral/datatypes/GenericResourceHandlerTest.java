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
import java.util.HashMap;
import java.util.Map;

import org.jcontainer.dna.Logger;
import org.jmock.builder.Mock;
import org.jmock.builder.MockObjectTestCase;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.Resource;
import org.objectledge.database.Database;

/**
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class GenericResourceHandlerTest extends MockObjectTestCase
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
    private Mock mockResourceClass;
    private ResourceClass resourceClass;
    private Mock mockAttributeDefinition;
    private AttributeDefinition attributeDefinition;

    private Mock mockConnection;
    private Connection connection;
    /*
    private Mock mockStatement;
    private Statement statement;
    private Mock mockResultSet;
    private ResultSet resultSet;
    */
    // not mock  
    private GenericResourceHandler handler;
    private NodeResourceImpl node;
    
    public void setUp() throws Exception
    {
        super.setUp();
        mockDatabase = new Mock(Database.class);
        database = (Database)mockDatabase.proxy();
        mockLogger = new Mock(Logger.class);
        logger = (Logger)mockLogger.proxy();

        mockSubject = new Mock(Subject.class);
        subject = (Subject)mockSubject.proxy();
        
        mockAttributeDefinition = new Mock(AttributeDefinition.class);
        attributeDefinition = (AttributeDefinition)mockAttributeDefinition.proxy();
        
        mockCoralStore = new Mock(CoralStore.class);
        coralStore = (CoralStore)mockCoralStore.proxy();
        mockCoralSecurity = new Mock(CoralSecurity.class);
        mockCoralSecurity.stub().method("getSubject").will(returnValue(subject));
        
        coralSecurity = (CoralSecurity)mockCoralSecurity.proxy();
        mockResourceClass = new Mock(ResourceClass.class);
        mockResourceClass.stub().method("getJavaClass").will(returnValue(NodeResourceImpl.class));
        mockResourceClass.stub().method("getName").will(returnValue("node"));
        mockResourceClass.stub().method("getAttribute").will(returnValue(attributeDefinition));
        resourceClass = (ResourceClass)mockResourceClass.proxy();
        
        mockCoralSchema = new Mock(CoralSchema.class);
        mockCoralSchema.stub().method("getResourceClass").will(returnValue(resourceClass));
        coralSchema = (CoralSchema)mockCoralSchema.proxy();
        
        
        mockResource = new Mock(Resource.class);
        mockResource.stub().method("getResourceClass").will(returnValue(resourceClass));
        resource = (Resource)mockResource.proxy();
        

        mockConnection = new Mock(Connection.class);
        //mockConnection.stub().method("createStatement").will(returnValue(statement));
        connection = (Connection)mockConnection.proxy();
              
              
        node = new NodeResourceImpl(database, logger, coralSchema);
        handler = new GenericResourceHandler(coralSchema, coralSecurity, resourceClass);
                                             
    }
    
    
    public void testGenericResourceHandler()
    {
        assertNotNull(handler);
    }

    public void testCreate()
        throws Exception
    {
        Map attributes = new HashMap();
        //handler.create(resource, attributes, connection);
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
    {
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

    public void testUpdate()
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
