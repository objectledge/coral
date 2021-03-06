package org.objectledge.coral.touchstone.level1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dbunit.dataset.datatype.DataType;
import org.objectledge.coral.datatypes.GenericResourceHandler;
import org.objectledge.coral.datatypes.PersistentResourceHandler;
import org.objectledge.coral.datatypes.StandardResource;
import org.objectledge.coral.query.CoralQuery;
import org.objectledge.coral.query.FilteredQueryResults;
import org.objectledge.coral.query.MalformedQueryException;
import org.objectledge.coral.query.QueryResults;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.schema.ResourceHandler;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.touchstone.CoralTestCase;

public class CoralQueryTests
    extends CoralTestCase
{
    private CoralSession coral;

    private CoralQuery query;

    private QueryResults results;

    private List<Resource[]> expected = new ArrayList<Resource[]>();

    private Resource first1;

    private Resource first2;

    private Resource second1;

    private Resource second2;

    private Resource third1;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();
        coral = coralSessionFactory.getRootSession();
        query = coral.getQuery();
    }

    @SuppressWarnings("rawtypes")
    private enum Implementation
    {
        // @formatter:off
        GENERIC(StandardResource.class, GenericResourceHandler.class), 
        TABULAR(StandardResource.class, PersistentResourceHandler.class);
        // @formatter:on

        private final Class<? extends Resource> resClass;

        private final Class<? extends ResourceHandler> handlerClass;

        private Implementation(Class<? extends Resource> resClass,
            Class<? extends ResourceHandler> handlerClass)
        {
            this.resClass = resClass;
            this.handlerClass = handlerClass;
        }

        public Class<? extends ResourceHandler> getHandlerClass()
        {
            return handlerClass;
        }

        public Class<? extends Resource> getResClass()
        {
            return resClass;
        }
    }

    protected void fixture(Implementation firstImpl, Implementation secondImpl,
        Implementation thirdImpl)
        throws Exception
    {
        CoralSchema schema = coral.getSchema();
        AttributeClass<String> stringAttr = schema.getAttributeClass("string", String.class);
        AttributeClass<Integer> intAttr = schema.getAttributeClass("integer", Integer.class);
        AttributeClass<Resource> resourceAttr = schema
            .getAttributeClass("resource", Resource.class);

        CoralStore store = coral.getStore();
        Resource rootRes = store.getResource(CoralStore.ROOT_RESOURCE);
        Map<AttributeDefinition<?>, Object> attributes = new HashMap<AttributeDefinition<?>, Object>();

        ResourceClass<?> firstClass;
        AttributeDefinition<String> a1;
        AttributeDefinition<Integer> a2;
        AttributeDefinition<Resource> a3;

        firstClass = schema.createResourceClass("first", firstImpl.getResClass().getName(),
            firstImpl.getHandlerClass().getName(), firstImpl == Implementation.TABULAR ? "first"
                : null, 0);
        a1 = schema.createAttribute("a1", stringAttr, "a1_", null, 0);
        schema.addAttribute(firstClass, a1, null);
        a2 = schema.createAttribute("a2", intAttr, null, null, 0);
        schema.addAttribute(firstClass, a2, 0);

        attributes.clear();
        attributes.put(a1, "foo");
        attributes.put(a2, 7);
        first1 = store.createResource("first1", rootRes, firstClass, attributes);

        attributes.clear();
        attributes.put(a1, "abab");
        attributes.put(a2, 9);
        first2 = store.createResource("first2", rootRes, firstClass, attributes);

        ResourceClass<?> secondClass = schema.createResourceClass("second", secondImpl
            .getResClass().getName(), secondImpl.getHandlerClass().getName(),
            secondImpl == Implementation.TABULAR ? "second" : null, 0);
        a1 = schema.createAttribute("a1", stringAttr, null, null, 0);
        schema.addAttribute(secondClass, a1, null);
        a2 = schema.createAttribute("a2", intAttr, null, null, 0);
        schema.addAttribute(secondClass, a2, 0);
        a3 = schema.createAttribute("a3", resourceAttr, null, "first", 0);
        schema.addAttribute(secondClass, a3, null);
        AttributeDefinition<String> a4 = schema.createAttribute("a4", stringAttr, null, null, 0);
        schema.addAttribute(secondClass, a4, null);

        attributes.clear();
        attributes.put(a1, "foo");
        attributes.put(a2, 7);
        attributes.put(a3, first1);
        attributes.put(a4, "f%");
        second1 = store.createResource("second1", rootRes, secondClass, attributes);

        attributes.clear();
        attributes.put(a1, "bam");
        attributes.put(a2, 7);
        second2 = store.createResource("second2", rootRes, secondClass, attributes);

        ResourceClass<?> thirdClass = schema.createResourceClass("third", thirdImpl.getResClass()
            .getName(), thirdImpl.getHandlerClass().getName(),
            thirdImpl == Implementation.TABULAR ? "third" : null, 0);
        a3 = schema.createAttribute("a3", resourceAttr, null, "first", 0);
        schema.addAttribute(thirdClass, a3, null);
        attributes.clear();
        schema.addParentClass(thirdClass, firstClass, attributes);
        a1 = firstClass.getAttribute("a1", String.class);
        a2 = firstClass.getAttribute("a2", Integer.class);

        attributes.put(a1, "quux");
        attributes.put(a2, 11);
        attributes.put(a3, first1);
        third1 = store.createResource("third1", rootRes, thirdClass, attributes);

        validateFixture(firstImpl, secondImpl, thirdImpl);
    }

    private void validateFixture(Implementation firstImpl, Implementation secondImpl,
        Implementation thirdImpl)
        throws Exception
    {
        expAttTable("string", "data", DataType.VARCHAR);
        if(firstImpl == Implementation.GENERIC)
        {
            expRow(first1.getId(), "a1", "foo");
            expRow(first2.getId(), "a1", "abab");
        }
        if(secondImpl == Implementation.GENERIC)
        {
            expRow(second1.getId(), "a1", "foo");
            expRow(second1.getId(), "a4", "f%");
            expRow(second2.getId(), "a1", "bam");
        }
        if(firstImpl == Implementation.GENERIC)
        {
            expRow(third1.getId(), "a1", "quux");
        }
        assertExpTable();

        expAttTable("integer", "data", DataType.INTEGER);
        if(firstImpl == Implementation.GENERIC)
        {
            expRow(first1.getId(), "a2", 7);
            expRow(first2.getId(), "a2", 9);
        }
        if(secondImpl == Implementation.GENERIC)
        {
            expRow(second1.getId(), "a2", 7);
            expRow(second2.getId(), "a2", 7);
        }
        if(firstImpl == Implementation.GENERIC)
        {
            expRow(third1.getId(), "a2", 11);
        }
        assertExpTable();

        expAttTable("resource", "ref", DataType.BIGINT);
        if(secondImpl == Implementation.GENERIC)
        {
            expRow(second1.getId(), "a3", first1.getId());
        }
        if(thirdImpl == Implementation.GENERIC)
        {
            expRow(third1.getId(), "a3", first1.getId());
        }
        assertExpTable();

        if(firstImpl == Implementation.TABULAR)
        {
            expTable("first", nnCol("resource_id", DataType.BIGINT), col("a1_", DataType.VARCHAR),
                col("a2", DataType.INTEGER));
            expRow(first1.getId(), "foo", 7);
            expRow(first2.getId(), "abab", 9);
            expRow(third1.getId(), "quux", 11);
            assertExpTable();
        }

        if(secondImpl == Implementation.TABULAR)
        {
            expTable("second", nnCol("resource_id", DataType.BIGINT), col("a1", DataType.VARCHAR),
                col("a2", DataType.INTEGER), col("a3", DataType.BIGINT),
                col("a4", DataType.VARCHAR));
            expRow(second1.getId(), "foo", 7, first1.getId(), "f%");
            expRow(second2.getId(), "bam", 7, null, null);
            assertExpTable();
        }

        if(thirdImpl == Implementation.TABULAR)
        {
            expTable("third", nnCol("resource_id", DataType.BIGINT), col("a3", DataType.BIGINT));
            expRow(third1.getId(), first1.getId());
            assertExpTable();
        }
    }

    @Override
    public void tearDown()
        throws Exception
    {
        coral.close();
        super.tearDown();
    }

    private void queryBuiltin()
        throws Exception
    {
        run("FIND RESOURCE WHERE name = 'first1'");
        expectRow(first1);
        assertExpectedResults();
    }

    private void queryByStringEquals()
        throws Exception
    {
        run("FIND RESOURCE FROM first WHERE a1 = 'foo'");
        expectRow(first1);
        assertExpectedResults();

        run("FIND RESOURCE FROM first WHERE a1 = 'bar'");
        assertExpectedResults();
    }

    private void queryByStringNotEquals()
        throws Exception
    {
        run("FIND RESOURCE FROM first WHERE a1 != 'foo' ORDER BY id");
        expectRow(first2);
        expectRow(third1);
        assertExpectedResults();

        run("FIND RESOURCE FROM first WHERE a1 != 'bar' ORDER BY id");
        expectRow(first1);
        expectRow(first2);
        expectRow(third1);
        assertExpectedResults();
    }

    private void queryByStringLike()
        throws Exception
    {
        run("FIND RESOURCE FROM first WHERE a1 LIKE 'f%'");
        expectRow(first1);
        assertExpectedResults();

        run("FIND RESOURCE FROM first WHERE NOT a1 LIKE 'f%' ORDER BY id");
        expectRow(first2);
        expectRow(third1);
        assertExpectedResults();

        run("FIND RESOURCE FROM first WHERE NOT a1 LIKE 'ba%' ORDER BY id");
        expectRow(first1);
        expectRow(first2);
        expectRow(third1);
        assertExpectedResults();

        run("FIND RESOURCE FROM second WHERE a1 LIKE a4");
        expectRow(second1);
        assertExpectedResults();

        run("FIND RESOURCE FROM second WHERE a1 LIKE_NC a4");
        expectRow(second1);
        assertExpectedResults();

    }

    private void queryByStringLikeNC()
        throws Exception
    {
        run("FIND RESOURCE FROM first WHERE a1 LIKE_NC 'F%'");
        expectRow(first1);
        assertExpectedResults();

        run("FIND RESOURCE FROM first WHERE NOT a1 LIKE_NC 'F%' ORDER BY id");
        expectRow(first2);
        expectRow(third1);
        assertExpectedResults();

        run("FIND RESOURCE FROM first WHERE NOT a1 LIKE_NC 'Ba%' ORDER BY id");
        expectRow(first1);
        expectRow(first2);
        expectRow(third1);
        assertExpectedResults();
    }

    private void queryByStringCompare()
        throws Exception
    {
        run("FIND RESOURCE FROM first WHERE a1 > 'e' ORDER BY id");
        expectRow(first1);
        expectRow(third1);
        assertExpectedResults();

        run("FIND RESOURCE FROM first WHERE a1 <= 'e'");
        expectRow(first2);
        assertExpectedResults();

        run("FIND RESOURCE FROM first WHERE a1 <= 'g' ORDER BY id");
        expectRow(first1);
        expectRow(first2);
        assertExpectedResults();

        run("FIND RESOURCE FROM first WHERE a1 > 'g'");
        expectRow(third1);
        assertExpectedResults();

        run("FIND RESOURCE FROM second WHERE a1 > a4");
        expectRow(second1);
        assertExpectedResults();
    }

    private void queryByNonNull()
        throws Exception
    {
        run("FIND RESOURCE FROM second WHERE DEFINED a4");
        expectRow(second1);
        assertExpectedResults();

        run("FIND RESOURCE FROM second WHERE NOT DEFINED a4");
        expectRow(second2);
        assertExpectedResults();

    }

    private void queryByConjunction()
        throws Exception
    {
        run("FIND RESOURCE FROM first WHERE a1 = 'foo' AND a2 = 7");
        expectRow(first1);
        assertExpectedResults();

        run("FIND RESOURCE FROM first WHERE a1 = 'foo' AND a2 = 9");
        assertExpectedResults();

        run("FIND RESOURCE FROM first WHERE NOT (a1 = 'foo' AND a2 = 9) ORDER BY id");
        expectRow(first1);
        expectRow(first2);
        expectRow(third1);
        assertExpectedResults();
    }

    private void queryByAlternative()
        throws Exception
    {
        run("FIND RESOURCE FROM first WHERE a1 = 'foo' OR a2 = 9 ORDER BY id");
        expectRow(first1);
        expectRow(first2);
        assertExpectedResults();

        run("FIND RESOURCE FROM first WHERE NOT a1 = 'foo' OR a2 != 7 ORDER BY id");
        expectRow(first2);
        expectRow(third1);
        assertExpectedResults();
    }

    private void queryByCompoundBoolean()
        throws Exception
    {
        run("FIND RESOURCE FROM first WHERE a1 > 'a' AND (a2 = 7 OR a2 = 9) ORDER BY id");
        expectRow(first1);
        expectRow(first2);
        assertExpectedResults();
    }

    private void queryUsingInnerJoin()
        throws Exception
    {
        run("FIND RESOURCE FROM first AS f, second AS s WHERE f.a1 = s.a1 AND f.a2 = 7");
        expectRow(first1, second1);
        assertExpectedResults();

        run("FIND RESOURCE FROM first AS f, second AS s WHERE f.a1 = s.a1 AND f.a2 != 7");
        assertExpectedResults();
    }

    private void queryUsingCartesianJoin()
        throws Exception
    {
        run("FIND RESOURCE FROM first AS f, second AS s ORDER BY f.id, s.id");
        expectRow(first1, second1);
        expectRow(first1, second2);
        expectRow(first2, second1);
        expectRow(first2, second2);
        expectRow(third1, second1);
        expectRow(third1, second2);
        assertExpectedResults();
    }

    private void queryUsingOrderClause()
        throws Exception
    {
        run("FIND RESOURCE FROM first ORDER BY a1");
        expectRow(first2);
        expectRow(first1);
        expectRow(third1);
        assertExpectedResults();

        run("FIND RESOURCE FROM first ORDER BY a1 ASC");
        expectRow(first2);
        expectRow(first1);
        expectRow(third1);
        assertExpectedResults();

        run("FIND RESOURCE FROM first ORDER BY a1 DESC");
        expectRow(third1);
        expectRow(first1);
        expectRow(first2);
        assertExpectedResults();
    }

    private void queryUsingLimitAndOffsetClauses()
        throws Exception
    {
        run("FIND RESOURCE FROM first ORDER BY a1");
        expectRow(first2);
        expectRow(first1);
        expectRow(third1);
        assertExpectedResults();

        run("FIND RESOURCE FROM first ORDER BY a1 LIMIT 2");
        expectRow(first2);
        expectRow(first1);
        assertExpectedResults();

        run("FIND RESOURCE FROM first ORDER BY a1 LIMIT 2 OFFSET 1");
        expectRow(first1);
        expectRow(third1);
        assertExpectedResults();
    }

    private void queryUsingBuiltinAttribute()
        throws Exception
    {
        run("FIND RESOURCE FROM first WHERE name = 'first1'");
        expectRow(first1);
        assertExpectedResults();

        run("FIND RESOURCE FROM first AS f, second AS s WHERE f.name = s.name");
        assertExpectedResults();
    }

    private void queryUsingReferenceAttribute()
        throws Exception
    {
        run("FIND RESOURCE FROM first AS f, second AS s WHERE s.a3 = f");
        expectRow(first1, second1);
        assertExpectedResults();

        run("FIND RESOURCE FROM first AS f, second AS s WHERE s.a3 != f ORDER BY f.id, s.id");
        expectRow(first2, second1);
        expectRow(third1, second1);
        assertExpectedResults();
    }

    private void queryAcrossHierarchy()
        throws Exception
    {
        run("FIND RESOURCE FROM first AS f, third AS t WHERE t.a1 = 'quux' AND t.a3 = f");
        expectRow(first1, third1);
        assertExpectedResults();
    }

    private void malformed()
    {
        expectMalformed("SELECT f,s FROM first AS f, second AS s WHERE s.a3 = f");
        expectMalformed("FIND REOSRUCE FROM first");
        expectMalformed("FIND RESOURCE FROM nonexistent");
        expectMalformed("FIND RESOURCE FROM first WHERE 7 = a2");
        expectMalformed("FIND RESOURCE FROM first AS f, second AS s WHERE a3 != f");
        expectMalformed("FIND RESOURCE FROM first AS f, second AS s WHERE s.a9 = f");
        expectMalformed("FIND RESOURCE FROM first ORDER BY path");
        expectMalformed("FIND RESOURCE FROM first WHERE path = '/first1'");
        expectMalformed("FIND RESOURCE FROM first WHERE a2 = 'baa'");
        expectMalformed("FIND RESOURCE FROM first WHERE a1 > a2");
        expectMalformed("FIND RESOURCE FROM second WHERE a1 = a3");
        expectMalformed("FIND RESOURCE FROM second WHERE a3 LIKE 'first1'");
        expectMalformed("FIND RESOURCE FROM second WHERE a3 > 1");
        expectMalformed("FIND RESOURCE FROM second WHERE path LIKE 'first1'");
        expectMalformed("FIND RESOURCE FROM second WHERE path > 1");
    }

    private void filtered()
        throws Exception
    {
        run("FIND RESOURCE FROM first WHERE a2 = 7 SELECT a1, a2");
        FilteredQueryResults fqr = results.getFiltered();
        Iterator<FilteredQueryResults.Row> ri = fqr.iterator();
        if(!ri.hasNext())
        {
            fail("too few rows");
        }
        FilteredQueryResults.Row r = ri.next();
        assertEquals("foo", r.get(1));
        assertEquals(7, r.get(2));
        if(ri.hasNext())
        {
            fail("too many rows");
        }
    }

    private void runTests()
        throws Exception
    {
        malformed();

        queryBuiltin();

        queryByStringEquals();
        queryByStringNotEquals();
        queryByStringLike();
        queryByStringLikeNC();
        queryByStringCompare();
        queryByNonNull();
        queryByConjunction();
        queryByAlternative();
        queryByCompoundBoolean();

        queryUsingInnerJoin();
        queryUsingCartesianJoin();
        queryAcrossHierarchy();

        queryUsingOrderClause();
        queryUsingLimitAndOffsetClauses();

        queryUsingBuiltinAttribute();
        queryUsingReferenceAttribute();

        filtered();
    }

    public void testGeneric()
        throws Exception
    {
        fixture(Implementation.GENERIC, Implementation.GENERIC, Implementation.GENERIC);
        runTests();
    }

    public void testTabular()
        throws Exception
    {
        fixture(Implementation.TABULAR, Implementation.TABULAR, Implementation.TABULAR);
        runTests();
    }

    public void testMixed1()
        throws Exception
    {
        fixture(Implementation.GENERIC, Implementation.TABULAR, Implementation.GENERIC);
        runTests();
    }

    public void testMixed2()
        throws Exception
    {
        fixture(Implementation.TABULAR, Implementation.GENERIC, Implementation.TABULAR);
        runTests();
    }

    public void testMixed3()
        throws Exception
    {
        fixture(Implementation.TABULAR, Implementation.TABULAR, Implementation.GENERIC);
        runTests();
    }

    public void testMixed4()
        throws Exception
    {
        fixture(Implementation.GENERIC, Implementation.TABULAR, Implementation.TABULAR);
        runTests();
    }

    public void testMixed5()
        throws Exception
    {
        fixture(Implementation.TABULAR, Implementation.GENERIC, Implementation.GENERIC);
        runTests();
    }

    public void testMixed6()
        throws Exception
    {
        fixture(Implementation.GENERIC, Implementation.GENERIC, Implementation.TABULAR);
        runTests();
    }

    private void run(String queryText)
        throws Exception
    {
        results = query.executeQuery(queryText);
        expected.clear();
    }

    private void expectRow(Resource... resources)
    {
        expected.add(resources);
    }

    private void assertExpectedResults()
    {
        Iterator<Resource[]> ei = expected.iterator();
        Iterator<QueryResults.Row> ai = results.iterator();
        int rowNum = 1;
        while(ei.hasNext() && ai.hasNext())
        {
            Resource[] er = ei.next();
            Resource[] ar = ai.next().getArray();
            assertTrue("row " + (rowNum++), Arrays.equals(er, ar));
        }
        if(ei.hasNext())
        {
            fail("too few rows");
        }
        if(ai.hasNext())
        {
            fail("too many rows");
        }
    }

    private void expectMalformed(String queryText)
    {
        try
        {
            query.executeQuery(queryText);
            fail("expected MalformedQueryException");
        }
        catch(MalformedQueryException e)
        {
            // OK
        }
    }
}
