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
package org.objectledge.session;

import org.objectledge.coral.CoralCore;
import org.objectledge.coral.CoralSession;
import org.objectledge.coral.event.AttributeClassChangeListener;
import org.objectledge.coral.event.AttributeDefinitionChangeListener;
import org.objectledge.coral.event.CoralEventWhiteboard;
import org.objectledge.coral.event.PermissionAssignmentChangeListener;
import org.objectledge.coral.event.PermissionAssociationChangeListener;
import org.objectledge.coral.event.PermissionChangeListener;
import org.objectledge.coral.event.ResourceChangeListener;
import org.objectledge.coral.event.ResourceClassAttributesChangeListener;
import org.objectledge.coral.event.ResourceClassChangeListener;
import org.objectledge.coral.event.ResourceClassInheritanceChangeListener;
import org.objectledge.coral.event.ResourceCreationListener;
import org.objectledge.coral.event.ResourceDeletionListener;
import org.objectledge.coral.event.ResourceOwnershipChangeListener;
import org.objectledge.coral.event.ResourceTreeChangeListener;
import org.objectledge.coral.event.ResourceTreeDeletionListener;
import org.objectledge.coral.event.RoleAssignmentChangeListener;
import org.objectledge.coral.event.RoleChangeListener;
import org.objectledge.coral.event.RoleImplicationChangeListener;
import org.objectledge.coral.event.SubjectChangeListener;
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

/**
 * Session local CoralEventWhiteboard wrapper.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: SessionCoralEventWhiteboard.java,v 1.2 2004-03-08 08:19:14 fil Exp $
 */
public class SessionCoralEventWhiteboard implements CoralEventWhiteboard
{
    private CoralCore coral;
    private CoralSessionImpl session;

    /**
     * Constructs a CoralEventWhiteboard wrapper instance.
     * 
     * @param coral the Coral components hub.
     * @param session the session.
     */
    SessionCoralEventWhiteboard(CoralCore coral, CoralSession session)
    {
        this.coral = coral;
    }

    /** 
     * {@inheritDoc}
     */
    public void addPermissionAssignmentChangeListener(
        PermissionAssignmentChangeListener listener,
        Object object)
    {
        session.checkOpen();
        coral.getEventWhiteboard().addPermissionAssignmentChangeListener(listener, object);
    }

    /** 
     * {@inheritDoc}
     */
    public void removePermissionAssignmentChangeListener(
        PermissionAssignmentChangeListener listener,
        Object object)
    {
        session.checkOpen();
        coral.getEventWhiteboard().removePermissionAssignmentChangeListener(listener, object);
    }

    /** 
     * {@inheritDoc}
     */
    public void addRoleAssignmentChangeListener(
        RoleAssignmentChangeListener listener,
        Object object)
    {
        session.checkOpen();
        coral.getEventWhiteboard().addRoleAssignmentChangeListener(listener, object);
    }

    /** 
     * {@inheritDoc}
     */
    public void removeRoleAssignmentChangeListener(
        RoleAssignmentChangeListener listener,
        Object object)
    {
        session.checkOpen();
        coral.getEventWhiteboard().removeRoleAssignmentChangeListener(listener, object);
    }

    /** 
     * {@inheritDoc}
     */
    public void addPermissionAssociationChangeListener(
        PermissionAssociationChangeListener listener,
        Object object)
    {
        session.checkOpen();
        coral.getEventWhiteboard().addPermissionAssociationChangeListener(listener, object);
    }

    /** 
     * {@inheritDoc}
     */
    public void removePermissionAssociationChangeListener(
        PermissionAssociationChangeListener listener,
        Object object)
    {
        session.checkOpen();
        coral.getEventWhiteboard().removePermissionAssociationChangeListener(listener, object);
    }

    /** 
     * {@inheritDoc}
     */
    public void addRoleImplicationChangeListener(
        RoleImplicationChangeListener listener,
        Object object)
    {
        session.checkOpen();
        coral.getEventWhiteboard().addRoleImplicationChangeListener(listener, object);
    }

