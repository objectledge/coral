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
package org.objectledge.coral.entity;

import java.util.HashMap;

import org.jcontainer.dna.Logger;
import org.jmock.Mock;
import org.objectledge.cache.CacheFactory;
import org.objectledge.coral.CoralCore;
import org.objectledge.coral.Instantiator;
import org.objectledge.coral.event.CoralEventHub;
import org.objectledge.coral.event.CoralEventWhiteboard;
import org.objectledge.coral.schema.AttributeClassImpl;
import org.objectledge.coral.schema.AttributeDefinitionImpl;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.ResourceClassImpl;
import org.objectledge.coral.schema.ResourceClassInheritanceImpl;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.security.PermissionAssignmentImpl;
import org.objectledge.coral.security.PermissionAssociationImpl;
import org.objectledge.coral.security.PermissionImpl;
import org.objectledge.coral.security.RoleAssignmentImpl;
import org.objectledge.coral.security.RoleImpl;
import org.objectledge.coral.security.RoleImplicationImpl;
import org.objectledge.coral.security.SubjectImpl;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.database.Database;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.PersistentFactory;
import org.objectledge.utils.LedgeTestCase;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: CoralRegistryTest.java,v 1.8 2005-01-28 01:04:08 rafal Exp $
 */
public class CoralRegistryTest extends LedgeTestCase
{
    private Mock mockDatabase;
    private Database database;
    private Mock mockPersistence;
    private Persistence persistence;
    private Mock mockCacheFactory;
    private CacheFactory cacheFactory;
    private Mock mockCoralEventHub;
    private CoralEventHub coralEventHub;
    private Mock mockCoralEventWhiteboard;
    private CoralEventWhiteboard coralEventWhiteboard;
    private Mock mockCoralSchema;
    private CoralSchema coralSchema;
    private Mock mockCoralSecurity;
    private CoralSecurity coralSecurity;
    private Mock mockCoralStore;
    private CoralStore coralStore;
    private Mock mockCoralCore;
    private CoralCore coralCore;
    private Mock mockInstantiator;
    private Instantiator instantiator;
    private Mock mockResourceClassImplPersistentFactory;
    private Mock mockAttributeClassImplPeristentFactory;
    private Mock mockSubjectImplPersistentFactory;
    private Mock mockRoleImplPersistentFactory;
    private Mock mockPermissionImplPersistentFactory;
    private Mock mockResourceClassInheritanceImplPersistentFactory;
    private Mock mockAttributeDefinitionImplPersistentFactory;
    private Mock mockRoleImplicationImplPersistentFactory;
    private Mock mockRoleAssignmentImplPersistentFactory;
    private Mock mockPermissionAssociationImplPersistentFactory;
    private Mock mockPermissionAssignmentImplPersistentFactory;
    private Mock mockLogger;
    private Logger logger;
    
