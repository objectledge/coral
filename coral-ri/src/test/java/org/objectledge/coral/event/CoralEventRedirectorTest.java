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

import org.jmock.builder.Mock;

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: CoralEventRedirectorTest.java,v 1.1 2004-02-27 15:21:31 fil Exp $
 */
public class CoralEventRedirectorTest extends CoralEventTestCase
{
    private Mock mockInboundCoralEventWhiteboard;
    private CoralEventWhiteboard inboundCoralEventWhiteboard;
    private Mock mockOutboundCoralEventWhiteboard;
    private CoralEventWhiteboard outboundCoralEventWhiteboard;
    private Mock mockLocalCoralEventWhiteboard;
    private CoralEventWhiteboard localCoralEventWhiteboard;
    private CoralEventRedirector coralEventRedirector; 
    private Object anchor = new Object();   

    public void setUp()
    {
        super.setUp();
        mockInboundCoralEventWhiteboard = new Mock(CoralEventWhiteboard.class);
        inboundCoralEventWhiteboard = (CoralEventWhiteboard)mockInboundCoralEventWhiteboard.proxy();
        mockOutboundCoralEventWhiteboard = new Mock(CoralEventWhiteboard.class);
        outboundCoralEventWhiteboard = (CoralEventWhiteboard)mockOutboundCoralEventWhiteboard.proxy();
        mockLocalCoralEventWhiteboard = new Mock(CoralEventWhiteboard.class);
        localCoralEventWhiteboard = (CoralEventWhiteboard)mockLocalCoralEventWhiteboard.proxy();
        
        coralEventRedirector = new CoralEventRedirector(inboundCoralEventWhiteboard, 
            localCoralEventWhiteboard, outboundCoralEventWhiteboard);
    }
    
