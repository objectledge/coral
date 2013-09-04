package org.objectledge.coral.touchstone.level1;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectledge.coral.datatypes.GenericResourceHandler;
import org.objectledge.coral.datatypes.PersistentResourceHandler;
import org.objectledge.coral.datatypes.StandardResource;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeHandler;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.touchstone.CoralTestCase;

public class FindByAttributeValueTest
    extends CoralTestCase
{
    private CoralSession coral;

    private CoralSchema schema;

    private CoralStore store;

    private AttributeClass<String> stringAttr;

    private AttributeHandler<String> stringHandler;

    private AttributeClass<Subject> subjectAttr;

    private AttributeHandler<Subject> subjectHandler;

    private AttributeDefinition<String> a1;

    private AttributeDefinition<Subject> a2;

    private AttributeDefinition<String> a3;

    private AttributeDefinition<Subject> a4;

    private Resource rootRes;

    private Subject rootSub;

    private Subject anonSub;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();
        coral = coralSessionFactory.getRootSession();
        schema = coral.getSchema();
        stringAttr = schema.getAttributeClass("string", String.class);
        stringHandler = stringAttr.getHandler();
        subjectAttr = schema.getAttributeClass("subject", Subject.class);
        subjectHandler = subjectAttr.getHandler();
        store = coral.getStore();
        rootRes = store.getResource(CoralStore.ROOT_RESOURCE);
        rootSub = coral.getSecurity().getSubject(Subject.ROOT);
        anonSub = coral.getSecurity().getSubject(Subject.ANONYMOUS);
        Map<AttributeDefinition<?>, Object> attributes = new HashMap<>();

        ResourceClass<?> rc1 = schema.createResourceClass("rc1", StandardResource.class.getName(),
            GenericResourceHandler.class.getName(), null, 0);
        a1 = schema.createAttribute("a1", stringAttr, null, null, 0);
        schema.addAttribute(rc1, a1, null);
        a2 = schema.createAttribute("a2", subjectAttr, null, null, 0);
        schema.addAttribute(rc1, a2, null);

        ResourceClass<?> rc2 = schema.createResourceClass("rc2", StandardResource.class.getName(),
            PersistentResourceHandler.class.getName(), "rc2", 0);
        a3 = schema.createAttribute("a3", stringAttr, null, null, 0);
        schema.addAttribute(rc2, a3, null);
        a4 = schema.createAttribute("a4", subjectAttr, null, null, 0);
        schema.addAttribute(rc2, a4, null);

        ResourceClass<?> rc3 = schema.createResourceClass("rc3", StandardResource.class.getName(),
            PersistentResourceHandler.class.getName(), "rc3", 0);
        schema.addParentClass(rc3, rc1, attributes);
        schema.addParentClass(rc3, rc2, attributes);

        attributes.put(a1, "a");
        attributes.put(a2, rootSub);
        store.createResource("r1.1", rootRes, rc1, attributes);
        attributes.clear();
        attributes.put(a1, "b");
        attributes.put(a2, anonSub);
        store.createResource("r1.2", rootRes, rc1, attributes);
        attributes.clear();
        attributes.put(a3, "a");
        attributes.put(a4, rootSub);
        store.createResource("r2.1", rootRes, rc2, attributes);
        attributes.clear();
        attributes.put(a3, "b");
        attributes.put(a4, anonSub);
        store.createResource("r2.2", rootRes, rc2, attributes);
        attributes.clear();
        attributes.put(a1, "a");
        attributes.put(a2, rootSub);
        attributes.put(a3, "a");
        attributes.put(a4, rootSub);
        store.createResource("r3.1", rootRes, rc3, attributes);
        attributes.clear();
        attributes.put(a1, "b");
        attributes.put(a2, anonSub);
        attributes.put(a3, "b");
        attributes.put(a4, anonSub);
        store.createResource("r3.2", rootRes, rc3, attributes);
    }

    public void testString1()
        throws Exception
    {
        Map<AttributeDefinition<String>, long[]> ids = stringHandler.getResourcesByValue("a");
        checkResources(ids.get(a1), "r1.1", "r3.1");
        checkResources(ids.get(a3), "r2.1", "r3.1");
        assertEquals(ids.size(), 2);
    }

    public void testString2()
        throws Exception
    {
        Map<AttributeDefinition<String>, long[]> ids = stringHandler.getResourcesByValue("b");
        checkResources(ids.get(a1), "r1.2", "r3.2");
        checkResources(ids.get(a3), "r2.2", "r3.2");
        assertEquals(ids.size(), 2);
    }

    public void testSubject1()
        throws Exception
    {
        Map<AttributeDefinition<Subject>, long[]> ids = subjectHandler.getResourcesByValue(rootSub);
        checkResources(ids.get(a2), "r1.1", "r3.1");
        checkResources(ids.get(a4), "r2.1", "r3.1");
        assertEquals(ids.size(), 2);
    }

    public void testSubject2()
        throws Exception
    {
        Map<AttributeDefinition<Subject>, long[]> ids = subjectHandler.getResourcesByValue(anonSub);
        checkResources(ids.get(a2), "r1.2", "r3.2");
        checkResources(ids.get(a4), "r2.2", "r3.2");
        assertEquals(ids.size(), 2);
    }

    private void checkResources(long[] ids, String... names)
        throws EntityDoesNotExistException
    {
        Set<String> s1 = new HashSet<>(Arrays.asList(names));
        Set<String> s2 = new HashSet<>();
        for(long id : ids)
        {
            s2.add(store.getResource(id).getName());
        }
        assertEquals(s1, s2);
    }
}
