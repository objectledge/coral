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
import org.jmock.builder.Mock;
import org.jmock.builder.MockObjectTestCase;
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

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: CoralRegistryTest.java,v 1.4 2004-03-05 11:52:17 fil Exp $
 */
public class CoralRegistryTest extends MockObjectTestCase
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
        mockDatabase = new Mock(Database.class);
        database = (Database)mockDatabase.proxy();
        mockPersistence = new Mock(Persistence.class);
        persistence = (Persistence)mockPersistence.proxy();
        mockCacheFactory = new Mock(CacheFactory.class);
        cacheFactory = (CacheFactory)mockCacheFactory.proxy();
        mockCoralEventHub = new Mock(CoralEventHub.class);
        coralEventHub = (CoralEventHub)mockCoralEventHub.proxy();
        mockCoralEventWhiteboard = new Mock(CoralEventWhiteboard.class);
        coralEventWhiteboard = (CoralEventWhiteboard)mockCoralEventWhiteboard.proxy();
        mockCoralSchema = new Mock(CoralSchema.class);
        coralSchema = (CoralSchema)mockCoralSchema.proxy();
        mockCoralSecurity = new Mock(CoralSecurity.class);
        coralSecurity = (CoralSecurity)mockCoralSecurity.proxy();
        mockCoralStore = new Mock(CoralStore.class);
        coralStore = (CoralStore)mockCoralStore.proxy();
        mockCoralCore = new Mock(CoralCore.class);
        coralCore = (CoralCore)mockCoralCore.proxy();
        mockCoralCore.stub().method("getSchema").will(returnValue(coralSchema));
        mockCoralCore.stub().method("getSecurity").will(returnValue(coralSecurity));
        mockCoralCore.stub().method("getStore").will(returnValue(coralStore));
        mockInstantiator = new Mock(Instantiator.class);
        instantiator = (Instantiator)mockInstantiator.proxy();
        
        mockResourceClassInheritanceImplPersistentFactory = new Mock(PersistentFactory.class, "mockResourceClassInheritanceImplPersistentFactory");
        mockResourceClassInheritanceImplPersistentFactory.stub().method("newInstance").
            will(returnValue(new ResourceClassInheritanceImpl(coralCore)));
        mockInstantiator.stub().method("getPersistentFactory").with(eq(ResourceClassInheritanceImpl.class)).
            will(returnValue(mockResourceClassInheritanceImplPersistentFactory.proxy()));
        mockAttributeDefinitionImplPersistentFactory = new Mock(PersistentFactory.class, "mockAttributeDefinitionImplPersistentFactory");
        mockAttributeDefinitionImplPersistentFactory.stub().method("newInstance").
            will(returnValue(new AttributeDefinitionImpl(persistence, coralEventHub, coralCore)));
        mockInstantiator.stub().method("getPersistentFactory").with(eq(AttributeDefinitionImpl.class)).
            will(returnValue(mockAttributeDefinitionImplPersistentFactory.proxy()));
        mockRoleImplicationImplPersistentFactory = new Mock(PersistentFactory.class, "mockRoleImplicationImplImplPersistentFactory");
        mockRoleImplicationImplPersistentFactory.stub().method("newInstance").
            will(returnValue(new RoleImplicationImpl(coralCore)));
        mockInstantiator.stub().method("getPersistentFactory").with(eq(RoleImplicationImpl.class)).
            will(returnValue(mockRoleImplicationImplPersistentFactory.proxy()));
        mockRoleAssignmentImplPersistentFactory = new Mock(PersistentFactory.class, "mockRoleAssignmentImplPersistentFactory");
        mockRoleAssignmentImplPersistentFactory.stub().method("newInstance").
            will(returnValue(new RoleAssignmentImpl(coralCore)));
        mockInstantiator.stub().method("getPersistentFactory").with(eq(RoleAssignmentImpl.class)).
            will(returnValue(mockRoleAssignmentImplPersistentFactory.proxy()));
        mockPermissionAssignmentImplPersistentFactory = new Mock(PersistentFactory.class, "mockPermissionAssociationImplPersistentFactory");
        mockPermissionAssignmentImplPersistentFactory.stub().method("newInstance").
            will(returnValue(new PermissionAssignmentImpl(coralCore)));
        mockInstantiator.stub().method("getPersistentFactory").with(eq(PermissionAssignmentImpl.class)).
            will(returnValue(mockPermissionAssignmentImplPersistentFactory.proxy()));
        mockPermissionAssociationImplPersistentFactory = new Mock(PersistentFactory.class, "mockPermissionAssignmentImplPersistentFactory");
        mockPermissionAssociationImplPersistentFactory.stub().method("newInstance").
            will(returnValue(new PermissionAssociationImpl(coralCore)));
        mockInstantiator.stub().method("getPersistentFactory").with(eq(PermissionAssociationImpl.class)).
            will(returnValue(mockPermissionAssociationImplPersistentFactory.proxy()));

        mockSubjectImplPersistentFactory = new Mock(PersistentFactory.class, "mockSubjectImplPersistentFactory");
        mockSubjectImplPersistentFactory.stub().method("newInstance").
            will(returnValue(new SubjectImpl(persistence, coralEventHub, coralCore)));
        mockInstantiator.stub().method("getPersistentFactory").with(eq(SubjectImpl.class)).
            will(returnValue(mockSubjectImplPersistentFactory.proxy()));

        mockRoleImplPersistentFactory = new Mock(PersistentFactory.class, "mockRoleImplPersistentFactory");
        mockRoleImplPersistentFactory.stub().method("newInstance").
            will(returnValue(new RoleImpl(persistence, coralEventHub, coralCore)));
        mockInstantiator.stub().method("getPersistentFactory").with(eq(RoleImpl.class)).
            will(returnValue(mockRoleImplPersistentFactory.proxy()));

        mockPermissionImplPersistentFactory = new Mock(PersistentFactory.class, "mockPermissionImplPersistentFactory");
        mockPermissionImplPersistentFactory.stub().method("newInstance").
            will(returnValue(new PermissionImpl(persistence, coralEventHub, coralCore)));
        mockInstantiator.stub().method("getPersistentFactory").with(eq(PermissionImpl.class)).
            will(returnValue(mockPermissionImplPersistentFactory.proxy()));

        mockResourceClassImplPersistentFactory = new Mock(PersistentFactory.class, "mockResourceClassImplPersistentFactory");
        mockResourceClassImplPersistentFactory.stub().method("newInstance").
            will(returnValue(new ResourceClassImpl(persistence, instantiator, coralEventHub, coralCore)));
        mockInstantiator.stub().method("getPersistentFactory").with(eq(ResourceClassImpl.class)).
            will(returnValue(mockResourceClassImplPersistentFactory.proxy()));

        mockAttributeClassImplPeristentFactory = new Mock(PersistentFactory.class, "mockAttributeClassImplPersistentFactory");
        mockAttributeClassImplPeristentFactory.stub().method("newInstance").
            will(returnValue(new AttributeClassImpl(persistence, instantiator, coralEventHub)));
        mockInstantiator.stub().method("getPersistentFactory").with(eq(AttributeClassImpl.class)).
            will(returnValue(mockAttributeClassImplPeristentFactory.proxy()));

        mockLogger = new Mock(Logger.class);        
        logger = (Logger)mockLogger.proxy();
    }
    
    private CoralRegistry createRegistry()
        throws Exception
    {
        mockCacheFactory.stub().method("getInstance").will(returnValue(new HashMap()));
        mockCoralEventHub.stub().method("getInbound").will(returnValue(coralEventWhiteboard));
        mockCoralEventWhiteboard.expect(once()).method("addPermissionAssociationChangeListener").with(ANYTHING, NULL);
        mockCoralEventWhiteboard.expect(once()).method("addPermissionAssignmentChangeListener").with(ANYTHING, NULL);
        mockCoralEventWhiteboard.expect(once()).method("addRoleAssignmentChangeListener").with(ANYTHING, NULL);
        mockCoralEventWhiteboard.expect(once()).method("addRoleImplicationChangeListener").with(ANYTHING, NULL);
        mockCoralEventWhiteboard.expect(once()).method("addResourceClassInheritanceChangeListener").with(ANYTHING, NULL);
        mockCoralEventWhiteboard.expect(once()).method("addResourceClassAttributesChangeListener").with(ANYTHING, NULL);
        return new CoralRegistryImpl(database, persistence, cacheFactory, coralEventHub, coralCore, 
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