    /** 
     * {@inheritDoc}
     */
    public void removeRoleImplicationChangeListener(
        RoleImplicationChangeListener listener,
        Object object)
    {
        session.checkOpen();
        coral.getEventWhiteboard().removeRoleImplicationChangeListener(listener, object);
    }

    /** 
     * {@inheritDoc}
     */
    public void addResourceClassInheritanceChangeListener(
        ResourceClassInheritanceChangeListener listener,
        Object object)
    {
        session.checkOpen();
        coral.getEventWhiteboard().addResourceClassInheritanceChangeListener(listener, object);
    }

    /** 
     * {@inheritDoc}
     */
    public void removeResourceClassInheritanceChangeListener(
        ResourceClassInheritanceChangeListener listener,
        Object object)
    {
        session.checkOpen();
        coral.getEventWhiteboard().removeResourceClassInheritanceChangeListener(listener, object);
    }

    /** 
     * {@inheritDoc}
     */
    public void addResourceClassAttributesChangeListener(
        ResourceClassAttributesChangeListener listener,
        Object object)
    {
        session.checkOpen();
        coral.getEventWhiteboard().addResourceClassAttributesChangeListener(listener, object);
    }

    /** 
     * {@inheritDoc}
     */
    public void removeResourceClassAttributesChangeListener(
        ResourceClassAttributesChangeListener listener,
        Object object)
    {
        session.checkOpen();
        coral.getEventWhiteboard().removeResourceClassAttributesChangeListener(listener, object);
    }

    /** 
     * {@inheritDoc}
     */
    public void addResourceTreeChangeListener(ResourceTreeChangeListener listener, Object object)
    {
        session.checkOpen();
        coral.getEventWhiteboard().addResourceTreeChangeListener(listener, object);
    }

    /** 
     * {@inheritDoc}
     */
    public void removeResourceTreeChangeListener(
        ResourceTreeChangeListener listener,
        Object object)
    {
        session.checkOpen();
        coral.getEventWhiteboard().removeResourceTreeChangeListener(listener, object);
    }

    /** 
     * {@inheritDoc}
     */
    public void addResourceOwnershipChangeListener(
        ResourceOwnershipChangeListener listener,
        Object object)
    {
        session.checkOpen();
        coral.getEventWhiteboard().addResourceOwnershipChangeListener(listener, object);
    }

    /** 
     * {@inheritDoc}
     */
    public void removeResourceOwnershipChangeListener(
        ResourceOwnershipChangeListener listener,
        Object object)
    {
        session.checkOpen();
        coral.getEventWhiteboard().removeResourceOwnershipChangeListener(listener, object);
    }

    /** 
     * {@inheritDoc}
     */
    public void addResourceClassChangeListener(ResourceClassChangeListener listener, Object object)
    {
        session.checkOpen();
        coral.getEventWhiteboard().addResourceClassChangeListener(listener, object);
    }

    /** 
     * {@inheritDoc}
     */
    public void removeResourceClassChangeListener(
        ResourceClassChangeListener listener,
        Object object)
    {
        session.checkOpen();
        coral.getEventWhiteboard().removeResourceClassChangeListener(listener, object);
    }

    /** 
     * {@inheritDoc}
     */
    public void addAttributeClassChangeListener(
        AttributeClassChangeListener listener,
        Object object)
    {
        session.checkOpen();
        coral.getEventWhiteboard().addAttributeClassChangeListener(listener, object);
    }

    /** 
     * {@inheritDoc}
     */
    public void removeAttributeClassChangeListener(
        AttributeClassChangeListener listener,
        Object object)
    {
        session.checkOpen();
        coral.getEventWhiteboard().removeAttributeClassChangeListener(listener, object);
    }

