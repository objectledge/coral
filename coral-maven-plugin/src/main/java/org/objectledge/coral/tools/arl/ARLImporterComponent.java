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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.sql.DataSource;

import org.objectledge.database.DatabaseUtils;

/**
 * Performs importing data from old style ARL schema database to brand new CORAL scheme.
 *
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: ARLImporterComponent.java,v 1.6 2005-01-13 17:29:33 pablo Exp $
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

    /** the base path */
    private String basePath;

    /** the attribute definition class mapping */
    private Map adcMap;

    /** the resource class mapping */
    private Map rcMap;
    
    /** the attribute definition mapping */
    private Map adMap;
    
    /** permission map */
    private Map pMap;
    
    /** persmission associations map */
    private Map paMap;
    
    /** java class mapping */
    private HashMap javaClassMap; 

    /** cross reference attribute mapping */
    private HashMap xrefMap;
    
    /**
     * Creates new ARLImporterComponent instance.
     * 
     * @param source the source data source.
     * @param target the target data source.
     * @param basePath the base path of ledge installation.
     * @throws Exception if the component could not be initialized.
     */
    public ARLImporterComponent(DataSource source, DataSource target, String basePath) throws Exception
    {
        this.source = source;
        this.target = target;
        this.basePath = basePath;
        adcMap = new HashMap();
        rcMap = new HashMap();
        adMap = new HashMap();
        pMap = new HashMap();
        paMap = new HashMap();
        javaClassMap = new HashMap();
        xrefMap = new HashMap();
    }

    /**
     * Performs arl importing.
     * 
     * @throws Exception if the generation fails for some reason.
     */
    public boolean execute(String mappingFile, boolean arlSchema, 
        boolean appSchema, boolean appData) throws Exception
    {
        System.out.println("ARL Importer started! "+mappingFile);
        prepareDefinedMaps(mappingFile);
        setUpConnections();
        if(arlSchema)
        {
            importAttributeClass();
        }
        else
        {
            prepareArlSchemaMaps();
        }
        if(appSchema)
        {
            importResourceClass();
            importResourceClassInheritance();
            importAttributeDefinition();
            importPermission();
            importPermissionAssociation();
        }
        else
        {
            prepareAppSchemaMaps();
        }
        if(appData)
        {
            importSubject();
            importRole();
            importRoleImplication();
            importRoleAssignment();
            importResource();
            importPermissionAssignment();
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
            importAttributeResourceList();
            importAttributeWeakResourceList();
            importAttributeCrossReference();
        }
        close();
        return true;
    }

    // implementation ///////////////////////////////////////////////////////////////////////////

    private void prepareDefinedMaps(String mappingFile)
        throws Exception
    {
        if(mappingFile == null)
        {
            throw new Exception("The mapping file was not defined - see project.properties");
        }
        System.out.println("Loading mapping from "+mappingFile);
        boolean xrefDef = false;
        String encoding = "UTF-8";
        InputStream is = new FileInputStream(new File(mappingFile));
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(is,encoding));
        int counter = 0;
        while(reader.ready())
        {
            counter++;
            String line = reader.readLine();
            if(line.trim().startsWith("#"))
            {
                continue;
            }
            if(line.contains("@xref"))
            {
                xrefDef = true;
                continue;
            }
            StringTokenizer st = new StringTokenizer(line," ");
            if(xrefDef)
            {
                if(st.countTokens() < 3)
                {
                    throw new Exception("Line "+counter+": Too few tokens for xref mapping");
                }
                String className = st.nextToken();
                String attrName = st.nextToken();
                String relationName = st.nextToken();
                System.out.println("Xref mapping: "+className+":"+attrName+"=>"+relationName);
                Map temp = (Map)xrefMap.get(className);
                if(temp == null)
                {
                    temp = new HashMap();
                    xrefMap.put(className, temp);
                }
                temp.put(attrName, relationName);
            }
            else
            {
                if(st.countTokens() < 2)
                {
                    throw new Exception("Line "+counter+": Too few tokens for java class mapping");
                }
                String oldClassName = st.nextToken();
                String newClassName = st.nextToken();
                System.out.println("Java mapping: "+oldClassName+"=>"+newClassName);
                javaClassMap.put(oldClassName, newClassName);
            }
        }
        is.close();
    }
    
    /**
     * Setup the connections and statement.
     * 
     * @throws Exception if somethings goes bad.
     */
    private void setUpConnections() throws Exception
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
    private void close() throws Exception
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
            targetStmt.execute(
                "INSERT INTO coral_attribute_class"
                    + " (attribute_class_id, name, java_class_name,"                    + " handler_class_name, db_table_name) "
                    + "VALUES ("
                    + attributeClassId
                    + ", '"
                    + name
                    + "','"
                    + javaClassName
                    + "','"
                    + handlerClassName
                    + "','"
                    + dbTableName
                    + "')");
        }
    }

    private void prepareArlSchemaMaps() throws Exception
    {
        //TODO
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_attribute_class");
        while (rs.next())
        {
            long attributeClassId = rs.getLong("attribute_class_id");
            String name = rs.getString("name");
            String javaClassName = rs.getString("java_class_name");
            String handlerClassName = rs.getString("handler_class_name");
            String dbTableName = rs.getString("db_table_name");
        }
        rs = targetStmt.executeQuery("SELECT * FROM coral_attribute_class");
        while (rs.next())
        {
            long attributeClassId = rs.getLong("attribute_class_id");
            String name = rs.getString("name");
            String javaClassName = rs.getString("java_class_name");
            String handlerClassName = rs.getString("handler_class_name");
            String dbTableName = rs.getString("db_table_name");
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
            System.out.println("Loading resourc class: "+
                resourceClassId+","+name+","+javaClassName+","+
                handlerClassName+","+dbTable+","+flags);
            targetStmt.execute(
                "INSERT INTO coral_resource_class"
                    + " (resource_class_id, name, java_class_name, "                     + "handler_class_name, db_table_name, flags) "
                    + "VALUES ("
                    + resourceClassId
                    + ", '"
                    + escape(name)
                    + "','"
                    + javaClassName
                    + "','"
                    + handlerClassName
                    + "','"
                    + dbTable
                    + "',"
                    + flags
                    + ")");
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
                               " (parent, child) " + "VALUES (" + parent + "," + child + ")");
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
            targetStmt.execute(
                "INSERT INTO coral_attribute_definition"
                    + " (attribute_definition_id, resource_class_id, attribute_class_id,"
                    + " domain, name, flags) "
                    + "VALUES ("
                    + attributeDefinitionId
                    + ","
                    + resourceClassId
                    + ","
                    + attributeClassId
                    + ",'"
                    + domain
                    + "','"
                    + escape(name)
                    + "',"
                    + flags
                    + ")");
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
             " (permission_id, name) " + "VALUES (" + permissionId + ",'" + escape(name) + "')");
        }
    }
    
    private void importPermissionAssociation() throws Exception
    {
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_permission_association");
        while (rs.next())
        {
            long resourceClassId = rs.getLong("resource_class_id");
            long permissionId = rs.getLong("permission_id");
            targetStmt.execute(
                "INSERT INTO coral_permission_association" +
                " (resource_class_id, permission_id) " + 
                "VALUES (" + resourceClassId + "," + permissionId + ")");
        }
    }

    private void prepareAppSchemaMaps() throws Exception
    {
        //TODO implement it!
    }

    
    
    private void importSubject() throws Exception
    {
        System.out.println("Subject importing started...");
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_subject");
        while (rs.next())
        {
            long subjectId = rs.getLong("subject_id");
            String name = rs.getString("name");
            System.out.println("Loading subject: "+subjectId+","+name);
            try
            {
                targetStmt.execute(
                    "INSERT INTO coral_subject" + " (subject_id, name) " + 
                    "VALUES (" + subjectId + ",'" + escape(name) + "')");
            }
            catch(Exception e)
            {
                if(subjectId == 1)
                {
                    System.out.println("Root subject already exists!");
                    continue;
                }
                if(subjectId == 2)
                {
                    System.out.println("Anonymous subject already exists!");
                    continue;
                }
                throw e;
            }
        }
        System.out.println("Subject importing finished!\n");
    }

    private void importRole() throws Exception
    {
        System.out.println("Roles importing started...");
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_role");
        while (rs.next())
        {
            long roleId = rs.getLong("role_id");
            String name = rs.getString("name");
            System.out.println("Loading role: "+roleId+","+name);
            try
            {
                targetStmt.execute("INSERT INTO coral_role" + " (role_id, name) " +
                 "VALUES (" + roleId + ",'" + escape(name) + "')");
            }
            catch(Exception e)
            {
                if(roleId == 1)
                {
                    System.out.println("Root role already exists!");
                    continue;
                }
                if(roleId == 2)
                {
                    System.out.println("Nobody role already exists!");
                    continue;
                }
                throw e;
            }
        }
        System.out.println("Roles importing finished!\n");
    }

    private void importRoleImplication() throws Exception
    {
        System.out.println("Role implications importing started...");
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_role_implication");
        System.out.print("\nLoading role implication:");
        while (rs.next())
        {
            long superRole = rs.getLong("super_role");
            long subRole = rs.getLong("sub_role");
            System.out.print(".");
            targetStmt.execute("INSERT INTO coral_role_implication" + 
            " (super_role, sub_role) " + "VALUES (" + superRole + "," + subRole + ")");
        }
        System.out.print("\n");
        System.out.println("Role implications importing finished!\n");
    }

    private void importRoleAssignment() throws Exception
    {
        System.out.println("Role assignments importing started...");
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_role_assignment");
        String stmtContent = "INSERT INTO coral_role_assignment " +
        " (subject_id, role_id, grantor, grant_time, granting_allowed) " +
        " VALUES (?, ?, ?, ?, ?)"; 
        PreparedStatement stmt = targetConn.prepareStatement(stmtContent);
        int size = 0;
        boolean rootAssignment = false;
        Date rootAssignmentDate = null;
        System.out.print("\nLoading role assignment:");
        while (rs.next())
        {
            System.out.print(".");
            long subjectId = rs.getLong("subject_id");
            long roleId = rs.getLong("role_id");
            if(subjectId == 1 && roleId == 1)
            {
                rootAssignment = true;
                rootAssignmentDate = rs.getDate("grant_time");
                continue;
            }
            long grantor = rs.getLong("grantor");
            Date grantTime = rs.getDate("grant_time");
            boolean grantingAllowed = rs.getBoolean("granting_allowed");
            stmt.setLong(1, subjectId);
            stmt.setLong(2, roleId);
            stmt.setLong(3, grantor);
            stmt.setDate(4, grantTime);
            stmt.setBoolean(5, grantingAllowed);
            stmt.addBatch();
            size++;
        }
        System.out.print("\n");
        int[] result = null;
        try
        {
            result = stmt.executeBatch();
        }
        catch(SQLException e)
        {
            throw e.getNextException();
        }
        if (result.length != size)
        {
            throw new Exception("Failed to copy all records from arl_role_assignment" +
            " - source length:" + size + " , copied: " + result.length);
        }
        if(rootAssignment)
        {
            stmt = targetConn.prepareStatement(stmtContent);
            stmt.setLong(1, 1);
            stmt.setLong(2, 1);
            stmt.setLong(3, 1);
            stmt.setDate(4, rootAssignmentDate);
            stmt.setBoolean(5, true);
            stmt.addBatch();
            try
            {
                result = stmt.executeBatch();
            }
            catch(Exception e)
            {
                System.out.println("Root role/subject grant already exists!");
            }
        }
        System.out.println("Role assignments importing finished!\n");
    }

    private void importResource() throws Exception
    {
        System.out.println("Resources importing started...");
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_resource");
        String stmtContent = "INSERT INTO coral_resource "
                    + " (resource_id, resource_class_id, parent, name, created_by, "
                    + "creation_time, owned_by, modified_by, modification_time) "
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"; 
        PreparedStatement stmt = targetConn.prepareStatement(stmtContent);
        ResourceData rootResource = null;
        System.out.print("\nLoading resources:");
        HashMap resourceMap = new HashMap();
        HashSet untouchedSet = new HashSet();
        while (rs.next())
        {
            System.out.print(".");
            ResourceData resourceData = new ResourceData(rs);
            resourceMap.put(resourceData.getId(), resourceData);
            untouchedSet.add(resourceData.getId());
        }
        System.out.print("\n");
        List list = new ArrayList(untouchedSet.size());
        while(!untouchedSet.isEmpty())
        {
            Iterator it = untouchedSet.iterator();
            Long id = (Long)it.next();
            load(id, resourceMap, untouchedSet, list);
        }   
            
        if(list.size() > 0)
        {
            ResourceData rd = (ResourceData)list.get(0);
            rd.addBatch(stmt);
            try
            {
                stmt.executeBatch();
            }
            catch(Exception e)
            {
                System.out.println("Root resource already exists!");
            }
        }
        if(list.size() > 1)
        {
            stmt = targetConn.prepareStatement(stmtContent);
            for(int i = 1; i < list.size(); i++)
            {
                ResourceData rd = (ResourceData)list.get(i);
                rd.addBatch(stmt);
            }
            int[] result = null;
            try
            {
                result = stmt.executeBatch();
            }
            catch(SQLException e)
            {
                throw e.getNextException();
            }
            if (result.length != (list.size()-1))
            {
                throw new Exception("Failed to copy all records from arl_resources" +
                    " - source length:" + (list.size()-1) + " , copied: " + result.length);
            }
        }
        System.out.println("Resources importing finished!\n");
    }

    
    private void load(Long id, Map resourceMap, Set untouchedSet, List list)
        throws Exception
    {
        ResourceData rd = (ResourceData)resourceMap.get(id);
        Long parent = rd.getParentId();
        // check the root or orphants
        if(id.longValue() == 1 || parent.longValue() == -1)
        {
            if(!resourceMap.containsKey(id))
            {
                throw new Exception("failed to find resource: "+id.longValue());
            }
            if(untouchedSet.contains(id))
            {
                if(id.longValue() == 1)
                {
                    list.add(0, rd);
                }
                else
                {
                    list.add(rd);
                }
                untouchedSet.remove(id);
            }
            return;
        }
        if(untouchedSet.contains(parent))
        {
            try
            {
                load(parent, resourceMap, untouchedSet, list);
            }
            catch(Exception e)
            {
                throw new Exception("failed to find parent resource: "+
                    parent.longValue()+" for resource "+id.longValue(), e);
            }
        }
        list.add(rd);
        untouchedSet.remove(id);
    }
    
    private void importPermissionAssignment() throws Exception
    {
        System.out.println("Permission assignments importing started...");
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_permission_assignment");
        
        String stmtContent = "INSERT INTO coral_permission_assignment " +
        " (resource_id, role_id, permission_id, is_inherited, grantor, grant_time) " +
        " VALUES (?, ?, ?, ?, ?, ?)"; 
        PreparedStatement stmt = targetConn.prepareStatement(stmtContent);
        int size = 0;
        boolean rootAssignment = false;
        Date rootAssignmentDate = null;
        System.out.print("\nLoading permission assignment:");
        while (rs.next())
        {
            System.out.print(".");
            long resourceId = rs.getLong("resource_id");
            long roleId = rs.getLong("role_id");
            long permissionId = rs.getLong("permission_id");
            boolean isInherited = rs.getBoolean("is_inherited");
            long grantor = rs.getLong("grantor");
            Date grantTime = rs.getDate("grant_time");
            stmt.setLong(1, resourceId);
            stmt.setLong(2, roleId);
            stmt.setLong(3, permissionId);
            stmt.setBoolean(4, isInherited);
            stmt.setLong(5, grantor);
            stmt.setDate(6, grantTime);
            stmt.addBatch();
            size++;
        }
        System.out.print("\n");
        int[] result = null;
        try
        {
            result = stmt.executeBatch();
        }
        catch(SQLException e)
        {
            throw e.getNextException();
        }
        if (result.length != size)
        {
            throw new Exception("Failed to copy all records from arl_permission_assignments" +
            " - source length:" + size + " , copied: " + result.length);
        }
        System.out.println("Permission assignments importing started...");
    }

    private void importGenericResource() throws Exception
    {
        System.out.println("Generis resource importing started...");
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_generic_resource");
        while (rs.next())
        {
            long resourceId = rs.getLong("resource_id");
            long attributeDefinitionId = rs.getLong("attribute_definition_id");
            long dataKey = rs.getLong("data_key");
            targetStmt.execute(
                "INSERT INTO coral_generic_resource"
                    + " (resource_id, attribute_definition_id, data_key) "
                    + "VALUES ("
                    + resourceId
                    + ","
                    + attributeDefinitionId
                    + ","
                    + dataKey
                    + ")");
        }
        System.out.println("Generis resource importing finished!\n");
    }

    private void importAttributeBoolean() throws Exception
    {
        System.out.println("Boolean attribute importing started...");
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_attribute_boolean");
        while (rs.next())
        {
            long dataKey = rs.getLong("data_key");
            int data = rs.getInt("data");
            targetStmt.execute("INSERT INTO coral_attribute_boolean" + 
            " (data_key, data) " + "VALUES (" + dataKey + "," + data + ")");
        }
        System.out.println("Boolean attribute importing finished!'\n");
    }

    private void importAttributeInteger() throws Exception
    {
        System.out.println("Integer attribute importing started...");
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_attribute_integer");
        while (rs.next())
        {
            long dataKey = rs.getLong("data_key");
            int data = rs.getInt("data");
            targetStmt.execute("INSERT INTO coral_attribute_integer" +
            " (data_key, data) " + "VALUES (" + dataKey + "," + data + ")");
        }
        System.out.println("Integer attribute importing finished!\n");
    }

    private void importAttributeLong() throws Exception
    {
        System.out.println("Long attribute importing started...");
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_attribute_long");
        while (rs.next())
        {
            long dataKey = rs.getLong("data_key");
            long data = rs.getLong("data");
            targetStmt.execute("INSERT INTO coral_attribute_long" + 
            " (data_key, data) " + "VALUES (" + dataKey + "," + data + ")");
        }
        System.out.println("Long attribute importing finished!\n");
    }

    private void importAttributeNumber() throws Exception
    {
        System.out.println("Number attribute importing started...");
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_attribute_number");
        while (rs.next())
        {
            long dataKey = rs.getLong("data_key");
            BigDecimal data = rs.getBigDecimal("data");
            targetStmt.execute("INSERT INTO coral_attribute_number" + 
            " (data_key, data) " + "VALUES (" + dataKey + "," + data + ")");
        }
        System.out.println("Number attribute importing finished!\n");
    }

    private void importAttributeString() throws Exception
    {
        System.out.println("String attribute importing started...");
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_attribute_string");
        while (rs.next())
        {
            long dataKey = rs.getLong("data_key");
            String data = rs.getString("data");
            targetStmt.execute("INSERT INTO coral_attribute_string" + 
            " (data_key, data) " + "VALUES (" + dataKey + ",'" + escape(data) + "')");
        }
        System.out.println("String attribute importing finished!\n");
    }

    private void importAttributeText() throws Exception
    {
        System.out.println("Text attribute importing started...");
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_attribute_text");
        while (rs.next())
        {
            long dataKey = rs.getLong("data_key");
            String data = rs.getString("data");
            targetStmt.execute("INSERT INTO coral_attribute_text" + 
            " (data_key, data) " + "VALUES (" + dataKey + ",'" + escape(data) + "')");
        }
        System.out.println("Text attribute importing finished!\n");
    }

    private void importAttributeDate() throws Exception
    {
        System.out.println("Date attribute importing started...");
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_attribute_date");
        PreparedStatement stmt = targetConn.prepareStatement("INSERT INTO coral_attribute_date " + 
            "(data_key, data) VALUES (?, ?)");
        int size = 0;
        while (rs.next())
        {
            long dataKey = rs.getLong("data_key");
            Date data = rs.getDate("data");
            stmt.setLong(1, dataKey);
            stmt.setDate(2, data);
            stmt.addBatch();
            size++;
        }
        int[] result = stmt.executeBatch();
        if (result.length != size)
        {
            throw new Exception("Failed to copy all records from arl_attribute_date" + 
                                 " - source length:" + size + " , copied: " + result.length);
        }
        System.out.println("Date attribute importing finished!\n");
    }

    private void importAttributeDateRange() throws Exception
    {
        System.out.println("Date range attribute importing started...");
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_attribute_date_range");
        PreparedStatement stmt = targetConn.
            prepareStatement("INSERT INTO coral_attribute_date_range " +
                             "(data_key, startDate, endDate) VALUES (?, ?, ?)");
        int size = 0;
        while (rs.next())
        {
            long dataKey = rs.getLong("data_key");
            Date startDate = rs.getDate("start_date");
            Date endDate = rs.getDate("end_date");
            stmt.setLong(1, dataKey);
            stmt.setDate(2, startDate);
            stmt.setDate(3, endDate);
            stmt.addBatch();
            size++;
        }
        int[] result = stmt.executeBatch();
        if (result.length != size)
        {
            throw new Exception("Failed to copy all records from 'arl_attribute_date_range'" +
                                 " - source length:" + size + " , copied: " + result.length);
        }
    }

    private void importAttributeResourceClass() throws Exception
    {
        System.out.println("Resource class attribute importing started...");
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_attribute_resource_class");
        while (rs.next())
        {
            long dataKey = rs.getLong("data_key");
            long ref = rs.getLong("ref");
            targetStmt.execute("INSERT INTO coral_attribute_resource_class" +
                               " (data_key, ref) " + "VALUES (" + dataKey + "," + ref + ")");
        }
        System.out.println("Resource class attribute importing finished!\n");
    }

    private void importAttributeResource() throws Exception
    {
        System.out.println("Resource attribute importing started...");
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_attribute_resource");
        while (rs.next())
        {
            long dataKey = rs.getLong("data_key");
            long ref = rs.getLong("ref");
            targetStmt.execute("INSERT INTO coral_attribute_resource" + " (data_key, ref) " +
                               "VALUES (" + dataKey + "," + ref + ")");
        }
        System.out.println("Resource attribute importing started!\n");
    }

    private void importAttributeSubject() throws Exception
    {
        System.out.println("Subject attribute importing started...");
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_attribute_subject");
        while (rs.next())
        {
            long dataKey = rs.getLong("data_key");
            long ref = rs.getLong("ref");
            targetStmt.execute("INSERT INTO coral_attribute_subject" + " (data_key, ref) " +
                               "VALUES (" + dataKey + "," + ref + ")");
        }
        System.out.println("Subject attribute importing finished!\n");
    }
    

    private void importAttributeRole() throws Exception
    {
        System.out.println("Role attribute importing started...");
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_attribute_role");
        while (rs.next())
        {
            long dataKey = rs.getLong("data_key");
            long ref = rs.getLong("ref");
            targetStmt.execute("INSERT INTO coral_attribute_role" + " (data_key, ref) " +
                               "VALUES (" + dataKey + "," + ref + ")");
        }
        System.out.println("Role attribute importing finished!\n");
    }

    private void importAttributePermission() throws Exception
    {
        System.out.println("Permission attribute importing started...");
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_attribute_permission");
        while (rs.next())
        {
            long dataKey = rs.getLong("data_key");
            long ref = rs.getLong("ref");
            targetStmt.execute("INSERT INTO coral_attribute_permission" + " (data_key, ref) " +
                               "VALUES (" + dataKey + "," + ref + ")");
        }
        System.out.println("Permission attribute importing finished!\n");
    }

    private void importAttributeResourceList() throws Exception
    {
        System.out.println("Resource list attribute importing started...");
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_attribute_resource_list");
        while (rs.next())
        {
            long dataKey = rs.getLong("data_key");
            int pos = rs.getInt("pos");
            long ref = rs.getLong("ref");
            targetStmt.execute("INSERT INTO coral_attribute_resource_list" + 
                               " (data_key, pos, ref) " + "VALUES (" + dataKey + 
                               "," + pos + "," + ref + ")");
        }
        System.out.println("Resource list attribute importing started!\n");
    }

    private void importAttributeWeakResourceList() throws Exception
    {
        System.out.println("Weak resource list attribute importing started...");
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_attribute_weak_resource_list");
        while (rs.next())
        {
            long dataKey = rs.getLong("data_key");
            int pos = rs.getInt("pos");
            long ref = rs.getLong("ref");
            targetStmt.execute(
                "INSERT INTO coral_attribute_weak_resource_list" + 
                " (data_key, pos, ref) " + "VALUES (" + dataKey + "," + pos + "," + ref + ")");
        }
        System.out.println("Weak resource list attribute importing finished!\n");
    }

    private void importAttributeCrossReference() throws Exception
    {
        System.out.println("Cross reference attribute importing started...");
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_attribute_class WHERE name = 'cross_reference'");
        long acId = -1;
        if(rs.next())
        {
            acId = rs.getLong("attribute_class_id");
        }
        else
        {
            throw new Exception("no cross reference attribute definition");
        }
        String query = "SELECT rc.name, ad.attribute_definition_id, " +
                "ad.name AS adname FROM arl_resource_class rc, " +
                "arl_attribute_definition ad WHERE " +
                "rc.resource_class_id = ad.resource_class_id AND " +
                "ad.attribute_class_id = "+acId;
        rs = sourceStmt.executeQuery(query);
        Map adMap = new HashMap();
        while(rs.next())
        {
            String className = rs.getString("name");
            String attributeName = rs.getString("adname");
            long adId = rs.getLong("attribute_definition_id");
            String relationName = resolveCrossReference(className, attributeName);
            System.out.println("XREF Mapping: "+adId+" => "+relationName);
            adMap.put(new Long(adId), relationName);
        }
        
        Iterator it = adMap.keySet().iterator();
        int relationId = 0;
        query = "SELECT cr.resource1, cr.resource2 FROM " +
        "arl_attribute_cross_reference cr, " +
        "arl_generic_resource gr WHERE " +
        "cr.data_key = gr.data_key AND " +
        "gr.attribute_definition_id = ";
        while(it.hasNext())
        {
            Long adId = (Long)it.next();
            String relationName = (String)adMap.get(adId);
            /**
            if(relationName.trim().equals("IGNORE"))
            {
                continue;
            }
            */
            relationId++;
            targetStmt.execute(
                "INSERT INTO coral_relation " + 
                " (relation_id, name) " + "VALUES (" + relationId + ",'" + relationName + "')");
            rs = sourceStmt.executeQuery(query+adId);
            PreparedStatement stmt = targetConn.prepareStatement("INSERT INTO coral_relation_data " + 
                "(relation_id, resource1, resource2) VALUES (?, ?, ?)");
            int size = 0;
            while (rs.next())
            {
                long resource1 = rs.getLong("resource1");
                long resource2 = rs.getLong("resource2");
                System.out.println("Relation: '"+relationName+"', R1:"+resource1+", R2:"+resource2);
                stmt.setLong(1, relationId);
                stmt.setLong(2, resource1);
                stmt.setLong(3, resource2);
                stmt.addBatch();
                size++;
            }
            int[] result = stmt.executeBatch();
            if (result.length != size)
            {
                throw new Exception("Failed to copy all records from arl_attribute_date" + 
                                     " - source length:" + size + " , copied: " + result.length);
            }
        }
        System.out.println("Cross reference attribute importing finished!\n");
    }

   
    /**
     * Backslash escape the \ and ' characters.
     */
    private static String escape(String string)
    {
        return DatabaseUtils.escapeSqlString(string);
    }

    /**
     * Unescape unicode escapes.
     */
    private static String unescape(String string)
    {
        return DatabaseUtils.unescapeSqlString(string);
    }

    /**
     * Resolve cross reference mapping. 
     */
    private String resolveCrossReference(String className, String attrName)
        throws Exception
    {
        Map temp = (Map)xrefMap.get(className);
        if(temp == null)
        {
            throw new Exception("Couldn't find mapping for attribute '"+attrName+
                "' in class '"+className+"'");
        }
        String relationName = (String)temp.get(attrName);
        if(relationName == null)
        {
            throw new Exception("Couldn't find mapping for attribute '"+attrName+
                "' in class '"+className+"'");
        }
        return relationName;
    }
    
    /**
     * Resolve cross reference mapping. 
     */
    private String resolveJavaClassName(String className)
        throws Exception
    {
        String newClassName = (String)javaClassMap.get(className);
        if(newClassName == null)
        {
            return className;
        }
        return newClassName;
    }
    
    
    private class ResourceData
    {
        long resourceId;
        Long resourceIdLong;
        long resourceClassId;
        long parent;
        Long parentLong;
        String name;
        long createdBy;
        Date creationTime;
        long ownedBy;
        long modifiedBy;
        Date modificationTime;

        public ResourceData(ResultSet rs)
            throws Exception
        {
            resourceId = rs.getLong("resource_id");
            resourceIdLong = new Long(resourceId);
            resourceClassId = rs.getLong("resource_class_id");
            if(resourceId == 1)
            {
                parent = -1;
            }
            else
            {
                parent = rs.getLong("parent");
            }
            parentLong = new Long(parent);
            name = rs.getString("name");
            createdBy = rs.getLong("created_by");
            creationTime = rs.getDate("creation_time");
            ownedBy = rs.getLong("owned_by");
            modifiedBy = rs.getLong("modified_by");
            modificationTime = rs.getDate("modification_time");
        }

        public void addBatch(PreparedStatement stmt)
            throws Exception
        {
            stmt.setLong(1, resourceId);
            stmt.setLong(2, resourceClassId);
            if(parent != -1)
            {
                stmt.setLong(3, parent);
            }
            else
            {
                stmt.setNull(3, Types.BIGINT);
            }
            stmt.setString(4, name);
            stmt.setLong(5, createdBy);
            stmt.setDate(6, creationTime);
            stmt.setLong(7, ownedBy);
            stmt.setLong(8, modifiedBy);
            stmt.setDate(9, modificationTime);
            stmt.addBatch();
        }
        
        public Long getId()
        {
            return resourceIdLong;
        }

        public Long getParentId()
        {
            return parentLong;
        }
    }
}
