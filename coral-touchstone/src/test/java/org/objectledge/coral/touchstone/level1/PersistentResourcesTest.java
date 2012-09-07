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
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.touchstone.CoralTestCase;
import org.objectledge.database.DatabaseUtils;
import org.objectledge.database.IdGenerator;
import org.objectledge.database.ThreadDataSource;
import org.objectledge.filesystem.FileSystem;
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

    private Map<AttributeDefinition<?>, Object> attributes = new HashMap<AttributeDefinition<?>, Object>();

    private AttributeClass<String> stringAttr;

    private AttributeClass<Integer> intAttr;

    private AttributeClass<Resource> resourceAttr;

    private AttributeClass<Boolean> booleanAttr;

    private AttributeClass<Parameters> parametersAttr;

    private AttributeClass<ResourceList<Resource>> resourceListAttr;

    private AttributeClass<Subject> subjectAttr;

    private ResourceClass<?> firstClass;

    private ResourceClass<?> seconClass;

    private ResourceClass<?> thirdClass;

    private AttributeDefinition<String> a1;

    private AttributeDefinition<Integer> a2;

    private AttributeDefinition<Resource> a3;

    private AttributeDefinition<Boolean> a4;

    private AttributeDefinition<Parameters> a5;

    private AttributeDefinition<ResourceList<Resource>> a6;

    private AttributeDefinition<Subject> a7;

    private Column[] firstCols = { nnCol("RESOURCE_ID", DataType.BIGINT),
                    col("SELECT_", DataType.VARCHAR), nnCol("A2", DataType.INTEGER),
                    col("A3", DataType.BIGINT) };

    private Column[] secondCols = { nnCol("RESOURCE_ID", DataType.BIGINT),
                    col("A4", DataType.BOOLEAN), nnCol("A5", DataType.BIGINT) };

    private Column[] thirdCols = { nnCol("RESOURCE_ID", DataType.BIGINT),
                    nnCol("A7", DataType.BIGINT) };

    private Column[] parametersCols = { nnCol("PARAMETERS_ID", DataType.BIGINT),
                    nnCol("NAME", DataType.VARCHAR), nnCol("VALUE", DataType.VARCHAR) };

    private Column[] resourceListCols = { nnCol("DATA_KEY", DataType.BIGINT),
                    nnCol("POS", DataType.INTEGER), nnCol("REF", DataType.BIGINT) };

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
        stringAttr = (AttributeClass<String>)schema.getAttributeClass("string");
        intAttr = (AttributeClass<Integer>)schema.getAttributeClass("integer");
        resourceAttr = (AttributeClass<Resource>)schema.getAttributeClass("resource");
        booleanAttr = (AttributeClass<Boolean>)schema.getAttributeClass("boolean");
        parametersAttr = (AttributeClass<Parameters>)schema.getAttributeClass("parameters");
        resourceListAttr = (AttributeClass<ResourceList<Resource>>)schema
            .getAttributeClass("resource_list");
        subjectAttr = (AttributeClass<Subject>)schema.getAttributeClass("subject");
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
        firstClass = schema.createResourceClass("first", PersistentResource.class.getName(),
            PersistentResourceHandler.class.getName(), "first", 0);
        a1 = schema.createAttribute("select", stringAttr, null, 0);
        schema.addAttribute(firstClass, a1, null);
        a2 = schema.createAttribute("a2", intAttr, null, AttributeFlags.REQUIRED);
        schema.addAttribute(firstClass, a2, 0);
        a3 = schema.createAttribute("a3", resourceAttr, "first", 0);
        schema.addAttribute(firstClass, a3, null);

        expTable("first", firstCols);
        assertExpTable();
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

        expTable("first", firstCols);
        expRow(firstRes.getIdObject(), "foo", 7, null);
        expRow(secondRes.getIdObject(), "bar", 9, firstRes.getIdObject());
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

        expTable("first", firstCols);
        expRow(firstRes.getIdObject(), "baz", 7, null);
        expRow(secondRes.getIdObject(), "bar", 11, null);
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

        expTable("first", firstCols);
        assertExpTable();
    }

    public void testDeleteAttributes()
        throws Exception
    {
        testCreateResource();

        schema.deleteAttribute(firstClass, a3);

        expTable("first", firstCols[0], firstCols[1], firstCols[2]);
        expRow(firstRes.getIdObject(), "foo", 7);
        expRow(secondRes.getIdObject(), "bar", 9);
        assertExpTable();

        schema.deleteAttribute(firstClass, a2);

        expTable("first", firstCols[0], firstCols[1]);
        expRow(firstRes.getIdObject(), "foo");
        expRow(secondRes.getIdObject(), "bar");
        assertExpTable();

        schema.deleteAttribute(firstClass, a1);
        assertFalse(DatabaseUtils.hasTable(dataSource, "first"));

        assertEquals(0, firstClass.getDeclaredAttributes().length);
        assertEquals(1, store.getResource("test1").length);
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

        seconClass = schema.createResourceClass("second",
            PersistentResource.class.getName(), PersistentResourceHandler.class.getName(),
            "second", 0);
        attributes.put(a2, 0);
        a4 = schema.createAttribute("a4", booleanAttr, null, 0);
        schema.addAttribute(seconClass, a4, null);
        a5 = schema.createAttribute("a5", parametersAttr, null, AttributeFlags.REQUIRED);
        schema.addAttribute(seconClass, a5, new DefaultParameters());

        expTable("second", secondCols);
        assertExpTable();

        thirdClass = schema.createResourceClass("third",
            PersistentResource.class.getName(), PersistentResourceHandler.class.getName(), "third",
            0);
        attributes.put(a5, new DefaultParameters());
        a7 = schema.createAttribute("a7", subjectAttr, null, 0);
        schema.addAttribute(thirdClass, a7, null);

        expTable("third", thirdCols);
        assertExpTable();

        schema.addParentClass(thirdClass, seconClass, attributes);
        schema.addParentClass(seconClass, firstClass, attributes);
    }

    public void testCreateMultiLevelClasses2()
        throws Exception
    {
        firstClass = schema.createResourceClass("first", PersistentResource.class.getName(),
            PersistentResourceHandler.class.getName(), "first", 0);
        a1 = schema.createAttribute("select", stringAttr, null, 0);
        schema.addAttribute(firstClass, a1, null);
        a3 = schema.createAttribute("a3", resourceAttr, "first", 0);
        schema.addAttribute(firstClass, a3, null);

        seconClass = schema.createResourceClass("second",
            PersistentResource.class.getName(), PersistentResourceHandler.class.getName(),
            "second", 0);
        schema.addParentClass(seconClass, firstClass, attributes);
        a4 = schema.createAttribute("a4", booleanAttr, null, 0);
        schema.addAttribute(seconClass, a4, null);
        a5 = schema.createAttribute("a5", parametersAttr, null, AttributeFlags.REQUIRED);
        schema.addAttribute(seconClass, a5, new DefaultParameters());

        attributes.put(a5, new DefaultParameters());
        secondRes = store.createResource("test2", rootRes, seconClass, attributes);

        a2 = schema.createAttribute("a2", intAttr, null, AttributeFlags.REQUIRED);
        schema.addAttribute(firstClass, a2, 0);

        expTable("second", secondCols);
        expRow(secondRes.getIdObject(), null, 0L);
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
        secondRes = store.createResource("test2", rootRes, seconClass, attributes);

        expTable("first", firstCols);
        expRow(firstRes.getIdObject(), "foo", 7, null);
        expRow(secondRes.getIdObject(), "bar", 9, firstRes.getIdObject());
        assertExpTable();

        expTable("second", secondCols);
        expRow(secondRes.getIdObject(), true, 0L);
        assertExpTable();

        expTable("ledge_parameters", parametersCols);
        expRow(0L, "", "");
        expRow(0L, "ok", "true");
        expRow(0L, "number", "11");
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

        expTable("first", firstCols);
        expRow(firstRes.getIdObject(), "foo_1", 7, secondRes.getIdObject());
        expRow(secondRes.getIdObject(), "bar_1", 9, null);
        assertExpTable();

        expTable("second", secondCols);
        expRow(secondRes.getIdObject(), false, 0L);
        assertExpTable();

        expTable("ledge_parameters", parametersCols);
        expRow(0L, "", "");
        expRow(0L, "ok", "true");
        expRow(0L, "number", "22");
        assertExpTable();
    }

    public void testDeleteMultiLevelResources()
        throws Exception
    {
        testCreateMultiLevelResources();

        store.deleteResource(secondRes);
        store.deleteResource(firstRes);

        expTable("first", firstCols);
        assertExpTable();

        expTable("second", secondCols);
        assertExpTable();

        expTable("ledge_parameters", parametersCols);
        assertExpTable();
    }

    public void testCreateMultiLevelResourceClassWithRequiredAttributes()
        throws Exception
    {
        testCreateClass();

        seconClass = schema.createResourceClass("second",
            PersistentResource.class.getName(), PersistentResourceHandler.class.getName(),
            "second", 0);
        a4 = schema.createAttribute("a4", booleanAttr, null, 0);
        schema.addAttribute(seconClass, a4, null);
        a5 = schema.createAttribute("a5", parametersAttr, null, AttributeFlags.REQUIRED);
        schema.addAttribute(seconClass, a5, null);

        attributes.put(a5, new DefaultParameters());
        secondRes = store.createResource("test2", rootRes, seconClass, attributes);

        attributes.clear();
        attributes.put(a2, 9);

        schema.addParentClass(seconClass, firstClass, attributes);

        expTable("first", firstCols);
        expRow(secondRes.getIdObject(), null, 9, null);
        assertExpTable();

        expTable("second", secondCols);
        expRow(secondRes.getIdObject(), null, 0L);
        assertExpTable();

        expTable("ledge_parameters", parametersCols);
        expRow(0L, "", "");
        assertExpTable();

        assertTrue(Arrays.asList(seconClass.getParentClasses())
            .contains(firstClass));
        assertTrue(Arrays.asList(seconClass.getAllAttributes()).contains(a2));
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

        expTable("first", firstCols[0], firstCols[1], firstCols[2]);
        expRow(firstRes.getIdObject(), "foo", 7);
        expRow(secondRes.getIdObject(), "bar", 9);
        assertExpTable();

        schema.deleteAttribute(firstClass, a2);

        expTable("first", firstCols[0], firstCols[1]);
        expRow(firstRes.getIdObject(), "foo");
        expRow(secondRes.getIdObject(), "bar");
        assertExpTable();

        schema.deleteAttribute(firstClass, a1);

        assertFalse(DatabaseUtils.hasTable(dataSource, "first"));

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
        final AttributeDefinition<?>[] ad1 = seconClass.getDeclaredAttributes();
        Arrays.sort(ad1, byId);
        final AttributeDefinition<?>[] ad2 = seconClass.getAllAttributes();
        Arrays.sort(ad2, byId);
        assertTrue(Arrays.equals(ad1, ad2));

        a6 = schema.createAttribute("a6", resourceListAttr, null, AttributeFlags.REQUIRED);
        schema.addAttribute(firstClass, a6, new ResourceList<Resource>(coralSessionFactory));

        firstRes.get(a6).add(firstRes);
        firstRes.setModified(a6);
        firstRes.update();
        secondRes.get(a6).add(secondRes);
        secondRes.setModified(a6);
        secondRes.update();

        expTable("coral_attribute_resource_list", resourceListCols);
        expRow(0, 0, firstRes.getId());
        expRow(1, 0, secondRes.getId());
        assertExpTable();

        schema.deleteAttribute(firstClass, a6);

        expTable("coral_attribute_resource_list", resourceListCols);
        assertExpTable();
    }

    public void testDeleteMultiLevelResourceClass()
        throws Exception
    {
        testCreateMultiLevelResources();

        a6 = schema.createAttribute("a6", resourceListAttr, null, AttributeFlags.REQUIRED);
        schema.addAttribute(firstClass, a6, new ResourceList<Resource>(coralSessionFactory));

        firstRes.get(a6).add(firstRes);
        firstRes.setModified(a6);
        firstRes.update();
        secondRes.get(a6).add(secondRes);
        secondRes.setModified(a6);
        secondRes.update();

        expTable("coral_attribute_resource_list", resourceListCols);
        expRow(0, 0, firstRes.getId());
        expRow(1, 0, secondRes.getId());
        assertExpTable();

        schema.deleteParentClass(seconClass, firstClass);

        expTable("first", firstCols[0], firstCols[1], firstCols[2], firstCols[3],
            nnCol("A6", DataType.BIGINT));
        expRow(firstRes.getIdObject(), "foo", 7, null, 0);
        assertExpTable();

        expTable("coral_attribute_resource_list", resourceListCols);
        expRow(0, 0, firstRes.getId());
        assertExpTable();
    }

    public void testCustomAttributeOps()
        throws Exception
    {
        firstClass = schema.createResourceClass("first", PersistentResource.class.getName(),
            PersistentResourceHandler.class.getName(), "first", 0);
        a6 = schema.createAttribute("a6", resourceListAttr, null, 0);
        schema.addAttribute(firstClass, a6, null);
        firstRes = store.createResource("test1", rootRes, firstClass, attributes);

        expTable("first", nnCol("RESOURCE_ID", DataType.BIGINT), col("A6", DataType.BIGINT));
        expRow(firstRes.getIdObject(), null);
        assertExpTable();

        expTable("coral_attribute_resource_list", resourceListCols);
        assertExpTable();

        databaseConnection.close();

        firstRes.set(a6, new ResourceList<Resource>(coralSessionFactory));
        firstRes.update();

        expTable("first", nnCol("RESOURCE_ID", DataType.BIGINT), col("A6", DataType.BIGINT));
        expRow(firstRes.getIdObject(), 0);
        assertExpTable();

        expTable("coral_attribute_resource_list", resourceListCols);
        assertExpTable();

        firstRes.get(a6).add(firstRes);
        firstRes.update();

        expTable("coral_attribute_resource_list", resourceListCols);
        expRow(0L, 0, firstRes.getIdObject());
        assertExpTable();

        firstRes.get(a6).add(firstRes);
        firstRes.get(a6).add(firstRes);
        firstRes.update();

        expTable("coral_attribute_resource_list", resourceListCols);
        expRow(0L, 0, firstRes.getIdObject());
        expRow(0L, 1, firstRes.getIdObject());
        expRow(0L, 2, firstRes.getIdObject());
        assertExpTable();

        firstRes.unset(a6);
        firstRes.update();

        expTable("first", nnCol("RESOURCE_ID", DataType.BIGINT), col("A6", DataType.BIGINT));
        expRow(firstRes.getIdObject(), null);
        assertExpTable();

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

        IdGenerator idGenerator = (IdGenerator)container.getContainer().getComponentInstanceOfType(
            IdGenerator.class);
        idGenerator.getNextId("global_transaction_hack");

        coral = coralSessionFactory.getRootSession();
        store = coral.getStore();

        r = store.getResource(firstRes.getId());
        assertEquals("foo", r.get(a1));
        assertEquals(Integer.valueOf(7), r.get(a2));
    }
}
