// 
// Copyright (c) 2003,2004 , Caltha - Gajda, Krzewski, Mach, Potempski Sp.J. 
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
package org.objectledge.coral.tools.arl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.objectledge.utils.StringUtils;

/**
 * Performs wrapper generation.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: ARLImporterComponent.java,v 1.1 2004-04-09 06:46:39 pablo Exp $
 */
public class ARLImporterComponent
{
    /** The source data source. */
    private DataSource source;

    /** The target data source. */
    private DataSource target;

    /** The source connection */
    private Connection sourceConn;
    
    /** The target connection */
    private Connection targetConn;
    
    /** The source statement */
    private Statement sourceStmt;
    
    /** The target statement */
    private Statement targetStmt;


    /**
     * Creates new ARLImporterComponent instance.
     * 
     * @param source the source data source.
     * @param target the target data source.
     * @throws Exception if the component could not be initialized.
     */
    public ARLImporterComponent(DataSource source, DataSource target) throws Exception
    {
        this.source = source;
        this.target = target;
    }

    /**
     * Performs arl importing.
     * 
     * @throws Exception if the generation fails for some reason.
     */
    public void execute() throws Exception
    {
        setUpConnections();
        importAttributeClass();
        importResourceClass();
        importResourceClassInheritance();
        importAttributeDefinition();
        importSubject();
        importRole();
        importRoleImplication();
        importRoleAssignment();
        importPermission();
        importResourceClassPermission();
        importResource();
        importPermissionAssignment();
        importPermissionAssociation();
        importGenericResource();
        importAttributeBoolean();
        importAttributeInteger();
        importAttributeLong();
        importAttributeNumber();
        importAttributeString();
        importAttributeText();
        importAttributeDate();
        importAttributeDateRange();
        importAttributeResourceClass();
        importAttributeResource();
        importAttributeSubject();
        importAttributeRole();
        importAttributePermission();
        importAttributeCrossReference();
        importAttributeResourceList();
        importAttributeWeakResourceList();
        close();
    }

    // implementation ///////////////////////////////////////////////////////////////////////////

    /**
     * Setup the connections and statement.
     * 
     * @throws Exception if somethings goes bad.
     */
    private void setUpConnections()
        throws Exception
    {
        sourceConn = source.getConnection();
        targetConn = target.getConnection();
        sourceStmt = sourceConn.createStatement();
        targetStmt = targetConn.createStatement();
    }
    
    
    /**
     * Close all the connections.
     * 
     * @throws Exception if something goes bad.
     */
    private void close()
        throws Exception
    {
        sourceConn.close();
        targetConn.close();
    }
    
    private void importAttributeClass() throws Exception
    {
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_attribute_class");
        while (rs.next())
        {
             long attributeClassId = rs.getLong("attribute_class_id");
             String name = rs.getString("name");
             String javaClassName = rs.getString("java_class_name");
             String handlerClassName = rs.getString("handler_class_name");
             String dbTableName = rs.getString("db_table_name");
             targetStmt.execute("INSERT INTO coral_attribute_class" +
             " (attribute_class_id, name, java_class_name, handler_class_name, db_table_name) " +             "VALUES (" + attributeClassId+ ", '" + name + "','"+javaClassName+"','"+
             handlerClassName+"','"+ dbTableName + "')"); 
        }        
    }
    
    private void importResourceClass() throws Exception
    {
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_resource_class");
        while (rs.next())
        {
            long resourceClassId = rs.getLong("resource_class_id");
            String name = rs.getString("name");
            String javaClassName = rs.getString("java_class_name");
            String handlerClassName = rs.getString("handler_class_name");
            String dbTable = rs.getString("db_table");
            int flags = rs.getInt("flags");
            targetStmt.execute("INSERT INTO coral_resource_class" +
            " (resource_class_id, name, java_class_name, handler_class_name, db_table, flags) " +            "VALUES (" + resourceClassId+ ", '" + name + "','"+javaClassName+"','"+handlerClassName+
              "','"+ dbTable + "',"+flags+")"); 
        }
    }

    private void importResourceClassInheritance() throws Exception
    {
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_resource_class_inheritance");
        while (rs.next())
        {
            long parent = rs.getLong("parent");
            long child = rs.getLong("child");
            targetStmt.execute("INSERT INTO coral_resource_class_inheritance" +
            " (parent, child) " + "VALUES (" + parent+ "," + child +")"); 
        }        
    }

