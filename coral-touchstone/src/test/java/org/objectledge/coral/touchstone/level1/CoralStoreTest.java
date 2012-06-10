package org.objectledge.coral.touchstone.level1;

import java.util.Collections;

import org.objectledge.coral.datatypes.Node;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.session.CoralSession;
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

}
