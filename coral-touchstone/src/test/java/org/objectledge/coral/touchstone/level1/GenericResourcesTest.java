package org.objectledge.coral.touchstone.level1;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.datatype.DataType;
import org.objectledge.container.LedgeContainer;
import org.objectledge.coral.datatypes.GenericResourceHandler;
import org.objectledge.coral.datatypes.ResourceList;
import org.objectledge.coral.datatypes.StandardResource;
import org.objectledge.coral.entity.Entity;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.touchstone.CoralTestCase;
import org.objectledge.database.DatabaseUtils;
import org.objectledge.database.ThreadDataSource;
import org.objectledge.filesystem.FileSystem;
import org.objectledge.parameters.DefaultParameters;
import org.objectledge.parameters.Parameters;

/**
 * Test for the generic backend.
 * 
 * @author rafal
 */
public class GenericResourcesTest
    extends CoralTestCase
{
    private CoralSession coral;

    private CoralSchema schema;

    private CoralStore store;

    private Map<AttributeDefinition<?>, Object> attributes = new HashMap<AttributeDefinition<?>, Object>();

    private AttributeClass<String> stringAttr;

    private AttributeClass<Integer> intAttr;

    private AttributeClass<Resource> resourceAttr;

    private AttributeClass<Boolean> booleanAttr;

    private AttributeClass<Parameters> parametersAttr;

    private AttributeClass<ResourceList<Resource>> resourceListAttr;

    private AttributeClass<Subject> subjectAttr;

    private ResourceClass<?> firstClass;

    private ResourceClass<?> secondClass;

    private ResourceClass<?> thirdClass;

    private AttributeDefinition<String> a1;

    private AttributeDefinition<Integer> a2;

    private AttributeDefinition<Resource> a3;

    private AttributeDefinition<Boolean> a4;

    private AttributeDefinition<Parameters> a5;

    private AttributeDefinition<ResourceList<Resource>> a6;

    private AttributeDefinition<Subject> a7;

    private Column[] parametersCols = { nnCol("PARAMETERS_ID", DataType.BIGINT),
                    nnCol("NAME", DataType.VARCHAR), nnCol("VALUE", DataType.VARCHAR) };

    private static final Column DATA_KEY = nnCol("DATA_KEY", DataType.BIGINT);

    private Column[] resourceListCols = { DATA_KEY, nnCol("POS", DataType.INTEGER),
                    nnCol("REF", DataType.BIGINT) };

    private Column[] stringAttrCols = { DATA_KEY, nnCol("DATA", DataType.VARCHAR) };

    private Column[] intAttrCols = { DATA_KEY, nnCol("DATA", DataType.INTEGER) };

    private Column[] booleanAttrCols = { DATA_KEY, nnCol("DATA", DataType.BOOLEAN) };

    private Column[] resourceAttrCols = { DATA_KEY, nnCol("REF", DataType.BIGINT) };

    private Column[] genericResCols = { nnCol("RESOURCE_ID", DataType.BIGINT),
                    nnCol("ATTRIBUTE_DEFINITION_ID", DataType.BIGINT), DATA_KEY };

    private Resource rootRes;

    private Resource firstRes;

    private Resource secondRes;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();
        coral = coralSessionFactory.getRootSession();
        schema = coral.getSchema();
        stringAttr = schema.getAttributeClass("string", String.class);
        intAttr = schema.getAttributeClass("integer", Integer.class);
        resourceAttr = schema.getAttributeClass("resource", Resource.class);
        booleanAttr = schema.getAttributeClass("boolean", Boolean.class);
        parametersAttr = schema.getAttributeClass("parameters", Parameters.class);
        subjectAttr = schema.getAttributeClass("subject", Subject.class);
        resourceListAttr = (AttributeClass<ResourceList<Resource>>)schema
            .getAttributeClass("resource_list");
        store = coral.getStore();
        rootRes = store.getResource(CoralStore.ROOT_RESOURCE);
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
        firstClass = schema.createResourceClass("first", StandardResource.class.getName(),
            GenericResourceHandler.class.getName(), null, 0);
        a1 = schema.createAttribute("select", stringAttr, null, null, 0);
        schema.addAttribute(firstClass, a1, null);
        a2 = schema.createAttribute("a2", intAttr, null, null, AttributeFlags.REQUIRED);
        schema.addAttribute(firstClass, a2, 0);
        a3 = schema.createAttribute("a3", resourceAttr, null, "first", 0);
        schema.addAttribute(firstClass, a3, null);
    }

    public void testCreateResource()
        throws Exception
    {
        testCreateClass();

        attributes.put(a1, "foo");
        attributes.put(a2, 7);
        firstRes = store.createResource("test1", rootRes, firstClass, attributes);

        attributes.put(a1, "bar");
        attributes.put(a2, 9);
        attributes.put(a3, firstRes);
        secondRes = store.createResource("test2", rootRes, firstClass, attributes);

        expGenResTable();
        expRow(firstRes.getId(), a1.getId(), 0);
        expRow(firstRes.getId(), a2.getId(), 0);
        expRow(secondRes.getId(), a1.getId(), 1);
        expRow(secondRes.getId(), a2.getId(), 1);
        expRow(secondRes.getId(), a3.getId(), 0);
        assertGenResTable();

        expTable("coral_attribute_string", stringAttrCols);
        expRow(0, "foo");
        expRow(1, "bar");
        assertExpTable();

        expTable("coral_attribute_integer", intAttrCols);
        expRow(0, 7);
        expRow(1, 9);
        assertExpTable();

        expTable("coral_attribute_resource", resourceAttrCols);
        expRow(0, firstRes.getId());
        assertExpTable();
    }

    public void testUpdateResource()
        throws Exception
    {
        testCreateResource();

        firstRes.set(a1, "baz");
        firstRes.update();
        secondRes.set(a2, 11);
        secondRes.unset(a3);
        secondRes.update();

        expGenResTable();
        expRow(firstRes.getId(), a1.getId(), 0);
        expRow(firstRes.getId(), a2.getId(), 0);
        expRow(secondRes.getId(), a1.getId(), 1);
        expRow(secondRes.getId(), a2.getId(), 1);
        assertGenResTable();

        expTable("coral_attribute_string", stringAttrCols);
        expRow(0, "baz");
        expRow(1, "bar");
        assertExpTable();

        expTable("coral_attribute_integer", intAttrCols);
        expRow(0, 7);
        expRow(1, 11);
        assertExpTable();

        expTable("coral_attribute_resource", resourceAttrCols);
        assertExpTable();
    }

    public void testRevertResource()
        throws Exception
    {
        testCreateResource();

        firstRes.set(a1, "baz");
        firstRes.revert();
        secondRes.set(a2, 11);
        secondRes.unset(a3);
        secondRes.revert();

        assertEquals("foo", firstRes.get(a1));
        assertEquals(Integer.valueOf(9), secondRes.get(a2));
        assertEquals(firstRes, secondRes.get(a3));
    }

    public void testDeleteResource()
        throws Exception
    {
        testCreateResource();

        store.deleteResource(secondRes);
        store.deleteResource(firstRes);

        expGenResTable();
        assertGenResTable();

        expTable("coral_attribute_string", stringAttrCols);
        assertExpTable();

        expTable("coral_attribute_integer", intAttrCols);
        assertExpTable();

        expTable("coral_attribute_resource", resourceAttrCols);
        assertExpTable();
    }

    public void testDeleteAttributes()
        throws Exception
    {
        testCreateResource();

        schema.deleteAttribute(firstClass, a3);

        expGenResTable();
        expRow(firstRes.getId(), a1.getId(), 0);
        expRow(firstRes.getId(), a2.getId(), 0);
        expRow(secondRes.getId(), a1.getId(), 1);
        expRow(secondRes.getId(), a2.getId(), 1);
        assertGenResTable();

        expTable("coral_attribute_string", stringAttrCols);
        expRow(0, "foo");
        expRow(1, "bar");
        assertExpTable();

        expTable("coral_attribute_integer", intAttrCols);
        expRow(0, 7);
        expRow(1, 9);
        assertExpTable();

        expTable("coral_attribute_resource", resourceAttrCols);
        assertExpTable();

        schema.deleteAttribute(firstClass, a2);

        expGenResTable();
        expRow(firstRes.getId(), a1.getId(), 0);
        expRow(secondRes.getId(), a1.getId(), 1);
        assertGenResTable();

        expTable("coral_attribute_string", stringAttrCols);
        expRow(0, "foo");
        expRow(1, "bar");
        assertExpTable();

        expTable("coral_attribute_integer", intAttrCols);
        assertExpTable();

        schema.deleteAttribute(firstClass, a1);

        assertEquals(0, firstClass.getDeclaredAttributes().length);
        assertEquals(1, store.getResource("test1").length);

        expGenResTable();
        assertGenResTable();

        expTable("coral_attribute_string", stringAttrCols);
        assertExpTable();
    }

    public void testDeleteResourceClass()
        throws Exception
    {
        testCreateResource();

        store.deleteResource(secondRes);
        store.deleteResource(firstRes);
        schema.deleteResourceClass(firstClass);
        assertFalse(DatabaseUtils.hasTable(dataSource, "first"));
    }

    public void testCreateMultiLevelClasses()
        throws Exception
    {
        testCreateClass();

        secondClass = schema.createResourceClass("second", StandardResource.class.getName(),
            GenericResourceHandler.class.getName(), null, 0);
        attributes.put(a2, 0);
        a4 = schema.createAttribute("a4", booleanAttr, null, null, 0);
        schema.addAttribute(secondClass, a4, null);
        a5 = schema.createAttribute("a5", parametersAttr, null, null, AttributeFlags.REQUIRED);
        schema.addAttribute(secondClass, a5, new DefaultParameters());

        thirdClass = schema.createResourceClass("third", StandardResource.class.getName(),
            GenericResourceHandler.class.getName(), null, 0);
        attributes.put(a5, new DefaultParameters());
        a7 = schema.createAttribute("a7", subjectAttr, null, null, 0);
        schema.addAttribute(thirdClass, a7, null);

        schema.addParentClass(thirdClass, secondClass, attributes);
        schema.addParentClass(secondClass, firstClass, attributes);

        expGenResTable();
        assertGenResTable();

        expTable("coral_attribute_string", stringAttrCols);
        assertExpTable();

        expTable("coral_attribute_integer", intAttrCols);
        assertExpTable();

        expTable("coral_attribute_resource", resourceAttrCols);
        assertExpTable();

        expTable("coral_attribute_boolean", booleanAttrCols);
        assertExpTable();

        expTable("ledge_parameters", parametersCols);
        assertExpTable();
    }

    public void testCreateMultiLevelClasses2()
        throws Exception
    {
        firstClass = schema.createResourceClass("first", StandardResource.class.getName(),
            GenericResourceHandler.class.getName(), null, 0);
        a1 = schema.createAttribute("select", stringAttr, null, null, 0);
        schema.addAttribute(firstClass, a1, null);
        a3 = schema.createAttribute("a3", resourceAttr, null, "first", 0);
        schema.addAttribute(firstClass, a3, null);

        secondClass = schema.createResourceClass("second", StandardResource.class.getName(),
            GenericResourceHandler.class.getName(), null, 0);
        schema.addParentClass(secondClass, firstClass, attributes);
        a4 = schema.createAttribute("a4", booleanAttr, null, null, 0);
        schema.addAttribute(secondClass, a4, null);
        a5 = schema.createAttribute("a5", parametersAttr, null, null, AttributeFlags.REQUIRED);
        schema.addAttribute(secondClass, a5, new DefaultParameters());

        attributes.put(a5, new DefaultParameters());
        secondRes = store.createResource("test2", rootRes, secondClass, attributes);

        expGenResTable();
        expRow(secondRes.getId(), a5.getId(), 0);
        assertGenResTable();

        expTable("coral_attribute_string", stringAttrCols);
        assertExpTable();

        expTable("coral_attribute_integer", intAttrCols);
        assertExpTable();

        expTable("coral_attribute_resource", resourceAttrCols);
        assertExpTable();

        expTable("coral_attribute_boolean", booleanAttrCols);
        assertExpTable();

        expTable("ledge_parameters", parametersCols);
        expRow(0L, "", "");
        assertExpTable();

        a2 = schema.createAttribute("a2", intAttr, null, null, AttributeFlags.REQUIRED);
        schema.addAttribute(firstClass, a2, 0);

        expGenResTable();
        expRow(secondRes.getId(), a5.getId(), 0);
        expRow(secondRes.getId(), a2.getId(), 0);
        assertGenResTable();

        expTable("coral_attribute_string", stringAttrCols);
        assertExpTable();

        expTable("coral_attribute_integer", intAttrCols);
        expRow(0, 0);
        assertExpTable();

        expTable("coral_attribute_resource", resourceAttrCols);
        assertExpTable();

        expTable("coral_attribute_boolean", booleanAttrCols);
        assertExpTable();

        expTable("ledge_parameters", parametersCols);
        expRow(0L, "", "");
        assertExpTable();
    }

    public void testCreateMultiLevelResources()
        throws Exception
    {
        testCreateMultiLevelClasses();

        attributes.put(a1, "foo");
        attributes.put(a2, 7);
        firstRes = store.createResource("test1", rootRes, firstClass, attributes);

        attributes.put(a1, "bar");
        attributes.put(a2, 9);
        attributes.put(a3, firstRes);
        attributes.put(a4, true);
        Parameters p = new DefaultParameters();
        p.set("ok", true);
        p.set("number", 11);
        attributes.put(a5, p);
        secondRes = store.createResource("test2", rootRes, secondClass, attributes);

        expGenResTable();
        expRow(firstRes.getId(), a1.getId(), 0);
        expRow(firstRes.getId(), a2.getId(), 0);
        expRow(secondRes.getId(), a1.getId(), 1);
        expRow(secondRes.getId(), a2.getId(), 1);
        expRow(secondRes.getId(), a3.getId(), 0);
        expRow(secondRes.getId(), a4.getId(), 0);
        expRow(secondRes.getId(), a5.getId(), 0);
        assertGenResTable();

        expTable("coral_attribute_string", stringAttrCols);
        expRow(0, "foo");
        expRow(1, "bar");
        assertExpTable();

        expTable("coral_attribute_integer", intAttrCols);
        expRow(0, 7);
        expRow(1, 9);
        assertExpTable();

        expTable("coral_attribute_resource", resourceAttrCols);
        expRow(0, firstRes.getId());
        assertExpTable();

        expTable("coral_attribute_boolean", booleanAttrCols);
        expRow(0, true);
        assertExpTable();

        expTable("ledge_parameters", parametersCols);
        expRow(0L, "", "");
        expRow(0L, "number", "11");
        expRow(0L, "ok", "true");
        assertExpTable();
    }

    public void testUpdateMultiLevelResources()
        throws Exception
    {
        testCreateMultiLevelResources();

        firstRes.set(a1, "foo_1");
        firstRes.set(a3, secondRes);

        secondRes.set(a1, "bar_1");
        secondRes.unset(a3);
        secondRes.set(a4, false);
        secondRes.get(a5).set("number", 22);

        firstRes.update();
        secondRes.update();

        expGenResTable();
        expRow(firstRes.getId(), a1.getId(), 0);
        expRow(firstRes.getId(), a2.getId(), 0);
        expRow(firstRes.getId(), a3.getId(), 1);
        expRow(secondRes.getId(), a1.getId(), 1);
        expRow(secondRes.getId(), a2.getId(), 1);
        expRow(secondRes.getId(), a4.getId(), 0);
        expRow(secondRes.getId(), a5.getId(), 0);
        assertGenResTable();

        expTable("coral_attribute_string", stringAttrCols);
        expRow(0, "foo_1");
        expRow(1, "bar_1");
        assertExpTable();

        expTable("coral_attribute_integer", intAttrCols);
        expRow(0, 7);
        expRow(1, 9);
        assertExpTable();

        expTable("coral_attribute_resource", resourceAttrCols);
        expRow(1, secondRes.getId());
        assertExpTable();

        expTable("coral_attribute_boolean", booleanAttrCols);
        expRow(0, false);
        assertExpTable();

        expTable("ledge_parameters", parametersCols);
        expRow(0L, "", "");
        expRow(0L, "number", "22");
        expRow(0L, "ok", "true");
        assertExpTable();
    }

    public void testDeleteMultiLevelResources()
        throws Exception
    {
        testCreateMultiLevelResources();

        store.deleteResource(secondRes);
        store.deleteResource(firstRes);

        expGenResTable();
        assertGenResTable();

        expTable("coral_attribute_string", stringAttrCols);
        assertExpTable();

        expTable("coral_attribute_integer", intAttrCols);
        assertExpTable();

        expTable("coral_attribute_resource", resourceAttrCols);
        assertExpTable();

        expTable("coral_attribute_boolean", booleanAttrCols);
        assertExpTable();

        expTable("ledge_parameters", parametersCols);
        assertExpTable();
    }

    public void testCreateMultiLevelResourceClassWithRequiredAttributes()
        throws Exception
    {
        testCreateClass();

        secondClass = schema.createResourceClass("second", StandardResource.class.getName(),
            GenericResourceHandler.class.getName(), null, 0);
        a4 = schema.createAttribute("a4", booleanAttr, null, null, 0);
        schema.addAttribute(secondClass, a4, null);
        a5 = schema.createAttribute("a5", parametersAttr, null, null, AttributeFlags.REQUIRED);
        schema.addAttribute(secondClass, a5, null);

        attributes.put(a5, new DefaultParameters());
        secondRes = store.createResource("test2", rootRes, secondClass, attributes);

        attributes.clear();
        attributes.put(a2, 9);

        schema.addParentClass(secondClass, firstClass, attributes);

        expGenResTable();
        expRow(secondRes.getId(), a2.getId(), 0);
        expRow(secondRes.getId(), a5.getId(), 0);
        assertGenResTable();

        expTable("coral_attribute_string", stringAttrCols);
        assertExpTable();

        expTable("coral_attribute_integer", intAttrCols);
        expRow(0, 9);
        assertExpTable();

        expTable("coral_attribute_resource", resourceAttrCols);
        assertExpTable();

        expTable("coral_attribute_boolean", booleanAttrCols);
        assertExpTable();

        expTable("ledge_parameters", parametersCols);
        expRow(0, "", "");
        assertExpTable();

        assertTrue(Arrays.asList(secondClass.getParentClasses()).contains(firstClass));
        assertTrue(Arrays.asList(secondClass.getAllAttributes()).contains(a2));
        assertEquals(Integer.valueOf(9), secondRes.get(a2));
    }

    public void testRevertMultiLevelResource()
        throws Exception
    {
        testCreateMultiLevelResources();

        firstRes.set(a1, "baz");
        firstRes.revert();
        secondRes.set(a2, 11);
        secondRes.unset(a3);
        secondRes.set(a4, false);
        secondRes.revert();

        assertEquals("foo", firstRes.get(a1));
        assertEquals(Integer.valueOf(9), secondRes.get(a2));
        assertEquals(firstRes, secondRes.get(a3));
        assertEquals(Boolean.TRUE, secondRes.get(a4));
    }

    public void testDeleteMultiLevelAttribute()
        throws Exception
    {
        testCreateMultiLevelResources();

        schema.deleteAttribute(firstClass, a3);

        expGenResTable();
        expRow(firstRes.getId(), a1.getId(), 0);
        expRow(firstRes.getId(), a2.getId(), 0);
        expRow(secondRes.getId(), a1.getId(), 1);
        expRow(secondRes.getId(), a2.getId(), 1);
        expRow(secondRes.getId(), a4.getId(), 0);
        expRow(secondRes.getId(), a5.getId(), 0);
        assertGenResTable();

        expTable("coral_attribute_string", stringAttrCols);
        expRow(0, "foo");
        expRow(1, "bar");
        assertExpTable();

        expTable("coral_attribute_integer", intAttrCols);
        expRow(0, 7);
        expRow(1, 9);
        assertExpTable();

        expTable("coral_attribute_resource", resourceAttrCols);
        assertExpTable();

        expTable("coral_attribute_boolean", booleanAttrCols);
        expRow(0, true);
        assertExpTable();

        expTable("ledge_parameters", parametersCols);
        expRow(0L, "", "");
        expRow(0L, "number", "11");
        expRow(0L, "ok", "true");
        assertExpTable();

        schema.deleteAttribute(firstClass, a2);

        expGenResTable();
        expRow(firstRes.getId(), a1.getId(), 0);
        expRow(secondRes.getId(), a1.getId(), 1);
        expRow(secondRes.getId(), a4.getId(), 0);
        expRow(secondRes.getId(), a5.getId(), 0);
        assertGenResTable();

        expTable("coral_attribute_string", stringAttrCols);
        expRow(0, "foo");
        expRow(1, "bar");
        assertExpTable();

        expTable("coral_attribute_integer", intAttrCols);
        assertExpTable();

        expTable("coral_attribute_resource", resourceAttrCols);
        assertExpTable();

        expTable("coral_attribute_boolean", booleanAttrCols);
        expRow(0, true);
        assertExpTable();

        expTable("ledge_parameters", parametersCols);
        expRow(0L, "", "");
        expRow(0L, "number", "11");
        expRow(0L, "ok", "true");
        assertExpTable();

        schema.deleteAttribute(firstClass, a1);

        expGenResTable();
        expRow(secondRes.getId(), a4.getId(), 0);
        expRow(secondRes.getId(), a5.getId(), 0);
        assertGenResTable();

        expTable("coral_attribute_string", stringAttrCols);
        assertExpTable();

        expTable("coral_attribute_integer", intAttrCols);
        assertExpTable();

        expTable("coral_attribute_resource", resourceAttrCols);
        assertExpTable();

        expTable("coral_attribute_boolean", booleanAttrCols);
        expRow(0, true);
        assertExpTable();

        expTable("ledge_parameters", parametersCols);
        expRow(0L, "", "");
        expRow(0L, "number", "11");
        expRow(0L, "ok", "true");
        assertExpTable();

        assertEquals(0, firstClass.getDeclaredAttributes().length);
        assertEquals(1, store.getResource("test1").length);
        assertEquals(1, store.getResource("test2").length);

        Comparator<Entity> byId = new Comparator<Entity>()
            {
                public int compare(Entity e1, Entity e2)
                {
                    return (int)(e1.getId() - e2.getId());
                }
            };
        final AttributeDefinition<?>[] ad1 = secondClass.getDeclaredAttributes();
        Arrays.sort(ad1, byId);
        final AttributeDefinition<?>[] ad2 = secondClass.getAllAttributes();
        Arrays.sort(ad2, byId);
        assertTrue(Arrays.equals(ad1, ad2));

        a6 = schema.createAttribute("a6", resourceListAttr, null, null, AttributeFlags.REQUIRED);
        schema.addAttribute(firstClass, a6, new ResourceList<Resource>(coralSessionFactory));

        firstRes.get(a6).add(firstRes);
        firstRes.setModified(a6);
        firstRes.update();
        secondRes.get(a6).add(secondRes);
        secondRes.setModified(a6);
        secondRes.update();

        expGenResTable();
        expRow(firstRes.getId(), a6.getId(), 0);
        expRow(secondRes.getId(), a4.getId(), 0);
        expRow(secondRes.getId(), a5.getId(), 0);
        expRow(secondRes.getId(), a6.getId(), 1);
        assertGenResTable();

        expTable("coral_attribute_string", stringAttrCols);
        assertExpTable();

        expTable("coral_attribute_integer", intAttrCols);
        assertExpTable();

        expTable("coral_attribute_resource", resourceAttrCols);
        assertExpTable();

        expTable("coral_attribute_boolean", booleanAttrCols);
        expRow(0, true);
        assertExpTable();

        expTable("ledge_parameters", parametersCols);
        expRow(0L, "", "");
        expRow(0L, "number", "11");
        expRow(0L, "ok", "true");
        assertExpTable();

        expTable("coral_attribute_resource_list", resourceListCols);
        expRow(0, 0, firstRes.getId());
        expRow(1, 0, secondRes.getId());
        assertExpTable();

        schema.deleteAttribute(firstClass, a6);

        expGenResTable();
        expRow(secondRes.getId(), a4.getId(), 0);
        expRow(secondRes.getId(), a5.getId(), 0);
        assertGenResTable();

        expTable("coral_attribute_string", stringAttrCols);
        assertExpTable();

        expTable("coral_attribute_integer", intAttrCols);
        assertExpTable();

        expTable("coral_attribute_resource", resourceAttrCols);
        assertExpTable();

        expTable("coral_attribute_boolean", booleanAttrCols);
        expRow(0, true);
        assertExpTable();

        expTable("ledge_parameters", parametersCols);
        expRow(0L, "", "");
        expRow(0L, "number", "11");
        expRow(0L, "ok", "true");
        assertExpTable();

        expTable("coral_attribute_resource_list", resourceListCols);
        assertExpTable();
    }

    public void testDeleteMultiLevelResourceClass()
        throws Exception
    {
        testCreateMultiLevelResources();

        a6 = schema.createAttribute("a6", resourceListAttr, null, null, AttributeFlags.REQUIRED);
        schema.addAttribute(firstClass, a6, new ResourceList<Resource>(coralSessionFactory));

        firstRes.get(a6).add(firstRes);
        firstRes.setModified(a6);
        firstRes.update();
        secondRes.get(a6).add(secondRes);
        secondRes.setModified(a6);
        secondRes.update();

        expGenResTable();
        expRow(firstRes.getId(), a1.getId(), 0);
        expRow(firstRes.getId(), a2.getId(), 0);
        expRow(firstRes.getId(), a6.getId(), 0);
        expRow(secondRes.getId(), a1.getId(), 1);
        expRow(secondRes.getId(), a2.getId(), 1);
        expRow(secondRes.getId(), a3.getId(), 0);
        expRow(secondRes.getId(), a4.getId(), 0);
        expRow(secondRes.getId(), a5.getId(), 0);
        expRow(secondRes.getId(), a6.getId(), 1);
        assertGenResTable();

        expTable("coral_attribute_string", stringAttrCols);
        expRow(0, "foo");
        expRow(1, "bar");
        assertExpTable();

        expTable("coral_attribute_integer", intAttrCols);
        expRow(0, 7);
        expRow(1, 9);
        assertExpTable();

        expTable("coral_attribute_resource", resourceAttrCols);
        expRow(0, firstRes.getId());
        assertExpTable();

        expTable("coral_attribute_boolean", booleanAttrCols);
        expRow(0, true);
        assertExpTable();

        expTable("ledge_parameters", parametersCols);
        expRow(0L, "", "");
        expRow(0L, "number", "11");
        expRow(0L, "ok", "true");
        assertExpTable();

        expTable("coral_attribute_resource_list", resourceListCols);
        expRow(0, 0, firstRes.getId());
        expRow(1, 0, secondRes.getId());
        assertExpTable();

        schema.deleteParentClass(secondClass, firstClass);

        expGenResTable();
        expRow(firstRes.getId(), a1.getId(), 0);
        expRow(firstRes.getId(), a2.getId(), 0);
        expRow(firstRes.getId(), a6.getId(), 0);
        expRow(secondRes.getId(), a4.getId(), 0);
        expRow(secondRes.getId(), a5.getId(), 0);
        assertGenResTable();

        expTable("coral_attribute_string", stringAttrCols);
        expRow(0, "foo");
        assertExpTable();

        expTable("coral_attribute_integer", intAttrCols);
        expRow(0, 7);
        assertExpTable();

        expTable("coral_attribute_resource", resourceAttrCols);
        assertExpTable();

        expTable("coral_attribute_boolean", booleanAttrCols);
        expRow(0, true);
        assertExpTable();

        expTable("ledge_parameters", parametersCols);
        expRow(0L, "", "");
        expRow(0L, "number", "11");
        expRow(0L, "ok", "true");
        assertExpTable();

        expTable("coral_attribute_resource_list", resourceListCols);
        expRow(0, 0, firstRes.getId());
        assertExpTable();
    }

    public void testCustomAttributeOps()
        throws Exception
    {
        firstClass = schema.createResourceClass("first", StandardResource.class.getName(),
            GenericResourceHandler.class.getName(), null, 0);
        a6 = schema.createAttribute("a6", resourceListAttr, null, null, 0);
        schema.addAttribute(firstClass, a6, null);
        firstRes = store.createResource("test1", rootRes, firstClass, attributes);

        expGenResTable();
        assertGenResTable();

        expTable("coral_attribute_resource_list", resourceListCols);
        assertExpTable();

        firstRes.set(a6, new ResourceList<Resource>(coralSessionFactory));
        firstRes.update();

        expGenResTable();
        expRow(firstRes.getId(), a6.getId(), 0);
        assertGenResTable();

        expTable("coral_attribute_resource_list", resourceListCols);
        assertExpTable();

        firstRes.get(a6).add(firstRes);
        firstRes.update();

        expGenResTable();
        expRow(firstRes.getId(), a6.getId(), 0);
        assertGenResTable();

        expTable("coral_attribute_resource_list", resourceListCols);
        expRow(0L, 0, firstRes.getIdObject());
        assertExpTable();

        firstRes.get(a6).add(firstRes);
        firstRes.get(a6).add(firstRes);
        firstRes.update();

        expGenResTable();
        expRow(firstRes.getId(), a6.getId(), 0);
        assertGenResTable();

        expTable("coral_attribute_resource_list", resourceListCols);
        expRow(0L, 0, firstRes.getIdObject());
        expRow(0L, 1, firstRes.getIdObject());
        expRow(0L, 2, firstRes.getIdObject());
        assertExpTable();

        firstRes.unset(a6);
        firstRes.update();

        expGenResTable();
        assertGenResTable();

        expTable("coral_attribute_resource_list", resourceListCols);
        assertExpTable();
    }

    public void testRetrieval()
        throws Exception
    {
        testCreateResource();

        Resource r = store.getResource(firstRes.getId());
        assertEquals(firstRes, r);

        coral.close();
        container.killContainer();

        FileSystem fs = FileSystem.getStandardFileSystem("src/test/resources");
        container = new LedgeContainer(fs, "/config", getClass().getClassLoader());
        coralSessionFactory = (CoralSessionFactory)container.getContainer().getComponentInstance(
            CoralSessionFactory.class);
        dataSource = (DataSource)container.getContainer().getComponentInstanceOfType(
            ThreadDataSource.class);
        log = Logger.getLogger(getClass());

        coral = coralSessionFactory.getRootSession();
        store = coral.getStore();

        r = store.getResource(firstRes.getId());
        assertEquals("foo", r.get(a1));
        assertEquals(Integer.valueOf(7), r.get(a2));
    }

    private void expGenResTable()
    {
        expTable("coral_generic_resource", genericResCols);
    }

    private void assertGenResTable()
        throws Exception
    {
        actual = databaseConnection.createQueryTable("coral_generic_resource",
            "SELECT * FROM coral_generic_resource ORDER BY resource_id, attribute_definition_id");
        try
        {
            assertEquals(expected, actual);
        }
        finally
        {
            databaseConnection.close();
        }
    }
}
