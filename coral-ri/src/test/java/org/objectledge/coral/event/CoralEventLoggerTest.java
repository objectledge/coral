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

import org.jcontainer.dna.Logger;
import org.jmock.builder.Mock;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: CoralEventLoggerTest.java,v 1.3 2004-03-01 15:34:53 fil Exp $
 */
public class CoralEventLoggerTest 
    extends CoralEventTestCase
{
    private Mock mockCoralEventHub;
    private CoralEventHub coralEventHub;
    private Mock mockCoralEventWhiteboard;
    private CoralEventWhiteboard coralEventWhiteboard;
    
    private Mock mockLogger;
    private Logger logger;
    
    private CoralEventLogger coralEventLogger;
    
    public void setUp()
        throws Exception
    {
        super.setUp();
        mockCoralEventHub = new Mock(CoralEventHub.class);
        coralEventHub = (CoralEventHub)mockCoralEventHub.proxy();
        mockCoralEventWhiteboard = new Mock(CoralEventWhiteboard.class);
        coralEventWhiteboard = (CoralEventWhiteboard)mockCoralEventWhiteboard.proxy();
        mockLogger = new Mock(Logger.class);
        logger = (Logger)mockLogger.proxy();
        mockCoralEventHub.expect(once()).method("getGlobal").will(returnValue(coralEventWhiteboard));
        mockCoralEventWhiteboard.expect(atLeastOnce()).method(stringContains("add"));
        coralEventLogger = new CoralEventLogger(coralEventHub, logger);
    }    
    
    public void testPermissionAssociationsChanged()
    {
        mockPermissionAssociation.expect(once()).method("getResourceClass").will(returnValue(resourceClass));
        mockResourceClass.expect(once()).method("getId").will(returnValue(1L));
        mockPermissionAssociation.expect(once()).method("getPermission").will(returnValue(permission));
        mockPermission.expect(once()).method("getId").will(returnValue(2L));
        
        mockLogger.expect(once()).method("info").with(eq("Coral event: PermissionAssociationChange(#1, #2, true)"));
        coralEventLogger.permissionsChanged(permissionAssociation, true);
    }
    
    public void testPermissionAssignmentsChanged()
    {
        mockPermissionAssignment.expect(once()).method("getResource").will(returnValue(resource));
        mockResource.expect(once()).method("getId").will(returnValue(1L));
        mockPermissionAssignment.expect(once()).method("getRole").will(returnValue(role));
        mockRole.expect(once()).method("getId").will(returnValue(2L));
        mockPermissionAssignment.expect(once()).method("getPermission").will(returnValue(permission));
        mockPermission.expect(once()).method("getId").will(returnValue(3L));
        
        mockLogger.expect(once()).method("info").with(eq("Coral event: PermissionAssignmentChange(#1, #2, #3, true)"));
        coralEventLogger.permissionsChanged(permissionAssignment, true);
    }
    
    public void testRoleAssignmetsChanged()
    {
        mockRoleAssignment.expect(once()).method("getSubject").will(returnValue(subject));
        mockSubject.expect(once()).method("getId").will(returnValue(1L));
        mockRoleAssignment.expect(once()).method("getRole").will(returnValue(role));
        mockRole.expect(once()).method("getId").will(returnValue(2L));
        
        mockLogger.expect(once()).method("info").with(eq("Coral event: RoleAssignmentChange(#1, #2, true)"));
        coralEventLogger.rolesChanged(roleAssignment, true);
    }
    
    public void testRoleImplicationsChanged()
    {
        mockRoleImplication.expect(once()).method("getSuperRole").will(returnValue(parentRole));
        mockRoleImplication.expect(once()).method("getSubRole").will(returnValue(childRole));
        mockParentRole.expect(once()).method("getId").will(returnValue(1L));
        mockChildRole.expect(once()).method("getId").will(returnValue(2L));
        
        mockLogger.expect(once()).method("info").with(eq("Coral event: RoleImplicationChange(#1, #2, true)"));
        coralEventLogger.roleChanged(roleImplication, true);
    }
    
    public void testResourceClassInheritanceChanged()
    {
        mockResourceClassInheritance.expect(once()).method("getParent").will(returnValue(parentResourceClass));
        mockResourceClassInheritance.expect(once()).method("getChild").will(returnValue(childResourceClass));
        mockParentResourceClass.expect(once()).method("getId").will(returnValue(1L));
        mockChildResourceClass.expect(once()).method("getId").will(returnValue(2L));
        
        mockLogger.expect(once()).method("info").with(eq("Coral event: ResourceClassInheritanceChange(#1, #2, true)"));
        coralEventLogger.inheritanceChanged(resourceClassInheritance, true);
    }
    
    public void testResourceClassAttributesChanged()
    {
        mockAttributeDefinition.expect(once()).method("getId").will(returnValue(1L));
        
        mockLogger.expect(once()).method("info").with(eq("Coral event: ResourceClassAttributesChange(#1, true)"));
        coralEventLogger.attributesChanged(attributeDefinition, true);        
    }
    
    public void testResourceTreeChanged()
    {
        mockResourceInheritance.expect(once()).method("getParent").will(returnValue(parentResource));
        mockResourceInheritance.expect(once()).method("getChild").will(returnValue(childResource));
        mockParentResource.expect(once()).method("getId").will(returnValue(1L));
        mockChildResource.expect(once()).method("getId").will(returnValue(2L));

        mockLogger.expect(once()).method("info").with(eq("Coral event: ResourceTreeChange(#1, #2, true)"));
        coralEventLogger.resourceTreeChanged(resourceInheritance, true);        
    }
    
    public void testResourceOwnershipChanged()
    {
        mockResourceOwnership.expect(once()).method("getOwner").will(returnValue(subject));
        mockSubject.expect(once()).method("getId").will(returnValue(1L));
        mockResourceOwnership.expect(once()).method("getResource").will(returnValue(resource));
        mockResource.expect(once()).method("getId").will(returnValue(2L));

        mockLogger.expect(once()).method("info").with(eq("Coral event: ResourceOwnershipChange(#1, #2, true)"));
        coralEventLogger.resourceOwnershipChanged(resourceOwnership, true);        
    }
    
    public void testSubjectChanged()
    {
        mockSubject.expect(once()).method("getId").will(returnValue(1L));
        
        mockLogger.expect(once()).method("info").with(eq("Coral event: SubjectChange(#1, false)"));
        coralEventLogger.subjectChanged(subject);                
    }

    public void testRoleChanged()
    {
        mockRole.expect(once()).method("getId").will(returnValue(1L));
        
        mockLogger.expect(once()).method("info").with(eq("Coral event: RoleChange(#1, false)"));
        coralEventLogger.roleChanged(role);                
    }

    public void testPermissionChanged()
    {
        mockPermission.expect(once()).method("getId").will(returnValue(1L));
        
        mockLogger.expect(once()).method("info").with(eq("Coral event: PermissionChange(#1, false)"));
        coralEventLogger.permissionChanged(permission);                
    }

    public void testResourceCreated()
    {
        mockResource.expect(once()).method("getId").will(returnValue(1L));
        
        mockLogger.expect(once()).method("info").with(eq("Coral event: ResourceCreation(#1, false)"));
        coralEventLogger.resourceCreated(resource);                
    }

    public void testResourceChanged()
    {
        mockResource.expect(once()).method("getId").will(returnValue(1L));
        mockSubject.expect(once()).method("getId").will(returnValue(2L));
        
        mockLogger.expect(once()).method("info").with(eq("Coral event: ResourceChange(#1, #2, false)"));
        coralEventLogger.resourceChanged(resource, subject);                
    }

    public void testResourceDeleted()
    {
        mockResource.expect(once()).method("getId").will(returnValue(1L));
        
        mockLogger.expect(once()).method("info").with(eq("Coral event: ResourceDeletion(#1, false)"));
        coralEventLogger.resourceDeleted(resource);                
    }

    public void testResourceTreeDeleted()
    {
        mockResource.expect(once()).method("getId").will(returnValue(1L));
        
        mockLogger.expect(once()).method("info").with(eq("Coral event: ResourceTreeDeletion(#1, false)"));
        coralEventLogger.resourceTreeDeleted(resource);                
    }
    
    public void testResourceClassChanged()
    {
        mockResourceClass.expect(once()).method("getId").will(returnValue(1L));
        
        mockLogger.expect(once()).method("info").with(eq("Coral event: ResourceClassChange(#1, false)"));
        coralEventLogger.resourceClassChanged(resourceClass);                
    }

    public void testAttributeClassChanged()
    {
        mockAttributeClass.expect(once()).method("getId").will(returnValue(1L));
        
        mockLogger.expect(once()).method("info").with(eq("Coral event: AttributeClassChange(#1, false)"));
        coralEventLogger.attributeClassChanged(attributeClass);                
    }

    public void testAttributeDefinitionChanged()
    {
        mockAttributeDefinition.expect(once()).method("getId").will(returnValue(1L));
        
        mockLogger.expect(once()).method("info").with(eq("Coral event: AttributeDefinitionChange(#1, false)"));
        coralEventLogger.attributeDefinitionChanged(attributeDefinition);                
    }    
}
