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
import org.jmock.Mock;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: CoralEventLoggerTest.java,v 1.5 2004-05-28 10:04:11 fil Exp $
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
        mockCoralEventHub = mock(CoralEventHub.class);
        coralEventHub = (CoralEventHub)mockCoralEventHub.proxy();
        mockCoralEventWhiteboard = mock(CoralEventWhiteboard.class);
        coralEventWhiteboard = (CoralEventWhiteboard)mockCoralEventWhiteboard.proxy();
        mockLogger = mock(Logger.class);
        logger = (Logger)mockLogger.proxy();
        mockCoralEventHub.expects(once()).method("getGlobal").will(returnValue(coralEventWhiteboard));
        mockCoralEventWhiteboard.expects(atLeastOnce()).method(stringContains("add"));
        coralEventLogger = new CoralEventLogger(coralEventHub, logger);
    }    
    
    public void testPermissionAssociationsChanged()
    {
        mockPermissionAssociation.expects(once()).method("getResourceClass").will(returnValue(resourceClass));
        mockResourceClass.expects(once()).method("getId").will(returnValue(1L));
        mockPermissionAssociation.expects(once()).method("getPermission").will(returnValue(permission));
        mockPermission.expects(once()).method("getId").will(returnValue(2L));
        
        mockLogger.expects(once()).method("info").with(eq("Coral event: PermissionAssociationChange(#1, #2, true)"));
        coralEventLogger.permissionsChanged(permissionAssociation, true);
    }
    
    public void testPermissionAssignmentsChanged()
    {
        mockPermissionAssignment.expects(once()).method("getResource").will(returnValue(resource));
        mockResource.expects(once()).method("getId").will(returnValue(1L));
        mockPermissionAssignment.expects(once()).method("getRole").will(returnValue(role));
        mockRole.expects(once()).method("getId").will(returnValue(2L));
        mockPermissionAssignment.expects(once()).method("getPermission").will(returnValue(permission));
        mockPermission.expects(once()).method("getId").will(returnValue(3L));
        
        mockLogger.expects(once()).method("info").with(eq("Coral event: PermissionAssignmentChange(#1, #2, #3, true)"));
        coralEventLogger.permissionsChanged(permissionAssignment, true);
    }
    
    public void testRoleAssignmetsChanged()
    {
        mockRoleAssignment.expects(once()).method("getSubject").will(returnValue(subject));
        mockSubject.expects(once()).method("getId").will(returnValue(1L));
        mockRoleAssignment.expects(once()).method("getRole").will(returnValue(role));
        mockRole.expects(once()).method("getId").will(returnValue(2L));
        
        mockLogger.expects(once()).method("info").with(eq("Coral event: RoleAssignmentChange(#1, #2, true)"));
        coralEventLogger.rolesChanged(roleAssignment, true);
    }
    
    public void testRoleImplicationsChanged()
    {
        mockRoleImplication.expects(once()).method("getSuperRole").will(returnValue(parentRole));
        mockRoleImplication.expects(once()).method("getSubRole").will(returnValue(childRole));
        mockParentRole.expects(once()).method("getId").will(returnValue(1L));
        mockChildRole.expects(once()).method("getId").will(returnValue(2L));
        
        mockLogger.expects(once()).method("info").with(eq("Coral event: RoleImplicationChange(#1, #2, true)"));
        coralEventLogger.roleChanged(roleImplication, true);
    }
    
    public void testResourceClassInheritanceChanged()
    {
        mockResourceClassInheritance.expects(once()).method("getParent").will(returnValue(parentResourceClass));
        mockResourceClassInheritance.expects(once()).method("getChild").will(returnValue(childResourceClass));
        mockParentResourceClass.expects(once()).method("getId").will(returnValue(1L));
        mockChildResourceClass.expects(once()).method("getId").will(returnValue(2L));
        
        mockLogger.expects(once()).method("info").with(eq("Coral event: ResourceClassInheritanceChange(#1, #2, true)"));
        coralEventLogger.inheritanceChanged(resourceClassInheritance, true);
    }
    
    public void testResourceClassAttributesChanged()
    {
        mockAttributeDefinition.expects(once()).method("getId").will(returnValue(1L));
        
        mockLogger.expects(once()).method("info").with(eq("Coral event: ResourceClassAttributesChange(#1, true)"));
        coralEventLogger.attributesChanged(attributeDefinition, true);        
    }
    
    public void testResourceTreeChanged()
    {
        mockResourceInheritance.expects(once()).method("getParent").will(returnValue(parentResource));
        mockResourceInheritance.expects(once()).method("getChild").will(returnValue(childResource));
        mockParentResource.expects(once()).method("getId").will(returnValue(1L));
        mockChildResource.expects(once()).method("getId").will(returnValue(2L));

        mockLogger.expects(once()).method("info").with(eq("Coral event: ResourceTreeChange(#1, #2, true)"));
        coralEventLogger.resourceTreeChanged(resourceInheritance, true);        
    }
    
    public void testResourceOwnershipChanged()
    {
        mockResourceOwnership.expects(once()).method("getOwner").will(returnValue(subject));
        mockSubject.expects(once()).method("getId").will(returnValue(1L));
        mockResourceOwnership.expects(once()).method("getResource").will(returnValue(resource));
        mockResource.expects(once()).method("getId").will(returnValue(2L));

        mockLogger.expects(once()).method("info").with(eq("Coral event: ResourceOwnershipChange(#1, #2, true)"));
        coralEventLogger.resourceOwnershipChanged(resourceOwnership, true);        
    }
    
    public void testSubjectChanged()
    {
        mockSubject.expects(once()).method("getId").will(returnValue(1L));
        
        mockLogger.expects(once()).method("info").with(eq("Coral event: SubjectChange(#1, false)"));
        coralEventLogger.subjectChanged(subject);                
    }

    public void testRoleChanged()
    {
        mockRole.expects(once()).method("getId").will(returnValue(1L));
        
        mockLogger.expects(once()).method("info").with(eq("Coral event: RoleChange(#1, false)"));
        coralEventLogger.roleChanged(role);                
    }

    public void testPermissionChanged()
    {
        mockPermission.expects(once()).method("getId").will(returnValue(1L));
        
        mockLogger.expects(once()).method("info").with(eq("Coral event: PermissionChange(#1, false)"));
        coralEventLogger.permissionChanged(permission);                
    }

    public void testResourceCreated()
    {
        mockResource.expects(once()).method("getId").will(returnValue(1L));
        
        mockLogger.expects(once()).method("info").with(eq("Coral event: ResourceCreation(#1, false)"));
        coralEventLogger.resourceCreated(resource);                
    }

    public void testResourceChanged()
    {
        mockResource.expects(once()).method("getId").will(returnValue(1L));
        mockSubject.expects(once()).method("getId").will(returnValue(2L));
        
        mockLogger.expects(once()).method("info").with(eq("Coral event: ResourceChange(#1, #2, false)"));
        coralEventLogger.resourceChanged(resource, subject);                
    }

    public void testResourceDeleted()
    {
        mockResource.expects(once()).method("getId").will(returnValue(1L));
        
        mockLogger.expects(once()).method("info").with(eq("Coral event: ResourceDeletion(#1, false)"));
        coralEventLogger.resourceDeleted(resource);                
    }

    public void testResourceTreeDeleted()
    {
        mockResource.expects(once()).method("getId").will(returnValue(1L));
        
        mockLogger.expects(once()).method("info").with(eq("Coral event: ResourceTreeDeletion(#1, false)"));
        coralEventLogger.resourceTreeDeleted(resource);                
    }
    
    public void testResourceClassChanged()
    {
        mockResourceClass.expects(once()).method("getId").will(returnValue(1L));
        
        mockLogger.expects(once()).method("info").with(eq("Coral event: ResourceClassChange(#1, false)"));
        coralEventLogger.resourceClassChanged(resourceClass);                
    }

    public void testAttributeClassChanged()
    {
        mockAttributeClass.expects(once()).method("getId").will(returnValue(1L));
        
        mockLogger.expects(once()).method("info").with(eq("Coral event: AttributeClassChange(#1, false)"));
        coralEventLogger.attributeClassChanged(attributeClass);                
    }

    public void testAttributeDefinitionChanged()
    {
        mockAttributeDefinition.expects(once()).method("getId").will(returnValue(1L));
        
        mockLogger.expects(once()).method("info").with(eq("Coral event: AttributeDefinitionChange(#1, false)"));
        coralEventLogger.attributeDefinitionChanged(attributeDefinition);                
    }    
}