    public void testAddPermissionAssociationChangeListener()
    {
        mockInboundCoralEventWhiteboard.expect(once()).method("addPermissionAssociationChangeListener").
            with(same(permissionAssoicationChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expect(once()).method("addPermissionAssociationChangeListener").
            with(same(permissionAssoicationChangeListener), same(anchor));
        coralEventRedirector.addPermissionAssociationChangeListener(permissionAssoicationChangeListener, anchor);    
    }

    public void testRemovePermissionAssociationChangeListener()
    {
        mockInboundCoralEventWhiteboard.expect(once()).method("removePermissionAssociationChangeListener").
            with(same(permissionAssoicationChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expect(once()).method("removePermissionAssociationChangeListener").
            with(same(permissionAssoicationChangeListener), same(anchor));
        coralEventRedirector.removePermissionAssociationChangeListener(permissionAssoicationChangeListener, anchor);    
    }

    public void testFirePermissionAssociationChangeEvent()
    {
        mockOutboundCoralEventWhiteboard.expect(once()).method("firePermissionAssociationChangeEvent").
            with(same(permissionAssociation), eq(false));
        mockLocalCoralEventWhiteboard.expect(once()).method("firePermissionAssociationChangeEvent").
            with(same(permissionAssociation), eq(false));
        coralEventRedirector.firePermissionAssociationChangeEvent(permissionAssociation, false);            
    }
    
    public void testAddPermissionAssignmentChangeListener()
    {
        mockInboundCoralEventWhiteboard.expect(once()).method("addPermissionAssignmentChangeListener").
            with(same(permissionAssignmentChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expect(once()).method("addPermissionAssignmentChangeListener").
            with(same(permissionAssignmentChangeListener), same(anchor));
        coralEventRedirector.addPermissionAssignmentChangeListener(permissionAssignmentChangeListener, anchor);    
    }

    public void testRemovePermissionAssignmentChangeListener()
    {
        mockInboundCoralEventWhiteboard.expect(once()).method("removePermissionAssignmentChangeListener").
            with(same(permissionAssignmentChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expect(once()).method("removePermissionAssignmentChangeListener").
            with(same(permissionAssignmentChangeListener), same(anchor));
        coralEventRedirector.removePermissionAssignmentChangeListener(permissionAssignmentChangeListener, anchor);    
    }

    public void testFirePermissionAssignmentChangeEvent()
    {
        mockOutboundCoralEventWhiteboard.expect(once()).method("firePermissionAssignmentChangeEvent").
            with(same(permissionAssignment), eq(false));
        mockLocalCoralEventWhiteboard.expect(once()).method("firePermissionAssignmentChangeEvent").
            with(same(permissionAssignment), eq(false));
        coralEventRedirector.firePermissionAssignmentChangeEvent(permissionAssignment, false);            
    }

    public void testAddRoleAssignmentChangeListener()
    {
        mockInboundCoralEventWhiteboard.expect(once()).method("addRoleAssignmentChangeListener").
            with(same(roleAssignmentChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expect(once()).method("addRoleAssignmentChangeListener").
            with(same(roleAssignmentChangeListener), same(anchor));
        coralEventRedirector.addRoleAssignmentChangeListener(roleAssignmentChangeListener, anchor);    
    }

    public void testRemoveRoleAssignmentChangeListener()
    {
        mockInboundCoralEventWhiteboard.expect(once()).method("removeRoleAssignmentChangeListener").
            with(same(roleAssignmentChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expect(once()).method("removeRoleAssignmentChangeListener").
            with(same(roleAssignmentChangeListener), same(anchor));
        coralEventRedirector.removeRoleAssignmentChangeListener(roleAssignmentChangeListener, anchor);    
    }

    public void testFireRoleAssignmentChangeEvent()
    {
        mockOutboundCoralEventWhiteboard.expect(once()).method("fireRoleAssignmentChangeEvent").
            with(same(roleAssignment), eq(false));
        mockLocalCoralEventWhiteboard.expect(once()).method("fireRoleAssignmentChangeEvent").
            with(same(roleAssignment), eq(false));
        coralEventRedirector.fireRoleAssignmentChangeEvent(roleAssignment, false);            
    }
    
    public void testAddRoleImplicationChangeListener()
    {
        mockInboundCoralEventWhiteboard.expect(once()).method("addRoleImplicationChangeListener").
            with(same(roleImplicationChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expect(once()).method("addRoleImplicationChangeListener").
            with(same(roleImplicationChangeListener), same(anchor));
        coralEventRedirector.addRoleImplicationChangeListener(roleImplicationChangeListener, anchor);    
    }

    public void testRemoveRoleImplicationChangeListener()
    {
        mockInboundCoralEventWhiteboard.expect(once()).method("removeRoleImplicationChangeListener").
            with(same(roleImplicationChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expect(once()).method("removeRoleImplicationChangeListener").
            with(same(roleImplicationChangeListener), same(anchor));
        coralEventRedirector.removeRoleImplicationChangeListener(roleImplicationChangeListener, anchor);    
    }

    public void testFireRoleImplicationChangeEvent()
    {
        mockOutboundCoralEventWhiteboard.expect(once()).method("fireRoleImplicationChangeEvent").
            with(same(roleImplication), eq(false));
        mockLocalCoralEventWhiteboard.expect(once()).method("fireRoleImplicationChangeEvent").
            with(same(roleImplication), eq(false));
        coralEventRedirector.fireRoleImplicationChangeEvent(roleImplication, false);            
    }

    public void testAddResourceClassInheritanceChangeListener()
    {
        mockInboundCoralEventWhiteboard.expect(once()).method("addResourceClassInheritanceChangeListener").
            with(same(resourceClassInheritanceChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expect(once()).method("addResourceClassInheritanceChangeListener").
            with(same(resourceClassInheritanceChangeListener), same(anchor));
        coralEventRedirector.addResourceClassInheritanceChangeListener(resourceClassInheritanceChangeListener, anchor);    
    }

    public void testRemoveResourceClassInheritanceChangeListener()
    {
        mockInboundCoralEventWhiteboard.expect(once()).method("removeResourceClassInheritanceChangeListener").
            with(same(resourceClassInheritanceChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expect(once()).method("removeResourceClassInheritanceChangeListener").
            with(same(resourceClassInheritanceChangeListener), same(anchor));
        coralEventRedirector.removeResourceClassInheritanceChangeListener(resourceClassInheritanceChangeListener, anchor);    
    }

    public void testFireResourceClassInheritanceChangeEvent()
    {
        mockOutboundCoralEventWhiteboard.expect(once()).method("fireResourceClassInheritanceChangeEvent").
            with(same(resourceClassInheritance), eq(false));
        mockLocalCoralEventWhiteboard.expect(once()).method("fireResourceClassInheritanceChangeEvent").
            with(same(resourceClassInheritance), eq(false));
        coralEventRedirector.fireResourceClassInheritanceChangeEvent(resourceClassInheritance, false);            
    }

    public void testAddResourceClassAttributesChangeListener()
    {
        mockInboundCoralEventWhiteboard.expect(once()).method("addResourceClassAttributesChangeListener").
            with(same(resourceClassAttributesChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expect(once()).method("addResourceClassAttributesChangeListener").
            with(same(resourceClassAttributesChangeListener), same(anchor));
        coralEventRedirector.addResourceClassAttributesChangeListener(resourceClassAttributesChangeListener, anchor);    
    }

    public void testRemoveResourceClassAttributesChangeListener()
    {
        mockInboundCoralEventWhiteboard.expect(once()).method("removeResourceClassAttributesChangeListener").
            with(same(resourceClassAttributesChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expect(once()).method("removeResourceClassAttributesChangeListener").
            with(same(resourceClassAttributesChangeListener), same(anchor));
        coralEventRedirector.removeResourceClassAttributesChangeListener(resourceClassAttributesChangeListener, anchor);    
    }

    public void testFireResourceClassAttributesChangeEvent()
    {
        mockOutboundCoralEventWhiteboard.expect(once()).method("fireResourceClassAttributesChangeEvent").
            with(same(attributeDefinition), eq(false));
        mockLocalCoralEventWhiteboard.expect(once()).method("fireResourceClassAttributesChangeEvent").
            with(same(attributeDefinition), eq(false));
        coralEventRedirector.fireResourceClassAttributesChangeEvent(attributeDefinition, false);            
    }

    public void testAddResourceTreeChangeListener()
    {
        mockInboundCoralEventWhiteboard.expect(once()).method("addResourceTreeChangeListener").
            with(same(resourceTreeChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expect(once()).method("addResourceTreeChangeListener").
            with(same(resourceTreeChangeListener), same(anchor));
        coralEventRedirector.addResourceTreeChangeListener(resourceTreeChangeListener, anchor);    
    }

    public void testRemoveResourceTreeChangeListener()
    {
        mockInboundCoralEventWhiteboard.expect(once()).method("removeResourceTreeChangeListener").
            with(same(resourceTreeChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expect(once()).method("removeResourceTreeChangeListener").
            with(same(resourceTreeChangeListener), same(anchor));
        coralEventRedirector.removeResourceTreeChangeListener(resourceTreeChangeListener, anchor);    
    }

    public void testFireResourceTreeChangeEvent()
    {
        mockOutboundCoralEventWhiteboard.expect(once()).method("fireResourceTreeChangeEvent").
            with(same(resourceInheritance), eq(false));
        mockLocalCoralEventWhiteboard.expect(once()).method("fireResourceTreeChangeEvent").
            with(same(resourceInheritance), eq(false));
        coralEventRedirector.fireResourceTreeChangeEvent(resourceInheritance, false);            
    }

    public void testAddResourceOwnershipChangeListener()
    {
        mockInboundCoralEventWhiteboard.expect(once()).method("addResourceOwnershipChangeListener").
            with(same(resourceOwnershipChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expect(once()).method("addResourceOwnershipChangeListener").
            with(same(resourceOwnershipChangeListener), same(anchor));
        coralEventRedirector.addResourceOwnershipChangeListener(resourceOwnershipChangeListener, anchor);    
    }

    public void testRemoveResourceOwnershipChangeListener()
    {
        mockInboundCoralEventWhiteboard.expect(once()).method("removeResourceOwnershipChangeListener").
            with(same(resourceOwnershipChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expect(once()).method("removeResourceOwnershipChangeListener").
            with(same(resourceOwnershipChangeListener), same(anchor));
        coralEventRedirector.removeResourceOwnershipChangeListener(resourceOwnershipChangeListener, anchor);    
    }

    public void testFireResourceOwnershipChangeEvent()
    {
        mockOutboundCoralEventWhiteboard.expect(once()).method("fireResourceOwnershipChangeEvent").
            with(same(resourceOwnership), eq(false));
        mockLocalCoralEventWhiteboard.expect(once()).method("fireResourceOwnershipChangeEvent").
            with(same(resourceOwnership), eq(false));
        coralEventRedirector.fireResourceOwnershipChangeEvent(resourceOwnership, false);            
    }
    
    public void testAddResourceClassChangeListener()
    {
        mockInboundCoralEventWhiteboard.expect(once()).method("addResourceClassChangeListener").
            with(same(resourceClassChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expect(once()).method("addResourceClassChangeListener").
            with(same(resourceClassChangeListener), same(anchor));
        coralEventRedirector.addResourceClassChangeListener(resourceClassChangeListener, anchor);    
    }

    public void testRemoveResourceClassChangeListener()
    {
        mockInboundCoralEventWhiteboard.expect(once()).method("removeResourceClassChangeListener").
            with(same(resourceClassChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expect(once()).method("removeResourceClassChangeListener").
            with(same(resourceClassChangeListener), same(anchor));
        coralEventRedirector.removeResourceClassChangeListener(resourceClassChangeListener, anchor);    
    }

    public void testFireResourceClassChangeEvent()
    {
        mockOutboundCoralEventWhiteboard.expect(once()).method("fireResourceClassChangeEvent").
            with(same(resourceClass));
        mockLocalCoralEventWhiteboard.expect(once()).method("fireResourceClassChangeEvent").
            with(same(resourceClass));
        coralEventRedirector.fireResourceClassChangeEvent(resourceClass);            
    }    
    
    public void testAddAttributeClassChangeListener()
    {
        mockInboundCoralEventWhiteboard.expect(once()).method("addAttributeClassChangeListener").
            with(same(attributeClassChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expect(once()).method("addAttributeClassChangeListener").
            with(same(attributeClassChangeListener), same(anchor));
        coralEventRedirector.addAttributeClassChangeListener(attributeClassChangeListener, anchor);    
    }

    public void testRemoveAttributeClassChangeListener()
    {
        mockInboundCoralEventWhiteboard.expect(once()).method("removeAttributeClassChangeListener").
            with(same(attributeClassChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expect(once()).method("removeAttributeClassChangeListener").
            with(same(attributeClassChangeListener), same(anchor));
        coralEventRedirector.removeAttributeClassChangeListener(attributeClassChangeListener, anchor);    
    }

    public void testFireAttributeClassChangeEvent()
    {
        mockOutboundCoralEventWhiteboard.expect(once()).method("fireAttributeClassChangeEvent").
            with(same(attributeClass));
        mockLocalCoralEventWhiteboard.expect(once()).method("fireAttributeClassChangeEvent").
            with(same(attributeClass));
        coralEventRedirector.fireAttributeClassChangeEvent(attributeClass);            
    }    

    public void testAddAttributeDefinitionChangeListener()
    {
        mockInboundCoralEventWhiteboard.expect(once()).method("addAttributeDefinitionChangeListener").
            with(same(attributeDefinitionChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expect(once()).method("addAttributeDefinitionChangeListener").
            with(same(attributeDefinitionChangeListener), same(anchor));
        coralEventRedirector.addAttributeDefinitionChangeListener(attributeDefinitionChangeListener, anchor);    
    }

    public void testRemoveAttributeDefinitionChangeListener()
    {
        mockInboundCoralEventWhiteboard.expect(once()).method("removeAttributeDefinitionChangeListener").
            with(same(attributeDefinitionChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expect(once()).method("removeAttributeDefinitionChangeListener").
            with(same(attributeDefinitionChangeListener), same(anchor));
        coralEventRedirector.removeAttributeDefinitionChangeListener(attributeDefinitionChangeListener, anchor);    
    }

    public void testFireAttributeDefinitionChangeEvent()
    {
        mockOutboundCoralEventWhiteboard.expect(once()).method("fireAttributeDefinitionChangeEvent").
            with(same(attributeDefinition));
        mockLocalCoralEventWhiteboard.expect(once()).method("fireAttributeDefinitionChangeEvent").
            with(same(attributeDefinition));
        coralEventRedirector.fireAttributeDefinitionChangeEvent(attributeDefinition);            
    }    

    public void testAddResourceCreationListener()
    {
        mockInboundCoralEventWhiteboard.expect(once()).method("addResourceCreationListener").
            with(same(resourceCreationListener), same(anchor));
        mockLocalCoralEventWhiteboard.expect(once()).method("addResourceCreationListener").
            with(same(resourceCreationListener), same(anchor));
        coralEventRedirector.addResourceCreationListener(resourceCreationListener, anchor);    
    }

    public void testRemoveResourceCreationListener()
    {
        mockInboundCoralEventWhiteboard.expect(once()).method("removeResourceCreationListener").
            with(same(resourceCreationListener), same(anchor));
        mockLocalCoralEventWhiteboard.expect(once()).method("removeResourceCreationListener").
            with(same(resourceCreationListener), same(anchor));
        coralEventRedirector.removeResourceCreationListener(resourceCreationListener, anchor);    
    }

    public void testFireResourceCreationEvent()
    {
        mockOutboundCoralEventWhiteboard.expect(once()).method("fireResourceCreationEvent").
            with(same(resource));
        mockLocalCoralEventWhiteboard.expect(once()).method("fireResourceCreationEvent").
            with(same(resource));
        coralEventRedirector.fireResourceCreationEvent(resource);            
    }    

    public void testAddResourceChangeListener()
    {
        mockInboundCoralEventWhiteboard.expect(once()).method("addResourceChangeListener").
            with(same(resourceChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expect(once()).method("addResourceChangeListener").
            with(same(resourceChangeListener), same(anchor));
        coralEventRedirector.addResourceChangeListener(resourceChangeListener, anchor);    
    }

    public void testRemoveResourceChangeListener()
    {
        mockInboundCoralEventWhiteboard.expect(once()).method("removeResourceChangeListener").
            with(same(resourceChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expect(once()).method("removeResourceChangeListener").
            with(same(resourceChangeListener), same(anchor));
        coralEventRedirector.removeResourceChangeListener(resourceChangeListener, anchor);    
    }

    public void testFireResourceChangeEvent()
    {
        mockOutboundCoralEventWhiteboard.expect(once()).method("fireResourceChangeEvent").
            with(same(resource), same(subject));
        mockLocalCoralEventWhiteboard.expect(once()).method("fireResourceChangeEvent").
            with(same(resource), same(subject));
        coralEventRedirector.fireResourceChangeEvent(resource, subject);            
    }    

    public void testAddResourceDeletionListener()
    {
        mockInboundCoralEventWhiteboard.expect(once()).method("addResourceDeletionListener").
            with(same(resourceDeletionListener), same(anchor));
        mockLocalCoralEventWhiteboard.expect(once()).method("addResourceDeletionListener").
            with(same(resourceDeletionListener), same(anchor));
        coralEventRedirector.addResourceDeletionListener(resourceDeletionListener, anchor);    
    }

    public void testRemoveResourceDeletionListener()
    {
        mockInboundCoralEventWhiteboard.expect(once()).method("removeResourceDeletionListener").
            with(same(resourceDeletionListener), same(anchor));
        mockLocalCoralEventWhiteboard.expect(once()).method("removeResourceDeletionListener").
            with(same(resourceDeletionListener), same(anchor));
        coralEventRedirector.removeResourceDeletionListener(resourceDeletionListener, anchor);    
    }

    public void testFireResourceDeletionEvent()
    {
        mockOutboundCoralEventWhiteboard.expect(once()).method("fireResourceDeletionEvent").
            with(same(resource));
        mockLocalCoralEventWhiteboard.expect(once()).method("fireResourceDeletionEvent").
            with(same(resource));
        coralEventRedirector.fireResourceDeletionEvent(resource);            
    }    

    public void testAddResourceTreeDeletionListener()
    {
        mockInboundCoralEventWhiteboard.expect(once()).method("addResourceTreeDeletionListener").
            with(same(resourceTreeDeletionListener), same(anchor));
        mockLocalCoralEventWhiteboard.expect(once()).method("addResourceTreeDeletionListener").
            with(same(resourceTreeDeletionListener), same(anchor));
        coralEventRedirector.addResourceTreeDeletionListener(resourceTreeDeletionListener, anchor);    
    }

    public void testRemoveResourceTreeDeletionListener()
    {
        mockInboundCoralEventWhiteboard.expect(once()).method("removeResourceTreeDeletionListener").
            with(same(resourceTreeDeletionListener), same(anchor));
        mockLocalCoralEventWhiteboard.expect(once()).method("removeResourceTreeDeletionListener").
            with(same(resourceTreeDeletionListener), same(anchor));
        coralEventRedirector.removeResourceTreeDeletionListener(resourceTreeDeletionListener, anchor);    
    }

    public void testFireResourceTreeDeletionEvent()
    {
        mockOutboundCoralEventWhiteboard.expect(once()).method("fireResourceTreeDeletionEvent").
            with(same(resource));
        mockLocalCoralEventWhiteboard.expect(once()).method("fireResourceTreeDeletionEvent").
            with(same(resource));
        coralEventRedirector.fireResourceTreeDeletionEvent(resource);            
    }    

    public void testAddSubjectChangeListener()
    {
        mockInboundCoralEventWhiteboard.expect(once()).method("addSubjectChangeListener").
            with(same(subjectChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expect(once()).method("addSubjectChangeListener").
            with(same(subjectChangeListener), same(anchor));
        coralEventRedirector.addSubjectChangeListener(subjectChangeListener, anchor);    
    }

    public void testRemoveSubjectChangeListener()
    {
        mockInboundCoralEventWhiteboard.expect(once()).method("removeSubjectChangeListener").
            with(same(subjectChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expect(once()).method("removeSubjectChangeListener").
            with(same(subjectChangeListener), same(anchor));
        coralEventRedirector.removeSubjectChangeListener(subjectChangeListener, anchor);    
    }

    public void testFireSubjectChangeEvent()
    {
        mockOutboundCoralEventWhiteboard.expect(once()).method("fireSubjectChangeEvent").
            with(same(subject));
        mockLocalCoralEventWhiteboard.expect(once()).method("fireSubjectChangeEvent").
            with(same(subject));
        coralEventRedirector.fireSubjectChangeEvent(subject);            
    }    

    public void testAddRoleChangeListener()
    {
        mockInboundCoralEventWhiteboard.expect(once()).method("addRoleChangeListener").
            with(same(roleChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expect(once()).method("addRoleChangeListener").
            with(same(roleChangeListener), same(anchor));
        coralEventRedirector.addRoleChangeListener(roleChangeListener, anchor);    
    }

    public void testRemoveRoleChangeListener()
    {
        mockInboundCoralEventWhiteboard.expect(once()).method("removeRoleChangeListener").
            with(same(roleChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expect(once()).method("removeRoleChangeListener").
            with(same(roleChangeListener), same(anchor));
        coralEventRedirector.removeRoleChangeListener(roleChangeListener, anchor);    
    }

    public void testFireRoleChangeEvent()
    {
        mockOutboundCoralEventWhiteboard.expect(once()).method("fireRoleChangeEvent").
            with(same(role));
        mockLocalCoralEventWhiteboard.expect(once()).method("fireRoleChangeEvent").
            with(same(role));
        coralEventRedirector.fireRoleChangeEvent(role);            
    }    

    public void testAddPermissionChangeListener()
    {
        mockInboundCoralEventWhiteboard.expect(once()).method("addPermissionChangeListener").
            with(same(permissionChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expect(once()).method("addPermissionChangeListener").
            with(same(permissionChangeListener), same(anchor));
        coralEventRedirector.addPermissionChangeListener(permissionChangeListener, anchor);    
    }

    public void testRemovePermissionChangeListener()
    {
        mockInboundCoralEventWhiteboard.expect(once()).method("removePermissionChangeListener").
            with(same(permissionChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expect(once()).method("removePermissionChangeListener").
            with(same(permissionChangeListener), same(anchor));
        coralEventRedirector.removePermissionChangeListener(permissionChangeListener, anchor);    
    }

    public void testFirePermissoinChangeEvent()
    {
        mockOutboundCoralEventWhiteboard.expect(once()).method("firePermissionChangeEvent").
            with(same(permission));
        mockLocalCoralEventWhiteboard.expect(once()).method("firePermissionChangeEvent").
            with(same(permission));
        coralEventRedirector.firePermissionChangeEvent(permission);            
    }    
}
