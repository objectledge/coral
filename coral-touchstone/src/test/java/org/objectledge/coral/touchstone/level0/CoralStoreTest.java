// 
// Copyright (c) 2003, Caltha - Gajda, Krzewski, Mach, Potempski Sp.J. 
// All rights reserved. 
// 
// Redistribution and use in source and binary forms, with or without modification,  
// are permitted provided that the following conditions are met: 
//  
// * Redistributions of source code must retain the above copyright notice,  
//	 this list of conditions and the following disclaimer. 
// * Redistributions in binary form must reproduce the above copyright notice,  
//	 this list of conditions and the following disclaimer in the documentation  
//	 and/or other materials provided with the distribution. 
// * Neither the name of the Caltha - Gajda, Krzewski, Mach, Potempski Sp.J.  
//	 nor the names of its contributors may be used to endorse or promote products  
//	 derived from this software without specific prior written permission. 
// 
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"  
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED  
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
// IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,  
// INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,  
// BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
// OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,  
// WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)  
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE  
// POSSIBILITY OF SUCH DAMAGE. 
// 
package org.objectledge.coral.touchstone.level0;

import java.util.HashMap;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.datatype.DataType;
import org.objectledge.coral.CoralSession;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.touchstone.CoralTestCase;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: CoralStoreTest.java,v 1.1 2004-03-15 09:18:56 fil Exp $
 */
public class CoralStoreTest
    extends CoralTestCase
{
    private Column[] coralResourceColumns = new Column[]
    {
        new Column("resource_id", DataType.BIGINT),
        new Column("resource_class_id", DataType.BIGINT),
        new Column("parent", DataType.BIGINT),
        new Column("name", DataType.VARCHAR),
        new Column("created_by", DataType.BIGINT),
        //new Column("creation_time", DataType.TIMESTAMP),
        new Column("owned_by", DataType.BIGINT),
        new Column("modified_by", DataType.BIGINT)
        //new Column("modification_time", DataType.TIMESTAMP)
    }; 

    public void testBuiltinResources()
        throws Exception
    {
        CoralSession session = coralSessionFactory.getAnonymousSession();
        Resource root = session.getStore().getResource(1L);
        session.close();
        assertNotNull(root);
    }
    
    public void testResourceOperations()
        throws Exception
    {
        CoralSession session = coralSessionFactory.getAnonymousSession();
        Resource root = session.getStore().getResource(1L);
        ResourceClass nodeClass = session.getSchema().getResourceClass("node");
        Resource resource = session.getStore().createResource("resource", root, nodeClass, new HashMap());
        
        DefaultTable expectedTable = new DefaultTable("resource", coralResourceColumns);
        expectedTable.addRow(new Object[] { new Long(resource.getId()), new Long(nodeClass.getId()),
            new Long(root.getId()), "resource", new Long(2), new Long(2), new Long(2)});
        ITable actualTable = databaseConnection.createQueryTable("resource",
            "SELECT resource_id, resource_class_id, parent, name, created_by, owned_by, modified_by FROM coral_resource"+
            " WHERE name = 'resource'");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);
        
        session.getStore().deleteResource(resource);
        expectedTable = new DefaultTable("resource", coralResourceColumns);
        actualTable = databaseConnection.createQueryTable("resource",
            "SELECT resource_id, resource_class_id, parent, name, created_by, owned_by, modified_by FROM coral_resource"+
            " WHERE name = 'resource'");        
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);
        session.close();
    }
}
