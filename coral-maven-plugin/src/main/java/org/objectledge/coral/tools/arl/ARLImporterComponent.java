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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
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
import org.objectledge.utils.StringUtils;

/**
 * Performs importing data from old style ARL schema database to brand new CORAL scheme.
 *
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: ARLImporterComponent.java,v 1.11 2005-02-01 23:50:08 pablo Exp $
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

    /** the attribute class mapping */
    private Map acMap;

    /** the attribute class mapping class id to class name */
    private Map acNameMap;

    /** the resource class mapping */
    private Map rcMap;

    /** the attribute definition mapping */
    private Map adMap;

    /** the attribute to attribute class name mapping */
    private Map adNameMap;

    /** permission map */
    private Map pMap;

    /** java class mapping */
    private HashMap javaClassMap;

    /** cross reference attribute mapping */
    private HashMap xrefMap;

    /** classes to ignore */
    private Set classesToIgnore;

    /** ignored resources */
    private Set ignoredResources;

    /** ignored data_keys */
    private HashMap ignoredDataKeys;

    /**
     * Creates new ARLImporterComponent instance.
     * 
     * @param source the source data source.
     * @param target the target data source.
     * @param basePath the base path of ledge installation.
     * @throws Exception if the component could not be initialized.
     */
    public ARLImporterComponent(DataSource source, DataSource target, String basePath)
        throws Exception
    {
        this.source = source;
        this.target = target;
        this.basePath = basePath;
        acMap = new HashMap();
        acNameMap = new HashMap();
        rcMap = new HashMap();
        adMap = new HashMap();
        adNameMap = new HashMap();
        pMap = new HashMap();
        javaClassMap = new HashMap();
        xrefMap = new HashMap();
        classesToIgnore = new HashSet();
        ignoredResources = new HashSet();
        ignoredDataKeys = new HashMap();
    }

    /**
     * Performs arl importing.
     * 
     * @param mappingFile the path of configuration file
     * @param arlSchema should ARL core schema be imported?
     * @param appSchema should application schema (resource classes, permissions) be imported?
     * @param appData should application data (subjects, roles, resources, grants) be imported?
     * @throws Exception if the generation fails for some reason.
     * @return unused true value (?)
     */
    public boolean execute(String mappingFile, boolean arlSchema, boolean appSchema, 
        boolean appData)
        throws Exception
    {
        System.out.println("ARL Importer started! " + mappingFile);
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
            importAttributeParameters();
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
        System.out.println("Loading mapping from " + mappingFile);
        boolean xrefDef = false;
        boolean ignoredClasses = false;
        String encoding = "UTF-8";
        InputStream is = new FileInputStream(new File(mappingFile));
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(is, encoding));
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
            if(line.contains("@ignore"))
            {
                xrefDef = true;
                ignoredClasses = true;
                continue;
            }
            if(!xrefDef)
            {
                StringTokenizer st = new StringTokenizer(line, " ");
                if(st.countTokens() < 2)
                {
                    throw new Exception("Line " + counter
                        + ": Too few tokens for java class mapping");
                }
                String oldClassName = st.nextToken();
                String newClassName = st.nextToken();
                System.out.println("Java mapping: " + oldClassName + "=>" + newClassName);
                javaClassMap.put(oldClassName, newClassName);
                continue;
            }
            if(!ignoredClasses)
            {
                StringTokenizer st = new StringTokenizer(line, " ");
                if(st.countTokens() < 3)
                {
                    throw new Exception("Line " + counter + ": Too few tokens for xref mapping");
                }
                String className = st.nextToken();
                String attrName = st.nextToken();
                String relationName = st.nextToken();
                System.out.println("Xref mapping: " + className + ":" + attrName + "=>"
                    + relationName);
                Map temp = (Map)xrefMap.get(className);
                if(temp == null)
                {
                    temp = new HashMap();
                    xrefMap.put(className, temp);
                }
                temp.put(attrName, relationName);
                continue;
            }
            String classToIgnore = line.trim();
            System.out.println("Class to ignore: " + classToIgnore);
            classesToIgnore.add(classToIgnore);
        }
        is.close();
    }

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

    private void importAttributeClass()
        throws Exception
    {
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_attribute_class");
        while(rs.next())
        {
            long attributeClassId = rs.getLong("attribute_class_id");
            String name = rs.getString("name");
            String javaClassName = rs.getString("java_class_name");
            String handlerClassName = rs.getString("handler_class_name");
            String dbTableName = rs.getString("db_table_name");
            targetStmt.execute("INSERT INTO coral_attribute_class"
                + " (attribute_class_id, name, java_class_name,"
                + " handler_class_name, db_table_name) " + "VALUES (" + attributeClassId + ", '"
                + name + "','" + javaClassName + "','" + handlerClassName + "','" + dbTableName
                + "')");
            Long acId = new Long(attributeClassId);
            acMap.put(acId, acId);
            acNameMap.put(acId, name);
        }
        updateLedgeIdTabel("coral_attribute_class", "attribute_class_id");
    }

    /**
     * Prepares Attribute class map.
     * 
     * @throws Exception
     */
    private void prepareArlSchemaMaps()
        throws Exception
    {
        HashMap nameToId = new HashMap();
        ResultSet rs = targetStmt.executeQuery("SELECT * FROM coral_attribute_class");
        while(rs.next())
        {
            long attributeClassId = rs.getLong("attribute_class_id");
            String name = rs.getString("name");
            String javaClassName = rs.getString("java_class_name");
            nameToId.put(name, new Long(attributeClassId));
        }
        rs = sourceStmt.executeQuery("SELECT * FROM arl_attribute_class");
        while(rs.next())
        {
            long attributeClassId = rs.getLong("attribute_class_id");
            String name = rs.getString("name");
            String trueName = null;
            Long targetId = null;
            if(name.equals("parameter_container"))
            {
                targetId = (Long)nameToId.get("parameters");
                trueName = "parameters";
            }
            else
            {
                targetId = (Long)nameToId.get(name);
                trueName = name;
            }
            if(targetId != null)
            {
                System.out.println("Attribute class " + name + " mapping: " + attributeClassId
                    + " => " + targetId);
                Long acId = new Long(attributeClassId);
                acMap.put(acId, targetId);
                acNameMap.put(targetId, trueName);
            }
            else
            {

                System.out.println("Attribute class " + name + " mapping: " + attributeClassId
                    + " => IGNORE");
            }
        }
        updateLedgeIdTabel("coral_attribute_class", "attribute_class_id");
    }

    private void importResourceClass()
        throws Exception
    {
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_resource_class");
        while(rs.next())
        {
            long resourceClassId = rs.getLong("resource_class_id");
            String name = rs.getString("name");
            String javaClassName = rs.getString("java_class_name");
            String handlerClassName = rs.getString("handler_class_name");
            String dbTable = rs.getString("db_table");
            int flags = rs.getInt("flags");
            System.out.println("Loading resourc class: " + resourceClassId + "," + name + ","
                + javaClassName + "," + handlerClassName + "," + dbTable + "," + flags);
            targetStmt.execute("INSERT INTO coral_resource_class"
                + " (resource_class_id, name, java_class_name, "
                + "handler_class_name, db_table_name, flags) " + "VALUES (" + resourceClassId
                + ", '" + escape(name) + "','" + javaClassName + "','" + handlerClassName + "','"
                + dbTable + "'," + flags + ")");
            Long classId = new Long(resourceClassId);
            rcMap.put(classId, classId);
        }
        updateLedgeIdTabel("coral_resource_class", "resource_class_id");
    }

    private void importResourceClassInheritance()
        throws Exception
    {
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_resource_class_inheritance");
        while(rs.next())
        {
            long parent = rs.getLong("parent");
            long child = rs.getLong("child");
            targetStmt.execute("INSERT INTO coral_resource_class_inheritance" + " (parent, child) "
                + "VALUES (" + parent + "," + child + ")");
        }
    }

    private void importAttributeDefinition()
        throws Exception
    {
        //ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_attribute_definition");
        ResultSet rs = sourceStmt.executeQuery("SELECT *,rc.name AS rcname FROM "
            + "arl_attribute_definition ad, arl_resource_class rc "
            + " WHERE ad.resource_class_id = rc.resource_class_id");
        while(rs.next())
        {
            long attributeDefinitionId = rs.getLong("attribute_definition_id");
            long resourceClassId = rs.getLong("resource_class_id");
            long attributeClassId = rs.getLong("attribute_class_id");
            String className = rs.getString("rcname");
            String domain = rs.getString("domain");
            String name = rs.getString("name");
            int flags = rs.getInt("flags");
            targetStmt.execute("INSERT INTO coral_attribute_definition"
                + " (attribute_definition_id, resource_class_id, attribute_class_id,"
                + " domain, name, flags) " + "VALUES (" + attributeDefinitionId + ","
                + resourceClassId + "," + attributeClassId + ",'" + domain + "','" + escape(name)
                + "'," + flags + ")");
            Long adId = new Long(attributeDefinitionId);
            adMap.put(adId, adId);
            adNameMap.put(adId, className);
        }
        updateLedgeIdTabel("coral_attribute_definition", "attribute_definition_id");
    }

    private void importPermission()
        throws Exception
    {
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_permission");
        while(rs.next())
        {
            long permissionId = rs.getLong("permission_id");
            String name = rs.getString("name");
            targetStmt.execute("INSERT INTO coral_permission" + " (permission_id, name) "
                + "VALUES (" + permissionId + ",'" + escape(name) + "')");
            Long pId = new Long(permissionId);
            pMap.put(pId, pId);
        }
        updateLedgeIdTabel("coral_permission", "permission_id");
    }

    private void importPermissionAssociation()
        throws Exception
    {
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_permission_association");
        while(rs.next())
        {
            long resourceClassId = rs.getLong("resource_class_id");
            long permissionId = rs.getLong("permission_id");
            targetStmt.execute("INSERT INTO coral_permission_association"
                + " (resource_class_id, permission_id) " + "VALUES (" + resourceClassId + ","
                + permissionId + ")");
        }
    }

    private void prepareAppSchemaMaps()
        throws Exception
    {
        //resource classes
        HashMap nameToId = new HashMap();
        ResultSet rs = targetStmt.executeQuery("SELECT * FROM coral_resource_class");
        while(rs.next())
        {
            long classId = rs.getLong("resource_class_id");
            String name = rs.getString("name");
            nameToId.put(name, new Long(classId));
        }
        rs = sourceStmt.executeQuery("SELECT * FROM arl_resource_class ORDER BY name");
        while(rs.next())
        {
            long classId = rs.getLong("resource_class_id");
            String name = rs.getString("name");
            Long targetId = (Long)nameToId.get(name);
            if(targetId != null)
            {
                System.out.println("Resource class " + name + " mapping: " + classId + " => "
                    + targetId);
                rcMap.put(new Long(classId), targetId);
            }
            else
            {
                System.out
                    .println("Resource class " + name + " mapping: " + classId + " => IGNORE");
            }
        }

        // permissions
        nameToId = new HashMap();
        rs = targetStmt.executeQuery("SELECT * FROM coral_permission");
        while(rs.next())
        {
            long classId = rs.getLong("permission_id");
            String name = rs.getString("name");
            nameToId.put(name, new Long(classId));
        }
        rs = sourceStmt.executeQuery("SELECT * FROM arl_permission ORDER BY name");
        while(rs.next())
        {
            long classId = rs.getLong("permission_id");
            String name = rs.getString("name");
            Long targetId = (Long)nameToId.get(name);
            if(targetId != null)
            {
                System.out.println("Permission " + name + " mapping: " + classId + " => "
                    + targetId);
                pMap.put(new Long(classId), targetId);
            }
            else
            {
                System.out.println("Permission " + name + " mapping: " + classId + " => IGNORE");
            }
        }

        //      attribute definition
        HashMap rcAcToId = new HashMap();
        rs = targetStmt.executeQuery("SELECT * FROM coral_attribute_definition");
        while(rs.next())
        {
            long attributeDefinitionId = rs.getLong("attribute_definition_id");
            long resourceClassId = rs.getLong("resource_class_id");
            //long attributeClassId = rs.getLong("attribute_class_id");
            String name = rs.getString("name");
            Long rcId = new Long(resourceClassId);
            Long adId = new Long(attributeDefinitionId);
            Map acToId = (Map)rcAcToId.get(rcId);
            if(acToId == null)
            {
                acToId = new HashMap();
                rcAcToId.put(rcId, acToId);
            }
            acToId.put(name, adId);
        }

        rs = sourceStmt.executeQuery("SELECT *,rc.name AS rcname, ad.name AS adname "
            + "FROM arl_attribute_definition ad, arl_resource_class rc "
            + " WHERE ad.resource_class_id = rc.resource_class_id");
        while(rs.next())
        {
            long attributeDefinitionId = rs.getLong("attribute_definition_id");
            long resourceClassId = rs.getLong("resource_class_id");
            long attributeClassId = rs.getLong("attribute_class_id");
            String className = rs.getString("rcname");
            String attrName = rs.getString("adname");
            attrName = camelCaseAttribute(attrName);
            Long rcId = new Long(resourceClassId);
            Long acId = new Long(attributeClassId);
            Long adId = new Long(attributeDefinitionId);
            rcId = (Long)rcMap.get(rcId);
            acId = (Long)acMap.get(acId);
            if(rcId == null)
            {
                System.out.println("Attribute Definition mapping: " + attributeDefinitionId
                    + " for RC: " + className + " (" + resourceClassId + ") ignored (RC ignored!)");
                continue;
            }
            if(acId == null)
            {
                System.out.println("Attribute Definition mapping: " + attributeDefinitionId
                    + " for RC: " + className + " (" + resourceClassId
                    + ") ignored (AC ignored! - I bet that was Xref)");
                continue;
            }
            Map acToId = (Map)rcAcToId.get(rcId);
            if(acToId == null)
            {
                System.out.println("Attribute Definition mapping: " + attributeDefinitionId
                    + " for RC: " + className + " (" + resourceClassId
                    + ") ignored (No RC=>AC/AD mapping!)");
                continue;
            }
            Long newAdId = (Long)acToId.get(attrName);
            if(newAdId == null)
            {
                System.out.println("Attribute Definition mapping: " + attrName + " ("
                    + attributeDefinitionId + ") for RC: " + className + " (" + resourceClassId
                    + ") ignored (No RC/AC=>AD mapping!)");
                continue;
            }
            System.out.println("Attribute Definition mapping: " + attributeDefinitionId + " => "
                + newAdId);
            adMap.put(adId, newAdId);
            adNameMap.put(adId, className);
        }
        updateLedgeIdTabel("coral_resource_class", "resource_class_id");
        updateLedgeIdTabel("coral_permission", "permission_id");
        updateLedgeIdTabel("coral_attribute_definition", "attribute_definition_id");
    }

    private void importSubject()
        throws Exception
    {
        System.out.println("Subject importing started...");
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_subject");
        while(rs.next())
        {
            long subjectId = rs.getLong("subject_id");
            String name = rs.getString("name");
            System.out.println("Loading subject: " + subjectId + "," + name);
            try
            {
                targetStmt.execute("INSERT INTO coral_subject" + " (subject_id, name) "
                    + "VALUES (" + subjectId + ",'" + escape(name) + "')");
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
        updateLedgeIdTabel("coral_subject", "subject_id");
        System.out.println("Subject importing finished!\n");
    }

    private void importRole()
        throws Exception
    {
        System.out.println("Roles importing started...");
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_role");
        while(rs.next())
        {
            long roleId = rs.getLong("role_id");
            String name = rs.getString("name");
            System.out.println("Loading role: " + roleId + "," + name);
            try
            {
                targetStmt.execute("INSERT INTO coral_role" + " (role_id, name) " + "VALUES ("
                    + roleId + ",'" + escape(name) + "')");
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
        updateLedgeIdTabel("coral_role", "role_id");
        System.out.println("Roles importing finished!\n");
    }

    private void importRoleImplication()
        throws Exception
    {
        System.out.println("Role implications importing started...");
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_role_implication");
        System.out.print("\nLoading role implication:");
        while(rs.next())
        {
            long superRole = rs.getLong("super_role");
            long subRole = rs.getLong("sub_role");
            System.out.print(".");
            targetStmt.execute("INSERT INTO coral_role_implication" + " (super_role, sub_role) "
                + "VALUES (" + superRole + "," + subRole + ")");
        }
        System.out.print("\n");
        System.out.println("Role implications importing finished!\n");
    }

    private void importRoleAssignment()
        throws Exception
    {
        System.out.println("Role assignments importing started...");
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_role_assignment");
        String stmtContent = "INSERT INTO coral_role_assignment "
            + " (subject_id, role_id, grantor, grant_time, granting_allowed) "
            + " VALUES (?, ?, ?, ?, ?)";
        PreparedStatement stmt = targetConn.prepareStatement(stmtContent);
        int size = 0;
        boolean rootAssignment = false;
        Timestamp rootAssignmentDate = null;
        System.out.print("\nLoading role assignment:");
        while(rs.next())
        {
            System.out.print(".");
            long subjectId = rs.getLong("subject_id");
            long roleId = rs.getLong("role_id");
            if(subjectId == 1 && roleId == 1)
            {
                rootAssignment = true;
                rootAssignmentDate = rs.getTimestamp("grant_time");
                continue;
            }
            long grantor = rs.getLong("grantor");
            Timestamp grantTime = rs.getTimestamp("grant_time");
            boolean grantingAllowed = rs.getBoolean("granting_allowed");
            stmt.setLong(1, subjectId);
            stmt.setLong(2, roleId);
            stmt.setLong(3, grantor);
            stmt.setTimestamp(4, grantTime);
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
        if(result.length != size)
        {
            throw new Exception("Failed to copy all records from arl_role_assignment"
                + " - source length:" + size + " , copied: " + result.length);
        }
        if(rootAssignment)
        {
            stmt = targetConn.prepareStatement(stmtContent);
            stmt.setLong(1, 1);
            stmt.setLong(2, 1);
            stmt.setLong(3, 1);
            stmt.setTimestamp(4, rootAssignmentDate);
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

    private void importResource()
        throws Exception
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
        while(rs.next())
        {
            System.out.print(".");
            ResourceData resourceData = new ResourceData(rs);
            Long rcId = resourceData.getResourceClass();
            rcId = (Long)rcMap.get(rcId);
            if(rcId == null)
            {
                System.out.println("\nResource: " + resourceData.getId() + " ignored");
                ignoredResources.add(resourceData.getId());
            }
            else
            {
                resourceData.setResourceClass(rcId);
                resourceMap.put(resourceData.getId(), resourceData);
                untouchedSet.add(resourceData.getId());
            }
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
            if(result.length != (list.size() - 1))
            {
                throw new Exception("Failed to copy all records from arl_resources"
                    + " - source length:" + (list.size() - 1) + " , copied: " + result.length);
            }
        }
        updateLedgeIdTabel("coral_resource", "resource_id");
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
                throw new Exception("failed to find resource: " + id.longValue());
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
                throw new Exception("failed to find parent resource: " + parent.longValue()
                    + " for resource " + id.longValue(), e);
            }
        }
        list.add(rd);
        untouchedSet.remove(id);
    }

    private void importPermissionAssignment()
        throws Exception
    {
        System.out.println("Permission assignments importing started...");
        ResultSet rs = sourceStmt
            .executeQuery("SELECT * FROM arl_permission_assignment pa, arl_permission p"
                + " WHERE pa.permission_id = p.permission_id");

        String stmtContent = "INSERT INTO coral_permission_assignment "
            + " (resource_id, role_id, permission_id, is_inherited, grantor, grant_time) "
            + " VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = targetConn.prepareStatement(stmtContent);
        int size = 0;
        boolean rootAssignment = false;
        Timestamp rootAssignmentDate = null;
        System.out.print("\nLoading permission assignment:");
        while(rs.next())
        {
            System.out.print(".");
            long resourceId = rs.getLong("resource_id");
            long roleId = rs.getLong("role_id");
            long permissionId = rs.getLong("permission_id");
            String name = rs.getString("name");
            Long pId = new Long(permissionId);
            pId = (Long)pMap.get(pId);
            if(pId == null)
            {
                System.out.println("\nPermission '" + name + "' ignored - no mapping!");
                continue;
            }
            Long rId = new Long(resourceId);
            if(ignoredResources.contains(rId))
            {
                System.out.println("\nPermission '" + name + "' ignored - banned resource: " + rId);
                continue;
            }
            boolean isInherited = rs.getBoolean("is_inherited");
            long grantor = rs.getLong("grantor");
            Timestamp grantTime = rs.getTimestamp("grant_time");
            stmt.setLong(1, resourceId);
            stmt.setLong(2, roleId);
            stmt.setLong(3, pId.longValue());
            stmt.setBoolean(4, isInherited);
            stmt.setLong(5, grantor);
            stmt.setTimestamp(6, grantTime);
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
        if(result.length != size)
        {
            throw new Exception("Failed to copy all records from arl_permission_assignments"
                + " - source length:" + size + " , copied: " + result.length);
        }
        System.out.println("Permission assignments importing started...");
    }

    private void importGenericResource()
        throws Exception
    {
        System.out.println("Generis resource importing started...");
        ResultSet rs = sourceStmt
            .executeQuery("SELECT * FROM arl_generic_resource ORDER BY resource_id");
        PreparedStatement stmt = targetConn.prepareStatement("INSERT INTO coral_generic_resource"
            + " (resource_id, attribute_definition_id, data_key) " + "VALUES (?, ?, ?)");
        int size = 0;
        while(rs.next())
        {
            long resourceId = rs.getLong("resource_id");
            long attributeDefinitionId = rs.getLong("attribute_definition_id");
            long dataKey = rs.getLong("data_key");
            Long adId = new Long(attributeDefinitionId);
            Long rId = new Long(resourceId);
            adId = (Long)adMap.get(adId);
            boolean contains = ignoredResources.contains(rId);
            if(adId == null || contains)
            {
                if(adId != null)
                {
                    System.out.println("Generic Resource " + resourceId + ":"
                        + attributeDefinitionId + " ignored - banned Resource!");
                    continue;
                }
                if(!contains)
                {
                    System.out.println("Generic Resource " + resourceId + ":"
                        + attributeDefinitionId + " ignored - no AD mapping!");
                }
                if(contains && adId == null)
                {
                    System.out.println("Generic Resource " + resourceId + ":"
                        + attributeDefinitionId + " ignored - no AD mapping and Banned resource!");
                }
                //TO remember the data_keys
                String className = (String)adNameMap.get(new Long(attributeDefinitionId));
                Set classSet = (Set)ignoredDataKeys.get(className);
                if(classSet == null)
                {
                    classSet = new HashSet();
                    ignoredDataKeys.put(className, classSet);
                }
                classSet.add(new Long(dataKey));
                continue;
            }
            stmt.setLong(1, resourceId);
            stmt.setLong(2, adId.longValue());
            stmt.setLong(3, dataKey);
            stmt.addBatch();
            size++;
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
        if(result.length != size)
        {
            throw new Exception("Failed to copy all records from arl_generic_resource"
                + " - source length:" + size + " , copied: " + result.length);
        }
        System.out.println("Generic resource importing finished!\n");
    }

    private void importAttributeBoolean()
        throws Exception
    {
        System.out.println("Boolean attribute importing started...");
        Set classSet = (Set)ignoredDataKeys.get("boolean");
        if(classSet == null)
        {
            classSet = new HashSet();
        }
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_attribute_boolean");
        PreparedStatement stmt = targetConn.prepareStatement("INSERT INTO coral_attribute_boolean "
            + "(data_key, data) VALUES (?, ?)");
        int size = 0;
        while(rs.next())
        {
            long dataKey = rs.getLong("data_key");
            Long dataKeyLong = new Long(dataKey);
            if(classSet.contains(dataKeyLong))
            {
                continue;
            }
            int data = rs.getInt("data");
            stmt.setLong(1, dataKey);
            stmt.setInt(2, data);
            stmt.addBatch();
            size++;
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
        if(result.length != size)
        {
            throw new Exception("Failed to copy all records from arl_attribute_boolean"
                + " - source length:" + size + " , copied: " + result.length);
        }
        updateLedgeIdTabel("coral_attribute_boolean", "data_key");
        System.out.println("Boolean attribute importing finished!'\n");
    }

    private void importAttributeInteger()
        throws Exception
    {
        System.out.println("Integer attribute importing started...");
        Set classSet = (Set)ignoredDataKeys.get("integer");
        if(classSet == null)
        {
            classSet = new HashSet();
        }
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_attribute_integer");
        PreparedStatement stmt = targetConn.prepareStatement("INSERT INTO coral_attribute_integer "
            + "(data_key, data) VALUES (?, ?)");
        int size = 0;
        while(rs.next())
        {
            long dataKey = rs.getLong("data_key");
            Long dataKeyLong = new Long(dataKey);
            if(classSet.contains(dataKeyLong))
            {
                continue;
            }
            int data = rs.getInt("data");
            stmt.setLong(1, dataKey);
            stmt.setInt(2, data);
            stmt.addBatch();
            size++;
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
        if(result.length != size)
        {
            throw new Exception("Failed to copy all records from arl_attribute_integer"
                + " - source length:" + size + " , copied: " + result.length);
        }
        updateLedgeIdTabel("coral_attribute_integer", "data_key");
        System.out.println("Integer attribute importing finished!\n");
    }

    private void importAttributeLong()
        throws Exception
    {
        System.out.println("Long attribute importing started...");
        Set classSet = (Set)ignoredDataKeys.get("long");
        if(classSet == null)
        {
            classSet = new HashSet();
        }
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_attribute_long");
        PreparedStatement stmt = targetConn.prepareStatement("INSERT INTO coral_attribute_long "
            + "(data_key, data) VALUES (?, ?)");
        int size = 0;
        while(rs.next())
        {
            long dataKey = rs.getLong("data_key");
            Long dataKeyLong = new Long(dataKey);
            if(classSet.contains(dataKeyLong))
            {
                continue;
            }
            long data = rs.getLong("data");
            stmt.setLong(1, dataKey);
            stmt.setLong(2, data);
            stmt.addBatch();
            size++;
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
        if(result.length != size)
        {
            throw new Exception("Failed to copy all records from arl_attribute_long"
                + " - source length:" + size + " , copied: " + result.length);
        }
        updateLedgeIdTabel("coral_attribute_long", "data_key");
        System.out.println("Long attribute importing finished!\n");
    }

    private void importAttributeNumber()
        throws Exception
    {
        System.out.println("Number attribute importing started...");
        Set classSet = (Set)ignoredDataKeys.get("number");
        if(classSet == null)
        {
            classSet = new HashSet();
        }
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_attribute_number");
        PreparedStatement stmt = targetConn.prepareStatement("INSERT INTO coral_attribute_number "
            + "(data_key, data) VALUES (?, ?)");
        int size = 0;
        while(rs.next())
        {
            long dataKey = rs.getLong("data_key");
            Long dataKeyLong = new Long(dataKey);
            if(classSet.contains(dataKeyLong))
            {
                continue;
            }
            BigDecimal data = rs.getBigDecimal("data");
            stmt.setLong(1, dataKey);
            stmt.setBigDecimal(2, data);
            stmt.addBatch();
            size++;
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
        if(result.length != size)
        {
            throw new Exception("Failed to copy all records from arl_attribute_number"
                + " - source length:" + size + " , copied: " + result.length);
        }
        updateLedgeIdTabel("coral_attribute_number", "data_key");
        System.out.println("Number attribute importing finished!\n");
    }

    private void importAttributeString()
        throws Exception
    {
        System.out.println("String attribute importing started...");
        Set classSet = (Set)ignoredDataKeys.get("string");
        if(classSet == null)
        {
            classSet = new HashSet();
        }
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_attribute_string");
        PreparedStatement stmt = targetConn.prepareStatement("INSERT INTO coral_attribute_string "
            + "(data_key, data) VALUES (?, ?)");
        int size = 0;
        while(rs.next())
        {
            long dataKey = rs.getLong("data_key");
            Long dataKeyLong = new Long(dataKey);
            if(classSet.contains(dataKeyLong))
            {
                continue;
            }
            String data = rs.getString("data");
            data = unescape(data);
            String escapedData = escape(data);
            stmt.setLong(1, dataKey);
            stmt.setString(2, escapedData);
            if(data.length() > 255 || escapedData.length() > 255)
            {
                System.out.println("Data key:" + dataKey + " source length:" + data.length()
                    + " escaped length:" + escapedData.length());
                System.out.println("------------------");
                System.out.println(data);
                System.out.println("------------------");
                System.out.println(escapedData);
                System.out.println("------------------");
            }
            stmt.addBatch();
            size++;
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
        if(result.length != size)
        {
            throw new Exception("Failed to copy all records from arl_attribute_string"
                + " - source length:" + size + " , copied: " + result.length);
        }
        updateLedgeIdTabel("coral_attribute_string", "data_key");
        System.out.println("String attribute importing finished!\n");
    }

    private void importAttributeText()
        throws Exception
    {
        System.out.println("Text attribute importing started...");
        Set classSet = (Set)ignoredDataKeys.get("text");
        if(classSet == null)
        {
            classSet = new HashSet();
        }
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_attribute_text");
        PreparedStatement stmt = targetConn.prepareStatement("INSERT INTO coral_attribute_text "
            + "(data_key, data) VALUES (?, ?)");
        int size = 0;
        while(rs.next())
        {
            long dataKey = rs.getLong("data_key");
            Long dataKeyLong = new Long(dataKey);
            if(classSet.contains(dataKeyLong))
            {
                continue;
            }
            String data = rs.getString("data");
            data = unescape(data);
            String escapedData = escape(data);
            stmt.setLong(1, dataKey);
            stmt.setString(2, escapedData);
            stmt.addBatch();
            size++;
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
        if(result.length != size)
        {
            throw new Exception("Failed to copy all records from arl_attribute_text"
                + " - source length:" + size + " , copied: " + result.length);
        }
        updateLedgeIdTabel("coral_attribute_text", "data_key");
        System.out.println("Text attribute importing finished!\n");
    }

    private void importAttributeDate()
        throws Exception
    {
        System.out.println("Date attribute importing started...");
        Set classSet = (Set)ignoredDataKeys.get("date");
        if(classSet == null)
        {
            classSet = new HashSet();
        }
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_attribute_date");
        PreparedStatement stmt = targetConn.prepareStatement("INSERT INTO coral_attribute_date "
            + "(data_key, data) VALUES (?, ?)");
        int size = 0;
        while(rs.next())
        {
            long dataKey = rs.getLong("data_key");
            Long dataKeyLong = new Long(dataKey);
            if(classSet.contains(dataKeyLong))
            {
                continue;
            }
            Timestamp data = rs.getTimestamp("data");
            stmt.setLong(1, dataKey);
            stmt.setTimestamp(2, data);
            stmt.addBatch();
            size++;
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
        if(result.length != size)
        {
            throw new Exception("Failed to copy all records from arl_attribute_date"
                + " - source length:" + size + " , copied: " + result.length);
        }
        updateLedgeIdTabel("coral_attribute_date", "data_key");
        System.out.println("Date attribute importing finished!\n");
    }

    private void importAttributeDateRange()
        throws Exception
    {
        System.out.println("Date range attribute importing started...");
        Set classSet = (Set)ignoredDataKeys.get("date_range");
        if(classSet == null)
        {
            classSet = new HashSet();
        }
        System.out.println("Date range attribute importing started...");
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_attribute_date_range");
        PreparedStatement stmt = targetConn
            .prepareStatement("INSERT INTO coral_attribute_date_range "
                + "(data_key, startDate, endDate) VALUES (?, ?, ?)");
        int size = 0;
        while(rs.next())
        {
            long dataKey = rs.getLong("data_key");
            Long dataKeyLong = new Long(dataKey);
            if(classSet.contains(dataKeyLong))
            {
                continue;
            }
            
            Timestamp startDate = rs.getTimestamp("start_date");
            Timestamp endDate = rs.getTimestamp("end_date");
            stmt.setLong(1, dataKey);
            stmt.setTimestamp(2, startDate);
            stmt.setTimestamp(3, endDate);
            stmt.addBatch();
            size++;
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
        if(result.length != size)
        {
            throw new Exception("Failed to copy all records from 'arl_attribute_date_range'"
                + " - source length:" + size + " , copied: " + result.length);
        }
        updateLedgeIdTabel("coral_attribute_date_range", "data_key");
        System.out.println("Date range attribute importing finished!\n");
    }

    private void importAttributeResourceClass()
        throws Exception
    {
        System.out.println("Resource class attribute importing started...");
        Set classSet = (Set)ignoredDataKeys.get("resource_class");
        if(classSet == null)
        {
            classSet = new HashSet();
        }
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_attribute_resource_class");
        while(rs.next())
        {
            long dataKey = rs.getLong("data_key");
            Long dataKeyLong = new Long(dataKey);
            if(classSet.contains(dataKeyLong))
            {
                continue;
            }
            long ref = rs.getLong("ref");
            Long rcId = new Long(ref);
            rcId = (Long)rcMap.get(rcId);
            if(rcId == null)
            {
                continue;
            }
            targetStmt.execute("INSERT INTO coral_attribute_resource_class" + " (data_key, ref) "
                + "VALUES (" + dataKey + "," + rcId + ")");
        }
        updateLedgeIdTabel("coral_attribute_resource_class", "data_key");
        System.out.println("Resource class attribute importing finished!\n");
    }

    /**
     * Copy resource reference attributes content.
     * 
     * @throws Exception if anything goes wrong
     */
    private void importAttributeResource()
        throws Exception
    {
        System.out.println("Resource attribute importing started...");
        Set classSet = (Set)ignoredDataKeys.get("resource");
        if(classSet == null)
        {
            classSet = new HashSet();
        }
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_attribute_resource");
        while(rs.next())
        {
            long dataKey = rs.getLong("data_key");
            Long dataKeyLong = new Long(dataKey);
            if(classSet.contains(dataKeyLong))
            {
                continue;
            }
            long ref = rs.getLong("ref");
            Long rId = new Long(ref);
            if(ignoredResources.contains(rId))
            {
                continue;
            }
            targetStmt.execute("INSERT INTO coral_attribute_resource" + " (data_key, ref) "
                + "VALUES (" + dataKey + "," + ref + ")");
        }
        updateLedgeIdTabel("coral_attribute_resource", "data_key");
        System.out.println("Resource attribute importing started!\n");
    }

    /**
     * Copy subject attributes content.
     * 
     * @throws Exception if anything goes wrong
     */
    private void importAttributeSubject()
        throws Exception
    {
        System.out.println("Subject attribute importing started...");
        Set classSet = (Set)ignoredDataKeys.get("subject");
        if(classSet == null)
        {
            classSet = new HashSet();
        }
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_attribute_subject");
        while(rs.next())
        {
            long dataKey = rs.getLong("data_key");
            Long dataKeyLong = new Long(dataKey);
            if(classSet.contains(dataKeyLong))
            {
                continue;
            }
            long ref = rs.getLong("ref");
            targetStmt.execute("INSERT INTO coral_attribute_subject" + " (data_key, ref) "
                + "VALUES (" + dataKey + "," + ref + ")");
        }
        updateLedgeIdTabel("coral_attribute_subject", "data_key");
        System.out.println("Subject attribute importing finished!\n");
    }

    /**
     * Copy role attributes content.
     * 
     * @throws Exception if anything goes wrong
     */
    private void importAttributeRole()
        throws Exception
    {
        System.out.println("Role attribute importing started...");
        Set classSet = (Set)ignoredDataKeys.get("role");
        if(classSet == null)
        {
            classSet = new HashSet();
        }
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_attribute_role");
        while(rs.next())
        {
            long dataKey = rs.getLong("data_key");
            Long dataKeyLong = new Long(dataKey);
            if(classSet.contains(dataKeyLong))
            {
                continue;
            }
            long ref = rs.getLong("ref");
            targetStmt.execute("INSERT INTO coral_attribute_role" + " (data_key, ref) "
                + "VALUES (" + dataKey + "," + ref + ")");
        }
        updateLedgeIdTabel("coral_attribute_role", "data_key");
        System.out.println("Role attribute importing finished!\n");
    }

    /**
     * Copy permission attributes content.
     * 
     * @throws Exception if anything goes wrong
     */
    private void importAttributePermission()
        throws Exception
    {
        System.out.println("Permission attribute importing started...");
        Set classSet = (Set)ignoredDataKeys.get("permission");
        if(classSet == null)
        {
            classSet = new HashSet();
        }
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_attribute_permission");
        while(rs.next())
        {
            long dataKey = rs.getLong("data_key");
            Long dataKeyLong = new Long(dataKey);
            if(classSet.contains(dataKeyLong))
            {
                continue;
            }
            long ref = rs.getLong("ref");
            Long pId = new Long(ref);
            pId = (Long)pMap.get(pId);
            if(pId == null)
            {
                continue;
            }
            targetStmt.execute("INSERT INTO coral_attribute_permission" + " (data_key, ref) "
                + "VALUES (" + dataKey + "," + pId + ")");
        }
        updateLedgeIdTabel("coral_attribute_permission", "data_key");
        System.out.println("Permission attribute importing finished!\n");
    }

    /**
     * Copy resource list attributes content.
     * 
     * @throws Exception if anything goes wrong
     */
    private void importAttributeResourceList()
        throws Exception
    {
        System.out.println("Resource list attribute importing started...");
        Set classSet = (Set)ignoredDataKeys.get("resource_list");
        if(classSet == null)
        {
            classSet = new HashSet();
        }
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_attribute_resource_list");
        while(rs.next())
        {
            long dataKey = rs.getLong("data_key");
            Long dataKeyLong = new Long(dataKey);
            if(classSet.contains(dataKeyLong))
            {
                continue;
            }
            int pos = rs.getInt("pos");
            long ref = rs.getLong("ref");
            targetStmt.execute("INSERT INTO coral_attribute_resource_list"
                + " (data_key, pos, ref) " + "VALUES (" + dataKey + "," + pos + "," + ref + ")");
        }
        updateLedgeIdTabel("coral_attribute_resource_list", "data_key");
        System.out.println("Resource list attribute importing started!\n");
    }

    /**
     * Copy weak resource list attributes content.
     * 
     * @throws Exception if anything goes wrong
     */
    private void importAttributeWeakResourceList()
        throws Exception
    {
        System.out.println("Weak resource list attribute importing started...");
        Set classSet = (Set)ignoredDataKeys.get("weak_resource_list");
        if(classSet == null)
        {
            classSet = new HashSet();
        }
        ResultSet rs = sourceStmt.executeQuery("SELECT * FROM arl_attribute_weak_resource_lis");
        while(rs.next())
        {
            long dataKey = rs.getLong("data_key");
            Long dataKeyLong = new Long(dataKey);
            if(classSet.contains(dataKeyLong))
            {
                continue;
            }
            int pos = rs.getInt("pos");
            long ref = rs.getLong("ref");
            targetStmt.execute("INSERT INTO coral_attribute_weak_resource_list"
                + " (data_key, pos, ref) " + "VALUES (" + dataKey + "," + pos + "," + ref + ")");
        }
        updateLedgeIdTabel("coral_attribute_weak_resource_list", "data_key");
        System.out.println("Weak resource list attribute importing finished!\n");
    }

    /**
     * Convert cross reference to relation.
     * 
     * @throws Exception if anything goes wrong
     */
    private void importAttributeCrossReference()
        throws Exception
    {
        Set classSet = (Set)ignoredDataKeys.get("cross_reference");
        if(classSet == null)
        {
            classSet = new HashSet();
        }
        System.out.println("Cross reference attribute importing started...");
        ResultSet rs = sourceStmt
            .executeQuery("SELECT * FROM arl_attribute_class WHERE name = 'cross_reference'");
        long acId = -1;
        if(rs.next())
        {
            acId = rs.getLong("attribute_class_id");
        }
        else
        {
            throw new Exception("no cross reference attribute definition");
        }
        String query = "SELECT rc.name, ad.attribute_definition_id, "
            + "ad.name AS adname FROM arl_resource_class rc, "
            + "arl_attribute_definition ad WHERE "
            + "rc.resource_class_id = ad.resource_class_id AND " + "ad.attribute_class_id = "
            + acId;
        rs = sourceStmt.executeQuery(query);
        Map xrefToRelation = new HashMap();
        while(rs.next())
        {
            String className = rs.getString("name");
            String attributeName = rs.getString("adname");
            long adId = rs.getLong("attribute_definition_id");
            String relationName = resolveCrossReference(className, attributeName);
            System.out.println("XREF Mapping: " + adId + " => " + relationName);
            xrefToRelation.put(new Long(adId), relationName);
        }

        Iterator it = xrefToRelation.keySet().iterator();
        int relationId = 0;
        query = "SELECT cr.resource1, cr.resource2, gr.data_key AS grdatakey FROM "
            + "arl_attribute_cross_reference cr, " + "arl_generic_resource gr WHERE "
            + "cr.data_key = gr.data_key AND " + "gr.attribute_definition_id = ";
        while(it.hasNext())
        {
            Long adId = (Long)it.next();
            String relationName = (String)xrefToRelation.get(adId);
            /**
             if(relationName.trim().equals("IGNORE"))
             {
             continue;
             }
             */
            relationId++;
            targetStmt.execute("INSERT INTO coral_relation " + " (relation_id, name) " + "VALUES ("
                + relationId + ",'" + relationName + "')");
            rs = sourceStmt.executeQuery(query + adId);
            PreparedStatement stmt = targetConn.prepareStatement("INSERT INTO coral_relation_data "
                + "(relation_id, resource1, resource2) VALUES (?, ?, ?)");
            int size = 0;
            Set alreadyInserted = new HashSet();
            while(rs.next())
            {
                long resource1 = rs.getLong("resource1");
                long resource2 = rs.getLong("resource2");
                long dataKey = rs.getLong("grdatakey");
                Long rId = new Long(resource1);
                if(ignoredResources.contains(rId))
                {
                    continue;
                }
                rId = new Long(resource2);
                if(ignoredResources.contains(rId))
                {
                    continue;
                }
                Long dataKeyLong = new Long(dataKey);
                if(classSet.contains(dataKeyLong))
                {
                    continue;
                }
                System.out.println("Relation: '" + relationName + "', R1:" + resource1 + ", R2:"
                    + resource2);
                String key = "" + relationId + "_" + resource1 + "_" + resource2;
                if(alreadyInserted.contains(key))
                {
                    System.out.println("Duplicate!");
                    continue;
                }
                alreadyInserted.add(key);
                stmt.setLong(1, relationId);
                stmt.setLong(2, resource1);
                stmt.setLong(3, resource2);
                stmt.addBatch();
                size++;
            }
            int[] result = null;
            if(size > 0)
            {
                try
                {
                    System.out.println("Adding to relation ID " + relationId);
                    result = stmt.executeBatch();
                }
                catch(SQLException e)
                {
                    throw e.getNextException();
                }
                if(result.length != size)
                {
                    throw new Exception("Failed to copy all records from arl_attribute_date"
                        + " - source length:" + size + " , copied: " + result.length);
                }
            }
        }
        updateLedgeIdTabel("coral_relation", "relation_id");
        System.out.println("Cross reference attribute importing finished!\n");
    }

    /**
     * Copy parameter attributes content.
     * 
     * @throws Exception if anything goes wrong
     */
    private void importAttributeParameters()
        throws Exception
    {
        System.out.println("Parameters attribute importing started...");
        Set classSet = (Set)ignoredDataKeys.get("parameter_container");
        if(classSet == null)
        {
            classSet = new HashSet();
        }
        String query1 = "SELECT attribute_class_id from arl_attribute_class"
            + " where name = 'parameter_container'";
        String query2 = "SELECT resource_id, container_id, p.name AS pname, p.value AS value "
            + "FROM arl_attribute_definition ad, arl_generic_resource gr, parameter_container p "
            + "WHERE ad.attribute_definition_id = gr.attribute_definition_id "
            + "AND p.container_id = gr.data_key AND ad.attribute_class_id = ";
        ResultSet rs = sourceStmt.executeQuery(query1);
        long acId = -1;
        if(rs.next())
        {
            acId = rs.getLong("attribute_class_id");
        }
        else
        {
            throw new Exception("couldn't find parameters_container attribute class");
        }
        rs = sourceStmt.executeQuery(query2 + acId);
        PreparedStatement stmt = targetConn.prepareStatement("INSERT INTO ledge_parameters "
            + "(parameters_id, name, value) VALUES (?, ?, ?)");
        int size = 0;
        while(rs.next())
        {
            long resourceId = rs.getLong("resource_id");
            if(ignoredResources.contains(new Long(resourceId)))
            {
                continue;
            }
            long containerId = rs.getLong("container_id");
            Long dataKeyLong = new Long(containerId);
            if(classSet.contains(dataKeyLong))
            {
                continue;
            }
            String pname = rs.getString("pname");
            String value = rs.getString("value");
            stmt.setLong(1, containerId);
            stmt.setString(2, escape(unescape(pname)));
            stmt.setString(3, escape(unescape(value)));
            stmt.addBatch();
            size++;
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
        if(result.length != size)
        {
            throw new Exception("Failed to copy all records to ledge_parameters"
                + " - source length:" + size + " , copied: " + result.length);
        }
        updateLedgeIdTabel("ledge_parameters", "parameters_id");
        System.out.println("Parameters attribute importing finished!\n");
    }

    /**
     * Backslash escape the \ and ' characters.
     * 
     * @param string the input string.
     * @return the converted string.
     */
    private static String escape(String string)
    {
        return DatabaseUtils.escapeSqlString(string);
    }

    /**
     * Unescape unicode escapes.
     * 
     * @param string the input string
     * @return the unescaped string.
     */
    private static String unescape(String string)
    {
        return StringUtils.expandUnicodeEscapes(string);
        //return DatabaseUtils.unescapeSqlString(string);
    }

    /**
     * Resolve cross reference mapping.
     * 
     * @param className the class name.
     * @param attrName the attribute name.
     */
    private String resolveCrossReference(String className, String attrName)
        throws Exception
    {
        Map temp = (Map)xrefMap.get(className);
        if(temp == null)
        {
            throw new Exception("Couldn't find mapping for attribute '" + attrName + "' in class '"
                + className + "'");
        }
        String relationName = (String)temp.get(attrName);
        if(relationName == null)
        {
            throw new Exception("Couldn't find mapping for attribute '" + attrName + "' in class '"
                + className + "'");
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

    /**
     * Simple wrapper for resource. 
     *
     */
    private class ResourceData
    {
        private long resourceId;

        private Long resourceIdLong;

        private long resourceClassId;

        private Long resourceClassIdLong;

        private long parent;

        private Long parentLong;

        private String name;

        private long createdBy;

        private Timestamp creationTime;

        private long ownedBy;

        private long modifiedBy;

        private Timestamp modificationTime;

        public ResourceData(ResultSet rs)
            throws Exception
        {
            resourceId = rs.getLong("resource_id");
            resourceIdLong = new Long(resourceId);
            resourceClassId = rs.getLong("resource_class_id");
            resourceClassIdLong = new Long(resourceClassId);
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
            creationTime = rs.getTimestamp("creation_time");
            ownedBy = rs.getLong("owned_by");
            modifiedBy = rs.getLong("modified_by");
            modificationTime = rs.getTimestamp("modification_time");
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
            stmt.setTimestamp(6, creationTime);
            stmt.setLong(7, ownedBy);
            stmt.setLong(8, modifiedBy);
            stmt.setTimestamp(9, modificationTime);
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

        public Long getResourceClass()
        {
            return resourceClassIdLong;
        }

        public void setResourceClass(Long rcId)
        {
            resourceClassIdLong = rcId;
            resourceClassId = rcId.longValue();
        }
    }

    /**
     * Convert string to camel case.
     * 
     * @param input the input string.
     * @return the converted string.
     */
    private String camelCaseAttribute(String input)
    {
        if(input.indexOf("_") < 0)
        {
            return input;
        }
        StringTokenizer st2 = new StringTokenizer(input, "_");
        String result = st2.nextToken();
        while(st2.hasMoreTokens())
        {
            String temp = st2.nextToken();
            result = result + temp.substring(0, 1).toUpperCase();
            result = result + temp.substring(1, temp.length());
        }
        return result;
    }

    /**
     * Fix the ledge_id_table for given table.
     * 
     * @param tableName the table name.
     * @param pKey the primary key column
     * @throws Exception if anything goes wrong.
     */
    private void updateLedgeIdTabel(String tableName, String pKey)
        throws Exception
    {
        long maxKey = 1;
        ResultSet rs = targetStmt.executeQuery("SELECT " + pKey + " FROM " + tableName
            + " ORDER BY " + pKey + " DESC LIMIT 1");
        if(rs.next())
        {
            maxKey = rs.getLong(pKey) + 1;
        }
        else
        {
            maxKey = 1;
        }
        rs = targetStmt.executeQuery("SELECT * FROM ledge_id_table " + "WHERE table_name = '"
            + tableName + "'");
        boolean exist = rs.next();
        if(exist)
        {
            targetStmt.execute("UPDATE ledge_id_table SET next_id = " + maxKey
                + " WHERE table_name = '" + tableName + "'");
        }
        else
        {
            targetStmt.execute("INSERT INTO ledge_id_table" + " (next_id, table_name) "
                + "VALUES (" + maxKey + ",'" + tableName + "')");
        }
    }
}
