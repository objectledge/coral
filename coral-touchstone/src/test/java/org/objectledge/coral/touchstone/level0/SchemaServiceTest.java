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

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.datatype.DataType;
import org.objectledge.coral.CoralSession;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.touchstone.CoralTestCase;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: SchemaServiceTest.java,v 1.4 2004-03-12 12:01:40 fil Exp $
 */
public class SchemaServiceTest
    extends CoralTestCase
{
    private Column[] coralAttributeClassColumns = new Column[] {
        new Column("attribute_class_id", DataType.BIGINT),
        new Column("name", DataType.VARCHAR),
        new Column("java_class_name", DataType.VARCHAR),
        new Column("handler_class_name", DataType.VARCHAR),
        new Column("db_table_name", DataType.VARCHAR)
    };

    private Column[] coralResourceClassColumns = new Column[] {
        new Column("resource_class_id", DataType.BIGINT),
        new Column("name", DataType.VARCHAR),
        new Column("java_class_name", DataType.VARCHAR),
        new Column("handler_class_name", DataType.VARCHAR),
        new Column("db_table_name", DataType.VARCHAR),
        new Column("flags", DataType.INTEGER)
    };
    
    public void testBuiltinAttributeClasses()
    {
        CoralSession session = coralSessionFactory.getAnonymousSession();
        AttributeClass[] classes = session.getSchema().getAttributeClass();
        session.close();
        assertTrue(classes.length > 0);
    }
    
    public void testAttributeClassOperations()
        throws Exception
    {
        CoralSession session = coralSessionFactory.getAnonymousSession();
        session.getSchema().createAttributeClass("alt_integer", "java.lang.Integer", 
            "org.objectledge.coral.datatypes.IntegerAttributeHandler", "coral_attribute_integer");
        DefaultTable expectedTable = new DefaultTable("alt_integer_attribute_class",
            coralAttributeClassColumns);
        expectedTable.addRow(new Object[] { new Integer(17), "alt_integer", "java.lang.Integer",
            "org.objectledge.coral.datatypes.IntegerAttributeHandler", "coral_attribute_integer"});
        ITable actualTable = databaseConnection.createQueryTable("alt_integer_attribute_class",
            "SELECT * FROM coral_attribute_class WHERE name = 'alt_integer'");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);

        AttributeClass ac = session.getSchema().getAttributeClass("alt_integer");

        session.getSchema().deleteAttributeClass(ac);
        expectedTable = new DefaultTable("alt_integer_attribute_class",
             coralAttributeClassColumns);
        actualTable = databaseConnection.createQueryTable("alt_integer_attribute_class",
             "SELECT * FROM coral_attribute_class WHERE name = 'alt_integer'");                    
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);
    }    
    
    public void testBuiltinResourceClasses()
    {
        CoralSession session = coralSessionFactory.getAnonymousSession();
        ResourceClass[] classes = session.getSchema().getResourceClass();
        session.close();
        assertTrue(classes.length > 0);
    }    

    public void testResourceClassOperations()
        throws Exception
    {
        CoralSession session = coralSessionFactory.getAnonymousSession();
        session.getSchema().createResourceClass("alt_node", 
            "org.objectledge.coral.datatypes.NodeResourceImpl", 
            "org.objectledge.coral.datatypes.GenericResourceHandler", null, 0);
        DefaultTable expectedTable = new DefaultTable("alt_node_resource_class",
            coralResourceClassColumns);
        expectedTable.addRow(new Object[] { new Integer(2), "alt_node", "org.objectledge.coral.datatypes.NodeResourceImpl", 
            "org.objectledge.coral.datatypes.GenericResourceHandler", null, new Integer(0)});
        ITable actualTable = databaseConnection.createQueryTable("alt_node_resource_class",
            "SELECT * FROM coral_resource_class WHERE name = 'alt_node'");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);

        ResourceClass rc = session.getSchema().getResourceClass("alt_node");

        session.getSchema().deleteResourceClass(rc);
        expectedTable = new DefaultTable("alt_node_resource_class",
             coralResourceClassColumns);
        actualTable = databaseConnection.createQueryTable("alt_node_resource_class",
             "SELECT * FROM coral_resource_class WHERE name = 'alt_node'");                    
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);
    }    
}
