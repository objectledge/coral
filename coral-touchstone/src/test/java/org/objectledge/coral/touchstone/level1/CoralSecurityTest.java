package org.objectledge.coral.touchstone.level1;

import java.util.Collections;
import java.util.Map;

import org.objectledge.coral.datatypes.Node;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.security.Permission;
import org.objectledge.coral.security.Role;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.touchstone.CoralTestCase;

public class CoralSecurityTest
    extends CoralTestCase
{
    private Permission perm;

    private ResourceClass<Node> rc;

    private Node resR;

    private Role role1;

    private Role role2;

    private Role role3;

    private Node resA1;

    private Node resA2;

    private Node resB1;

    private CoralSession coral;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();
        coral = coralSessionFactory.getRootSession();
        perm = coral.getSecurity().createPermission("TestPermission");
        rc = coral.getSchema().getResourceClass("coral.Node", Node.class);
        coral.getSecurity().addPermission(rc, perm);
        resR = (Node)coral.getStore().getResource(CoralStore.ROOT_RESOURCE);
        role1 = coral.getSecurity().createRole("Role1");
        role2 = coral.getSecurity().createRole("Role2");
        coral.getSecurity().addSubRole(role1, role2);
        role3 = coral.getSecurity().createRole("Role3");
        Map<AttributeDefinition<?>, Object> emptyAttrs = Collections
            .<AttributeDefinition<?>, Object> emptyMap();
        resA1 = coral.getStore().createResource("A1", resR, rc, emptyAttrs);
        resA2 = coral.getStore().createResource("A2", resR, rc, emptyAttrs);
        resB1 = coral.getStore().createResource("B1", resA1, rc, emptyAttrs);
    }

    @Override
    public void tearDown()
        throws Exception
    {
        if(coral != null)
        {
            coral.close();
        }
        super.tearDown();
    }

    public void testDirectAssignment()
        throws Exception
    {
        assertFalse(role1.hasPermission(resB1, perm));
        coral.getSecurity().grant(resB1, role1, perm, false);
        assertTrue(role1.hasPermission(resB1, perm));
        coral.getSecurity().revoke(resB1, role1, perm);
        assertFalse(role1.hasPermission(resB1, perm));
    }

    public void testInheritedAssignment()
        throws Exception
    {
        assertFalse(role1.hasPermission(resB1, perm));
        coral.getSecurity().grant(resA1, role1, perm, false); // not inherited
        assertFalse(role1.hasPermission(resB1, perm));
        coral.getSecurity().revoke(resA1, role1, perm);
        assertFalse(role1.hasPermission(resB1, perm));
        coral.getSecurity().grant(resA1, role1, perm, true);
        assertTrue(role1.hasPermission(resB1, perm));
        coral.getSecurity().revoke(resA1, role1, perm);
        assertFalse(role1.hasPermission(resB1, perm));
    }

    public void testSubRoleAssignment()
        throws Exception
    {
        assertFalse(role1.hasPermission(resB1, perm));
        coral.getSecurity().grant(resB1, role2, perm, false);
        assertTrue(role2.hasPermission(resB1, perm));
        assertTrue(role1.hasPermission(resB1, perm));
        coral.getSecurity().revoke(resB1, role2, perm);
        assertFalse(role1.hasPermission(resB1, perm));
    }

    public void testTreeChange()
        throws Exception
    {
        assertFalse(role1.hasPermission(resB1, perm));
        coral.getSecurity().grant(resA2, role1, perm, true);
        assertFalse(role1.hasPermission(resB1, perm));
        coral.getStore().setParent(resB1, resA2);
        assertTrue(role1.hasPermission(resB1, perm));
        coral.getStore().setParent(resB1, resA1);
        assertFalse(role1.hasPermission(resB1, perm));
    }

    public void testTreeChange2()
        throws Exception
    {
        assertFalse(role1.hasPermission(resB1, perm));
        coral.getSecurity().grant(resA1, role1, perm, true);
        assertTrue(role1.hasPermission(resB1, perm));
        coral.getStore().setParent(resB1, resA2);
        assertFalse(role1.hasPermission(resB1, perm));
        coral.getStore().setParent(resB1, resA1);
        assertTrue(role1.hasPermission(resB1, perm));
    }

    public void testRoleTreeChange()
        throws Exception
    {
        coral.getSecurity().grant(resA1, role3, perm, false);
        assertTrue(role3.hasPermission(resA1, perm));

        assertFalse(role1.hasPermission(resA1, perm));
        coral.getSecurity().addSubRole(role1, role3);
        assertTrue(role1.hasPermission(resA1, perm));

        coral.getSecurity().deleteSubRole(role1, role3);
        assertFalse(role1.hasPermission(resA1, perm));
    }

    public void testRoleTreeChange2()
        throws Exception
    {
        coral.getSecurity().addSubRole(role1, role3);
        coral.getSecurity().grant(resA1, role3, perm, false);
        assertTrue(role3.hasPermission(resA1, perm));
        assertTrue(role1.hasPermission(resA1, perm));

        coral.getSecurity().deleteSubRole(role1, role3);
        assertFalse(role1.hasPermission(resA1, perm));
        assertTrue(role3.hasPermission(resA1, perm));
    }

    public void testCombined()
        throws Exception
    {
        assertFalse(role1.hasPermission(resB1, perm));
        coral.getSecurity().grant(resA2, role3, perm, true);
        assertFalse(role1.hasPermission(resB1, perm));
        coral.getSecurity().addSubRole(role1, role3);
        assertFalse(role1.hasPermission(resB1, perm));
        coral.getStore().setParent(resB1, resA2);
        assertTrue(role1.hasPermission(resB1, perm));

        coral.getStore().setParent(resB1, resA1);
        assertFalse(role1.hasPermission(resB1, perm));

        coral.getSecurity().grant(resA1, role3, perm, true);
        assertTrue(role1.hasPermission(resB1, perm));

        coral.getSecurity().deleteSubRole(role1, role3);
        assertFalse(role1.hasPermission(resB1, perm));

        coral.getSecurity().grant(resA1, role2, perm, true);
        assertTrue(role1.hasPermission(resB1, perm));

        coral.getSecurity().deleteSubRole(role1, role2);
        assertFalse(role1.hasPermission(resB1, perm));

        coral.getSecurity().grant(resA1, role1, perm, true);
        assertTrue(role1.hasPermission(resB1, perm));

        coral.getSecurity().revoke(resA1, role1, perm);
        coral.getSecurity().grant(resA1, role1, perm, false);
        assertFalse(role1.hasPermission(resB1, perm));
    }
}