    /** 
     * {@inheritDoc}
     */
    public void addAttributeDefinitionChangeListener(
        AttributeDefinitionChangeListener listener,
        Object object)
    {
        session.checkOpen();
        coral.getEventWhiteboard().addAttributeDefinitionChangeListener(listener, object);
    }

    /** 
     * {@inheritDoc}
     */
    public void removeAttributeDefinitionChangeListener(
        AttributeDefinitionChangeListener listener,
        Object object)
    {
        session.checkOpen();
        coral.getEventWhiteboard().removeAttributeDefinitionChangeListener(listener, object);
    }

    /** 
     * {@inheritDoc}
     */
    public void addResourceCreationListener(ResourceCreationListener listener, Object object)
    {
        session.checkOpen();
        coral.getEventWhiteboard().addResourceCreationListener(listener, object);
    }

    /** 
     * {@inheritDoc}
     */
    public void removeResourceCreationListener(ResourceCreationListener listener, Object object)
    {
        session.checkOpen();
        coral.getEventWhiteboard().removeResourceCreationListener(listener, object);
    }

    /** 
     * {@inheritDoc}
     */
    public void addResourceChangeListener(ResourceChangeListener listener, Object object)
    {
        session.checkOpen();
        coral.getEventWhiteboard().addResourceChangeListener(listener, object);
    }

    /** 
     * {@inheritDoc}
     */
    public void removeResourceChangeListener(ResourceChangeListener listener, Object object)
    {
        session.checkOpen();
        coral.getEventWhiteboard().removeResourceChangeListener(listener, object);
    }

    /** 
     * {@inheritDoc}
     */
    public void addResourceDeletionListener(ResourceDeletionListener listener, Object object)
    {
        session.checkOpen();
        coral.getEventWhiteboard().addResourceDeletionListener(listener, object);
    }

    /** 
     * {@inheritDoc}
     */
    public void removeResourceDeletionListener(ResourceDeletionListener listener, Object object)
    {
        session.checkOpen();
        coral.getEventWhiteboard().removeResourceDeletionListener(listener, object);
    }

    /** 
     * {@inheritDoc}
     */
    public void addResourceTreeDeletionListener(
        ResourceTreeDeletionListener listener,
        Object object)
    {
        session.checkOpen();
        coral.getEventWhiteboard().addResourceTreeDeletionListener(listener, object);
    }

    /** 
     * {@inheritDoc}
     */
    public void removeResourceTreeDeletionListener(
        ResourceTreeDeletionListener listener,
        Object object)
    {
        session.checkOpen();
        coral.getEventWhiteboard().removeResourceTreeDeletionListener(listener, object);
    }

    /** 
     * {@inheritDoc}
     */
    public void addSubjectChangeListener(SubjectChangeListener listener, Object object)
    {
        session.checkOpen();
        coral.getEventWhiteboard().addSubjectChangeListener(listener, object);
    }

    /** 
     * {@inheritDoc}
     */
    public void removeSubjectChangeListener(SubjectChangeListener listener, Object object)
    {
        session.checkOpen();
        coral.getEventWhiteboard().removeSubjectChangeListener(listener, object);
    }

    /** 
     * {@inheritDoc}
     */
    public void addRoleChangeListener(RoleChangeListener listener, Object object)
    {
        session.checkOpen();
        coral.getEventWhiteboard().addRoleChangeListener(listener, object);
    }

    /** 
     * {@inheritDoc}
     */
    public void removeRoleChangeListener(RoleChangeListener listener, Object object)
    {
        session.checkOpen();
        coral.getEventWhiteboard().removeRoleChangeListener(listener, object);
    }

    /** 
     * {@inheritDoc}
     */
    public void addPermissionChangeListener(PermissionChangeListener listener, Object object)
    {
        session.checkOpen();
        coral.getEventWhiteboard().addPermissionChangeListener(listener, object);
    }

    /** 
     * {@inheritDoc}
     */
    public void removePermissionChangeListener(PermissionChangeListener listener, Object object)
    {
        session.checkOpen();
        coral.getEventWhiteboard().removePermissionChangeListener(listener, object);
    }

