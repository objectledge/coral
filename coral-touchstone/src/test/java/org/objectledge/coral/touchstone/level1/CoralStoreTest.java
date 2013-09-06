package org.objectledge.coral.touchstone.level1;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectledge.coral.datatypes.GenericResourceHandler;
import org.objectledge.coral.datatypes.Node;
import org.objectledge.coral.datatypes.StandardResource;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.InvalidResourceNameException;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ValueRequiredException;
import org.objectledge.coral.touchstone.CoralTestCase;

public class CoralStoreTest
    extends CoralTestCase
{
    public void testGetAllResources()
        throws Exception
    {
        CoralSession session = coralSessionFactory.getAnonymousSession();
        try
        {
            Resource[] all = session.getStore().getResource();
            assertTrue(all.length >= 1);
        }
        finally
        {
            session.close();
        }
    }

    public void testGetResourceById()
        throws Exception
    {
        CoralSession session = coralSessionFactory.getAnonymousSession();
        try
        {
            Resource root = session.getStore().getResource(1l);
            assertEquals("/", root.getPath());
        }
        finally
        {
            session.close();
        }
    }

    private void ensureUniqueTest1ResourceExists(CoralSession session)
        throws EntityDoesNotExistException, InvalidResourceNameException, ValueRequiredException
    {
        Resource[] r = session.getStore().getResource("test1");
        if(r.length == 0)
        {
            Resource root = session.getStore().getResource(1l);
            ResourceClass<Node> rc = session.getSchema().getResourceClass("coral.Node", Node.class);
            session.getStore().createResource("test1", root, rc,
                Collections.<AttributeDefinition<?>, Object> emptyMap());
            assertEquals(1, session.getStore().getResource("test1").length);
        }
    }

    public void testGetResourceByName()
        throws Exception
    {
        CoralSession session = coralSessionFactory.getAnonymousSession();
        try
        {
            ensureUniqueTest1ResourceExists(session);

            Resource[] r = session.getStore().getResource("test1");
            assertTrue(r.length >= 1);
            assertTrue(r[0].getId() > 1l);
            assertEquals(1l, r[0].getParent().getId());
            assertEquals("coral.Node", r[0].getResourceClass().getName());
            assertEquals(session.getUserSubject(), r[0].getCreatedBy());
        }
        finally
        {
            session.close();
        }
    }

    public void testGetChildren()
        throws Exception
    {
        CoralSession session = coralSessionFactory.getAnonymousSession();
        try
        {
            ensureUniqueTest1ResourceExists(session);
            Resource root = session.getStore().getResource(1l);
            Resource[] c = session.getStore().getResource(root);
            assertTrue(c.length >= 1);
            boolean found = false;
            for(Resource r : c)
            {
                if(r.getName().equals("test1"))
                {
                    found = true;
                }
            }
            if(!found)
            {
                fail();
            }
        }
        finally
        {
            session.close();
        }
    }

    public void testGetChild()
        throws Exception
    {
        CoralSession session = coralSessionFactory.getAnonymousSession();
        try
        {
            ensureUniqueTest1ResourceExists(session);
            Resource root = session.getStore().getResource(1l);
            Resource[] c = session.getStore().getResource(root, "test1");
            assertTrue(c.length >= 1);
        }
        finally
        {
            session.close();
        }
    }

    public void testGetUniqueResourceByName()
        throws Exception
    {
        CoralSession session = coralSessionFactory.getAnonymousSession();
        try
        {
            ensureUniqueTest1ResourceExists(session);
            session.getStore().getUniqueResource("test1");
        }
        finally
        {
            session.close();
        }
    }

    public void testGetUniqueChild()
        throws Exception
    {
        CoralSession session = coralSessionFactory.getAnonymousSession();
        try
        {
            ensureUniqueTest1ResourceExists(session);
            Resource root = session.getStore().getResource(1l);
            session.getStore().getUniqueResource(root, "test1");
        }
        finally
        {
            session.close();
        }
    }

    public void testGetResourceByPath()
        throws Exception
    {
        CoralSession session = coralSessionFactory.getAnonymousSession();
        try
        {
            ensureUniqueTest1ResourceExists(session);
            Resource[] r = session.getStore().getResourceByPath("/test1");
            assertEquals(1, r.length);
        }
        finally
        {
            session.close();
        }
    }

    public void testGetUniqueResourceByPath()
        throws Exception
    {
        CoralSession session = coralSessionFactory.getAnonymousSession();
        try
        {
            ensureUniqueTest1ResourceExists(session);
            session.getStore().getUniqueResourceByPath("/test1");
        }
        finally
        {
            session.close();
        }
    }

    public void testGetChildByPath()
        throws Exception
    {
        CoralSession session = coralSessionFactory.getAnonymousSession();
        try
        {
            ensureUniqueTest1ResourceExists(session);
            Resource root = session.getStore().getResource(1l);
            Resource[] r = session.getStore().getResourceByPath(root, "test1");
            assertEquals(1, r.length);
        }
        finally
        {
            session.close();
        }
    }

    public void testGetUniqueChildByPath()
        throws Exception
    {
        CoralSession session = coralSessionFactory.getAnonymousSession();
        try
        {
            ensureUniqueTest1ResourceExists(session);
            Resource root = session.getStore().getResource(1l);
            session.getStore().getUniqueResourceByPath(root, "/test1");
        }
        finally
        {
            session.close();
        }
    }

    public void testDeleteResource()
        throws Exception
    {
        CoralSession session = coralSessionFactory.getAnonymousSession();
        try
        {
            Resource root = session.getStore().getResource(1l);
            ResourceClass<Node> rc = session.getSchema().getResourceClass("coral.Node", Node.class);
            Node r = session.getStore().createResource("test2", root, rc,
                Collections.<AttributeDefinition<?>, Object> emptyMap());
            session.getStore().deleteResource(r);
        }
        finally
        {
            session.close();
        }
    }

    public void testSetName()
        throws Exception
    {
        CoralSession session = coralSessionFactory.getAnonymousSession();
        try
        {
            Resource root = session.getStore().getResource(1l);
            ResourceClass<Node> rc = session.getSchema().getResourceClass("coral.Node", Node.class);
            Node r = session.getStore().createResource("test2", root, rc,
                Collections.<AttributeDefinition<?>, Object> emptyMap());
            session.getStore().setName(r, "test3");
            session.getStore().getUniqueResource("test3");
            session.getStore().deleteResource(r);
        }
        finally
        {
            session.close();
        }
    }

    public void testUnsetParent()
        throws Exception
    {
        CoralSession session = coralSessionFactory.getAnonymousSession();
        try
        {
            Resource root = session.getStore().getResource(1l);
            ResourceClass<Node> rc = session.getSchema().getResourceClass("coral.Node", Node.class);
            Node test2 = session.getStore().createResource("test2", root, rc,
                Collections.<AttributeDefinition<?>, Object> emptyMap());
            session.getStore().unsetParent(test2);
            assertEquals(0, session.getStore().getResource(root, "test2").length);
            assertNull(test2.getParent());
            session.getStore().deleteResource(test2);
        }
        finally
        {
            session.close();
        }
    }

    public void testSetParent()
        throws Exception
    {
        CoralSession session = coralSessionFactory.getAnonymousSession();
        try
        {
            ensureUniqueTest1ResourceExists(session);
            Resource root = session.getStore().getResource(1l);
            ResourceClass<Node> rc = session.getSchema().getResourceClass("coral.Node", Node.class);
            Node test2 = session.getStore().createResource("test2", root, rc,
                Collections.<AttributeDefinition<?>, Object> emptyMap());
            Node test1 = (Node)session.getStore().getUniqueResource("test1");
            session.getStore().setParent(test2, test1);
            assertEquals(0, session.getStore().getResource(root, "test2").length);
            assertEquals(1, session.getStore().getResource(test1, "test2").length);
            session.getStore().deleteResource(test2);
        }
        finally
        {
            session.close();
        }
    }

    public void testCopyResource()
        throws Exception
    {
        CoralSession session = coralSessionFactory.getAnonymousSession();
        try
        {
            ensureUniqueTest1ResourceExists(session);
            Resource root = session.getStore().getResource(1l);
            ResourceClass<Node> rc = session.getSchema().getResourceClass("coral.Node", Node.class);
            Node test2 = session.getStore().createResource("test2", root, rc,
                Collections.<AttributeDefinition<?>, Object> emptyMap());
            Node test3 = (Node)session.getStore().copyResource(test2, root, "test3");
            assertEquals(1, session.getStore().getResource(root, "test2").length);
            assertEquals(1, session.getStore().getResource(root, "test3").length);
            session.getStore().deleteResource(test2);
            session.getStore().deleteResource(test3);
        }
        finally
        {
            session.close();
        }
    }

    public void testCopyAndDeleteTree()
        throws Exception
    {
        CoralSession session = coralSessionFactory.getAnonymousSession();
        try
        {
            ensureUniqueTest1ResourceExists(session);
            Resource root = session.getStore().getResource(1l);
            ResourceClass<Node> rc = session.getSchema().getResourceClass("coral.Node", Node.class);
            Node test2 = session.getStore().createResource("test2", root, rc,
                Collections.<AttributeDefinition<?>, Object> emptyMap());
            session.getStore().createResource("test3", test2, rc,
                Collections.<AttributeDefinition<?>, Object> emptyMap());
            session.getStore().copyTree(test2, root, "test4");

            assertEquals(1, session.getStore().getResourceByPath("/test2/test3").length);
            assertEquals(1, session.getStore().getResourceByPath("/test4/test3").length);
            assertEquals(2, session.getStore().getResourceByPath("/*/test3").length);

            session.getStore().deleteTree(session.getStore().getUniqueResource("test4"));
            assertEquals(1, session.getStore().getResourceByPath("/*/test3").length);

            session.getStore().deleteTree(test2);
            assertEquals(0, session.getStore().getResourceByPath("/*/test3").length);
        }
        finally
        {
            session.close();
        }
    }

    public void testGetResouceBySubjectMetadata()
        throws Exception
    {
        try(CoralSession rootSession = coralSessionFactory.getRootSession())
        {
            Map<AttributeDefinition<?>, Object> attributes = new HashMap<>();
            final CoralStore store = rootSession.getStore();
            final CoralSchema schema = rootSession.getSchema();
            Resource root = store.getResource(1l);

            ResourceClass<?> rc = schema.createResourceClass("rc1",
                StandardResource.class.getName(), GenericResourceHandler.class.getName(), null, 0);
            AttributeClass<String> stringAttr = schema.getAttributeClass("string", String.class);
            AttributeDefinition<String> a1 = schema
                .createAttribute("a1", stringAttr, null, null, 0);
            schema.addAttribute(rc, a1, null);
            ResourceClass<Node> nodeRc = schema.getResourceClass("coral.Node", Node.class);
            schema.addParentClass(rc, nodeRc, attributes);

            Subject s1 = rootSession.getSecurity().createSubject("s1");
            Subject s2 = rootSession.getSecurity().createSubject("s2");
            Resource r1;

            try(CoralSession s1Session = coralSessionFactory.getSession(principal("s1")))
            {
                r1 = s1Session.getStore().createResource("r1", root, rc, attributes);
            }

            try(CoralSession s2Session = coralSessionFactory.getSession(principal("s2")))
            {
                s2Session.getStore().createResource("r2", root, rc, attributes);
                r1.set(a1, "x");
                r1.update();
                s2Session.getStore().setOwner(r1, s2);
            }

            AttributeDefinition<Subject> createdByAttr = (AttributeDefinition<Subject>)rc
                .getAttribute("created_by");
            AttributeDefinition<Subject> modifiedByAttr = (AttributeDefinition<Subject>)rc
                .getAttribute("modified_by");
            AttributeDefinition<Subject> ownerAttr = (AttributeDefinition<Subject>)rc
                .getAttribute("owner");

            Map<AttributeDefinition<Subject>, long[]> s1ids = store.getResouceBySubjectMetadata(s1);
            checkResources(store, s1ids.get(createdByAttr), "r1");
            checkResources(store, s1ids.get(modifiedByAttr));
            checkResources(store, s1ids.get(ownerAttr));

            Map<AttributeDefinition<Subject>, long[]> s2ids = store.getResouceBySubjectMetadata(s2);
            checkResources(store, s2ids.get(createdByAttr), "r2");
            checkResources(store, s2ids.get(modifiedByAttr), "r1", "r2");
            checkResources(store, s2ids.get(ownerAttr), "r1", "r2");
        }
    }

    public void testReplaceSubjectReferences()
        throws Exception
    {
        try(CoralSession rootSession = coralSessionFactory.getRootSession())
        {
            Map<AttributeDefinition<?>, Object> attributes = new HashMap<>();
            final CoralStore store = rootSession.getStore();
            final CoralSchema schema = rootSession.getSchema();
            Resource root = store.getResource(1l);

            ResourceClass<?> rc = schema.createResourceClass("rc1",
                StandardResource.class.getName(), GenericResourceHandler.class.getName(), null, 0);
            AttributeClass<String> stringAttr = schema.getAttributeClass("string", String.class);
            AttributeClass<Subject> subjectAttr = schema
                .getAttributeClass("subject", Subject.class);
            AttributeDefinition<String> a1 = schema
                .createAttribute("a1", stringAttr, null, null, 0);
            schema.addAttribute(rc, a1, null);
            AttributeDefinition<Subject> a2 = schema.createAttribute("a2", subjectAttr, null, null,
                0);
            schema.addAttribute(rc, a2, null);
            ResourceClass<Node> nodeRc = schema.getResourceClass("coral.Node", Node.class);
            schema.addParentClass(rc, nodeRc, attributes);

            Subject s1 = rootSession.getSecurity().createSubject("s1");
            Subject s2 = rootSession.getSecurity().createSubject("s2");
            Resource r1;
            Resource r2;

            try(CoralSession s1Session = coralSessionFactory.getSession(principal("s1")))
            {
                r1 = s1Session.getStore().createResource("r1", root, rc, attributes);
            }

            try(CoralSession s2Session = coralSessionFactory.getSession(principal("s2")))
            {
                r2 = s2Session.getStore().createResource("r2", root, rc, attributes);
                r1.set(a1, "x");
                r1.set(a2, s2);
                r1.update();
                s2Session.getStore().setOwner(r1, s2);
            }

            assertEquals(s2, r2.getCreatedBy());
            assertEquals(s2, r1.getModifiedBy());
            assertEquals(s2, r1.getOwner());
            assertEquals(s2, r1.get(a2));

            store.replaceSubjectReferences(s2, s1);

            assertEquals(s1, r2.getCreatedBy());
            assertEquals(s1, r1.getModifiedBy());
            assertEquals(s1, r1.getOwner());
            assertEquals(s1, r1.get(a2));
        }
    }

    private Principal principal(final String name)
    {
        return new Principal()
            {
                @Override
                public String getName()
                {
                    return name;
                }
            };
    }

    private void checkResources(CoralStore store, long[] ids, String... names)
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
