package org.objectledge.coral.touchstone.level1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.datatype.DataType;
import org.objectledge.coral.datatypes.PersistentResource;
import org.objectledge.coral.datatypes.PersistentResourceHandler;
import org.objectledge.coral.datatypes.ResourceList;
import org.objectledge.coral.entity.Entity;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.touchstone.CoralTestCase;
import org.objectledge.database.DatabaseUtils;
import org.objectledge.parameters.DefaultParameters;
import org.objectledge.parameters.Parameters;

/**
 * Test for the tabular backend.
 * 
 * @author rafal
 */
public class PersistentResourcesTest
    extends CoralTestCase
{
    private CoralSession coral;

    private CoralSchema schema;

    private CoralStore store;

    private AttributeClass<String> stringAttrClass;

    private AttributeClass<Integer> intAttrClass;

    private AttributeClass<Resource> resourceAttrClass;

    private AttributeClass<Boolean> booleanAttrClass;

    private AttributeClass<Parameters> parametersAttrClass;

    private AttributeClass<ResourceList<Resource>> resourceListAttrClass;

    private AttributeClass<Subject> subjectAttrClass;

    private Resource root;

    private ResourceClass<?> testResourceClass;

    private AttributeDefinition<String> a1;

    private AttributeDefinition<Integer> a2;

    private AttributeDefinition<Resource> a3;

    private Column[] testTableCols = { new Column("RESOURCE_ID", DataType.BIGINT, Column.NO_NULLS),
                    new Column("SELECT_", DataType.VARCHAR, Column.NULLABLE),
                    new Column("A2", DataType.INTEGER, Column.NO_NULLS),
                    new Column("A3", DataType.BIGINT, Column.NULLABLE) };

    private Column[] secondTableCols = {
                    new Column("RESOURCE_ID", DataType.BIGINT, Column.NO_NULLS),
                    new Column("A4", DataType.BOOLEAN, Column.NULLABLE),
                    new Column("A5", DataType.BIGINT, Column.NO_NULLS) };

    private Column[] thirdTableCols = {
                    new Column("RESOURCE_ID", DataType.BIGINT, Column.NO_NULLS),
                    new Column("A7", DataType.BIGINT, Column.NO_NULLS) };

    private Column[] parametersTableCols = { new Column("PARAMETERS_ID", DataType.BIGINT),
                    new Column("NAME", DataType.VARCHAR), new Column("VALUE", DataType.VARCHAR) };

    private Column[] resourceListTableCols = {
                    new Column("DATA_KEY", DataType.BIGINT, Column.NO_NULLS),
                    new Column("POS", DataType.INTEGER, Column.NO_NULLS),
                    new Column("REF", DataType.BIGINT, Column.NO_NULLS) };

    private Resource testRes1;

    private Resource testRes2;

    private ResourceClass<?> secondResourceClass;

    private AttributeDefinition<Boolean> a4;

    private AttributeDefinition<Parameters> a5;

    private AttributeDefinition<ResourceList<Resource>> a6;

    private ResourceClass<?> thirdResourceClass;

    private AttributeDefinition<Subject> a7;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();
        coral = coralSessionFactory.getRootSession();
        schema = coral.getSchema();
        stringAttrClass = (AttributeClass<String>)schema.getAttributeClass("string");
        intAttrClass = (AttributeClass<Integer>)schema.getAttributeClass("integer");
        resourceAttrClass = (AttributeClass<Resource>)schema.getAttributeClass("resource");
        booleanAttrClass = (AttributeClass<Boolean>)schema.getAttributeClass("boolean");
        parametersAttrClass = (AttributeClass<Parameters>)schema.getAttributeClass("parameters");
        resourceListAttrClass = (AttributeClass<ResourceList<Resource>>)schema
            .getAttributeClass("resource_list");
        subjectAttrClass = (AttributeClass<Subject>)schema.getAttributeClass("subject");
        store = coral.getStore();
        root = store.getResource(CoralStore.ROOT_RESOURCE);
    }

    @Override
    public void tearDown()
        throws Exception
    {
        coral.close();
        super.tearDown();
    }

    public void testCreateClass()
        throws Exception
    {
        testResourceClass = schema.createResourceClass("test", PersistentResource.class.getName(),
            PersistentResourceHandler.class.getName(), "test", 0);
        a1 = schema.createAttribute("select", stringAttrClass, null, 0);
        schema.addAttribute(testResourceClass, a1, null);
        a2 = schema.createAttribute("a2", intAttrClass, null, AttributeFlags.REQUIRED);
        schema.addAttribute(testResourceClass, a2, Integer.valueOf(0));
        a3 = schema.createAttribute("a3", resourceAttrClass, "test", 0);
        schema.addAttribute(testResourceClass, a3, null);

        ITable expected = new DefaultTable("test", testTableCols);
        ITable actual = databaseConnection.createQueryTable("test", "SELECT * FROM test");
        databaseConnection.close();
        assertEquals(actual, expected);
    }

    public void testCreateResource()
        throws Exception
    {
        testCreateClass();

        Map<AttributeDefinition<?>, Object> attributes = new HashMap<AttributeDefinition<?>, Object>();
        attributes.put(a1, "foo");
        attributes.put(a2, 7);
        testRes1 = store.createResource("test1", root, testResourceClass, attributes);

        attributes.put(a1, "bar");
        attributes.put(a2, 9);
        attributes.put(a3, testRes1);
        testRes2 = store.createResource("test2", root, testResourceClass, attributes);

        DefaultTable expected = new DefaultTable("test", testTableCols);
        expected.addRow(new Object[] { testRes1.getIdObject(), "foo", Integer.valueOf(7), null });
        expected.addRow(new Object[] { testRes2.getIdObject(), "bar", Integer.valueOf(9),
                        testRes1.getIdObject() });
        ITable actual = databaseConnection.createQueryTable("test", "SELECT * FROM test");
        databaseConnection.close();
        assertEquals(expected, actual);
    }

    public void testUpdateResource()
        throws Exception
    {
        testCreateResource();

        testRes1.set(a1, "baz");
        testRes1.update();
        testRes2.set(a2, 11);
        testRes2.unset(a3);
        testRes2.update();

        DefaultTable expected = new DefaultTable("test", testTableCols);
        expected.addRow(new Object[] { testRes1.getIdObject(), "baz", Integer.valueOf(7), null });
        expected.addRow(new Object[] { testRes2.getIdObject(), "bar", Integer.valueOf(11), null });
        ITable actual = databaseConnection.createQueryTable("test", "SELECT * FROM test");
        assertEquals(expected, actual);

        databaseConnection.close();
    }

    public void testRevertResource()
        throws Exception
    {
        testCreateResource();

        testRes1.set(a1, "baz");
        testRes1.revert();
        testRes2.set(a2, 11);
        testRes2.unset(a3);
        testRes2.revert();

        assertEquals("foo", testRes1.get(a1));
        assertEquals(Integer.valueOf(9), testRes2.get(a2));
        assertEquals(testRes1, testRes2.get(a3));
    }

    public void testDeleteResource()
        throws Exception
    {
        testCreateResource();

        store.deleteResource(testRes2);
        store.deleteResource(testRes1);

        DefaultTable expected = new DefaultTable("test", testTableCols);
        ITable actual = databaseConnection.createQueryTable("test", "SELECT * FROM test");
        databaseConnection.close();
        assertEquals(expected, actual);
    }

    public void testDeleteAttributes()
        throws Exception
    {
        testCreateResource();

        schema.deleteAttribute(testResourceClass, a3);
        Column[] trimmedCols = new Column[testTableCols.length - 1];
        System.arraycopy(testTableCols, 0, trimmedCols, 0, testTableCols.length - 1);
        DefaultTable expected = new DefaultTable("test", trimmedCols);
        expected.addRow(new Object[] { testRes1.getIdObject(), "foo", Integer.valueOf(7) });
        expected.addRow(new Object[] { testRes2.getIdObject(), "bar", Integer.valueOf(9) });
        ITable actual = databaseConnection.createQueryTable("test", "SELECT * FROM test");
        databaseConnection.close();
        assertEquals(expected, actual);

        schema.deleteAttribute(testResourceClass, a2);
        Column[] trimmedCols2 = new Column[trimmedCols.length - 1];
        System.arraycopy(trimmedCols, 0, trimmedCols2, 0, trimmedCols.length - 1);
        expected = new DefaultTable("test", trimmedCols2);
        expected.addRow(new Object[] { testRes1.getIdObject(), "foo" });
        expected.addRow(new Object[] { testRes2.getIdObject(), "bar" });
        actual = databaseConnection.createQueryTable("test", "SELECT * FROM test");
        databaseConnection.close();
        assertEquals(expected, actual);

        schema.deleteAttribute(testResourceClass, a1);
        assertFalse(DatabaseUtils.hasTable(dataSource, "test"));

        assertEquals(0, testResourceClass.getDeclaredAttributes().length);
        assertEquals(1, store.getResource("test1").length);
    }

    public void testDeleteResourceClass()
        throws Exception
    {
        testCreateResource();

        store.deleteResource(testRes2);
        store.deleteResource(testRes1);
        schema.deleteResourceClass(testResourceClass);
        assertFalse(DatabaseUtils.hasTable(dataSource, "test"));
    }

    public void testCreateMultiLevelClasses()
        throws Exception
    {
        testCreateClass();

        secondResourceClass = schema.createResourceClass("second",
            PersistentResource.class.getName(), PersistentResourceHandler.class.getName(),
            "second", 0);
        Map<AttributeDefinition<?>, Object> attributes = new HashMap<AttributeDefinition<?>, Object>();
        attributes.put(a2, 0);
        a4 = schema.createAttribute("a4", booleanAttrClass, null, 0);
        schema.addAttribute(secondResourceClass, a4, null);
        a5 = schema.createAttribute("a5", parametersAttrClass, null, AttributeFlags.REQUIRED);
        schema.addAttribute(secondResourceClass, a5, new DefaultParameters());

        ITable expected = new DefaultTable("second", secondTableCols);
        ITable actual = databaseConnection.createQueryTable("test", "SELECT * FROM second");
        databaseConnection.close();
        assertEquals(actual, expected);

        thirdResourceClass = schema.createResourceClass("third",
            PersistentResource.class.getName(), PersistentResourceHandler.class.getName(), "third",
            0);
        attributes.put(a5, new DefaultParameters());
        a7 = schema.createAttribute("a7", subjectAttrClass, null, 0);
        schema.addAttribute(thirdResourceClass, a7, null);

        schema.addParentClass(thirdResourceClass, secondResourceClass, attributes);
        schema.addParentClass(secondResourceClass, testResourceClass, attributes);
    }

    public void testCreateMultiLevelClasses2()
        throws Exception
    {
        testResourceClass = schema.createResourceClass("test", PersistentResource.class.getName(),
            PersistentResourceHandler.class.getName(), "test", 0);
        a1 = schema.createAttribute("select", stringAttrClass, null, 0);
        schema.addAttribute(testResourceClass, a1, null);
        a3 = schema.createAttribute("a3", resourceAttrClass, "test", 0);
        schema.addAttribute(testResourceClass, a3, null);

        secondResourceClass = schema.createResourceClass("second",
            PersistentResource.class.getName(), PersistentResourceHandler.class.getName(),
            "second", 0);
        Map<AttributeDefinition<?>, Object> attributes = new HashMap<AttributeDefinition<?>, Object>();
        schema.addParentClass(secondResourceClass, testResourceClass, attributes);
        a4 = schema.createAttribute("a4", booleanAttrClass, null, 0);
        schema.addAttribute(secondResourceClass, a4, null);
        a5 = schema.createAttribute("a5", parametersAttrClass, null, AttributeFlags.REQUIRED);
        schema.addAttribute(secondResourceClass, a5, new DefaultParameters());

        attributes.put(a5, new DefaultParameters());
        testRes2 = store.createResource("test2", root, secondResourceClass, attributes);

        a2 = schema.createAttribute("a2", intAttrClass, null, AttributeFlags.REQUIRED);
        schema.addAttribute(testResourceClass, a2, Integer.valueOf(0));

        DefaultTable expected = new DefaultTable("second", secondTableCols);
        expected.addRow(new Object[] { testRes2.getIdObject(), null, 0L });
        ITable actual = databaseConnection.createQueryTable("second", "SELECT * FROM second");
        assertEquals(expected, actual);

        expected = new DefaultTable("ledge_parameters", parametersTableCols);
        expected.addRow(new Object[] { 0L, "", "" });
        actual = databaseConnection.createQueryTable("ledge_parameters",
            "SELECT * FROM ledge_parameters");
        assertEquals(expected, actual);

        databaseConnection.close();
    }

    public void testCreateMultiLevelResources()
        throws Exception
    {
        testCreateMultiLevelClasses();

        Map<AttributeDefinition<?>, Object> attributes = new HashMap<AttributeDefinition<?>, Object>();
        attributes.put(a1, "foo");
        attributes.put(a2, 7);
        testRes1 = store.createResource("test1", root, testResourceClass, attributes);

        attributes.put(a1, "bar");
        attributes.put(a2, 9);
        attributes.put(a3, testRes1);
        attributes.put(a4, true);
        Parameters p = new DefaultParameters();
        p.set("ok", true);
        p.set("number", 11);
        attributes.put(a5, p);
        testRes2 = store.createResource("test2", root, secondResourceClass, attributes);

        DefaultTable expected = new DefaultTable("test", testTableCols);
        expected.addRow(new Object[] { testRes1.getIdObject(), "foo", Integer.valueOf(7), null });
        expected.addRow(new Object[] { testRes2.getIdObject(), "bar", Integer.valueOf(9),
                        testRes1.getIdObject() });
        ITable actual = databaseConnection.createQueryTable("test", "SELECT * FROM test");
        assertEquals(expected, actual);

        expected = new DefaultTable("second", secondTableCols);
        expected.addRow(new Object[] { testRes2.getIdObject(), Boolean.TRUE, 0L });
        actual = databaseConnection.createQueryTable("second", "SELECT * FROM second");
        assertEquals(expected, actual);

        expected = new DefaultTable("ledge_parameters", parametersTableCols);
        expected.addRow(new Object[] { 0L, "", "" });
        expected.addRow(new Object[] { 0L, "ok", "true" });
        expected.addRow(new Object[] { 0L, "number", "11" });
        actual = databaseConnection.createQueryTable("ledge_parameters",
            "SELECT * FROM ledge_parameters");
        assertEquals(expected, actual);

        databaseConnection.close();
    }

    public void testUpdateMultiLevelResources()
        throws Exception
    {
        testCreateMultiLevelResources();

        testRes1.set(a1, "foo_1");
        testRes1.set(a3, testRes2);

        testRes2.set(a1, "bar_1");
        testRes2.unset(a3);
        testRes2.set(a4, false);
        testRes2.get(a5).set("number", 22);

        testRes1.update();
        testRes2.update();

        DefaultTable expected = new DefaultTable("test", testTableCols);
        expected.addRow(new Object[] { testRes1.getIdObject(), "foo_1", Integer.valueOf(7),
                        testRes2.getIdObject() });
        expected.addRow(new Object[] { testRes2.getIdObject(), "bar_1", Integer.valueOf(9), null });
        ITable actual = databaseConnection.createQueryTable("test", "SELECT * FROM test");
        assertEquals(expected, actual);

        expected = new DefaultTable("second", secondTableCols);
        expected.addRow(new Object[] { testRes2.getIdObject(), Boolean.FALSE, 0L });
        actual = databaseConnection.createQueryTable("second", "SELECT * FROM second");
        assertEquals(expected, actual);

        expected = new DefaultTable("ledge_parameters", parametersTableCols);
        expected.addRow(new Object[] { 0L, "", "" });
        expected.addRow(new Object[] { 0L, "ok", "true" });
        expected.addRow(new Object[] { 0L, "number", "22" });
        actual = databaseConnection.createQueryTable("ledge_parameters",
            "SELECT * FROM ledge_parameters");
        assertEquals(expected, actual);

        databaseConnection.close();
    }

    public void testDeleteMultiLevelResources()
        throws Exception
    {
        testCreateMultiLevelResources();

        store.deleteResource(testRes2);
        store.deleteResource(testRes1);

        DefaultTable expected = new DefaultTable("test", testTableCols);
        ITable actual = databaseConnection.createQueryTable("test", "SELECT * FROM test");
        assertEquals(expected, actual);

        expected = new DefaultTable("second", secondTableCols);
        actual = databaseConnection.createQueryTable("second", "SELECT * FROM second");
        assertEquals(expected, actual);

        expected = new DefaultTable("ledge_parameters", parametersTableCols);
        actual = databaseConnection.createQueryTable("ledge_parameters",
            "SELECT * FROM ledge_parameters");
        assertEquals(expected, actual);

        databaseConnection.close();
    }

    public void testCreateMultiLevelResourceClassWithRequiredAttributes()
        throws Exception
    {
        testCreateClass();

        secondResourceClass = schema.createResourceClass("second",
            PersistentResource.class.getName(), PersistentResourceHandler.class.getName(),
            "second", 0);
        a4 = schema.createAttribute("a4", booleanAttrClass, null, 0);
        schema.addAttribute(secondResourceClass, a4, null);
        a5 = schema.createAttribute("a5", parametersAttrClass, null, AttributeFlags.REQUIRED);
        schema.addAttribute(secondResourceClass, a5, null);

        Map<AttributeDefinition<?>, Object> attributes = new HashMap<AttributeDefinition<?>, Object>();
        Parameters p = new DefaultParameters();
        attributes.put(a5, p);
        testRes2 = store.createResource("test2", root, secondResourceClass, attributes);

        attributes.clear();
        attributes.put(a2, 9);

        schema.addParentClass(secondResourceClass, testResourceClass, attributes);

        DefaultTable expected = new DefaultTable("test", testTableCols);
        expected.addRow(new Object[] { testRes2.getIdObject(), null, Integer.valueOf(9), null });
        ITable actual = databaseConnection.createQueryTable("test", "SELECT * FROM test");
        assertEquals(expected, actual);

        expected = new DefaultTable("second", secondTableCols);
        expected.addRow(new Object[] { testRes2.getIdObject(), null, 0L });
        actual = databaseConnection.createQueryTable("second", "SELECT * FROM second");
        assertEquals(expected, actual);

        expected = new DefaultTable("ledge_parameters", parametersTableCols);
        expected.addRow(new Object[] { 0L, "", "" });
        actual = databaseConnection.createQueryTable("ledge_parameters",
            "SELECT * FROM ledge_parameters");
        assertEquals(expected, actual);

        databaseConnection.close();

        assertTrue(Arrays.asList(secondResourceClass.getParentClasses())
            .contains(testResourceClass));
        assertTrue(Arrays.asList(secondResourceClass.getAllAttributes()).contains(a2));
        assertEquals(Integer.valueOf(9), testRes2.get(a2));
    }

    public void testRevertMultiLevelResource()
        throws Exception
    {
        testCreateMultiLevelResources();

        testRes1.set(a1, "baz");
        testRes1.revert();
        testRes2.set(a2, 11);
        testRes2.unset(a3);
        testRes2.set(a4, false);
        testRes2.revert();

        assertEquals("foo", testRes1.get(a1));
        assertEquals(Integer.valueOf(9), testRes2.get(a2));
        assertEquals(testRes1, testRes2.get(a3));
        assertEquals(Boolean.TRUE, testRes2.get(a4));
    }

    public void testDeleteMultiLevelAttribute()
        throws Exception
    {
        testCreateMultiLevelResources();

        schema.deleteAttribute(testResourceClass, a3);
        Column[] trimmedCols = new Column[testTableCols.length - 1];
        System.arraycopy(testTableCols, 0, trimmedCols, 0, testTableCols.length - 1);
        DefaultTable expected = new DefaultTable("test", trimmedCols);
        expected.addRow(new Object[] { testRes1.getIdObject(), "foo", Integer.valueOf(7) });
        expected.addRow(new Object[] { testRes2.getIdObject(), "bar", Integer.valueOf(9) });
        ITable actual = databaseConnection.createQueryTable("test", "SELECT * FROM test");
        databaseConnection.close();
        assertEquals(expected, actual);

        schema.deleteAttribute(testResourceClass, a2);
        Column[] trimmedCols2 = new Column[trimmedCols.length - 1];
        System.arraycopy(trimmedCols, 0, trimmedCols2, 0, trimmedCols.length - 1);
        expected = new DefaultTable("test", trimmedCols2);
        expected.addRow(new Object[] { testRes1.getIdObject(), "foo" });
        expected.addRow(new Object[] { testRes2.getIdObject(), "bar" });
        actual = databaseConnection.createQueryTable("test", "SELECT * FROM test");
        databaseConnection.close();
        assertEquals(expected, actual);

        schema.deleteAttribute(testResourceClass, a1);
        assertFalse(DatabaseUtils.hasTable(dataSource, "test"));

        assertEquals(0, testResourceClass.getDeclaredAttributes().length);
        assertEquals(1, store.getResource("test1").length);
        assertEquals(1, store.getResource("test2").length);

        Comparator<Entity> byId = new Comparator<Entity>()
            {
                public int compare(Entity e1, Entity e2)
                {
                    return (int)(e1.getId() - e2.getId());
                }
            };
        final AttributeDefinition<?>[] ad1 = secondResourceClass.getDeclaredAttributes();
        Arrays.sort(ad1, byId);
        final AttributeDefinition<?>[] ad2 = secondResourceClass.getAllAttributes();
        Arrays.sort(ad2, byId);
        assertTrue(Arrays.equals(ad1, ad2));

        a6 = schema.createAttribute("a6", resourceListAttrClass, null, AttributeFlags.REQUIRED);
        final ResourceList<Resource> defValue = resourceListAttrClass.getHandler()
            .toAttributeValue(new ArrayList<Resource>());
        schema.addAttribute(testResourceClass, a6, defValue);

        testRes1.get(a6).add(testRes1);
        testRes1.setModified(a6);
        testRes1.update();
        testRes2.get(a6).add(testRes2);
        testRes2.setModified(a6);
        testRes2.update();

        expected = new DefaultTable("coral_attribute_resource_list", resourceListTableCols);
        row(expected, 0, 0, testRes1.getId());
        row(expected, 1, 0, testRes2.getId());
        actual = databaseConnection.createQueryTable("coral_attribute_resource_list",
            "SELECT * FROM coral_attribute_resource_list");
        assertEquals(expected, actual);
        databaseConnection.close();

        schema.deleteAttribute(testResourceClass, a6);

        expected = new DefaultTable("coral_attribute_resource_list", resourceListTableCols);
        actual = databaseConnection.createQueryTable("coral_attribute_resource_list",
            "SELECT * FROM coral_attribute_resource_list");
        assertEquals(expected, actual);
        databaseConnection.close();
    }

    public void testDeleteMultiLevelResourceClass()
        throws Exception
    {
        testCreateMultiLevelResources();

        a6 = schema.createAttribute("a6", resourceListAttrClass, null, AttributeFlags.REQUIRED);
        final ResourceList<Resource> defValue = resourceListAttrClass.getHandler()
            .toAttributeValue(new ArrayList<Resource>());
        schema.addAttribute(testResourceClass, a6, defValue);

        testRes1.get(a6).add(testRes1);
        testRes1.setModified(a6);
        testRes1.update();
        testRes2.get(a6).add(testRes2);
        testRes2.setModified(a6);
        testRes2.update();

        DefaultTable expected = new DefaultTable("coral_attribute_resource_list",
            resourceListTableCols);
        row(expected, 0, 0, testRes1.getId());
        row(expected, 1, 0, testRes2.getId());
        ITable actual = databaseConnection.createQueryTable("coral_attribute_resource_list",
            "SELECT * FROM coral_attribute_resource_list");
        assertEquals(expected, actual);
        databaseConnection.close();

        schema.deleteParentClass(secondResourceClass, testResourceClass);

        Column[] expTestTableCols = new Column[testTableCols.length + 1];
        System.arraycopy(testTableCols, 0, expTestTableCols, 0, testTableCols.length);
        expTestTableCols[testTableCols.length] = new Column("A6", DataType.BIGINT, Column.NO_NULLS);

        expected = new DefaultTable("test", expTestTableCols);
        expected.addRow(new Object[] { testRes1.getIdObject(), "foo", Integer.valueOf(7), null,
                        Integer.valueOf(0) });
        actual = databaseConnection.createQueryTable("test", "SELECT * FROM test");
        assertEquals(expected, actual);

        expected = new DefaultTable("coral_attribute_resource_list", resourceListTableCols);
        row(expected, 0, 0, testRes1.getId());
        actual = databaseConnection.createQueryTable("coral_attribute_resource_list",
            "SELECT * FROM coral_attribute_resource_list");
        assertEquals(expected, actual);
        databaseConnection.close();
    }

    public void testCustomAttributeOps()
        throws Exception
    {
        testResourceClass = schema.createResourceClass("test", PersistentResource.class.getName(),
            PersistentResourceHandler.class.getName(), "test", 0);
        a6 = schema.createAttribute("a6", resourceListAttrClass, null, 0);
        schema.addAttribute(testResourceClass, a6, null);
        Map<AttributeDefinition<?>, Object> attributes = new HashMap<AttributeDefinition<?>, Object>();
        testRes1 = store.createResource("test1", root, testResourceClass, attributes);

        Column[] t1a6Cols = new Column[] {
                        new Column("RESOURCE_ID", DataType.BIGINT, Column.NO_NULLS),
                        new Column("A6", DataType.BIGINT, Column.NULLABLE) };

        DefaultTable expected = new DefaultTable("test", t1a6Cols);
        expected.addRow(new Object[] { testRes1.getIdObject(), null });
        ITable actual = databaseConnection.createQueryTable("test", "SELECT * FROM test");
        assertEquals(expected, actual);

        expected = new DefaultTable("coral_attribute_resource_list", resourceListTableCols);
        actual = databaseConnection.createQueryTable("coral_attribute_resource_list",
            "SELECT * FROM coral_attribute_resource_list");
        assertEquals(expected, actual);

        databaseConnection.close();

        testRes1.set(a6, new ResourceList<Resource>(coralSessionFactory));
        testRes1.update();

        expected = new DefaultTable("test", t1a6Cols);
        expected.addRow(new Object[] { testRes1.getIdObject(), Integer.valueOf(0) });
        actual = databaseConnection.createQueryTable("test", "SELECT * FROM test");
        assertEquals(expected, actual);

        expected = new DefaultTable("coral_attribute_resource_list", resourceListTableCols);
        actual = databaseConnection.createQueryTable("coral_attribute_resource_list",
            "SELECT * FROM coral_attribute_resource_list");
        assertEquals(expected, actual);

        databaseConnection.close();

        testRes1.unset(a6);
        testRes1.update();

        expected = new DefaultTable("test", t1a6Cols);
        expected.addRow(new Object[] { testRes1.getIdObject(), null });
        actual = databaseConnection.createQueryTable("test", "SELECT * FROM test");
        assertEquals(expected, actual);

        expected = new DefaultTable("coral_attribute_resource_list", resourceListTableCols);
        actual = databaseConnection.createQueryTable("coral_attribute_resource_list",
            "SELECT * FROM coral_attribute_resource_list");
        assertEquals(expected, actual);

        databaseConnection.close();
    }

    private static void row(DefaultTable table, Object... columns)
        throws DataSetException
    {
        table.addRow(columns);
    }
}