    // firing events ////////////////////////////////////////////////////////////////////////////

    /** 
     * {@inheritDoc}
     */
    public void firePermissionAssignmentChangeEvent(PermissionAssignment assignment, boolean added)
    {
        throw new UnsupportedOperationException("events may be fired by the system only");
    }

    /** 
     * {@inheritDoc}
     */
    public void fireRoleAssignmentChangeEvent(RoleAssignment assignment, boolean added)
    {
        throw new UnsupportedOperationException("events may be fired by the system only");
    }

    /** 
     * {@inheritDoc}
     */
    public void firePermissionAssociationChangeEvent(
        PermissionAssociation association,
        boolean added)
    {
        throw new UnsupportedOperationException("events may be fired by the system only");
    }

    /** 
     * {@inheritDoc}
     */
    public void fireRoleImplicationChangeEvent(RoleImplication implication, boolean added)
    {
        throw new UnsupportedOperationException("events may be fired by the system only");
    }

    /** 
     * {@inheritDoc}
     */
    public void fireResourceClassInheritanceChangeEvent(
        ResourceClassInheritance inheritance,
        boolean added)
    {
        throw new UnsupportedOperationException("events may be fired by the system only");
    }

    /** 
     * {@inheritDoc}
     */
    public void fireResourceClassAttributesChangeEvent(
        AttributeDefinition attribute,
        boolean added)
    {
        throw new UnsupportedOperationException("events may be fired by the system only");
    }

    /** 
     * {@inheritDoc}
     */
    public void fireResourceTreeChangeEvent(ResourceInheritance inheritance, boolean added)
    {
        throw new UnsupportedOperationException("events may be fired by the system only");
    }

    /** 
     * {@inheritDoc}
     */
    public void fireResourceOwnershipChangeEvent(ResourceOwnership ownership, boolean added)
    {
        throw new UnsupportedOperationException("events may be fired by the system only");
    }

    /** 
     * {@inheritDoc}
     */
    public void fireSubjectChangeEvent(Subject subject)
    {
        throw new UnsupportedOperationException("events may be fired by the system only");
    }

    /** 
     * {@inheritDoc}
     */
    public void fireRoleChangeEvent(Role role)
    {
        throw new UnsupportedOperationException("events may be fired by the system only");
    }

    /** 
     * {@inheritDoc}
     */
    public void fireResourceCreationEvent(Resource resource)
    {
        throw new UnsupportedOperationException("events may be fired by the system only");
    }

    /** 
     * {@inheritDoc}
     */
    public void fireResourceChangeEvent(Resource resource, Subject subject)
    {
        throw new UnsupportedOperationException("events may be fired by the system only");
    }

    /** 
     * {@inheritDoc}
     */
    public void fireResourceDeletionEvent(Resource resource)
    {
        throw new UnsupportedOperationException("events may be fired by the system only");
    }

    /** 
     * {@inheritDoc}
     */
    public void fireResourceTreeDeletionEvent(Resource resource)
    {
        throw new UnsupportedOperationException("events may be fired by the system only");
    }

    /** 
     * {@inheritDoc}
     */
    public void firePermissionChangeEvent(Permission permission)
    {
        throw new UnsupportedOperationException("events may be fired by the system only");
    }

    /** 
     * {@inheritDoc}
     */
    public void fireResourceClassChangeEvent(ResourceClass resourceClass)
    {
        throw new UnsupportedOperationException("events may be fired by the system only");
    }

    /** 
     * {@inheritDoc}
     */
    public void fireAttributeClassChangeEvent(AttributeClass attributeClass)
    {
        throw new UnsupportedOperationException("events may be fired by the system only");
    }

    /** 
     * {@inheritDoc}
     */
    public void fireAttributeDefinitionChangeEvent(AttributeDefinition attributeDefinition)
    {
        throw new UnsupportedOperationException("events may be fired by the system only");
    }
}