    private void importAttributeDefinition() throws Exception
    {
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_attribute_definition");
        while (rs.next())
        {
            long attributeDefinitionId = rs.getLong("attribute_definition_id");
            long resourceClassId = rs.getLong("resource_class_id");
            long attributeClassId = rs.getLong("attribute_class_id");
            String domain = rs.getString("domain");
            String name = rs.getString("name");
            int flags = rs.getInt("flags");
            targetStmt.execute("INSERT INTO coral_attribute_definition" +
            " (attribute_definition_id, resource_class_id, attribute_class_id," +            " domain, name, flags) " + "VALUES (" + attributeDefinitionId + "," + 
            resourceClassId+ ","+attributeClassId+ ",'" + domain + "','"+name+"',"+flags+")"); 
        }    
    }
    
    private void importSubject() throws Exception
    {
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_subject");
        while (rs.next())
        {
            long subjectId = rs.getLong("subject_id");
            String name = rs.getString("name");
            long supervisor = rs.getLong("supervisor");
            targetStmt.execute("INSERT INTO coral_subject" +
            " (subject_id, name, supervisor) " + "VALUES (" + 
            subjectId+ ",'"+name+"'," + supervisor +")"); 
        }        
    }

    private void importRole() throws Exception
    {
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_role");
        while (rs.next())
        {
            long roleId = rs.getLong("role_id");
            String name = rs.getString("name");
            targetStmt.execute("INSERT INTO coral_role" +
            " (role_id, name) " + "VALUES (" + roleId+ ",'"+name+"')"); 
        }        
    }

    private void importRoleImplication() throws Exception
    {
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_role_implication");
        while (rs.next())
        {
            long superRole = rs.getLong("super_role");
            long subRole = rs.getLong("sub_role");
            targetStmt.execute("INSERT INTO coral_resource_class_inheritance" +
            " (super_role, sub_role) " + "VALUES (" + superRole+ "," + subRole +")"); 
        }        
    }

    private void importRoleAssignment() throws Exception
    {
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_role_assignment");
        while (rs.next())
        {
            long subjectId = rs.getLong("subject_id");
            long roleId = rs.getLong("role_id");
            long grantor = rs.getLong("grantor");
            String grantTime = rs.getString("grant_time");
            boolean grantingAllowed = rs.getBoolean("granting_allowed");
            targetStmt.execute("INSERT INTO coral_role_assignment" +
            " (subject_id, role_id, grantor, grant_time, granting_allowed) " +
            "VALUES (" + subjectId+ "," + roleId + ","+ grantor+
            ","+grantTime+","+grantingAllowed+")"); 
        }        
    }

    private void importPermission() throws Exception
    {
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_permission");
        while (rs.next())
        {
            long permissionId = rs.getLong("permission_id");
            String name = rs.getString("name");
            targetStmt.execute("INSERT INTO coral_permission" +
            " (permission_id, name) " + "VALUES (" + permissionId+ ",'"+name+"')"); 
        }        
    }

    private void importResourceClassPermission() throws Exception
    {
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_resource_class_permission");
        while (rs.next())
        {
            long resourceClassId = rs.getLong("resource_class_id");
            long permissionId = rs.getLong("permission_id");
            targetStmt.execute("INSERT INTO coral_resource_class_permission" +
            " (resource_class_id, permission_id) " +
            "VALUES (" + resourceClassId+ ","+permissionId+")"); 
        }        
    }
    
    private void importResource() throws Exception
    {
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_resource");
        while (rs.next())
        {
             long resourceId = rs.getLong("resource_id");
             long resourceClassId = rs.getLong("resource_class_id");
             long parent = rs.getLong("parent");
             String name = rs.getString("name");
             long createdBy = rs.getLong("created_by");
             String creationTime = rs.getString("creation_time");
             long ownerBy = rs.getLong("owned_by");
             long modifiedBy = rs.getLong("modified_by");
             String modificationTime = rs.getString("modification_time");
             targetStmt.execute("INSERT INTO coral_resource" +
             " (resource_id, resource_class_id, parent, name, created_by, creation_time" +             "owned_by, modified_by, modification_time) " +
             "VALUES (" + resourceId+ ","+resourceClassId+","+ parent +", '" + name +
             "',"+createdBy+",'"+creationTime+"',"+ownerBy+","+modifiedBy+",'"+
             modificationTime + "')"); 
        }        
    }

