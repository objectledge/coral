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

/**
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: CoralEventRedirectorTest.java,v 1.4 2004-05-28 10:04:11 fil Exp $
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
        throws Exception
    {
        super.setUp();
        mockInboundCoralEventWhiteboard = mock(CoralEventWhiteboard.class);
        inboundCoralEventWhiteboard = (CoralEventWhiteboard)mockInboundCoralEventWhiteboard.proxy();
        mockOutboundCoralEventWhiteboard = mock(CoralEventWhiteboard.class);
        outboundCoralEventWhiteboard = (CoralEventWhiteboard)mockOutboundCoralEventWhiteboard.proxy();
        mockLocalCoralEventWhiteboard = mock(CoralEventWhiteboard.class);
        localCoralEventWhiteboard = (CoralEventWhiteboard)mockLocalCoralEventWhiteboard.proxy();
        
        coralEventRedirector = new CoralEventRedirector(inboundCoralEventWhiteboard, 
            localCoralEventWhiteboard, outboundCoralEventWhiteboard);
    }
    
    public void testAddPermissionAssociationChangeListener()
    {
        mockInboundCoralEventWhiteboard.expects(once()).method("addPermissionAssociationChangeListener").
            with(same(permissionAssoicationChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expects(once()).method("addPermissionAssociationChangeListener").
            with(same(permissionAssoicationChangeListener), same(anchor));
        coralEventRedirector.addPermissionAssociationChangeListener(permissionAssoicationChangeListener, anchor);    
    }

    public void testRemovePermissionAssociationChangeListener()
    {
        mockInboundCoralEventWhiteboard.expects(once()).method("removePermissionAssociationChangeListener").
            with(same(permissionAssoicationChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expects(once()).method("removePermissionAssociationChangeListener").
            with(same(permissionAssoicationChangeListener), same(anchor));
        coralEventRedirector.removePermissionAssociationChangeListener(permissionAssoicationChangeListener, anchor);    
    }

    public void testFirePermissionAssociationChangeEvent()
    {
        mockOutboundCoralEventWhiteboard.expects(once()).method("firePermissionAssociationChangeEvent").
            with(same(permissionAssociation), eq(false));
        mockLocalCoralEventWhiteboard.expects(once()).method("firePermissionAssociationChangeEvent").
            with(same(permissionAssociation), eq(false));
        coralEventRedirector.firePermissionAssociationChangeEvent(permissionAssociation, false);            
    }
    
    public void testAddPermissionAssignmentChangeListener()
    {
        mockInboundCoralEventWhiteboard.expects(once()).method("addPermissionAssignmentChangeListener").
            with(same(permissionAssignmentChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expects(once()).method("addPermissionAssignmentChangeListener").
            with(same(permissionAssignmentChangeListener), same(anchor));
        coralEventRedirector.addPermissionAssignmentChangeListener(permissionAssignmentChangeListener, anchor);    
    }

    public void testRemovePermissionAssignmentChangeListener()
    {
        mockInboundCoralEventWhiteboard.expects(once()).method("removePermissionAssignmentChangeListener").
            with(same(permissionAssignmentChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expects(once()).method("removePermissionAssignmentChangeListener").
            with(same(permissionAssignmentChangeListener), same(anchor));
        coralEventRedirector.removePermissionAssignmentChangeListener(permissionAssignmentChangeListener, anchor);    
    }

    public void testFirePermissionAssignmentChangeEvent()
    {
        mockOutboundCoralEventWhiteboard.expects(once()).method("firePermissionAssignmentChangeEvent").
            with(same(permissionAssignment), eq(false));
        mockLocalCoralEventWhiteboard.expects(once()).method("firePermissionAssignmentChangeEvent").
            with(same(permissionAssignment), eq(false));
        coralEventRedirector.firePermissionAssignmentChangeEvent(permissionAssignment, false);            
    }

    public void testAddRoleAssignmentChangeListener()
    {
        mockInboundCoralEventWhiteboard.expects(once()).method("addRoleAssignmentChangeListener").
            with(same(roleAssignmentChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expects(once()).method("addRoleAssignmentChangeListener").
            with(same(roleAssignmentChangeListener), same(anchor));
        coralEventRedirector.addRoleAssignmentChangeListener(roleAssignmentChangeListener, anchor);    
    }

    public void testRemoveRoleAssignmentChangeListener()
    {
        mockInboundCoralEventWhiteboard.expects(once()).method("removeRoleAssignmentChangeListener").
            with(same(roleAssignmentChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expects(once()).method("removeRoleAssignmentChangeListener").
            with(same(roleAssignmentChangeListener), same(anchor));
        coralEventRedirector.removeRoleAssignmentChangeListener(roleAssignmentChangeListener, anchor);    
    }

    public void testFireRoleAssignmentChangeEvent()
    {
        mockOutboundCoralEventWhiteboard.expects(once()).method("fireRoleAssignmentChangeEvent").
            with(same(roleAssignment), eq(false));
        mockLocalCoralEventWhiteboard.expects(once()).method("fireRoleAssignmentChangeEvent").
            with(same(roleAssignment), eq(false));
        coralEventRedirector.fireRoleAssignmentChangeEvent(roleAssignment, false);            
    }
    
    public void testAddRoleImplicationChangeListener()
    {
        mockInboundCoralEventWhiteboard.expects(once()).method("addRoleImplicationChangeListener").
            with(same(roleImplicationChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expects(once()).method("addRoleImplicationChangeListener").
            with(same(roleImplicationChangeListener), same(anchor));
        coralEventRedirector.addRoleImplicationChangeListener(roleImplicationChangeListener, anchor);    
    }

    public void testRemoveRoleImplicationChangeListener()
    {
        mockInboundCoralEventWhiteboard.expects(once()).method("removeRoleImplicationChangeListener").
            with(same(roleImplicationChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expects(once()).method("removeRoleImplicationChangeListener").
            with(same(roleImplicationChangeListener), same(anchor));
        coralEventRedirector.removeRoleImplicationChangeListener(roleImplicationChangeListener, anchor);    
    }

    public void testFireRoleImplicationChangeEvent()
    {
        mockOutboundCoralEventWhiteboard.expects(once()).method("fireRoleImplicationChangeEvent").
            with(same(roleImplication), eq(false));
        mockLocalCoralEventWhiteboard.expects(once()).method("fireRoleImplicationChangeEvent").
            with(same(roleImplication), eq(false));
        coralEventRedirector.fireRoleImplicationChangeEvent(roleImplication, false);            
    }

    public void testAddResourceClassInheritanceChangeListener()
    {
        mockInboundCoralEventWhiteboard.expects(once()).method("addResourceClassInheritanceChangeListener").
            with(same(resourceClassInheritanceChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expects(once()).method("addResourceClassInheritanceChangeListener").
            with(same(resourceClassInheritanceChangeListener), same(anchor));
        coralEventRedirector.addResourceClassInheritanceChangeListener(resourceClassInheritanceChangeListener, anchor);    
    }

    public void testRemoveResourceClassInheritanceChangeListener()
    {
        mockInboundCoralEventWhiteboard.expects(once()).method("removeResourceClassInheritanceChangeListener").
            with(same(resourceClassInheritanceChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expects(once()).method("removeResourceClassInheritanceChangeListener").
            with(same(resourceClassInheritanceChangeListener), same(anchor));
        coralEventRedirector.removeResourceClassInheritanceChangeListener(resourceClassInheritanceChangeListener, anchor);    
    }

    public void testFireResourceClassInheritanceChangeEvent()
    {
        mockOutboundCoralEventWhiteboard.expects(once()).method("fireResourceClassInheritanceChangeEvent").
            with(same(resourceClassInheritance), eq(false));
        mockLocalCoralEventWhiteboard.expects(once()).method("fireResourceClassInheritanceChangeEvent").
            with(same(resourceClassInheritance), eq(false));
        coralEventRedirector.fireResourceClassInheritanceChangeEvent(resourceClassInheritance, false);            
    }

    public void testAddResourceClassAttributesChangeListener()
    {
        mockInboundCoralEventWhiteboard.expects(once()).method("addResourceClassAttributesChangeListener").
            with(same(resourceClassAttributesChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expects(once()).method("addResourceClassAttributesChangeListener").
            with(same(resourceClassAttributesChangeListener), same(anchor));
        coralEventRedirector.addResourceClassAttributesChangeListener(resourceClassAttributesChangeListener, anchor);    
    }

    public void testRemoveResourceClassAttributesChangeListener()
    {
        mockInboundCoralEventWhiteboard.expects(once()).method("removeResourceClassAttributesChangeListener").
            with(same(resourceClassAttributesChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expects(once()).method("removeResourceClassAttributesChangeListener").
            with(same(resourceClassAttributesChangeListener), same(anchor));
        coralEventRedirector.removeResourceClassAttributesChangeListener(resourceClassAttributesChangeListener, anchor);    
    }

    public void testFireResourceClassAttributesChangeEvent()
    {
        mockOutboundCoralEventWhiteboard.expects(once()).method("fireResourceClassAttributesChangeEvent").
            with(same(attributeDefinition), eq(false));
        mockLocalCoralEventWhiteboard.expects(once()).method("fireResourceClassAttributesChangeEvent").
            with(same(attributeDefinition), eq(false));
        coralEventRedirector.fireResourceClassAttributesChangeEvent(attributeDefinition, false);            
    }

    public void testAddResourceTreeChangeListener()
    {
        mockInboundCoralEventWhiteboard.expects(once()).method("addResourceTreeChangeListener").
            with(same(resourceTreeChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expects(once()).method("addResourceTreeChangeListener").
            with(same(resourceTreeChangeListener), same(anchor));
        coralEventRedirector.addResourceTreeChangeListener(resourceTreeChangeListener, anchor);    
    }

    public void testRemoveResourceTreeChangeListener()
    {
        mockInboundCoralEventWhiteboard.expects(once()).method("removeResourceTreeChangeListener").
            with(same(resourceTreeChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expects(once()).method("removeResourceTreeChangeListener").
            with(same(resourceTreeChangeListener), same(anchor));
        coralEventRedirector.removeResourceTreeChangeListener(resourceTreeChangeListener, anchor);    
    }

    public void testFireResourceTreeChangeEvent()
    {
        mockOutboundCoralEventWhiteboard.expects(once()).method("fireResourceTreeChangeEvent").
            with(same(resourceInheritance), eq(false));
        mockLocalCoralEventWhiteboard.expects(once()).method("fireResourceTreeChangeEvent").
            with(same(resourceInheritance), eq(false));
        coralEventRedirector.fireResourceTreeChangeEvent(resourceInheritance, false);            
    }

    public void testAddResourceOwnershipChangeListener()
    {
        mockInboundCoralEventWhiteboard.expects(once()).method("addResourceOwnershipChangeListener").
            with(same(resourceOwnershipChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expects(once()).method("addResourceOwnershipChangeListener").
            with(same(resourceOwnershipChangeListener), same(anchor));
        coralEventRedirector.addResourceOwnershipChangeListener(resourceOwnershipChangeListener, anchor);    
    }

    public void testRemoveResourceOwnershipChangeListener()
    {
        mockInboundCoralEventWhiteboard.expects(once()).method("removeResourceOwnershipChangeListener").
            with(same(resourceOwnershipChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expects(once()).method("removeResourceOwnershipChangeListener").
            with(same(resourceOwnershipChangeListener), same(anchor));
        coralEventRedirector.removeResourceOwnershipChangeListener(resourceOwnershipChangeListener, anchor);    
    }

    public void testFireResourceOwnershipChangeEvent()
    {
        mockOutboundCoralEventWhiteboard.expects(once()).method("fireResourceOwnershipChangeEvent").
            with(same(resourceOwnership), eq(false));
        mockLocalCoralEventWhiteboard.expects(once()).method("fireResourceOwnershipChangeEvent").
            with(same(resourceOwnership), eq(false));
        coralEventRedirector.fireResourceOwnershipChangeEvent(resourceOwnership, false);            
    }
    
    public void testAddResourceClassChangeListener()
    {
        mockInboundCoralEventWhiteboard.expects(once()).method("addResourceClassChangeListener").
            with(same(resourceClassChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expects(once()).method("addResourceClassChangeListener").
            with(same(resourceClassChangeListener), same(anchor));
        coralEventRedirector.addResourceClassChangeListener(resourceClassChangeListener, anchor);    
    }

    public void testRemoveResourceClassChangeListener()
    {
        mockInboundCoralEventWhiteboard.expects(once()).method("removeResourceClassChangeListener").
            with(same(resourceClassChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expects(once()).method("removeResourceClassChangeListener").
            with(same(resourceClassChangeListener), same(anchor));
        coralEventRedirector.removeResourceClassChangeListener(resourceClassChangeListener, anchor);    
    }

    public void testFireResourceClassChangeEvent()
    {
        mockOutboundCoralEventWhiteboard.expects(once()).method("fireResourceClassChangeEvent").
            with(same(resourceClass));
        mockLocalCoralEventWhiteboard.expects(once()).method("fireResourceClassChangeEvent").
            with(same(resourceClass));
        coralEventRedirector.fireResourceClassChangeEvent(resourceClass);            
    }    
    
    public void testAddAttributeClassChangeListener()
    {
        mockInboundCoralEventWhiteboard.expects(once()).method("addAttributeClassChangeListener").
            with(same(attributeClassChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expects(once()).method("addAttributeClassChangeListener").
            with(same(attributeClassChangeListener), same(anchor));
        coralEventRedirector.addAttributeClassChangeListener(attributeClassChangeListener, anchor);    
    }

    public void testRemoveAttributeClassChangeListener()
    {
        mockInboundCoralEventWhiteboard.expects(once()).method("removeAttributeClassChangeListener").
            with(same(attributeClassChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expects(once()).method("removeAttributeClassChangeListener").
            with(same(attributeClassChangeListener), same(anchor));
        coralEventRedirector.removeAttributeClassChangeListener(attributeClassChangeListener, anchor);    
    }

    public void testFireAttributeClassChangeEvent()
    {
        mockOutboundCoralEventWhiteboard.expects(once()).method("fireAttributeClassChangeEvent").
            with(same(attributeClass));
        mockLocalCoralEventWhiteboard.expects(once()).method("fireAttributeClassChangeEvent").
            with(same(attributeClass));
        coralEventRedirector.fireAttributeClassChangeEvent(attributeClass);            
    }    

    public void testAddAttributeDefinitionChangeListener()
    {
        mockInboundCoralEventWhiteboard.expects(once()).method("addAttributeDefinitionChangeListener").
            with(same(attributeDefinitionChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expects(once()).method("addAttributeDefinitionChangeListener").
            with(same(attributeDefinitionChangeListener), same(anchor));
        coralEventRedirector.addAttributeDefinitionChangeListener(attributeDefinitionChangeListener, anchor);    
    }

    public void testRemoveAttributeDefinitionChangeListener()
    {
        mockInboundCoralEventWhiteboard.expects(once()).method("removeAttributeDefinitionChangeListener").
            with(same(attributeDefinitionChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expects(once()).method("removeAttributeDefinitionChangeListener").
            with(same(attributeDefinitionChangeListener), same(anchor));
        coralEventRedirector.removeAttributeDefinitionChangeListener(attributeDefinitionChangeListener, anchor);    
    }

    public void testFireAttributeDefinitionChangeEvent()
    {
        mockOutboundCoralEventWhiteboard.expects(once()).method("fireAttributeDefinitionChangeEvent").
            with(same(attributeDefinition));
        mockLocalCoralEventWhiteboard.expects(once()).method("fireAttributeDefinitionChangeEvent").
            with(same(attributeDefinition));
        coralEventRedirector.fireAttributeDefinitionChangeEvent(attributeDefinition);            
    }    

    public void testAddResourceCreationListener()
    {
        mockInboundCoralEventWhiteboard.expects(once()).method("addResourceCreationListener").
            with(same(resourceCreationListener), same(anchor));
        mockLocalCoralEventWhiteboard.expects(once()).method("addResourceCreationListener").
            with(same(resourceCreationListener), same(anchor));
        coralEventRedirector.addResourceCreationListener(resourceCreationListener, anchor);    
    }

    public void testRemoveResourceCreationListener()
    {
        mockInboundCoralEventWhiteboard.expects(once()).method("removeResourceCreationListener").
            with(same(resourceCreationListener), same(anchor));
        mockLocalCoralEventWhiteboard.expects(once()).method("removeResourceCreationListener").
            with(same(resourceCreationListener), same(anchor));
        coralEventRedirector.removeResourceCreationListener(resourceCreationListener, anchor);    
    }

    public void testFireResourceCreationEvent()
    {
        mockOutboundCoralEventWhiteboard.expects(once()).method("fireResourceCreationEvent").
            with(same(resource));
        mockLocalCoralEventWhiteboard.expects(once()).method("fireResourceCreationEvent").
            with(same(resource));
        coralEventRedirector.fireResourceCreationEvent(resource);            
    }    

    public void testAddResourceChangeListener()
    {
        mockInboundCoralEventWhiteboard.expects(once()).method("addResourceChangeListener").
            with(same(resourceChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expects(once()).method("addResourceChangeListener").
            with(same(resourceChangeListener), same(anchor));
        coralEventRedirector.addResourceChangeListener(resourceChangeListener, anchor);    
    }

    public void testRemoveResourceChangeListener()
    {
        mockInboundCoralEventWhiteboard.expects(once()).method("removeResourceChangeListener").
            with(same(resourceChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expects(once()).method("removeResourceChangeListener").
            with(same(resourceChangeListener), same(anchor));
        coralEventRedirector.removeResourceChangeListener(resourceChangeListener, anchor);    
    }

    public void testFireResourceChangeEvent()
    {
        mockOutboundCoralEventWhiteboard.expects(once()).method("fireResourceChangeEvent").
            with(same(resource), same(subject));
        mockLocalCoralEventWhiteboard.expects(once()).method("fireResourceChangeEvent").
            with(same(resource), same(subject));
        coralEventRedirector.fireResourceChangeEvent(resource, subject);            
    }    

    public void testAddResourceDeletionListener()
    {
        mockInboundCoralEventWhiteboard.expects(once()).method("addResourceDeletionListener").
            with(same(resourceDeletionListener), same(anchor));
        mockLocalCoralEventWhiteboard.expects(once()).method("addResourceDeletionListener").
            with(same(resourceDeletionListener), same(anchor));
        coralEventRedirector.addResourceDeletionListener(resourceDeletionListener, anchor);    
    }

    public void testRemoveResourceDeletionListener()
    {
        mockInboundCoralEventWhiteboard.expects(once()).method("removeResourceDeletionListener").
            with(same(resourceDeletionListener), same(anchor));
        mockLocalCoralEventWhiteboard.expects(once()).method("removeResourceDeletionListener").
            with(same(resourceDeletionListener), same(anchor));
        coralEventRedirector.removeResourceDeletionListener(resourceDeletionListener, anchor);    
    }

    public void testFireResourceDeletionEvent()
    {
        mockOutboundCoralEventWhiteboard.expects(once()).method("fireResourceDeletionEvent").
            with(same(resource));
        mockLocalCoralEventWhiteboard.expects(once()).method("fireResourceDeletionEvent").
            with(same(resource));
        coralEventRedirector.fireResourceDeletionEvent(resource);            
    }    

    public void testAddResourceTreeDeletionListener()
    {
        mockInboundCoralEventWhiteboard.expects(once()).method("addResourceTreeDeletionListener").
            with(same(resourceTreeDeletionListener), same(anchor));
        mockLocalCoralEventWhiteboard.expects(once()).method("addResourceTreeDeletionListener").
            with(same(resourceTreeDeletionListener), same(anchor));
        coralEventRedirector.addResourceTreeDeletionListener(resourceTreeDeletionListener, anchor);    
    }

    public void testRemoveResourceTreeDeletionListener()
    {
        mockInboundCoralEventWhiteboard.expects(once()).method("removeResourceTreeDeletionListener").
            with(same(resourceTreeDeletionListener), same(anchor));
        mockLocalCoralEventWhiteboard.expects(once()).method("removeResourceTreeDeletionListener").
            with(same(resourceTreeDeletionListener), same(anchor));
        coralEventRedirector.removeResourceTreeDeletionListener(resourceTreeDeletionListener, anchor);    
    }

    public void testFireResourceTreeDeletionEvent()
    {
        mockOutboundCoralEventWhiteboard.expects(once()).method("fireResourceTreeDeletionEvent").
            with(same(resource));
        mockLocalCoralEventWhiteboard.expects(once()).method("fireResourceTreeDeletionEvent").
            with(same(resource));
        coralEventRedirector.fireResourceTreeDeletionEvent(resource);            
    }    

    public void testAddSubjectChangeListener()
    {
        mockInboundCoralEventWhiteboard.expects(once()).method("addSubjectChangeListener").
            with(same(subjectChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expects(once()).method("addSubjectChangeListener").
            with(same(subjectChangeListener), same(anchor));
        coralEventRedirector.addSubjectChangeListener(subjectChangeListener, anchor);    
    }

    public void testRemoveSubjectChangeListener()
    {
        mockInboundCoralEventWhiteboard.expects(once()).method("removeSubjectChangeListener").
            with(same(subjectChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expects(once()).method("removeSubjectChangeListener").
            with(same(subjectChangeListener), same(anchor));
        coralEventRedirector.removeSubjectChangeListener(subjectChangeListener, anchor);    
    }

    public void testFireSubjectChangeEvent()
    {
        mockOutboundCoralEventWhiteboard.expects(once()).method("fireSubjectChangeEvent").
            with(same(subject));
        mockLocalCoralEventWhiteboard.expects(once()).method("fireSubjectChangeEvent").
            with(same(subject));
        coralEventRedirector.fireSubjectChangeEvent(subject);            
    }    

    public void testAddRoleChangeListener()
    {
        mockInboundCoralEventWhiteboard.expects(once()).method("addRoleChangeListener").
            with(same(roleChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expects(once()).method("addRoleChangeListener").
            with(same(roleChangeListener), same(anchor));
        coralEventRedirector.addRoleChangeListener(roleChangeListener, anchor);    
    }

    public void testRemoveRoleChangeListener()
    {
        mockInboundCoralEventWhiteboard.expects(once()).method("removeRoleChangeListener").
            with(same(roleChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expects(once()).method("removeRoleChangeListener").
            with(same(roleChangeListener), same(anchor));
        coralEventRedirector.removeRoleChangeListener(roleChangeListener, anchor);    
    }

    public void testFireRoleChangeEvent()
    {
        mockOutboundCoralEventWhiteboard.expects(once()).method("fireRoleChangeEvent").
            with(same(role));
        mockLocalCoralEventWhiteboard.expects(once()).method("fireRoleChangeEvent").
            with(same(role));
        coralEventRedirector.fireRoleChangeEvent(role);            
    }    

    public void testAddPermissionChangeListener()
    {
        mockInboundCoralEventWhiteboard.expects(once()).method("addPermissionChangeListener").
            with(same(permissionChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expects(once()).method("addPermissionChangeListener").
            with(same(permissionChangeListener), same(anchor));
        coralEventRedirector.addPermissionChangeListener(permissionChangeListener, anchor);    
    }

    public void testRemovePermissionChangeListener()
    {
        mockInboundCoralEventWhiteboard.expects(once()).method("removePermissionChangeListener").
            with(same(permissionChangeListener), same(anchor));
        mockLocalCoralEventWhiteboard.expects(once()).method("removePermissionChangeListener").
            with(same(permissionChangeListener), same(anchor));
        coralEventRedirector.removePermissionChangeListener(permissionChangeListener, anchor);    
    }

    public void testFirePermissoinChangeEvent()
    {
        mockOutboundCoralEventWhiteboard.expects(once()).method("firePermissionChangeEvent").
            with(same(permission));
        mockLocalCoralEventWhiteboard.expects(once()).method("firePermissionChangeEvent").
            with(same(permission));
        coralEventRedirector.firePermissionChangeEvent(permission);            
    }    
}
