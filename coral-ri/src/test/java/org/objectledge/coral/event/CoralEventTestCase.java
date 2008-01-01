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
package org.objectledge.coral.event;

import org.jmock.Mock;
import org.objectledge.cache.CacheFactory;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.schema.ResourceClassInheritance;
import org.objectledge.coral.security.Permission;
import org.objectledge.coral.security.PermissionAssignment;
import org.objectledge.coral.security.PermissionAssociation;
import org.objectledge.coral.security.Role;
import org.objectledge.coral.security.RoleAssignment;
import org.objectledge.coral.security.RoleImplication;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ResourceInheritance;
import org.objectledge.coral.store.ResourceOwnership;
import org.objectledge.utils.LedgeTestCase;

/**
 * An abstract test case that mocks a full set of objects used by event related classes.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: CoralEventTestCase.java,v 1.5 2008-01-01 23:32:43 rafal Exp $
 */
public abstract class CoralEventTestCase
    extends LedgeTestCase
{
    protected Mock mockPermissionAssociationChangeListener;
    protected PermissionAssociationChangeListener permissionAssoicationChangeListener;
    protected Mock mockPermissionAssociation;
    protected PermissionAssociation permissionAssociation;

    protected Mock mockPermissionAssignmentChangeListener;
    protected PermissionAssignmentChangeListener permissionAssignmentChangeListener;
    protected Mock mockPermissionAssignment;
    protected PermissionAssignment permissionAssignment;

    protected Mock mockRoleAssignmentChangeListener;
    protected RoleAssignmentChangeListener roleAssignmentChangeListener;
    protected Mock mockRoleAssignment;
    protected RoleAssignment roleAssignment;    

    protected Mock mockRoleImplicationChangeListener;
    protected RoleImplicationChangeListener roleImplicationChangeListener;
    protected Mock mockRoleImplication;
    protected RoleImplication roleImplication;    

    protected Mock mockResourceClassInheritanceChangeListener;
    protected ResourceClassInheritanceChangeListener resourceClassInheritanceChangeListener;
    protected Mock mockResourceClassInheritance;
    protected ResourceClassInheritance resourceClassInheritance;    
    
    protected Mock mockResourceClassAttributesChangeListener;
    protected ResourceClassAttributesChangeListener resourceClassAttributesChangeListener;
    protected Mock mockAttributeDefinition;
    protected AttributeDefinition attributeDefinition;    
    
    protected Mock mockResourceTreeChangeListener;
    protected ResourceTreeChangeListener resourceTreeChangeListener;
    protected Mock mockResourceInheritance;
    protected ResourceInheritance resourceInheritance;
    
    protected Mock mockResourceOwnershipChangeListener;
    protected ResourceOwnershipChangeListener resourceOwnershipChangeListener;
    protected Mock mockResourceOwnership; 
    protected ResourceOwnership resourceOwnership;
    
    protected Mock mockSubjectChangeListener;
    protected SubjectChangeListener subjectChangeListener;
    protected Mock mockSubject;
    protected Subject subject;
    
    protected Mock mockRoleChangeListener;
    protected RoleChangeListener roleChangeListener;
    protected Mock mockRole;
    protected Role role;
    protected Mock mockParentRole;
    protected Role parentRole;
    protected Mock mockChildRole;
    protected Role childRole;
    
    protected Mock mockPermissionChangeListener;
    protected PermissionChangeListener permissionChangeListener;
    protected Mock mockPermission;
    protected Permission permission; 
    
    protected Mock mockResourceCreationListener;
    protected ResourceCreationListener resourceCreationListener;
    protected Mock mockResourceChangeListener;
    protected ResourceChangeListener resourceChangeListener;
    protected Mock mockResourceDeletionListener;
    protected ResourceDeletionListener resourceDeletionListener;
    protected Mock mockResourceTreeDeletionListener;
    protected ResourceTreeDeletionListener resourceTreeDeletionListener;
    protected Mock mockResource;
    protected Resource resource;
    protected Mock mockParentResource;
    protected Resource parentResource;
    protected Mock mockChildResource;
    protected Resource childResource;
    
    protected Mock mockResourceClassChangeListener;
    protected ResourceClassChangeListener resourceClassChangeListener;
    protected Mock mockResourceClass;
    protected ResourceClass resourceClass;
    protected Mock mockParentResourceClass;
    protected ResourceClass parentResourceClass;
    protected Mock mockChildResourceClass;
    protected ResourceClass childResourceClass;
    
    protected Mock mockAttributeClassChangeListener;
    protected AttributeClassChangeListener attributeClassChangeListener; 
    protected Mock mockAttributeClass;
    protected AttributeClass attributeClass;

    protected Mock mockAttributeDefinitionChangeListener;
    protected AttributeDefinitionChangeListener attributeDefinitionChangeListener;
    
    protected Mock mockCacheFactory;
    protected CacheFactory cacheFactory;
    
    public void setUp()
        throws Exception
    {
        mockPermissionAssociationChangeListener = mock(PermissionAssociationChangeListener.class);
        permissionAssoicationChangeListener = (PermissionAssociationChangeListener)mockPermissionAssociationChangeListener.proxy();
        mockPermissionAssociation = mock(PermissionAssociation.class);
        permissionAssociation = (PermissionAssociation)mockPermissionAssociation.proxy();

        mockPermissionAssignmentChangeListener = mock(PermissionAssignmentChangeListener.class);
        permissionAssignmentChangeListener = (PermissionAssignmentChangeListener)mockPermissionAssignmentChangeListener.proxy();
        mockPermissionAssignment = mock(PermissionAssignment.class);
        permissionAssignment = (PermissionAssignment)mockPermissionAssignment.proxy();

        mockRoleAssignmentChangeListener = mock(RoleAssignmentChangeListener.class);
        roleAssignmentChangeListener = (RoleAssignmentChangeListener)mockRoleAssignmentChangeListener.proxy();
        mockRoleAssignment = mock(RoleAssignment.class);
        roleAssignment = (RoleAssignment)mockRoleAssignment.proxy();

        mockRoleImplicationChangeListener = mock(RoleImplicationChangeListener.class);
        roleImplicationChangeListener = (RoleImplicationChangeListener)mockRoleImplicationChangeListener.proxy();
        mockRoleImplication = mock(RoleImplication.class);
        roleImplication = (RoleImplication)mockRoleImplication.proxy();

        mockResourceClassInheritanceChangeListener = mock(ResourceClassInheritanceChangeListener.class);
        resourceClassInheritanceChangeListener = (ResourceClassInheritanceChangeListener)mockResourceClassInheritanceChangeListener.proxy();
        mockResourceClassInheritance = mock(ResourceClassInheritance.class);
        resourceClassInheritance = (ResourceClassInheritance)mockResourceClassInheritance.proxy();

        mockResourceClassAttributesChangeListener = mock(ResourceClassAttributesChangeListener.class);
        resourceClassAttributesChangeListener = (ResourceClassAttributesChangeListener)mockResourceClassAttributesChangeListener.proxy();
        mockAttributeDefinition = mock(AttributeDefinition.class);
        attributeDefinition = (AttributeDefinition)mockAttributeDefinition.proxy();
        
        mockResourceTreeChangeListener = mock(ResourceTreeChangeListener.class);
        resourceTreeChangeListener = (ResourceTreeChangeListener)mockResourceTreeChangeListener.proxy();
        mockResourceInheritance = mock(ResourceInheritance.class);
        resourceInheritance = (ResourceInheritance)mockResourceInheritance.proxy();
        
        mockResourceOwnershipChangeListener = mock(ResourceOwnershipChangeListener.class);
        resourceOwnershipChangeListener = (ResourceOwnershipChangeListener)mockResourceOwnershipChangeListener.proxy();
        mockResourceOwnership = mock(ResourceOwnership.class);
        resourceOwnership = (ResourceOwnership)mockResourceOwnership.proxy();
        
        mockSubjectChangeListener = mock(SubjectChangeListener.class);
        subjectChangeListener = (SubjectChangeListener)mockSubjectChangeListener.proxy();
        mockSubject = mock(Subject.class);
        subject = (Subject)mockSubject.proxy();
        
        mockRoleChangeListener = mock(RoleChangeListener.class);
        roleChangeListener = (RoleChangeListener)mockRoleChangeListener.proxy();
        mockRole = mock(Role.class);
        role = (Role)mockRole.proxy();
        mockParentRole = mock(Role.class);
        parentRole = (Role)mockParentRole.proxy();
        mockChildRole = mock(Role.class);
        childRole = (Role)mockChildRole.proxy();

        mockPermissionChangeListener = mock(PermissionChangeListener.class);
        permissionChangeListener = (PermissionChangeListener)mockPermissionChangeListener.proxy();
        mockPermission = mock(Permission.class);
        permission = (Permission)mockPermission.proxy();
        
        mockResourceCreationListener = mock(ResourceCreationListener.class);
        resourceCreationListener = (ResourceCreationListener)mockResourceCreationListener.proxy();
        mockResourceChangeListener = mock(ResourceChangeListener.class);
        resourceChangeListener = (ResourceChangeListener)mockResourceChangeListener.proxy();
        mockResourceDeletionListener = mock(ResourceDeletionListener.class);
        resourceDeletionListener = (ResourceDeletionListener)mockResourceDeletionListener.proxy();        
        mockResourceTreeDeletionListener = mock(ResourceTreeDeletionListener.class);
        resourceTreeDeletionListener = (ResourceTreeDeletionListener)mockResourceTreeDeletionListener.proxy();        
        mockResource = mock(Resource.class);
        resource = (Resource)mockResource.proxy();
        mockParentResource = mock(Resource.class);
        parentResource = (Resource)mockParentResource.proxy();
        mockChildResource = mock(Resource.class);
        childResource = (Resource)mockChildResource.proxy();
                
        mockResourceClassChangeListener = mock(ResourceClassChangeListener.class);
        resourceClassChangeListener = (ResourceClassChangeListener)mockResourceClassChangeListener.proxy();                 
        mockResourceClass = mock(ResourceClass.class);
        resourceClass = (ResourceClass)mockResourceClass.proxy();
        mockParentResourceClass = mock(ResourceClass.class);
        parentResourceClass = (ResourceClass)mockParentResourceClass.proxy();
        mockChildResourceClass = mock(ResourceClass.class);
        childResourceClass = (ResourceClass)mockChildResourceClass.proxy();
        
        mockAttributeClassChangeListener = mock(AttributeClassChangeListener.class);
        attributeClassChangeListener = (AttributeClassChangeListener)mockAttributeClassChangeListener.proxy();
        mockAttributeClass = mock(AttributeClass.class);
        attributeClass = (AttributeClass)mockAttributeClass.proxy();
        
        mockAttributeDefinitionChangeListener = mock(AttributeDefinitionChangeListener.class);
        attributeDefinitionChangeListener = (AttributeDefinitionChangeListener)mockAttributeDefinitionChangeListener.proxy();
        
        mockCacheFactory = mock(CacheFactory.class);
        mockCacheFactory.stubs().method("registerForPeriodicExpunge").isVoid();
        cacheFactory = (CacheFactory)mockCacheFactory.proxy();
    }
}