    public void setUp()
    {
        mockDatabase = mock(Database.class);
        database = (Database)mockDatabase.proxy();
        mockPersistence = mock(Persistence.class);
        persistence = (Persistence)mockPersistence.proxy();
        mockPersistence.stubs().method("getDatabase").will(returnValue(database));
        mockCacheFactory = mock(CacheFactory.class);
        cacheFactory = (CacheFactory)mockCacheFactory.proxy();
        mockCoralEventHub = mock(CoralEventHub.class);
        coralEventHub = (CoralEventHub)mockCoralEventHub.proxy();
        mockCoralEventWhiteboard = mock(CoralEventWhiteboard.class);
        coralEventWhiteboard = (CoralEventWhiteboard)mockCoralEventWhiteboard.proxy();
        mockCoralSchema = mock(CoralSchema.class);
        coralSchema = (CoralSchema)mockCoralSchema.proxy();
        mockCoralSecurity = mock(CoralSecurity.class);
        coralSecurity = (CoralSecurity)mockCoralSecurity.proxy();
        mockCoralStore = mock(CoralStore.class);
        coralStore = (CoralStore)mockCoralStore.proxy();
        mockCoralCore = mock(CoralCore.class);
        coralCore = (CoralCore)mockCoralCore.proxy();
        mockCoralCore.stubs().method("getSchema").will(returnValue(coralSchema));
        mockCoralCore.stubs().method("getSecurity").will(returnValue(coralSecurity));
        mockCoralCore.stubs().method("getStore").will(returnValue(coralStore));
        mockInstantiator = mock(Instantiator.class);
        instantiator = (Instantiator)mockInstantiator.proxy();
        
        mockResourceClassInheritanceImplPersistentFactory = 
            mock(PersistentFactory.class, "mockResourceClassInheritanceImplPersistentFactory");
        mockResourceClassInheritanceImplPersistentFactory.stubs().method("newInstance").
            will(returnValue(new ResourceClassInheritanceImpl(coralCore)));
        mockInstantiator.stubs().method("getPersistentFactory").
            with(eq(ResourceClassInheritanceImpl.class)).
            will(returnValue(mockResourceClassInheritanceImplPersistentFactory.proxy()));
        mockAttributeDefinitionImplPersistentFactory = 
            mock(PersistentFactory.class, "mockAttributeDefinitionImplPersistentFactory");
        mockAttributeDefinitionImplPersistentFactory.stubs().method("newInstance").
            will(returnValue(new AttributeDefinitionImpl(persistence, coralEventHub, coralCore)));
        mockInstantiator.stubs().method("getPersistentFactory").
            with(eq(AttributeDefinitionImpl.class)).
            will(returnValue(mockAttributeDefinitionImplPersistentFactory.proxy()));
        mockRoleImplicationImplPersistentFactory = 
            mock(PersistentFactory.class, "mockRoleImplicationImplImplPersistentFactory");
        mockRoleImplicationImplPersistentFactory.stubs().method("newInstance").
            will(returnValue(new RoleImplicationImpl(coralCore)));
        mockInstantiator.stubs().method("getPersistentFactory").with(eq(RoleImplicationImpl.class)).
            will(returnValue(mockRoleImplicationImplPersistentFactory.proxy()));
        mockRoleAssignmentImplPersistentFactory = 
            mock(PersistentFactory.class, "mockRoleAssignmentImplPersistentFactory");
        mockRoleAssignmentImplPersistentFactory.stubs().method("newInstance").
            will(returnValue(new RoleAssignmentImpl(coralCore)));
        mockInstantiator.stubs().method("getPersistentFactory").with(eq(RoleAssignmentImpl.class)).
            will(returnValue(mockRoleAssignmentImplPersistentFactory.proxy()));
        mockPermissionAssignmentImplPersistentFactory = 
            mock(PersistentFactory.class, "mockPermissionAssociationImplPersistentFactory");
        mockPermissionAssignmentImplPersistentFactory.stubs().method("newInstance").
            will(returnValue(new PermissionAssignmentImpl(coralCore)));
        mockInstantiator.stubs().method("getPersistentFactory").
            with(eq(PermissionAssignmentImpl.class)).
            will(returnValue(mockPermissionAssignmentImplPersistentFactory.proxy()));
        mockPermissionAssociationImplPersistentFactory = 
            mock(PersistentFactory.class, "mockPermissionAssignmentImplPersistentFactory");
        mockPermissionAssociationImplPersistentFactory.stubs().method("newInstance").
            will(returnValue(new PermissionAssociationImpl(coralCore)));
        mockInstantiator.stubs().method("getPersistentFactory").
            with(eq(PermissionAssociationImpl.class)).
            will(returnValue(mockPermissionAssociationImplPersistentFactory.proxy()));

        mockSubjectImplPersistentFactory = 
            mock(PersistentFactory.class, "mockSubjectImplPersistentFactory");
        mockSubjectImplPersistentFactory.stubs().method("newInstance").
            will(returnValue(new SubjectImpl(persistence, coralEventHub, coralCore)));
        mockInstantiator.stubs().method("getPersistentFactory").with(eq(SubjectImpl.class)).
            will(returnValue(mockSubjectImplPersistentFactory.proxy()));

        mockRoleImplPersistentFactory = 
            mock(PersistentFactory.class, "mockRoleImplPersistentFactory");
        mockRoleImplPersistentFactory.stubs().method("newInstance").
            will(returnValue(new RoleImpl(persistence, coralEventHub, coralCore)));
        mockInstantiator.stubs().method("getPersistentFactory").with(eq(RoleImpl.class)).
            will(returnValue(mockRoleImplPersistentFactory.proxy()));

        mockPermissionImplPersistentFactory = 
            mock(PersistentFactory.class, "mockPermissionImplPersistentFactory");
        mockPermissionImplPersistentFactory.stubs().method("newInstance").
            will(returnValue(new PermissionImpl(persistence, coralEventHub, coralCore)));
        mockInstantiator.stubs().method("getPersistentFactory").with(eq(PermissionImpl.class)).
            will(returnValue(mockPermissionImplPersistentFactory.proxy()));

        mockResourceClassImplPersistentFactory = 
            mock(PersistentFactory.class, "mockResourceClassImplPersistentFactory");
        mockResourceClassImplPersistentFactory.stubs().method("newInstance").
            will(returnValue(new ResourceClassImpl(persistence, instantiator, coralEventHub, 
                coralCore)));
        mockInstantiator.stubs().method("getPersistentFactory").with(eq(ResourceClassImpl.class)).
            will(returnValue(mockResourceClassImplPersistentFactory.proxy()));

        mockAttributeClassImplPeristentFactory = 
            mock(PersistentFactory.class, "mockAttributeClassImplPersistentFactory");
        mockAttributeClassImplPeristentFactory.stubs().method("newInstance").
            will(returnValue(new AttributeClassImpl(persistence, instantiator, coralEventHub)));
        mockInstantiator.stubs().method("getPersistentFactory").with(eq(AttributeClassImpl.class)).
            will(returnValue(mockAttributeClassImplPeristentFactory.proxy()));

        mockLogger = mock(Logger.class);        
        logger = (Logger)mockLogger.proxy();
    }
    
