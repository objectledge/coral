// 
// Copyright (c) 2003, 2004 Caltha - Gajda, Krzewski, Mach, Potempski Sp.J. 
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

import java.sql.Statement;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.datatype.DataType;
import org.objectledge.coral.entity.EntityExistsException;
import org.objectledge.coral.relation.Relation;
import org.objectledge.coral.relation.RelationModification;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.touchstone.CoralTestCase;
import org.objectledge.database.DatabaseUtils;

/**
 * 
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: CoralRelationTest.java,v 1.1 2004-03-17 14:55:08 zwierzem Exp $
 */
public class CoralRelationTest extends CoralTestCase
{
    private Column[] coralRelationColumns = new Column[] 
    {
        new Column("relation_id", DataType.BIGINT),
        new Column("name", DataType.VARCHAR)
    };
    private Column[] coralRelationDataColumns = new Column[] 
    {
        new Column("relation_id", DataType.BIGINT),
		new Column("resource1", DataType.BIGINT),
		new Column("resource2", DataType.BIGINT)
    };

    private CoralSession session;
    private Relation relation;
    
    public void setUp()
        throws Exception
    {
        super.setUp();
        Statement stmt = databaseConnection.getConnection().createStatement();

		// resources        
        stmt.execute("INSERT INTO coral_resource VALUES(2,1,1,'resource2',1,NOW(),1,1,NOW())");
		stmt.execute("INSERT INTO coral_resource VALUES(3,1,1,'resource3',1,NOW(),1,1,NOW())");
		stmt.execute("INSERT INTO coral_resource VALUES(4,1,1,'resource4',1,NOW(),1,1,NOW())");
		stmt.execute("INSERT INTO coral_resource VALUES(5,1,1,'resource5',1,NOW(),1,1,NOW())");
		stmt.execute("INSERT INTO coral_resource VALUES(6,1,1,'resource6',1,NOW(),1,1,NOW())");
		stmt.execute("INSERT INTO coral_resource VALUES(7,1,1,'resource7',1,NOW(),1,1,NOW())");
		stmt.execute("INSERT INTO coral_resource VALUES(8,1,1,'resource8',1,NOW(),1,1,NOW())");

        DatabaseUtils.close(stmt);
        databaseConnection.close();
        session = coralSessionFactory.getAnonymousSession();

		// create relation ??
		relation = session.getRelationManager().createRelation("relation");
		
		//stmt.execute("INSERT INTO coral_resource VALUES(9,1,1,'resource8',1,NOW(),1,1,NOW())");
    }
    
	public void testCreateRelation()
		throws Exception
	{
		Relation relation2 = null;
		try
        {
			relation2 = session.getRelationManager().createRelation("relation");
            fail("should throw exception");
        }
        catch (EntityExistsException e)
        {
        	// ok
        }
        
		try
        {
            relation2 = session.getRelationManager().createRelation("relation2");
        }
        catch (EntityExistsException e1)
        {
			fail("should not throw exceptions");
        }

		DefaultTable expectedTable = new DefaultTable("coral_relation",
			coralRelationColumns);
		expectedTable.addRow(new Object[] { new Long(relation.getId()), "relation" });
		expectedTable.addRow(new Object[] { new Long(relation2.getId()), "relation2" });

		ITable actualTable = databaseConnection.createQueryTable("coral_relation",
			"SELECT * FROM coral_relation ORDER BY name");            
		databaseConnection.close();
		assertEquals(expectedTable, actualTable);
	}

