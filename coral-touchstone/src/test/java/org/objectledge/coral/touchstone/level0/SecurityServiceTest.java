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
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.security.Permission;
import org.objectledge.coral.security.Role;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.touchstone.CoralTestCase;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: SecurityServiceTest.java,v 1.2 2004-03-15 09:15:07 fil Exp $
 */
public class SecurityServiceTest
    extends CoralTestCase
{
    private Column[] coralRoleColumns = new Column[] 
    {
        new Column("role_id", DataType.BIGINT),
        new Column("name", DataType.VARCHAR)
    };
    
    private Column[] coralRoleImplicationColumns = new Column[]
    {
        new Column("super_role", DataType.BIGINT),
        new Column("sub_role", DataType.BIGINT)
    };
    
    private Column[] coralRoleAssignmentColumns = new Column[]
    {
        new Column("subject_id", DataType.BIGINT),
        new Column("role_id", DataType.BIGINT),
        new Column("grantor", DataType.BIGINT),
        //new Column("grant_time", DataType.TIMESTAMP),
        new Column("granting_allowed", DataType.CHAR)
    }; 
    
    private Column[] coralSubjectColumns = new Column[] 
    {
        new Column("subject_id", DataType.BIGINT),
        new Column("name", DataType.VARCHAR)
    };

    private Column[] coralPermissionColumns = new Column[] 
    {
        new Column("permission_id", DataType.BIGINT),
        new Column("name", DataType.VARCHAR)
    };

    private Column[] coralPermissionAssociationColumns = new Column[]
    {
        new Column("resource_class_id", DataType.BIGINT),
        new Column("permission_id", DataType.BIGINT)
    };
    
    private Column[] coralPermissionAssignmentColumns = new Column[]
    {
        new Column("resource_id", DataType.BIGINT),
        new Column("role_id", DataType.BIGINT),
        new Column("permission_id", DataType.BIGINT),
        new Column("is_inherited", DataType.CHAR),
        new Column("grantor", DataType.BIGINT)
        //new Column("grant_time", DataType.TIMESTAMP),
    };
     
    public void testBuiltinRolesClasses()
    {
        CoralSession session = coralSessionFactory.getAnonymousSession();
        Role[] roles = session.getSecurity().getRole();
        session.close();
        assertTrue(roles.length > 0);
    }

    public void testRoleOperations()
        throws Exception
    {
        CoralSession session = coralSessionFactory.getAnonymousSession();
        Role role = session.getSecurity().createRole("custom_role");
        DefaultTable expectedTable = new DefaultTable("custom_role",
            coralRoleColumns);
        expectedTable.addRow(new Object[] { new Long(3), "custom_role" });
        ITable actualTable = databaseConnection.createQueryTable("custom_role",
            "SELECT * FROM coral_role WHERE name = 'custom_role'");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);

        session.getSecurity().deleteRole(role);
        expectedTable = new DefaultTable("custom_role",
            coralRoleColumns);
        actualTable = databaseConnection.createQueryTable("custom_role",
            "SELECT * FROM coral_role WHERE name = 'custom_role'");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);
    } 
    
    public void testRoleImplicationOperations()
        throws Exception
    {
        CoralSession session = coralSessionFactory.getAnonymousSession();
        Role role = session.getSecurity().createRole("custom_role");
        Role rootRole = session.getSecurity().getRole(Role.ROOT);
        session.getSecurity().addSubRole(rootRole, role);           
        DefaultTable expectedTable = new DefaultTable("coral_role_implication",
            coralRoleImplicationColumns);
        expectedTable.addRow(new Object[] { new Long(1), new Long(3) });
        ITable actualTable = databaseConnection.createDataSet().
            getTable("coral_role_implication");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);
        
        session.getSecurity().deleteSubRole(rootRole, role);
        expectedTable = new DefaultTable("coral_role_implication",
            coralRoleImplicationColumns);
        actualTable = databaseConnection.createDataSet().
            getTable("coral_role_implication");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);
        session.close();                        
    }

    public void testBuiltinSubjectsClasses()
    {
        CoralSession session = coralSessionFactory.getAnonymousSession();
        Subject[] subjects = session.getSecurity().getSubject();
        session.close();
        assertTrue(subjects.length > 0);
    }

    public void testSubjectOperations()
        throws Exception
    {
        CoralSession session = coralSessionFactory.getAnonymousSession();
        Subject subject = session.getSecurity().createSubject("custom_subject");
        DefaultTable expectedTable = new DefaultTable("custom_subject",
            coralSubjectColumns);
        expectedTable.addRow(new Object[] { new Long(3), "custom_subject" });
        ITable actualTable = databaseConnection.createQueryTable("custom_subject",
            "SELECT * FROM coral_subject WHERE name = 'custom_subject'");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);

        session.getSecurity().deleteSubject(subject);
        expectedTable = new DefaultTable("custom_subject",
            coralSubjectColumns);
        actualTable = databaseConnection.createQueryTable("custom_subject",
            "SELECT * FROM coral_subject WHERE name = 'custom_subject'");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);
    } 

    public void testPermissionOperations()
        throws Exception
    {
        CoralSession session = coralSessionFactory.getAnonymousSession();
        Permission permission = session.getSecurity().createPermission("custom_permission");
        DefaultTable expectedTable = new DefaultTable("custom_permission",
            coralPermissionColumns);
        expectedTable.addRow(new Object[] { new Long(0), "custom_permission" });
        ITable actualTable = databaseConnection.createQueryTable("custom_permission",
            "SELECT * FROM coral_permission WHERE name = 'custom_permission'");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);

        session.getSecurity().deletePermission(permission);
        expectedTable = new DefaultTable("custom_permission",
            coralPermissionColumns);
        actualTable = databaseConnection.createQueryTable("custom_permission",
            "SELECT * FROM coral_permission WHERE name = 'custom_permission'");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);
    } 

    public void testPermissionAssociationOperations()
        throws Exception
    {
        CoralSession session = coralSessionFactory.getAnonymousSession();
        Permission permission = session.getSecurity().createPermission("custom_permission");
        ResourceClass nodeClass = session.getSchema().getResourceClass("node");
        session.getSecurity().addPermission(nodeClass, permission);           
        DefaultTable expectedTable = new DefaultTable("coral_permission_association",
            coralPermissionAssociationColumns);
        expectedTable.addRow(new Object[] { new Long(nodeClass.getId()), new Long(permission.getId()) });
        ITable actualTable = databaseConnection.createDataSet().
            getTable("coral_permission_association");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);
        
        session.getSecurity().deletePermission(nodeClass, permission);
        expectedTable = new DefaultTable("coral_permission_association",
            coralPermissionAssociationColumns);
        actualTable = databaseConnection.createDataSet().
            getTable("coral_permission_association");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);
        session.close();                        
    }

    public void testRoleAssignmentOperations()
        throws Exception
    {
        CoralSession session = coralSessionFactory.getRootSession();
        Subject subject = session.getSecurity().createSubject("custom_subject");
        Role role = session.getSecurity().createRole("custom_role");
        session.getSecurity().grant(role, subject, true);
        DefaultTable expectedTable = new DefaultTable("coral_role_assignment",
            coralRoleAssignmentColumns);
        expectedTable.addRow(new Object[] { new Long(subject.getId()), new Long(role.getId()), new Long(1), "1" });
        ITable actualTable = databaseConnection.createQueryTable("coral_role_assignment",
            "SELECT subject_id, role_id, grantor, granting_allowed FROM coral_role_assignment"+
            " WHERE role_id = "+role.getId());
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);
        
        session.getSecurity().revoke(role, subject);
        expectedTable = new DefaultTable("coral_role_assignment",
            coralRoleAssignmentColumns);
        actualTable = databaseConnection.createQueryTable("coral_role_assignment",
            "SELECT subject_id, role_id, grantor, granting_allowed FROM coral_role_assignment"+
            " WHERE role_id = "+role.getId());                    
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);
        session.close();                        
    }

    public void testPermissionAssignmentOperations()
        throws Exception
    {
        CoralSession session = coralSessionFactory.getRootSession();
        Role role = session.getSecurity().createRole("custom_role");
        Permission permission = session.getSecurity().createPermission("custom_permission");
        ResourceClass nodeClass = session.getSchema().getResourceClass("node");
        session.getSecurity().addPermission(nodeClass, permission);
        Resource resource = session.getStore().getResource(1L);
        session.getSecurity().grant(resource, role, permission, true);
        DefaultTable expectedTable = new DefaultTable("coral_permission_assignment",
            coralPermissionAssignmentColumns);
        expectedTable.addRow(new Object[] { new Long(resource.getId()), new Long(role.getId()),  new Long(permission.getId()), "1", new Long(1) });
        ITable actualTable = databaseConnection.createQueryTable("coral_permission_assignment",
            "SELECT resource_id, role_id, permission_id, is_inherited, grantor FROM coral_permission_assignment"+
            " WHERE role_id = "+role.getId());
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);
        
        session.getSecurity().revoke(resource, role, permission);
        expectedTable = new DefaultTable("coral_permission_assignment",
            coralPermissionAssignmentColumns);
        actualTable = databaseConnection.createQueryTable("coral_permission_assignment",
            "SELECT resource_id, role_id, permission_id, is_inherited, grantor FROM coral_permission_assignment"+
            " WHERE role_id = "+role.getId());
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);
        session.close();                        
    }
}