    private CoralRegistry createRegistry()
        throws Exception
    {
        mockCacheFactory.stubs().method("getInstance").will(returnValue(new HashMap()));
        mockCoralEventHub.stubs().method("getInbound").will(returnValue(coralEventWhiteboard));
        mockCoralEventWhiteboard.expects(once()).
            method("addPermissionAssociationChangeListener").with(ANYTHING, NULL);
        mockCoralEventWhiteboard.expects(once()).
            method("addPermissionAssignmentChangeListener").with(ANYTHING, NULL);
        mockCoralEventWhiteboard.expects(once()).
            method("addRoleAssignmentChangeListener").with(ANYTHING, NULL);
        mockCoralEventWhiteboard.expects(once()).
            method("addRoleImplicationChangeListener").with(ANYTHING, NULL);
        mockCoralEventWhiteboard.expects(once()).
            method("addResourceClassInheritanceChangeListener").with(ANYTHING, NULL);
        mockCoralEventWhiteboard.expects(once()).
            method("addResourceClassAttributesChangeListener").with(ANYTHING, NULL);
        return new CoralRegistryImpl(persistence, cacheFactory, coralEventHub, coralCore, 
            instantiator, logger);
    }
    
    public void testCreation()
        throws Exception
    {
        createRegistry();
    }
    
    // schema ////////////////////////////////////////////////////////////////////////////////////
    
    // getAttributeClass()
    
    // getAttributeClass(long)
    
    // getAttributeClass(String)
    
    // addAttributeClass(AttributeClass)
    
    // renameAttributeClass(AttributeClass, String)
    
    // deleteAttributeClass(AttributeClass)
    
    
    // getResourceClass()
    
    // getResourceClass(long)
    
    // getResourceClass(String)
    
    // addResourceClass(AttributeClass)
    
    // renameResourceClass(AttributeClass, String)
    
    // deleteResourceClass(AttributeClass)
  
  
    // getDeclaredAttributes(ResourceClass)
    
    // getAttributeDefinition()
    
    // getAttributeDefinition(long)
    
    // addAttributeDefinition()
    
    // renameAttributeDefinition(AttributeDefinition, String)
    
    // deleteAttributeDefinition(AttributeDefinition)
    
    
    // getResourceClassInheritance(ResourceClass)
    
    // addResourceClassInheritance(ResourceClassInheritance)
    
    // deleteResourceClassInheritance(ResourceClassInheritance)
    
    
    // security /////////////////////////////////////////////////////////////////////////////////
    
    
    // getSubject()
    
    // getSubject(long)
    
    // getSubject(String)
    
    // addSubject(Subject)
    
    // renameSubject(Subject, String)
    
    // deleteSubject(Subject)
    
    
    // getRole()
    
    // getRole(long)
    
    // getRole(String)
    
    // addRole(Role)
    
    // renameRole(Role, String)
    
    // deleteRole(Role)
    
    
    // getPermission()
    
    // getPermission(long)
    
    // getPermission(String)
    
    // addPermission(Permission)
    
    // renamePermission(Premission, String)
    
    // deletePermisson(Permission)
    
    
    // getRoleImplications(Role)
    
    // addRoleImplication(RoleImplication)
    
    // deleteRoleImplication(RoleImplication)
    
    
    // getRoleAssignments(Role)
    
    // getRoleAssignments(Subject)
    
    // addRoleAssignment(RoleAssignment)
    
    // deleteRoleAssignment(RoleAssignment)
    
    
    // getPermissionAssociations(Permission)
    
    // getPermissionAssociations(ResourceClass)
    
    // addPermissionAssociation(PermissionAssociation)
    
    // deletePermissionAssociation(PermissionAssociation)
    
    
    // getPermissionAssignments(Resource)
    
    // getPermissionAssignments(Role)
    
    // addPermissionAssignment(PermissionAssignment)
    
    // deletePermissionAssignment(PermissionAssignment)
    
    
    // getSubordinates(Subject)
    
    // getGrantedRoleAssignments(Subject)
    
    // getGrantedPermissionAssignments(Subject)
    
    // getCreatedResources(Subject)
    
    // getOwnedResources(Subject)
}
