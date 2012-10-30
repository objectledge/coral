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

import java.math.BigDecimal;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.datatype.DataType;
import org.objectledge.coral.datatypes.DateRange;
import org.objectledge.coral.datatypes.Node;
import org.objectledge.coral.datatypes.ResourceList;
import org.objectledge.coral.datatypes.WeakResourceList;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.security.Permission;
import org.objectledge.coral.security.Role;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.touchstone.CoralTestCase;
import org.objectledge.database.DatabaseUtils;
import org.objectledge.parameters.Parameters;

/**
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: CoralDatatypesTest.java,v 1.7 2005-02-08 20:34:17 rafal Exp $
 */
public class CoralDatatypesTest
    extends CoralTestCase
{
    private Column[] coralAttributeStringColumns = new Column[] {
                    new Column("data_key", DataType.BIGINT), new Column("data", DataType.VARCHAR) };

    private Column[] coralAttributeTextColumns = new Column[] {
                    new Column("data_key", DataType.BIGINT), new Column("data", DataType.VARCHAR) };

    private Column[] coralAttributeBooleanColumns = new Column[] {
                    new Column("data_key", DataType.BIGINT), new Column("data", DataType.BOOLEAN) };

    private Column[] coralAttributeIntegerColumns = new Column[] {
                    new Column("data_key", DataType.BIGINT), new Column("data", DataType.INTEGER) };

    private Column[] coralAttributeLongColumns = new Column[] {
                    new Column("data_key", DataType.BIGINT), new Column("data", DataType.BIGINT) };

    private Column[] coralAttributeNumberColumns = new Column[] {
                    new Column("data_key", DataType.BIGINT), new Column("data", DataType.DECIMAL) };

    private Column[] coralAttributeDateColumns = new Column[] {
                    new Column("data_key", DataType.BIGINT), new Column("data", DataType.TIMESTAMP) };

    private Column[] coralAttributeResourceClassColumns = new Column[] {
                    new Column("data_key", DataType.BIGINT), new Column("ref", DataType.BIGINT) };

    private Column[] coralAttributeResourceColumns = new Column[] {
                    new Column("data_key", DataType.BIGINT), new Column("ref", DataType.BIGINT) };

    private Column[] coralAttributeSubjectColumns = new Column[] {
                    new Column("data_key", DataType.BIGINT), new Column("ref", DataType.BIGINT) };

    private Column[] coralAttributeRoleColumns = new Column[] {
                    new Column("data_key", DataType.BIGINT), new Column("ref", DataType.BIGINT) };

    private Column[] coralAttributePermissionColumns = new Column[] {
                    new Column("data_key", DataType.BIGINT), new Column("ref", DataType.BIGINT) };

    private Column[] coralAttributeDateRangeColumns = new Column[] {
                    new Column("data_key", DataType.BIGINT),
                    new Column("start_date", DataType.TIMESTAMP),
                    new Column("end_date", DataType.TIMESTAMP) };

    private Column[] ledgeParametersColumns = new Column[] {
                    new Column("parameters_id", DataType.BIGINT),
                    new Column("name", DataType.VARCHAR), new Column("value", DataType.VARCHAR) };

    private Column[] coralAttributeResourceListColumns = new Column[] {
                    new Column("data_key", DataType.BIGINT), new Column("pos", DataType.INTEGER),
                    new Column("ref", DataType.BIGINT) };

    private Column[] coralAttributeWeakResourceListColumns = new Column[] {
                    new Column("data_key", DataType.BIGINT), new Column("pos", DataType.INTEGER),
                    new Column("ref", DataType.BIGINT) };

    private CoralSession session;

    private Resource resource;

    public void setUp()
        throws Exception
    {
        super.setUp();
        Statement stmt = databaseConnection.getConnection().createStatement();
        stmt.execute("INSERT INTO coral_resource_class VALUES(3, "
            + "'test', 'org.objectledge.coral.datatypes.NodeImpl',"
            + "'org.objectledge.coral.datatypes.GenericResourceHandler', NULL, 0)");
        stmt.execute("ALTER SEQUENCE coral_resource_class_seq RESTART WITH 4");

        stmt.execute("INSERT INTO coral_resource VALUES(2,3,1,'resource',1,NOW(),1,1,NOW())");
        stmt.execute("ALTER SEQUENCE coral_resource_seq RESTART WITH 3");

        stmt.execute("INSERT INTO coral_attribute_definition VALUES(21, 3, 1,  NULL, NULL, 'string_attr', 0)");
        stmt.execute("INSERT INTO coral_generic_resource VALUES(2, 21, 1)");
        stmt.execute("INSERT INTO coral_attribute_string VALUES(1, 'value')");
        stmt.execute("ALTER SEQUENCE coral_attribute_string_seq RESTART WITH 2");

        stmt.execute("INSERT INTO coral_attribute_definition VALUES(22, 3, 2,  NULL, NULL, 'text_attr', 0)");
        stmt.execute("INSERT INTO coral_generic_resource VALUES(2, 22, 1)");
        stmt.execute("INSERT INTO coral_attribute_text VALUES(1, 'value')");
        stmt.execute("ALTER SEQUENCE coral_attribute_text_seq RESTART WITH 2");

        stmt.execute("INSERT INTO coral_attribute_definition VALUES(23, 3, 3,  NULL, NULL, 'boolean_attr', 0)");
        stmt.execute("INSERT INTO coral_generic_resource VALUES(2, 23, 1)");
        stmt.execute("INSERT INTO coral_attribute_boolean VALUES(1, true)");
        stmt.execute("ALTER SEQUENCE coral_attribute_boolean_seq RESTART WITH 2");

        stmt.execute("INSERT INTO coral_attribute_definition VALUES(24, 3, 4,  NULL, NULL, 'integer_attr', 0)");
        stmt.execute("INSERT INTO coral_generic_resource VALUES(2, 24, 1)");
        stmt.execute("INSERT INTO coral_attribute_integer VALUES(1, 1)");
        stmt.execute("ALTER SEQUENCE coral_attribute_integer_seq RESTART WITH 2");

        stmt.execute("INSERT INTO coral_attribute_definition VALUES(25, 3, 5,  NULL, NULL, 'long_attr', 0)");
        stmt.execute("INSERT INTO coral_generic_resource VALUES(2, 25, 1)");
        stmt.execute("INSERT INTO coral_attribute_long VALUES(1, 1)");
        stmt.execute("ALTER SEQUENCE coral_attribute_long_seq RESTART WITH 2");

        stmt.execute("INSERT INTO coral_attribute_definition VALUES(26, 3, 6,  NULL, NULL, 'number_attr', 0)");
        stmt.execute("INSERT INTO coral_generic_resource VALUES(2, 26, 1)");
        stmt.execute("INSERT INTO coral_attribute_number VALUES(1, 1)");
        stmt.execute("ALTER SEQUENCE coral_attribute_number_seq RESTART WITH 2");

        stmt.execute("INSERT INTO coral_attribute_definition VALUES(27, 3, 7,  NULL, NULL, 'date_attr', 0)");
        stmt.execute("INSERT INTO coral_generic_resource VALUES(2, 27, 1)");
        stmt.execute("INSERT INTO coral_attribute_date VALUES(1, NOW())");
        stmt.execute("ALTER SEQUENCE coral_attribute_date_seq RESTART WITH 2");

        stmt.execute("INSERT INTO coral_attribute_definition VALUES(28, 3, 8,  NULL, NULL, 'resource_class_attr', 0)");
        stmt.execute("INSERT INTO coral_generic_resource VALUES(2, 28, 1)");
        stmt.execute("INSERT INTO coral_attribute_resource_class VALUES(1, 1)");
        stmt.execute("ALTER SEQUENCE coral_attribute_resource_class_seq RESTART WITH 2");

        stmt.execute("INSERT INTO coral_attribute_definition VALUES(29, 3, 9,  NULL, NULL, 'resource_attr', 0)");
        stmt.execute("INSERT INTO coral_generic_resource VALUES(2, 29, 1)");
        stmt.execute("INSERT INTO coral_attribute_resource VALUES(1, 1)");
        stmt.execute("ALTER SEQUENCE coral_attribute_resource_seq RESTART WITH 2");

        stmt.execute("INSERT INTO coral_attribute_definition VALUES(30, 3, 10, NULL, NULL, 'subject_attr', 0)");
        stmt.execute("INSERT INTO coral_generic_resource VALUES(2, 30, 1)");
        stmt.execute("INSERT INTO coral_attribute_subject VALUES(1, 1)");
        stmt.execute("ALTER SEQUENCE coral_attribute_subject_seq RESTART WITH 2");

        stmt.execute("INSERT INTO coral_attribute_definition VALUES(31, 3, 11, NULL, NULL, 'role_attr', 0)");
        stmt.execute("INSERT INTO coral_generic_resource VALUES(2, 31, 1)");
        stmt.execute("INSERT INTO coral_attribute_role VALUES(1, 1)");
        stmt.execute("ALTER SEQUENCE coral_attribute_role_seq RESTART WITH 2");

        stmt.execute("INSERT INTO coral_attribute_definition VALUES(32, 3, 12, NULL, NULL, 'permission_attr', 0)");
        stmt.execute("INSERT INTO coral_generic_resource VALUES(2, 32, 1)");
        stmt.execute("INSERT INTO coral_permission VALUES(1,'permission')");
        stmt.execute("INSERT INTO coral_permission VALUES(2,'permission2')");
        stmt.execute("INSERT INTO coral_attribute_permission VALUES(1, 1)");
        stmt.execute("ALTER SEQUENCE coral_attribute_permission_seq RESTART WITH 2");

        stmt.execute("INSERT INTO coral_attribute_definition VALUES(33, 3, 13, NULL, NULL, 'date_range_attr', 0)");
        stmt.execute("INSERT INTO coral_generic_resource VALUES(2, 33, 1)");
        stmt.execute("INSERT INTO coral_attribute_date_range VALUES(1, NOW(), NOW())");
        stmt.execute("ALTER SEQUENCE coral_attribute_date_range_seq RESTART WITH 2");

        stmt.execute("INSERT INTO coral_attribute_definition VALUES(34, 3, 14, NULL, NULL, 'parameters_attr', 0)");
        stmt.execute("INSERT INTO coral_generic_resource VALUES(2, 34, 1)");
        stmt.execute("DELETE FROM ledge_parameters");
        stmt.execute("INSERT INTO ledge_parameters VALUES(1, 'key', 'value')");
        stmt.execute("ALTER SEQUENCE ledge_parameters_seq RESTART WITH 2");

        stmt.execute("INSERT INTO coral_attribute_definition VALUES(35, 3, 15, NULL, NULL, 'resource_list_attr', 0)");
        stmt.execute("INSERT INTO coral_generic_resource VALUES(2, 35, 1)");
        stmt.execute("INSERT INTO coral_attribute_resource_list VALUES(1, 0, 1)");
        stmt.execute("ALTER SEQUENCE coral_attribute_resource_list_seq RESTART WITH 2");

        stmt.execute("INSERT INTO coral_attribute_definition VALUES(36, 3, 16, NULL, NULL, 'weak_resource_list_attr', 0)");
        stmt.execute("INSERT INTO coral_generic_resource VALUES(2, 36, 1)");
        stmt.execute("INSERT INTO coral_attribute_weak_resource_list VALUES(1, 0, 1)");
        stmt.execute("ALTER SEQUENCE coral_attribute_weak_resource_list_seq RESTART WITH 2");

        DatabaseUtils.close(stmt);
        databaseConnection.close();
        session = coralSessionFactory.getAnonymousSession();
        resource = session.getStore().getUniqueResource("resource");
    }

    public void testStringAttribute()
        throws Exception
    {
        AttributeDefinition attr = resource.getResourceClass().getAttribute("string_attr");
        String value = (String)resource.get(attr);
        assertEquals("value", value);
        resource.set(attr, "value2");
        resource.update();
        DefaultTable expectedTable = new DefaultTable("coral_attribute_string",
            coralAttributeStringColumns);
        expectedTable.addRow(new Object[] { new Long(1), "value2" });
        ITable actualTable = databaseConnection.createQueryTable("coral_attribute_string",
            "SELECT * FROM coral_attribute_string");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);

        resource.unset(attr);
        resource.update();
        expectedTable = new DefaultTable("coral_attribute_string", coralAttributeStringColumns);
        actualTable = databaseConnection.createQueryTable("coral_attribute_string",
            "SELECT * FROM coral_attribute_string");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);

        resource.set(attr, "value2");
        resource.update();
        expectedTable = new DefaultTable("coral_attribute_string", coralAttributeStringColumns);
        expectedTable.addRow(new Object[] { new Long(2), "value2" });
        actualTable = databaseConnection.createQueryTable("coral_attribute_string",
            "SELECT * FROM coral_attribute_string");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);
    }

    public void testTextAttribute()
        throws Exception
    {
        AttributeDefinition attr = resource.getResourceClass().getAttribute("text_attr");
        String value = (String)resource.get(attr);
        assertEquals("value", value);
        resource.set(attr, "value2");
        resource.update();
        DefaultTable expectedTable = new DefaultTable("coral_attribute_text",
            coralAttributeTextColumns);
        expectedTable.addRow(new Object[] { new Long(1), "value2" });
        ITable actualTable = databaseConnection.createQueryTable("coral_attribute_text",
            "SELECT * FROM coral_attribute_text");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);

        resource.unset(attr);
        resource.update();
        expectedTable = new DefaultTable("coral_attribute_text", coralAttributeTextColumns);
        actualTable = databaseConnection.createQueryTable("coral_attribute_text",
            "SELECT * FROM coral_attribute_text");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);

        resource.set(attr, "value2");
        resource.update();
        expectedTable = new DefaultTable("coral_attribute_text", coralAttributeTextColumns);
        expectedTable.addRow(new Object[] { new Long(2), "value2" });
        actualTable = databaseConnection.createQueryTable("coral_attribute_text",
            "SELECT * FROM coral_attribute_text");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);
    }

    public void testBooleanAttribute()
        throws Exception
    {
        AttributeDefinition attr = resource.getResourceClass().getAttribute("boolean_attr");
        Boolean value = (Boolean)resource.get(attr);
        assertEquals(true, value.booleanValue());
        resource.set(attr, Boolean.FALSE);
        resource.update();
        DefaultTable expectedTable = new DefaultTable("coral_attribute_boolean",
            coralAttributeBooleanColumns);
        expectedTable.addRow(new Object[] { new Long(1), Boolean.FALSE });
        ITable actualTable = databaseConnection.createQueryTable("coral_attribute_boolean",
            "SELECT * FROM coral_attribute_boolean");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);

        resource.unset(attr);
        resource.update();
        expectedTable = new DefaultTable("coral_attribute_boolean", coralAttributeBooleanColumns);
        actualTable = databaseConnection.createQueryTable("coral_attribute_boolean",
            "SELECT * FROM coral_attribute_boolean");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);

        resource.set(attr, Boolean.FALSE);
        resource.update();
        expectedTable = new DefaultTable("coral_attribute_boolean", coralAttributeBooleanColumns);
        expectedTable.addRow(new Object[] { new Long(2), new Integer(0) });
        actualTable = databaseConnection.createQueryTable("coral_attribute_boolean",
            "SELECT * FROM coral_attribute_boolean");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);
    }

    public void testIntegerAttribute()
        throws Exception
    {
        AttributeDefinition attr = resource.getResourceClass().getAttribute("integer_attr");
        Integer value = (Integer)resource.get(attr);
        assertEquals(1, value.intValue());
        resource.set(attr, new Integer(2));
        resource.update();
        DefaultTable expectedTable = new DefaultTable("coral_attribute_integer",
            coralAttributeIntegerColumns);
        expectedTable.addRow(new Object[] { new Long(1), new Integer(2) });
        ITable actualTable = databaseConnection.createQueryTable("coral_attribute_integer",
            "SELECT * FROM coral_attribute_integer");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);

        resource.unset(attr);
        resource.update();
        expectedTable = new DefaultTable("coral_attribute_integer", coralAttributeIntegerColumns);
        actualTable = databaseConnection.createQueryTable("coral_attribute_integer",
            "SELECT * FROM coral_attribute_integer");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);

        resource.set(attr, new Integer(2));
        resource.update();
        resource.update();
        expectedTable = new DefaultTable("coral_attribute_integer", coralAttributeIntegerColumns);
        expectedTable.addRow(new Object[] { new Long(2), new Integer(2) });
        actualTable = databaseConnection.createQueryTable("coral_attribute_integer",
            "SELECT * FROM coral_attribute_integer");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);
    }

    public void testLongAttribute()
        throws Exception
    {
        AttributeDefinition attr = resource.getResourceClass().getAttribute("long_attr");
        Long value = (Long)resource.get(attr);
        assertEquals(1, value.longValue());
        resource.set(attr, new Long(2));
        resource.update();
        DefaultTable expectedTable = new DefaultTable("coral_attribute_long",
            coralAttributeLongColumns);
        expectedTable.addRow(new Object[] { new Long(1), new Long(2) });
        ITable actualTable = databaseConnection.createQueryTable("coral_attribute_long",
            "SELECT * FROM coral_attribute_long");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);

        resource.unset(attr);
        resource.update();
        expectedTable = new DefaultTable("coral_attribute_long", coralAttributeLongColumns);
        actualTable = databaseConnection.createQueryTable("coral_attribute_long",
            "SELECT * FROM coral_attribute_long");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);

        resource.set(attr, new Long(2));
        resource.update();
        expectedTable = new DefaultTable("coral_attribute_long", coralAttributeLongColumns);
        expectedTable.addRow(new Object[] { new Long(2), new Long(2) });
        actualTable = databaseConnection.createQueryTable("coral_attribute_long",
            "SELECT * FROM coral_attribute_long");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);
    }

    public void testNumberAttribute()
        throws Exception
    {
        AttributeDefinition attr = resource.getResourceClass().getAttribute("number_attr");
        Number value = (Number)resource.get(attr);
        assertEquals(1, value.longValue());
        resource.set(attr, new BigDecimal(2));
        resource.update();
        DefaultTable expectedTable = new DefaultTable("coral_attribute_nubmer",
            coralAttributeNumberColumns);
        expectedTable.addRow(new Object[] { new Long(1), new BigDecimal(2) });
        ITable actualTable = databaseConnection.createQueryTable("coral_attribute_number",
            "SELECT * FROM coral_attribute_number");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);

        resource.unset(attr);
        resource.update();
        expectedTable = new DefaultTable("coral_attribute_nubmer", coralAttributeNumberColumns);
        actualTable = databaseConnection.createQueryTable("coral_attribute_number",
            "SELECT * FROM coral_attribute_number");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);

        resource.set(attr, new BigDecimal(2));
        resource.update();
        expectedTable = new DefaultTable("coral_attribute_nubmer", coralAttributeNumberColumns);
        expectedTable.addRow(new Object[] { new Long(2), new BigDecimal(2) });
        actualTable = databaseConnection.createQueryTable("coral_attribute_number",
            "SELECT * FROM coral_attribute_number");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);
    }

    public void testDateAttibute()
        throws Exception
    {
        AttributeDefinition attr = resource.getResourceClass().getAttribute("date_attr");
        Date value = (Date)resource.get(attr);
        value = createDate(2001, Calendar.JANUARY, 1);
        resource.set(attr, value);
        resource.update();
        DefaultTable expectedTable = new DefaultTable("coral_attribute_nubmer",
            coralAttributeDateColumns);
        expectedTable.addRow(new Object[] { new Long(1), value });
        ITable actualTable = databaseConnection.createQueryTable("coral_attribute_date",
            "SELECT * FROM coral_attribute_date");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);

        resource.unset(attr);
        resource.update();
        expectedTable = new DefaultTable("coral_attribute_nubmer", coralAttributeDateColumns);
        actualTable = databaseConnection.createQueryTable("coral_attribute_date",
            "SELECT * FROM coral_attribute_date");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);

        resource.set(attr, value);
        resource.update();
        expectedTable = new DefaultTable("coral_attribute_nubmer", coralAttributeDateColumns);
        expectedTable.addRow(new Object[] { new Long(2), value });
        actualTable = databaseConnection.createQueryTable("coral_attribute_date",
            "SELECT * FROM coral_attribute_date");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);
    }

    /**
     * Constructs a Date object using GregorianCalendar.
     * 
     * @param year the year.
     * @param month the month.
     * @param day the day of month.
     * @return a Date object.
     */
    private Date createDate(int year, int month, int day)
    {
        Date value;
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        value = cal.getTime();
        return value;
    }

    public void testResourceClassAttribute()
        throws Exception
    {
        AttributeDefinition attr = resource.getResourceClass().getAttribute("resource_class_attr");
        ResourceClass value = (ResourceClass)resource.get(attr);
        ResourceClass value1 = session.getSchema().getResourceClass(1L);
        ResourceClass value2 = (ResourceClass<Node>)session.getSchema().createResourceClass(
            "alt_node", "org.objectledge.coral.datatypes.NodeImpl",
            "org.objectledge.coral.datatypes.GenericResourceHandler", null, 0);
        assertEquals(value1, value);
        resource.set(attr, value2);
        resource.update();
        DefaultTable expectedTable = new DefaultTable("coral_attribute_long",
            coralAttributeResourceClassColumns);
        expectedTable.addRow(new Object[] { new Long(1), new Long(4) });
        ITable actualTable = databaseConnection.createQueryTable("coral_attribute_resource_class",
            "SELECT * FROM coral_attribute_resource_class");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);

        resource.unset(attr);
        resource.update();
        expectedTable = new DefaultTable("coral_attribute_resource_class",
            coralAttributeResourceClassColumns);
        actualTable = databaseConnection.createQueryTable("coral_attribute_resource_class",
            "SELECT * FROM coral_attribute_resource_class");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);

        resource.set(attr, value2);
        resource.update();
        expectedTable = new DefaultTable("coral_attribute_resource_class",
            coralAttributeResourceClassColumns);
        expectedTable.addRow(new Object[] { new Long(2), new Long(4) });
        actualTable = databaseConnection.createQueryTable("coral_attribute_long",
            "SELECT * FROM coral_attribute_resource_class");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);
    }

    public void testResourceAttribute()
        throws Exception
    {
        AttributeDefinition attr = resource.getResourceClass().getAttribute("resource_attr");
        Resource value = (Resource)resource.get(attr);
        Resource value1 = session.getStore().getResource(1L);
        Resource value2 = session.getStore().getResource(2L);
        assertEquals(value1, value);
        resource.set(attr, value2);
        resource.update();
        DefaultTable expectedTable = new DefaultTable("coral_attribute_long",
            coralAttributeResourceColumns);
        expectedTable.addRow(new Object[] { new Long(1), new Long(2) });
        ITable actualTable = databaseConnection.createQueryTable("coral_attribute_resource",
            "SELECT * FROM coral_attribute_resource");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);

        resource.unset(attr);
        resource.update();
        expectedTable = new DefaultTable("coral_attribute_resource", coralAttributeResourceColumns);
        actualTable = databaseConnection.createQueryTable("coral_attribute_resource",
            "SELECT * FROM coral_attribute_resource");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);

        resource.set(attr, value2);
        resource.update();
        expectedTable = new DefaultTable("coral_attribute_resource", coralAttributeResourceColumns);
        expectedTable.addRow(new Object[] { new Long(2), new Long(2) });
        actualTable = databaseConnection.createQueryTable("coral_attribute_long",
            "SELECT * FROM coral_attribute_resource");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);
    }

    public void testSubjectAttribute()
        throws Exception
    {
        AttributeDefinition attr = resource.getResourceClass().getAttribute("subject_attr");
        Subject value = (Subject)resource.get(attr);
        Subject value1 = session.getSecurity().getSubject(1L);
        Subject value2 = session.getSecurity().getSubject(2L);
        assertEquals(value1, value);
        resource.set(attr, value2);
        resource.update();
        DefaultTable expectedTable = new DefaultTable("coral_attribute_long",
            coralAttributeSubjectColumns);
        expectedTable.addRow(new Object[] { new Long(1), new Long(2) });
        ITable actualTable = databaseConnection.createQueryTable("coral_attribute_subject",
            "SELECT * FROM coral_attribute_subject");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);

        resource.unset(attr);
        resource.update();
        expectedTable = new DefaultTable("coral_attribute_subject", coralAttributeSubjectColumns);
        actualTable = databaseConnection.createQueryTable("coral_attribute_subject",
            "SELECT * FROM coral_attribute_subject");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);

        resource.set(attr, value2);
        resource.update();
        expectedTable = new DefaultTable("coral_attribute_subject", coralAttributeSubjectColumns);
        expectedTable.addRow(new Object[] { new Long(2), new Long(2) });
        actualTable = databaseConnection.createQueryTable("coral_attribute_long",
            "SELECT * FROM coral_attribute_subject");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);
    }

    public void testRoleAttribute()
        throws Exception
    {
        AttributeDefinition attr = resource.getResourceClass().getAttribute("role_attr");
        Role value = (Role)resource.get(attr);
        Role value1 = session.getSecurity().getRole(1L);
        Role value2 = session.getSecurity().getRole(2L);
        assertEquals(value1, value);
        resource.set(attr, value2);
        resource.update();
        DefaultTable expectedTable = new DefaultTable("coral_attribute_long",
            coralAttributeRoleColumns);
        expectedTable.addRow(new Object[] { new Long(1), new Long(2) });
        ITable actualTable = databaseConnection.createQueryTable("coral_attribute_role",
            "SELECT * FROM coral_attribute_role");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);

        resource.unset(attr);
        resource.update();
        expectedTable = new DefaultTable("coral_attribute_role", coralAttributeRoleColumns);
        actualTable = databaseConnection.createQueryTable("coral_attribute_role",
            "SELECT * FROM coral_attribute_role");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);

        resource.set(attr, value2);
        resource.update();
        expectedTable = new DefaultTable("coral_attribute_role", coralAttributeRoleColumns);
        expectedTable.addRow(new Object[] { new Long(2), new Long(2) });
        actualTable = databaseConnection.createQueryTable("coral_attribute_long",
            "SELECT * FROM coral_attribute_role");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);
    }

    public void testPermissionAttribute()
        throws Exception
    {
        AttributeDefinition attr = resource.getResourceClass().getAttribute("permission_attr");
        Permission value = (Permission)resource.get(attr);
        Permission value1 = session.getSecurity().getPermission(1L);
        Permission value2 = session.getSecurity().getPermission(2L);
        assertEquals(value1, value);
        resource.set(attr, value2);
        resource.update();
        DefaultTable expectedTable = new DefaultTable("coral_attribute_long",
            coralAttributePermissionColumns);
        expectedTable.addRow(new Object[] { new Long(1), new Long(2) });
        ITable actualTable = databaseConnection.createQueryTable("coral_attribute_permission",
            "SELECT * FROM coral_attribute_permission");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);

        resource.unset(attr);
        resource.update();
        expectedTable = new DefaultTable("coral_attribute_permission",
            coralAttributePermissionColumns);
        actualTable = databaseConnection.createQueryTable("coral_attribute_permission",
            "SELECT * FROM coral_attribute_permission");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);

        resource.set(attr, value2);
        resource.update();
        expectedTable = new DefaultTable("coral_attribute_permission",
            coralAttributePermissionColumns);
        expectedTable.addRow(new Object[] { new Long(2), new Long(2) });
        actualTable = databaseConnection.createQueryTable("coral_attribute_long",
            "SELECT * FROM coral_attribute_permission");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);
    }

    public void testDateRangeAttibute()
        throws Exception
    {
        AttributeDefinition attr = resource.getResourceClass().getAttribute("date_range_attr");
        DateRange value = (DateRange)resource.get(attr);
        value = new DateRange(createDate(2001, Calendar.JANUARY, 1), createDate(2002,
            Calendar.JANUARY, 1));
        resource.set(attr, value);
        resource.update();
        DefaultTable expectedTable = new DefaultTable("coral_attribute_date_range",
            coralAttributeDateRangeColumns);
        expectedTable.addRow(new Object[] { new Long(1), value.getStart(), value.getEnd() });
        ITable actualTable = databaseConnection.createQueryTable("coral_attribute_date_range",
            "SELECT * FROM coral_attribute_date_range");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);

        resource.unset(attr);
        resource.update();
        expectedTable = new DefaultTable("coral_attribute_date_range",
            coralAttributeDateRangeColumns);
        actualTable = databaseConnection.createQueryTable("coral_attribute_date_range",
            "SELECT * FROM coral_attribute_date_range");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);

        resource.set(attr, value);
        resource.update();
        expectedTable = new DefaultTable("coral_attribute_date_range",
            coralAttributeDateRangeColumns);
        expectedTable.addRow(new Object[] { new Long(2), value.getStart(), value.getEnd() });
        actualTable = databaseConnection.createQueryTable("coral_attribute_date_range",
            "SELECT * FROM coral_attribute_date_range");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);
    }

    public void testParametersAttibute()
        throws Exception
    {
        AttributeDefinition attr = resource.getResourceClass().getAttribute("parameters_attr");
        Parameters parameters = (Parameters)resource.get(attr);
        assertEquals("value", parameters.get("key"));

        parameters.set("key", "value2");
        DefaultTable expectedTable = new DefaultTable("ledge_parameters", ledgeParametersColumns);
        expectedTable.addRow(new Object[] { new Long(1), "key", "value2" });
        ITable actualTable = databaseConnection.createQueryTable("ledge_parameters",
            "SELECT * FROM ledge_parameters");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);

        parameters.remove("key");
        expectedTable = new DefaultTable("ledge_parameters", ledgeParametersColumns);
        actualTable = databaseConnection.createQueryTable("ledge_parameters",
            "SELECT * FROM ledge_parameters");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);

        parameters.set("key", "value2");
        expectedTable = new DefaultTable("ledge_parameters", ledgeParametersColumns);
        expectedTable.addRow(new Object[] { new Long(1), "key", "value2" });
        actualTable = databaseConnection.createQueryTable("ledge_parameters",
            "SELECT * FROM ledge_parameters");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);
    }

    public void testResourceListAttribute()
        throws Exception
    {
        AttributeDefinition attr = resource.getResourceClass().getAttribute("resource_list_attr");
        ResourceList value = (ResourceList)resource.get(attr);
        Resource value1 = session.getStore().getResource(1L);
        Resource value2 = session.getStore().getResource(2L);
        assertEquals(value1, value.get(0));
        value.set(0, value2);
        resource.setModified(attr);
        resource.update();
        DefaultTable expectedTable = new DefaultTable("coral_attribute_resource_list",
            coralAttributeResourceListColumns);
        expectedTable.addRow(new Object[] { new Long(1), new Integer(0), new Long(2) });
        ITable actualTable = databaseConnection.createQueryTable("coral_attribute_resource_list",
            "SELECT * FROM coral_attribute_resource_list");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);

        value.remove(0);
        resource.setModified(attr);
        resource.update();
        expectedTable = new DefaultTable("coral_attribute_resource_list",
            coralAttributeResourceListColumns);
        actualTable = databaseConnection.createQueryTable("coral_attribute_resource_list",
            "SELECT * FROM coral_attribute_resource_list");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);

        value.add(value2);
        resource.setModified(attr);
        resource.update();
        expectedTable = new DefaultTable("coral_attribute_resource_list",
            coralAttributeResourceListColumns);
        expectedTable.addRow(new Object[] { new Long(1), new Integer(0), new Long(2) });
        actualTable = databaseConnection.createQueryTable("coral_attribute_resource_list",
            "SELECT * FROM coral_attribute_resource_list");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);
    }

    public void testWeakResourceListAttribute()
        throws Exception
    {
        AttributeDefinition attr = resource.getResourceClass().getAttribute(
            "weak_resource_list_attr");
        WeakResourceList value = (WeakResourceList)resource.get(attr);
        Resource value1 = session.getStore().getResource(1L);
        Resource value2 = session.getStore().getResource(2L);
        assertEquals(value1, value.get(0));
        value.set(0, value2);
        resource.setModified(attr);
        resource.update();
        DefaultTable expectedTable = new DefaultTable("coral_attribute_weak_resource_list",
            coralAttributeWeakResourceListColumns);
        expectedTable.addRow(new Object[] { new Long(1), new Integer(0), new Long(2) });
        ITable actualTable = databaseConnection.createQueryTable(
            "coral_attribute_weak_resource_list",
            "SELECT * FROM coral_attribute_weak_resource_list");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);

        value.remove(0);
        resource.setModified(attr);
        resource.update();
        expectedTable = new DefaultTable("coral_attribute_weak_resource_list",
            coralAttributeWeakResourceListColumns);
        actualTable = databaseConnection.createQueryTable("coral_attribute_weak_resource_list",
            "SELECT * FROM coral_attribute_weak_resource_list");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);

        value.add(value2);
        resource.setModified(attr);
        resource.update();
        expectedTable = new DefaultTable("coral_attribute_weak_resource_list",
            coralAttributeWeakResourceListColumns);
        expectedTable.addRow(new Object[] { new Long(1), new Integer(0), new Long(2) });
        actualTable = databaseConnection.createQueryTable("coral_attribute_weak_resource_list",
            "SELECT * FROM coral_attribute_weak_resource_list");
        databaseConnection.close();
        assertEquals(expectedTable, actualTable);
    }
}
