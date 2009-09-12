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
package org.objectledge.coral.security;

import java.util.HashSet;
import java.util.Set;

import org.jmock.Mock;
import org.objectledge.coral.CoralCore;
import org.objectledge.coral.entity.CoralRegistry;
import org.objectledge.coral.event.CoralEventHub;
import org.objectledge.coral.event.CoralEventWhiteboard;
import org.objectledge.coral.event.PermissionChangeListener;
import org.objectledge.coral.event.RoleChangeListener;
import org.objectledge.coral.event.SubjectChangeListener;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.Resource;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.Persistent;
import org.objectledge.test.LedgeTestCase;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: CoralSecurityImplTest.java,v 1.11 2008-01-08 21:18:56 rafal Exp $
 */
public class CoralSecurityImplTest 
    extends LedgeTestCase
{
    private Mock mockPersistence;
    private Persistence persistence;
    private Mock mockCoralEventHub;
    private CoralEventHub coralEventHub;
    private Mock mockLocalEventWhiteboard;
    private CoralEventWhiteboard localEventWhiteboard;
    private Mock mockOutboundEventWhiteboard;
    private CoralEventWhiteboard outboundEventWhiteboard;
    private Mock mockInboundEventWhiteboard;
    private CoralEventWhiteboard inboundEventWhiteboard;
    private Mock mockCoralRegistry;
    private CoralRegistry coralRegistry;
    private Mock mockCoralSchema;
    private CoralSchema coralSchema;
    private Mock mockCoralStore;
    private CoralStore coralStore;
    private Mock mockCoralCore;
    private CoralCore coralCore;
    
    private CoralSecurity coralSecurity;
    
    private Mock mockSubject;
    private Subject subject;
    private Mock mockRole;
    private Role role;
    private Mock mockSuperRole;
    private Role superRole;
    private Mock mockSubRole;
    private Role subRole;
    private Mock mockRootSubject;
    private Subject rootSubject;
    private Mock mockRootRole;
    private Role rootRole;
    private Mock mockPermission;
    private Permission permission;
    private Mock mockResourceClass;
    private ResourceClass resourceClass;
    private Mock mockResource;
    private Resource resource;
    
    public void setUp()
        throws Exception
    {
        super.setUp();
        
        mockPersistence = mock(Persistence.class);
        persistence = (Persistence)mockPersistence.proxy();
        mockCoralRegistry = mock(CoralRegistry.class);
        coralRegistry = (CoralRegistry)mockCoralRegistry.proxy();
        mockCoralEventHub = mock(CoralEventHub.class);
        coralEventHub = (CoralEventHub)mockCoralEventHub.proxy();
        mockLocalEventWhiteboard = mock(CoralEventWhiteboard.class, "localEventWhiteboard");
        localEventWhiteboard = (CoralEventWhiteboard)mockLocalEventWhiteboard.proxy();
        mockCoralEventHub.stubs().method("getLocal").will(returnValue(localEventWhiteboard));
        mockOutboundEventWhiteboard = mock(CoralEventWhiteboard.class, "outboundEventWhiteboard");
        outboundEventWhiteboard = (CoralEventWhiteboard)mockOutboundEventWhiteboard.proxy();
        mockCoralEventHub.stubs().method("getOutbound").will(returnValue(outboundEventWhiteboard));
        mockInboundEventWhiteboard = mock(CoralEventWhiteboard.class, "inboundEventWhiteboard");
        inboundEventWhiteboard = (CoralEventWhiteboard)mockInboundEventWhiteboard.proxy();
        mockCoralEventHub.stubs().method("getInbound").will(returnValue(inboundEventWhiteboard));
        mockCoralSchema = mock(CoralSchema.class);
        coralSchema = (CoralSchema)mockCoralSchema.proxy();
        mockCoralStore = mock(CoralStore.class);
        coralStore = (CoralStore)mockCoralStore.proxy();
        mockCoralCore = mock(CoralCore.class);
        coralCore = (CoralCore)mockCoralCore.proxy();
        mockCoralCore.stubs().method("getRegistry").will(returnValue(coralRegistry));
        mockCoralCore.stubs().method("getSchema").will(returnValue(coralSchema));
        mockCoralCore.stubs().method("getStore").will(returnValue(coralStore));
        
        coralSecurity = new CoralSecurityImpl(persistence, coralEventHub, coralCore);
        
        mockSubject = mock(Subject.class);
        subject = (Subject)mockSubject.proxy();
        mockRole = mock(Role.class);
        role = (Role)mockRole.proxy();
        mockSuperRole = mock(Role.class, "mockSuperRole");
        superRole = (Role)mockSuperRole.proxy();
        mockSubRole = mock(Role.class, "mockSubRole");
        subRole = (Role)mockSubRole.proxy();
        mockRootSubject = mock(Subject.class, "mockRootSubject");
        rootSubject = (Subject)mockRootSubject.proxy();
        mockRootRole = mock(Role.class, "mockRootRole");
        rootRole = (Role)mockRootRole.proxy();
        mockPermission = mock(Permission.class);
        permission = (Permission)mockPermission.proxy();
        mockResourceClass = mock(ResourceClass.class);
        resourceClass = (ResourceClass)mockResourceClass.proxy();
        mockResource = mock(Resource.class);
        resource = (Resource)mockResource.proxy();
    }
    
    public void testCreation()
    {    
        // just run setUp()
    }
    
    // subjects /////////////////////////////////////////////////////////////////////////////////
    
    public void testGetSubject()
    {
        Subject[] sa = new Subject[0];
        mockCoralRegistry.expects(once()).method("getSubject").will(returnValue(sa));
        assertSame(sa, coralSecurity.getSubject());
    }
    
    public void testGetSubjectById()
        throws Exception
    {
        mockCoralRegistry.expects(once()).method("getSubject").with(eq(1L)).will(returnValue(subject));        
        assertSame(subject, coralSecurity.getSubject(1L));
    }

    public void testGetSubjectByName()
        throws Exception
    {
        mockCoralRegistry.expects(once()).method("getSubject").with(eq("fred")).will(returnValue(subject));        
        assertSame(subject, coralSecurity.getSubject("fred"));
    }
    
    public void testCreateSubject()
        throws Exception
    {
        mockInboundEventWhiteboard.expects(once()).method("addSubjectChangeListener").with(isA(SubjectChangeListener.class), isA(Subject.class));
        mockCoralRegistry.expects(once()).method("addSubject").with(and(isA(Subject.class), isA(Persistent.class)));
        Subject realSubject = coralSecurity.createSubject("fred");
        assertEquals("fred", realSubject.getName());
    }
    
    public void testDeleteSubject()
        throws Exception
    {
        mockCoralRegistry.expects(once()).method("deleteSubject").with(same(subject));
        coralSecurity.deleteSubject(subject);
    }
    
    public void testRenameSubject()
        throws Exception
    {
        mockInboundEventWhiteboard.expects(once()).method("addSubjectChangeListener").with(isA(SubjectChangeListener.class), isA(Subject.class));
        mockCoralRegistry.expects(once()).method("addSubject").with(and(isA(Subject.class), isA(Persistent.class)));
        Subject realSubject = coralSecurity.createSubject("fred");
        assertEquals("fred", realSubject.getName());
        
        mockCoralRegistry.expects(once()).method("renameSubject").with(same(realSubject), eq("george"));
        mockOutboundEventWhiteboard.expects(once()).method("fireSubjectChangeEvent").with(same(realSubject));
        coralSecurity.setName(realSubject, "george");
    }
    
    // roles ////////////////////////////////////////////////////////////////////////////////////
 
    public void testGetRole()
    {
        Role[] ra = new Role[0];
        mockCoralRegistry.expects(once()).method("getRole").will(returnValue(ra));
        assertSame(ra, coralSecurity.getRole());
    }    

    public void testGetRoleById()
        throws Exception
    {
        mockCoralRegistry.expects(once()).method("getRole").with(eq(1L)).will(returnValue(role));        
        assertSame(role, coralSecurity.getRole(1L));
    }

    public void testGetRoleByName()
        throws Exception
    {
        Role[] ra = new Role[0];
        mockCoralRegistry.expects(once()).method("getRole").with(eq("fred")).will(returnValue(ra));        
        assertSame(ra, coralSecurity.getRole("fred"));
    }

    public void testGetUniqueRoleByName()
        throws Exception
    {
        mockCoralRegistry.expects(once()).method("getUniqueRole").with(eq("fred")).will(returnValue(role));        
        assertSame(role, coralSecurity.getUniqueRole("fred"));
    }
    
    public void testCreateRole()
        throws Exception
    {
        mockInboundEventWhiteboard.expects(once()).method("addRoleChangeListener").with(isA(RoleChangeListener.class), isA(Role.class));
        mockCoralRegistry.expects(once()).method("addRole").with(and(isA(Role.class), isA(Persistent.class)));
        Role realRole = coralSecurity.createRole("fred");
        assertEquals("fred", realRole.getName());
    }

    public void testDeleteRole()
        throws Exception
    {
        mockCoralRegistry.expects(once()).method("deleteRole").with(same(role));
        coralSecurity.deleteRole(role);
    }

    public void testRenameRole()
        throws Exception
    {
        mockInboundEventWhiteboard.expects(once()).method("addRoleChangeListener").with(isA(RoleChangeListener.class), isA(Role.class));
        mockCoralRegistry.expects(once()).method("addRole").with(and(isA(Role.class), isA(Persistent.class)));
        Role realRole = coralSecurity.createRole("fred");
        assertEquals("fred", realRole.getName());
        
        mockCoralRegistry.expects(once()).method("renameRole").with(same(realRole), eq("george"));
        mockOutboundEventWhiteboard.expects(once()).method("fireRoleChangeEvent").with(same(realRole));
        coralSecurity.setName(realRole, "george");
    }
    
    public void testAddSubRole()
        throws Exception
    {
        mockSuperRole.stubs().method("getId").will(returnValue(1L));
        mockSubRole.stubs().method("getId").will(returnValue(2L));
        mockCoralRegistry.stubs().method("getRoleImplications").with(eq(subRole)).will(returnValue(new HashSet(0)));
        mockSubRole.stubs().method("getSubRoles").will(returnValue(new Role[0]));
        RoleImplication ri = new RoleImplicationImpl(coralCore, superRole, subRole);
        mockCoralRegistry.expects(once()).method("addRoleImplication").with(eq(ri));
        // TODO no events here?
        coralSecurity.addSubRole(superRole, subRole);
    }
    
    public void testDeleteSubRole()
        throws Exception
    {
        mockSuperRole.stubs().method("getIdObject").will(returnValue(new Long(1L)));
        mockSubRole.stubs().method("getIdObject").will(returnValue(new Long(2L)));
        mockSuperRole.stubs().method("isSubRole").with(same(subRole)).will(returnValue(true));
        RoleImplication ri = new RoleImplicationImpl(coralCore, superRole, subRole);
        mockCoralRegistry.expects(once()).method("deleteRoleImplication").with(eq(ri));
        coralSecurity.deleteSubRole(superRole, subRole);
    }
    
    public void testGrantRole()
        throws Exception
    {
        mockCoralRegistry.stubs().method("getRoleAssignments").with(same(subject)).will(returnValue(new HashSet()));
        mockSubject.stubs().method("getId").will(returnValue(2L));
        mockRole.stubs().method("getId").will(returnValue(2L));
        mockCoralCore.stubs().method("getCurrentSubject").will(returnValue(rootSubject));
        mockCoralRegistry.stubs().method("getRole").with(eq(1L)).will(returnValue(rootRole));
        mockRootSubject.stubs().method("hasRole").with(same(rootRole)).will(returnValue(true));
        RoleAssignment ra = new RoleAssignmentImpl(coralCore, rootSubject, subject, role, true);

        mockCoralRegistry.expects(once()).method("addRoleAssignment").with(eq(ra));
        coralSecurity.grant(role, subject, true);
    }
    
    public void testRevokeRole()
        throws Exception
    {
        mockSubject.stubs().method("getId").will(returnValue(2L));
        mockRole.stubs().method("getId").will(returnValue(2L));
        RoleAssignment ra = new RoleAssignmentImpl(coralCore, rootSubject, subject, role, true);
        Set ras = new HashSet(1);
        ras.add(ra);
        mockCoralRegistry.stubs().method("getRoleAssignments").with(same(subject)).will(returnValue(ras));
        mockCoralCore.stubs().method("getCurrentSubject").will(returnValue(rootSubject));
        mockCoralRegistry.stubs().method("getRole").with(eq(1L)).will(returnValue(rootRole));
        mockRootSubject.stubs().method("hasRole").with(same(rootRole)).will(returnValue(true));

        mockCoralRegistry.expects(once()).method("deleteRoleAssignment").with(eq(ra));
        coralSecurity.revoke(role, subject);
    }
    
    // permissions //////////////////////////////////////////////////////////////////////////////
    
    public void testGetPermission()
    {
        Permission[] pa = new Permission[0];
        mockCoralRegistry.expects(once()).method("getPermission").will(returnValue(pa));
        assertSame(pa, coralSecurity.getPermission());
    }
    
    public void testGetPermissionById()
        throws Exception
    {
        mockCoralRegistry.expects(once()).method("getPermission").with(eq(1L)).will(returnValue(permission));
        assertSame(permission, coralSecurity.getPermission(1L));
    }
    
    public void testGetPermissionByName()
    {
        Permission[] pa = new Permission[0];
        mockCoralRegistry.expects(once()).method("getPermission").with(eq("fred")).will(returnValue(pa));
        assertSame(pa, coralSecurity.getPermission("fred"));
    }    

    public void testGetUniquePermissionById()
        throws Exception
    {
        mockCoralRegistry.expects(once()).method("getUniquePermission").with(eq("fred")).will(returnValue(permission));
        assertSame(permission, coralSecurity.getUniquePermission("fred"));
    }    

    public void testCreatePermission()
        throws Exception
    {
        mockInboundEventWhiteboard.expects(once()).method("addPermissionChangeListener").with(isA(PermissionChangeListener.class), isA(Permission.class));
        mockCoralRegistry.expects(once()).method("addPermission").with(and(isA(Permission.class), isA(Persistent.class)));
        Permission realPermission = coralSecurity.createPermission("fred");
        assertEquals("fred", realPermission.getName());
    }

    public void testDeletePermission()
        throws Exception
    {
        mockCoralRegistry.expects(once()).method("deletePermission").with(same(permission));
        coralSecurity.deletePermission(permission);
    }

    public void testRenamePermission()
        throws Exception
    {
        mockInboundEventWhiteboard.expects(once()).method("addPermissionChangeListener").with(isA(PermissionChangeListener.class), isA(Permission.class));
        mockCoralRegistry.expects(once()).method("addPermission").with(and(isA(Permission.class), isA(Persistent.class)));
        Permission realPermission = coralSecurity.createPermission("fred");
        assertEquals("fred", realPermission.getName());
        
        mockCoralRegistry.expects(once()).method("renamePermission").with(same(realPermission), eq("george"));
        mockOutboundEventWhiteboard.expects(once()).method("firePermissionChangeEvent").with(same(realPermission));
        coralSecurity.setName(realPermission, "george");
    }
    
    public void testAddPermissionAssociation()
        throws Exception
    {
        mockResourceClass.stubs().method("getId").will(returnValue(1L));
        mockPermission.stubs().method("getId").will(returnValue(1L));
        mockCoralRegistry.stubs().method("getPermissionAssociations").with(same(resourceClass)).will(returnValue(new HashSet(0)));
        PermissionAssociation pa = new PermissionAssociationImpl(coralCore, 
            resourceClass, permission);
        mockCoralRegistry.expects(once()).method("addPermissionAssociation").with(eq(pa));
        coralSecurity.addPermission(resourceClass, permission);
    }
    
    public void testDeletePermissionAssociation()
        throws Exception
    {
        mockResourceClass.stubs().method("getId").will(returnValue(1L));
        mockPermission.stubs().method("getId").will(returnValue(1L));
        PermissionAssociation pa = new PermissionAssociationImpl(coralCore, 
            resourceClass, permission);
        Set pas = new HashSet(1);
        pas.add(pa);
        mockCoralRegistry.stubs().method("getPermissionAssociations").with(same(resourceClass)).will(returnValue(pas));
        mockCoralRegistry.expects(once()).method("deletePermissionAssociation").with(eq(pa));
        coralSecurity.deletePermission(resourceClass, permission);        
    }
    
    public void testGrantPermission()
        throws Exception
    {
        mockCoralCore.stubs().method("getCurrentSubject").will(returnValue(rootSubject));
        mockCoralRegistry.stubs().method("getRole").with(eq(1L)).will(returnValue(rootRole));
        mockRootSubject.stubs().method("hasRole").with(same(rootRole)).will(returnValue(true));
        mockResource.stubs().method("getResourceClass").will(returnValue(resourceClass));
        mockResource.stubs().method("getId").will(returnValue(new Long(1L)));        
        mockResourceClass.stubs().method("isAssociatedWith").with(same(permission)).will(returnValue(true));
        mockResource.stubs().method("getIdObject").will(returnValue(new Long(1L)));
        mockRole.stubs().method("getIdObject").will(returnValue(new Long(2L)));
        mockRole.stubs().method("getId").will(returnValue(new Long(2L)));
        mockPermission.stubs().method("getIdObject").will(returnValue(new Long(1L)));
        mockPermission.stubs().method("getId").will(returnValue(new Long(1L)));
        PermissionAssignment pa = new PermissionAssignmentImpl(coralCore,
            rootSubject, resource, role, permission, true);
        assertEquals(resource, resource);
        assertEquals(role, role);
        assertEquals(permission, permission);
        assertEquals(pa, pa);
        mockCoralRegistry.expects(once()).method("addPermissionAssignment").with(eq(pa));
        coralSecurity.grant(resource, role, permission, true);
    }
    
    public void testRevokePermission()
        throws Exception
    {
        mockRole.stubs().method("hasPermission").with(same(resource), same(permission)).will(returnValue(true));
        mockCoralCore.stubs().method("getCurrentSubject").will(returnValue(rootSubject));
        mockCoralRegistry.stubs().method("getRole").with(eq(1L)).will(returnValue(rootRole));
        mockRootSubject.stubs().method("hasRole").with(same(rootRole)).will(returnValue(true));
        mockResource.stubs().method("getIdObject").will(returnValue(new Long(1L)));
        mockResource.stubs().method("getId").will(returnValue(new Long(1L)));        
        mockRole.stubs().method("getIdObject").will(returnValue(new Long(2L)));
        mockRole.stubs().method("getId").will(returnValue(new Long(2L)));
        mockPermission.stubs().method("getIdObject").will(returnValue(new Long(1L)));
        mockPermission.stubs().method("getId").will(returnValue(new Long(1L)));
        PermissionAssignment pa = new PermissionAssignmentImpl(coralCore,
            rootSubject, resource, role, permission, false);
        mockResource.stubs().method("getPermissionAssignments").with(same(role)).will(
            returnValue(new PermissionAssignment[] { pa }));
        mockCoralRegistry.expects(once()).method("deletePermissionAssignment").with(eq(pa));
        coralSecurity.revoke(resource, role, permission);
    }
}