    public void testUpdateRelation()
        throws Exception
    {
    	// used resources
		Resource res1 = session.getStore().getResource(1L); 
		Resource res2 = session.getStore().getResource(2L);
		Resource res3 = session.getStore().getResource(3L);
		Resource res4 = session.getStore().getResource(4L);
		Resource res5 = session.getStore().getResource(5L);
		Resource res6 = session.getStore().getResource(6L);
		Resource res7 = session.getStore().getResource(7L);
		Resource res8 = session.getStore().getResource(8L);
		

    	// add some data to empty relation
    	RelationModification relmod = new RelationModification();

    	relmod.add(res1, res4);
		relmod.add(res1, res5);
		relmod.add(res1, res6);

		relmod.add(res2, res5);
		relmod.add(res2, res6);

		relmod.add(res3, res6);
    	
		session.getRelationManager().updateRelation(relation, relmod);
    	
        DefaultTable expectedTable = new DefaultTable("coral_relation_data",
            coralRelationDataColumns);
        Long relId = new Long(relation.getId());
        expectedTable.addRow(new Object[] { relId, new Long(1), new Long(4) });
		expectedTable.addRow(new Object[] { relId, new Long(1), new Long(5) });
		expectedTable.addRow(new Object[] { relId, new Long(1), new Long(6) });

		expectedTable.addRow(new Object[] { relId, new Long(2), new Long(5) });
		expectedTable.addRow(new Object[] { relId, new Long(2), new Long(6) });

		expectedTable.addRow(new Object[] { relId, new Long(3), new Long(6) });
        ITable actualTable = databaseConnection.createQueryTable("coral_relation_data",
			"SELECT * FROM coral_relation_data ORDER BY resource1, resource2");            
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);
        
        // modify inverted relation - removals and additions
		relmod = new RelationModification();
        
		relmod.remove(res4, res1);
		relmod.remove(res5, res1);

		relmod.add(res8, res1);
		relmod.add(res8, res2);

		session.getRelationManager().updateRelation(relation.getInverted(), relmod);
		
		expectedTable = new DefaultTable("coral_relation_data", coralRelationDataColumns);
		expectedTable.addRow(new Object[] { relId, new Long(1), new Long(6) });
		expectedTable.addRow(new Object[] { relId, new Long(1), new Long(8) });

		expectedTable.addRow(new Object[] { relId, new Long(2), new Long(5) });
		expectedTable.addRow(new Object[] { relId, new Long(2), new Long(6) });
		expectedTable.addRow(new Object[] { relId, new Long(2), new Long(8) });

		expectedTable.addRow(new Object[] { relId, new Long(3), new Long(6) });

		actualTable = databaseConnection.createQueryTable("coral_relation_data",
			"SELECT * FROM coral_relation_data ORDER BY resource1, resource2");            
		databaseConnection.close();
		assertEquals(expectedTable, actualTable);

		// modify relation - removals only
		relmod = new RelationModification();

		relmod.remove(res1, res8);
		relmod.remove(res2, res8);
		relmod.remove(res2, res5);
		
		session.getRelationManager().updateRelation(relation, relmod);
		
		expectedTable = new DefaultTable("coral_relation_data", coralRelationDataColumns);
		expectedTable.addRow(new Object[] { relId, new Long(1), new Long(6) });

		expectedTable.addRow(new Object[] { relId, new Long(2), new Long(6) });

		expectedTable.addRow(new Object[] { relId, new Long(3), new Long(6) });

		actualTable = databaseConnection.createQueryTable("coral_relation_data",
			"SELECT * FROM coral_relation_data ORDER BY resource1, resource2");            
		databaseConnection.close();
		assertEquals(expectedTable, actualTable);

		// modify relation - clear and additions
		relmod = new RelationModification();
		
		relmod.clear();
		relmod.add(res1, res4);
		relmod.add(res2, res5);
		relmod.add(res3, res6);
		
		session.getRelationManager().updateRelation(relation, relmod);
		
		expectedTable = new DefaultTable("coral_relation_data", coralRelationDataColumns);
		expectedTable.addRow(new Object[] { relId, new Long(1), new Long(4) });

		expectedTable.addRow(new Object[] { relId, new Long(2), new Long(5) });

		expectedTable.addRow(new Object[] { relId, new Long(3), new Long(6) });

		actualTable = databaseConnection.createQueryTable("coral_relation_data",
			"SELECT * FROM coral_relation_data ORDER BY resource1, resource2");            
		databaseConnection.close();
		assertEquals(expectedTable, actualTable);
    }
    
    
}