    private void importPermissionAssignment() throws Exception
    {
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_permission_assignment");
        while (rs.next())
        {
            long resourceId = rs.getLong("resource_id");
            long roleId = rs.getLong("role_id");
            long permissionId = rs.getLong("permission_id");
            boolean isInherited = rs.getBoolean("is_inherited");
            long grantor = rs.getLong("grantor");
            String grantTime = rs.getString("grant_time");
            targetStmt.execute("INSERT INTO coral_permission_assignment" +
            " (resource_id, role_id, permission_id, is_inherited, grantor, grant_time) " +
            "VALUES (" + resourceId+ "," + roleId + ","+permissionId+","+ 
            isInherited+","+grantor+","+grantTime+")"); 
        }
    }

    private void importPermissionAssociation() throws Exception
    {
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_permission_association");
        while (rs.next())
        {
            long resourceClassId = rs.getLong("resource_class_id");
            long permissionId = rs.getLong("permission_id");
            targetStmt.execute("INSERT INTO coral_permission_association" +
            " (resource_class_id, permission_id) " +
            "VALUES (" + resourceClassId+ ","+permissionId+")"); 
        }        
    }


    private void importGenericResource() throws Exception
    {
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_generic_resource");
        while (rs.next())
        {
            long resourceId = rs.getLong("resource_id");
            long attributeDefinitionId = rs.getLong("attribute_definition_id");
            long dataKey = rs.getLong("data_key");
            targetStmt.execute("INSERT INTO coral_generic_resource" +
            " (resource_id, attribute_definition_id, data_key) " +
            "VALUES (" + resourceId+ ","+attributeDefinitionId+","+dataKey+")"); 
        }        
    }

    private void importAttributeBoolean() throws Exception
    {
        throw new UnsupportedOperationException("not implemented yet");        
    }

    private void importAttributeInteger() throws Exception
    {
        throw new UnsupportedOperationException("not implemented yet");        
    }

    private void importAttributeLong() throws Exception
    {
        throw new UnsupportedOperationException("not implemented yet");        
    }

    private void importAttributeNumber() throws Exception
    {
        throw new UnsupportedOperationException("not implemented yet");        
    }

    private void importAttributeString() throws Exception
    {
        throw new UnsupportedOperationException("not implemented yet");        
    }

    private void importAttributeText() throws Exception
    {
        throw new UnsupportedOperationException("not implemented yet");        
    }

    private void importAttributeDate() throws Exception
    {
        throw new UnsupportedOperationException("not implemented yet");        
    }

    private void importAttributeDateRange() throws Exception
    {
        throw new UnsupportedOperationException("not implemented yet");        
    }

    private void importAttributeResourceClass() throws Exception
    {
        throw new UnsupportedOperationException("not implemented yet");        
    }

    private void importAttributeResource() throws Exception
    {
        throw new UnsupportedOperationException("not implemented yet");        
    }

    private void importAttributeSubject() throws Exception
    {
        throw new UnsupportedOperationException("not implemented yet");        
    }

    private void importAttributeRole() throws Exception
    {
        throw new UnsupportedOperationException("not implemented yet");        
    }
    
    private void importAttributePermission() throws Exception
    {
        throw new UnsupportedOperationException("not implemented yet");        
    }

    private void importAttributeCrossReference() throws Exception
    {
        throw new UnsupportedOperationException("not implemented yet");        
    }

    private void importAttributeResourceList() throws Exception
    {
        throw new UnsupportedOperationException("not implemented yet");        
    }

    private void importAttributeWeakResourceList() throws Exception
    {
        throw new UnsupportedOperationException("not implemented yet");        
    }
    
    /**
     * Backslash escape the \ and ' characters.
     */
    private static String escape(String string)
    {
        return StringUtils.backslashEscape(StringUtils.escapeNonASCIICharacters(string), "'\\");
    }

    /**
     * Unescape unicode escapes.
     */
    private static String unescape(String string)
    {
        return StringUtils.expandUnicodeEscapes(string);
    }
    
    
    
    
    
    
}
