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

import org.jmock.builder.Mock;
import org.jmock.builder.MockObjectTestCase;
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

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: CoralSecurityImplTest.java,v 1.3 2004-03-05 11:52:18 fil Exp $
 */
public class CoralSecurityImplTest 
    extends MockObjectTestCase
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
        
        mockPersistence = new Mock(Persistence.class);
        persistence = (Persistence)mockPersistence.proxy();
        mockCoralRegistry = new Mock(CoralRegistry.class);
        coralRegistry = (CoralRegistry)mockCoralRegistry.proxy();
        mockCoralEventHub = new Mock(CoralEventHub.class);
        coralEventHub = (CoralEventHub)mockCoralEventHub.proxy();
        mockLocalEventWhiteboard = new Mock(CoralEventWhiteboard.class, "localEventWhiteboard");
        localEventWhiteboard = (CoralEventWhiteboard)mockLocalEventWhiteboard.proxy();
        mockCoralEventHub.stub().method("getLocal").will(returnValue(localEventWhiteboard));
        mockOutboundEventWhiteboard = new Mock(CoralEventWhiteboard.class, "outboundEventWhiteboard");
        outboundEventWhiteboard = (CoralEventWhiteboard)mockOutboundEventWhiteboard.proxy();
        mockCoralEventHub.stub().method("getOutbound").will(returnValue(outboundEventWhiteboard));
        mockInboundEventWhiteboard = new Mock(CoralEventWhiteboard.class, "inboundEventWhiteboard");
        inboundEventWhiteboard = (CoralEventWhiteboard)mockInboundEventWhiteboard.proxy();
        mockCoralEventHub.stub().method("getInbound").will(returnValue(inboundEventWhiteboard));
        mockCoralSchema = new Mock(CoralSchema.class);
        coralSchema = (CoralSchema)mockCoralSchema.proxy();
        mockCoralStore = new Mock(CoralStore.class);
        coralStore = (CoralStore)mockCoralStore.proxy();
        mockCoralCore = new Mock(CoralCore.class);
        coralCore = (CoralCore)mockCoralCore.proxy();
        mockCoralCore.stub().method("getRegistry").will(returnValue(coralRegistry));
        mockCoralCore.stub().method("getSchema").will(returnValue(coralSchema));
        mockCoralCore.stub().method("getStore").will(returnValue(coralStore));
        
        coralSecurity = new CoralSecurityImpl(persistence, coralEventHub, coralCore);
        
        mockSubject = new Mock(Subject.class);
        subject = (Subject)mockSubject.proxy();
        mockRole = new Mock(Role.class);
        role = (Role)mockRole.proxy();
        mockSuperRole = new Mock(Role.class, "mockSuperRole");
        superRole = (Role)mockSuperRole.proxy();
        mockSubRole = new Mock(Role.class, "mockSubRole");
        subRole = (Role)mockSubRole.proxy();
        mockRootSubject = new Mock(Subject.class, "mockRootSubject");
        rootSubject = (Subject)mockRootSubject.proxy();
        mockRootRole = new Mock(Role.class, "mockRootRole");
        rootRole = (Role)mockRootRole.proxy();
        mockPermission = new Mock(Permission.class);
        permission = (Permission)mockPermission.proxy();
        mockResourceClass = new Mock(ResourceClass.class);
        resourceClass = (ResourceClass)mockResourceClass.proxy();
        mockResource = new Mock(Resource.class);
        resource = (Resource)mockResource.proxy();
    }
    
    public void testCreation()
    {    
    }
    
    // subjects /////////////////////////////////////////////////////////////////////////////////
    
    public void testGetSubject()
    {
        Subject[] sa = new Subject[0];
        mockCoralRegistry.expect(once()).method("getSubject").will(returnValue(sa));
        assertSame(sa, coralSecurity.getSubject());
    }
    
    public void testGetSubjectById()
        throws Exception
    {
        mockCoralRegistry.expect(once()).method("getSubject").with(eq(1L)).will(returnValue(subject));        
        assertSame(subject, coralSecurity.getSubject(1L));
    }

    public void testGetSubjectByName()
        throws Exception
    {
        mockCoralRegistry.expect(once()).method("getSubject").with(eq("fred")).will(returnValue(subject));        
        assertSame(subject, coralSecurity.getSubject("fred"));
    }
    
    public void testCreateSubject()
        throws Exception
    {
        mockInboundEventWhiteboard.expect(once()).method("addSubjectChangeListener").with(isA(SubjectChangeListener.class), isA(Subject.class));
        mockCoralRegistry.expect(once()).method("addSubject").with(and(isA(Subject.class), isA(Persistent.class)));
        Subject realSubject = coralSecurity.createSubject("fred");
        assertEquals("fred", realSubject.getName());
    }
    
    public void testDeleteSubject()
        throws Exception
    {
        mockCoralRegistry.expect(once()).method("deleteSubject").with(same(subject));
        coralSecurity.deleteSubject(subject);
    }
    
    public void testRenameSubject()
        throws Exception
    {
        mockInboundEventWhiteboard.expect(once()).method("addSubjectChangeListener").with(isA(SubjectChangeListener.class), isA(Subject.class));
        mockCoralRegistry.expect(once()).method("addSubject").with(and(isA(Subject.class), isA(Persistent.class)));
        Subject realSubject = coralSecurity.createSubject("fred");
        assertEquals("fred", realSubject.getName());
        
        mockCoralRegistry.expect(once()).method("renameSubject").with(same(realSubject), eq("george"));
        mockOutboundEventWhiteboard.expect(once()).method("fireSubjectChangeEvent").with(same(realSubject));
        coralSecurity.setName(realSubject, "george");
    }
    
    // roles ////////////////////////////////////////////////////////////////////////////////////
 
    public void testGetRole()
    {
        Role[] ra = new Role[0];
        mockCoralRegistry.expect(once()).method("getRole").will(returnValue(ra));
        assertSame(ra, coralSecurity.getRole());
    }    

    public void testGetRoleById()
        throws Exception
    {
        mockCoralRegistry.expect(once()).method("getRole").with(eq(1L)).will(returnValue(role));        
        assertSame(role, coralSecurity.getRole(1L));
    }

    public void testGetRoleByName()
        throws Exception
    {
        Role[] ra = new Role[0];
        mockCoralRegistry.expect(once()).method("getRole").with(eq("fred")).will(returnValue(ra));        
        assertSame(ra, coralSecurity.getRole("fred"));
    }

    public void testGetUniqueRoleByName()
        throws Exception
    {
        mockCoralRegistry.expect(once()).method("getUniqueRole").with(eq("fred")).will(returnValue(role));        
        assertSame(role, coralSecurity.getUniqueRole("fred"));
    }
    
    public void testCreateRole()
        throws Exception
    {
        mockInboundEventWhiteboard.expect(once()).method("addRoleChangeListener").with(isA(RoleChangeListener.class), isA(Role.class));
        mockCoralRegistry.expect(once()).method("addRole").with(and(isA(Role.class), isA(Persistent.class)));
        Role realRole = coralSecurity.createRole("fred");
        assertEquals("fred", realRole.getName());
    }

    public void testDeleteRole()
        throws Exception
    {
        mockCoralRegistry.expect(once()).method("deleteRole").with(same(role));
        coralSecurity.deleteRole(role);
    }

    public void testRenameRole()
        throws Exception
    {
        mockInboundEventWhiteboard.expect(once()).method("addRoleChangeListener").with(isA(RoleChangeListener.class), isA(Role.class));
        mockCoralRegistry.expect(once()).method("addRole").with(and(isA(Role.class), isA(Persistent.class)));
        Role realRole = coralSecurity.createRole("fred");
        assertEquals("fred", realRole.getName());
        
        mockCoralRegistry.expect(once()).method("renameRole").with(same(realRole), eq("george"));
        mockOutboundEventWhiteboard.expect(once()).method("fireRoleChangeEvent").with(same(realRole));
        coralSecurity.setName(realRole, "george");
    }
    
    public void testAddSubRole()
        throws Exception
    {
        mockSuperRole.stub().method("getId").will(returnValue(1L));
        mockSubRole.stub().method("getId").will(returnValue(2L));
        mockCoralRegistry.stub().method("getRoleImplications").with(eq(subRole)).will(returnValue(new HashSet(0)));
        mockSubRole.stub().method("getSubRoles").will(returnValue(new Role[0]));
        RoleImplication ri = new RoleImplicationImpl(coralCore, superRole, subRole);
        mockCoralRegistry.expect(once()).method("addRoleImplication").with(eq(ri));
        // TODO no events here?
        coralSecurity.addSubRole(superRole, subRole);
    }
    
    public void testDeleteSubRole()
        throws Exception
    {
        mockSuperRole.stub().method("getId").will(returnValue(1L));
        mockSubRole.stub().method("getId").will(returnValue(2L));
        mockSuperRole.stub().method("isSubRole").with(same(subRole)).will(returnValue(true));
        RoleImplication ri = new RoleImplicationImpl(coralCore, superRole, subRole);
        mockCoralRegistry.expect(once()).method("deleteRoleImplication").with(eq(ri));
        coralSecurity.deleteSubRole(superRole, subRole);
    }
    
    public void testGrantRole()
        throws Exception
    {
        mockCoralRegistry.stub().method("getRoleAssignments").with(same(subject)).will(returnValue(new HashSet()));
        mockSubject.stub().method("getId").will(returnValue(2L));
        mockRole.stub().method("getId").will(returnValue(2L));
        mockCoralRegistry.stub().method("getRole").with(eq(1L)).will(returnValue(rootRole));
        mockRootSubject.stub().method("hasRole").with(same(rootRole)).will(returnValue(true));
        RoleAssignment ra = new RoleAssignmentImpl(coralCore, rootSubject, subject, role, true);

        mockCoralRegistry.expect(once()).method("addRoleAssignment").with(eq(ra));
        coralSecurity.grant(role, subject, true, rootSubject);
    }
    
    public void testRevokeRole()
        throws Exception
    {
        mockSubject.stub().method("getId").will(returnValue(2L));
        mockRole.stub().method("getId").will(returnValue(2L));
        RoleAssignment ra = new RoleAssignmentImpl(coralCore, rootSubject, subject, role, true);
        Set ras = new HashSet(1);
        ras.add(ra);
        mockCoralRegistry.stub().method("getRoleAssignments").with(same(subject)).will(returnValue(ras));
        mockCoralRegistry.stub().method("getRole").with(eq(1L)).will(returnValue(rootRole));
        mockRootSubject.stub().method("hasRole").with(same(rootRole)).will(returnValue(true));

        mockCoralRegistry.expect(once()).method("deleteRoleAssignment").with(eq(ra));
        coralSecurity.revoke(role, subject, rootSubject);
    }
    
    // permissions //////////////////////////////////////////////////////////////////////////////
    
    public void testGetPermission()
    {
        Permission[] pa = new Permission[0];
        mockCoralRegistry.expect(once()).method("getPermission").will(returnValue(pa));
        assertSame(pa, coralSecurity.getPermission());
    }
    
    public void testGetPermissionById()
        throws Exception
    {
        mockCoralRegistry.expect(once()).method("getPermission").with(eq(1L)).will(returnValue(permission));
        assertSame(permission, coralSecurity.getPermission(1L));
    }
    
    public void testGetPermissionByName()
    {
        Permission[] pa = new Permission[0];
        mockCoralRegistry.expect(once()).method("getPermission").with(eq("fred")).will(returnValue(pa));
        assertSame(pa, coralSecurity.getPermission("fred"));
    }    

    public void testGetUniquePermissionById()
        throws Exception
    {
        mockCoralRegistry.expect(once()).method("getUniquePermission").with(eq("fred")).will(returnValue(permission));
        assertSame(permission, coralSecurity.getUniquePermission("fred"));
    }    

    public void testCreatePermission()
        throws Exception
    {
        mockInboundEventWhiteboard.expect(once()).method("addPermissionChangeListener").with(isA(PermissionChangeListener.class), isA(Permission.class));
        mockCoralRegistry.expect(once()).method("addPermission").with(and(isA(Permission.class), isA(Persistent.class)));
        Permission realPermission = coralSecurity.createPermission("fred");
        assertEquals("fred", realPermission.getName());
    }

    public void testDeletePermission()
        throws Exception
    {
        mockCoralRegistry.expect(once()).method("deletePermission").with(same(permission));
        coralSecurity.deletePermission(permission);
    }

    public void testRenamePermission()
        throws Exception
    {
        mockInboundEventWhiteboard.expect(once()).method("addPermissionChangeListener").with(isA(PermissionChangeListener.class), isA(Permission.class));
        mockCoralRegistry.expect(once()).method("addPermission").with(and(isA(Permission.class), isA(Persistent.class)));
        Permission realPermission = coralSecurity.createPermission("fred");
        assertEquals("fred", realPermission.getName());
        
        mockCoralRegistry.expect(once()).method("renamePermission").with(same(realPermission), eq("george"));
        mockOutboundEventWhiteboard.expect(once()).method("firePermissionChangeEvent").with(same(realPermission));
        coralSecurity.setName(realPermission, "george");
    }
    
    public void testAddPermissionAssociation()
        throws Exception
    {
        mockResourceClass.stub().method("getId").will(returnValue(1L));
        mockPermission.stub().method("getId").will(returnValue(1L));
        mockCoralRegistry.stub().method("getPermissionAssociations").with(same(resourceClass)).will(returnValue(new HashSet(0)));
        PermissionAssociation pa = new PermissionAssociationImpl(coralCore, 
            resourceClass, permission);
        mockCoralRegistry.expect(once()).method("addPermissionAssociation").with(eq(pa));
        coralSecurity.addPermission(resourceClass, permission);
    }
    
    public void testDeletePermissionAssociation()
        throws Exception
    {
        mockResourceClass.stub().method("getId").will(returnValue(1L));
        mockPermission.stub().method("getId").will(returnValue(1L));
        PermissionAssociation pa = new PermissionAssociationImpl(coralCore, 
            resourceClass, permission);
        Set pas = new HashSet(1);
        pas.add(pa);
        mockCoralRegistry.stub().method("getPermissionAssociations").with(same(resourceClass)).will(returnValue(pas));
        mockCoralRegistry.expect(once()).method("deletePermissionAssociation").with(eq(pa));
        coralSecurity.deletePermission(resourceClass, permission);        
    }
    
    public void testGrantPermission()
        throws Exception
    {
        mockCoralRegistry.stub().method("getRole").with(eq(1L)).will(returnValue(rootRole));
        mockRootSubject.stub().method("hasRole").with(same(rootRole)).will(returnValue(true));
        mockResource.stub().method("getResourceClass").will(returnValue(resourceClass));
        mockResourceClass.stub().method("isAssociatedWith").with(same(permission)).will(returnValue(true));
        mockResource.stub().method("getId").will(returnValue(1L));
        mockRole.stub().method("getId").will(returnValue(2L));
        mockPermission.stub().method("getId").will(returnValue(1L));
        PermissionAssignment pa = new PermissionAssignmentImpl(coralCore,
            rootSubject, resource, role, permission, true);
        mockCoralRegistry.expect(once()).method("addPermissionAssignment").with(eq(pa));
        coralSecurity.grant(resource, role, permission, true, rootSubject);
    }
    
    public void testRevokePermission()
        throws Exception
    {
        mockRole.stub().method("hasPermission").with(same(resource), same(permission)).will(returnValue(true));
        mockCoralRegistry.stub().method("getRole").with(eq(1L)).will(returnValue(rootRole));
        mockRootSubject.stub().method("hasRole").with(same(rootRole)).will(returnValue(true));
        mockResource.stub().method("getId").will(returnValue(1L));
        mockRole.stub().method("getId").will(returnValue(2L));
        mockPermission.stub().method("getId").will(returnValue(1L));
        PermissionAssignment pa = new PermissionAssignmentImpl(coralCore,
            rootSubject, resource, role, permission, false);
        mockCoralRegistry.expect(once()).method("deletePermissionAssignment").with(eq(pa));
        coralSecurity.revoke(resource, role, permission, rootSubject);
    }
}
