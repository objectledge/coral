package org.objectledge.coral.touchstone.level1;

import java.util.HashMap;
import java.util.Map;

import org.dbunit.dataset.Column;
import org.dbunit.dataset.DefaultTable;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.datatype.DataType;
import org.objectledge.coral.datatypes.PersistentResource;
import org.objectledge.coral.datatypes.PersistentResourceHandler;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeFlags;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.touchstone.CoralTestCase;
import org.objectledge.database.DatabaseUtils;

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

    private AttributeClass<String> stringClass;

    private AttributeClass<Integer> intClass;

    private AttributeClass<Resource> resClass;

    private Resource root;

    private ResourceClass<?> testClass;

    private AttributeDefinition<String> astring;

    private AttributeDefinition<Integer> anint;

    private AttributeDefinition<Resource> aresource;

    private Column[] testTableCols = { new Column("RESOURCE_ID", DataType.BIGINT, Column.NO_NULLS),
                    new Column("ASTRING", DataType.VARCHAR, Column.NULLABLE),
                    new Column("ANINT", DataType.INTEGER, Column.NO_NULLS),
                    new Column("ARESOURCE", DataType.BIGINT, Column.NULLABLE) };

    private Resource testRes1;

    private Resource testRes2;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();
        coral = coralSessionFactory.getRootSession();
        schema = coral.getSchema();
        stringClass = (AttributeClass<String>)schema.getAttributeClass("string");
        intClass = (AttributeClass<Integer>)schema.getAttributeClass("integer");
        resClass = (AttributeClass<Resource>)schema.getAttributeClass("resource");
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
        testClass = schema.createResourceClass("test", PersistentResource.class.getName(),
            PersistentResourceHandler.class.getName(), "test", 0);
        astring = schema.createAttribute("astring", stringClass, null, 0);
        schema.addAttribute(testClass, astring, null);
        anint = schema.createAttribute("anint", intClass, null, AttributeFlags.REQUIRED);
        schema.addAttribute(testClass, anint, Integer.valueOf(0));
        aresource = schema.createAttribute("aresource", resClass, "test", 0);
        schema.addAttribute(testClass, aresource, null);

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
        attributes.put(astring, "foo");
        attributes.put(anint, 7);
        testRes1 = store.createResource("test1", root, testClass, attributes);
        attributes.put(astring, "bar");
        attributes.put(anint, 9);
        attributes.put(aresource, testRes1);
        testRes2 = store.createResource("test2", root, testClass, attributes);

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

        testRes1.set(astring, "baz");
        testRes1.update();
        testRes2.set(anint, 11);
        testRes2.unset(aresource);
        testRes2.update();

        DefaultTable expected = new DefaultTable("test", testTableCols);
        expected.addRow(new Object[] { testRes1.getIdObject(), "baz", Integer.valueOf(7), null });
        expected.addRow(new Object[] { testRes2.getIdObject(), "bar", Integer.valueOf(11), null });
        ITable actual = databaseConnection.createQueryTable("test", "SELECT * FROM test");
        databaseConnection.close();
        assertEquals(expected, actual);
    }

    public void testRevertResource()
        throws Exception
    {
        testCreateResource();

        testRes1.set(astring, "baz");
        testRes1.revert();
        testRes2.set(anint, 11);
        testRes2.unset(aresource);
        testRes2.revert();

        assertEquals("foo", testRes1.get(astring));
        assertEquals(Integer.valueOf(9), testRes2.get(anint));
        assertEquals(testRes1, testRes2.get(aresource));
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

        schema.deleteAttribute(testClass, aresource);
        Column[] trimmedCols = new Column[testTableCols.length - 1];
        System.arraycopy(testTableCols, 0, trimmedCols, 0, testTableCols.length - 1);
        DefaultTable expected = new DefaultTable("test", trimmedCols);
        expected.addRow(new Object[] { testRes1.getIdObject(), "foo", Integer.valueOf(7) });
        expected.addRow(new Object[] { testRes2.getIdObject(), "bar", Integer.valueOf(9) });
        ITable actual = databaseConnection.createQueryTable("test", "SELECT * FROM test");
        databaseConnection.close();
        assertEquals(expected, actual);

        schema.deleteAttribute(testClass, anint);
        Column[] trimmedCols2 = new Column[trimmedCols.length - 1];
        System.arraycopy(trimmedCols, 0, trimmedCols2, 0, trimmedCols.length - 1);
        expected = new DefaultTable("test", trimmedCols2);
        expected.addRow(new Object[] { testRes1.getIdObject(), "foo" });
        expected.addRow(new Object[] { testRes2.getIdObject(), "bar" });
        actual = databaseConnection.createQueryTable("test", "SELECT * FROM test");
        databaseConnection.close();
        assertEquals(expected, actual);

        schema.deleteAttribute(testClass, astring);
        assertFalse(DatabaseUtils.hasTable(dataSource, "test"));

        assertEquals(0, testClass.getDeclaredAttributes().length);
        assertEquals(1, store.getResource("test1").length);
    }

    public void testDeleteResourceClass()
        throws Exception
    {
        testCreateResource();

        store.deleteResource(testRes2);
        store.deleteResource(testRes1);
        schema.deleteResourceClass(testClass);
        assertFalse(DatabaseUtils.hasTable(dataSource, "test"));
    }
}
