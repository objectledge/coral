package org.objectledge.coral.event;

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
 * Convenience wrapper around <code>EventService</code>.
 *
 * @version $Id: CoralEventWhiteboard.java,v 1.2 2004-12-23 07:18:31 rafal Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface CoralEventWhiteboard
{
    // listener registration /////////////////////////////////////////////////

    /**
     * Adds a permission assignment change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void addPermissionAssignmentChangeListener(
        PermissionAssignmentChangeListener listener, Object object);
    
    /**
     * Removes a permission assignment change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void removePermissionAssignmentChangeListener(
        PermissionAssignmentChangeListener listener, Object object);

    /**
     * Adds a role assignment change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void addRoleAssignmentChangeListener(
        RoleAssignmentChangeListener listener, Object object);
    
    /**
     * Removes a role assignment change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void removeRoleAssignmentChangeListener(
        RoleAssignmentChangeListener listener, Object object);

    /**
     * Adds a permission association change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void addPermissionAssociationChangeListener(
        PermissionAssociationChangeListener listener, Object object);
    
    /**
     * Removes a permission association change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void removePermissionAssociationChangeListener(
        PermissionAssociationChangeListener listener, Object object);

    /**
     * Adds a role implication change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void addRoleImplicationChangeListener(
        RoleImplicationChangeListener listener, Object object);
    
    /**
     * Removes a role implication change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void removeRoleImplicationChangeListener(
        RoleImplicationChangeListener listener, Object object);

    /**
     * Adds a resource class inheritance change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void addResourceClassInheritanceChangeListener(
        ResourceClassInheritanceChangeListener listener, Object object);
    
    /**
     * Removes a resource class inheritance change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void removeResourceClassInheritanceChangeListener(
        ResourceClassInheritanceChangeListener listener, Object object);

    /**
     * Adds a resource class attributes change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void addResourceClassAttributesChangeListener(
        ResourceClassAttributesChangeListener listener, Object object);
    
    /**
     * Removes a resource class attributes change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void removeResourceClassAttributesChangeListener(
        ResourceClassAttributesChangeListener listener, Object object);

    /**
     * Adds a resource tree change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void addResourceTreeChangeListener(
        ResourceTreeChangeListener listener, Object object);
    
    /**
     * Removes a resource tree change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void removeResourceTreeChangeListener(
        ResourceTreeChangeListener listener, Object object);

    /**
     * Adds a resource ownership change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void addResourceOwnershipChangeListener(
        ResourceOwnershipChangeListener listener, Object object);
    
    /**
     * Removes a resource ownership change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void removeResourceOwnershipChangeListener(
        ResourceOwnershipChangeListener listener, Object object);

    /**
     * Adds a resource class change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void addResourceClassChangeListener(
        ResourceClassChangeListener listener, Object object);
    
    /**
     * Removes a resource class change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void removeResourceClassChangeListener(
        ResourceClassChangeListener listener, Object object);

    /**
     * Adds a attribute class change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void addAttributeClassChangeListener(
        AttributeClassChangeListener listener, Object object);
    
    /**
     * Removes a attribute class change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void removeAttributeClassChangeListener(
        AttributeClassChangeListener listener, Object object);

    /**
     * Adds a attribute definition change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void addAttributeDefinitionChangeListener(
        AttributeDefinitionChangeListener listener, Object object);
    
    /**
     * Removes a attribute definition change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void removeAttributeDefinitionChangeListener(
        AttributeDefinitionChangeListener listener, Object object);

    /**
     * Adds a resource creation listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void addResourceCreationListener(
        ResourceCreationListener listener, Object object);
    
    /**
     * Removes a resource creation listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void removeResourceCreationListener(
        ResourceCreationListener listener, Object object);

    /**
     * Adds a resource change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void addResourceChangeListener(
        ResourceChangeListener listener, Object object);
    
    /**
     * Removes a resource change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void removeResourceChangeListener(
        ResourceChangeListener listener, Object object);

	/**
	 * Adds a resource deletion listener.
	 *
	 * @param listener the listener object.
	 * @param object the object the listener is recieving notifications on, or
	 *        <code>null</code> for all objects.
	 */
	public void addResourceDeletionListener(
		ResourceDeletionListener listener, Object object);
    
	/**
	 * Removes a resource deletion listener.
	 *
	 * @param listener the listener object.
	 * @param object the object the listener is recieving notifications on, or
	 *        <code>null</code> for all objects.
	 */
	public void removeResourceDeletionListener(
		ResourceDeletionListener listener, Object object);

	/**
	 * Adds a resource tree deletion listener.
	 *
	 * @param listener the listener object.
	 * @param object the object the listener is recieving notifications on, or
	 *        <code>null</code> for all objects.
	 */
	public void addResourceTreeDeletionListener(
		ResourceTreeDeletionListener listener, Object object);
    
	/**
	 * Removes a resource tree deletion listener.
	 *
	 * @param listener the listener object.
	 * @param object the object the listener is recieving notifications on, or
	 *        <code>null</code> for all objects.
	 */
	public void removeResourceTreeDeletionListener(
		ResourceTreeDeletionListener listener, Object object);

    /**
     * Adds a subject change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void addSubjectChangeListener(
        SubjectChangeListener listener, Object object);
    
    /**
     * Removes a subject change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void removeSubjectChangeListener(
        SubjectChangeListener listener, Object object);
    
    /**
     * Adds a role change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void addRoleChangeListener(
        RoleChangeListener listener, Object object);
    
    /**
     * Removes a role change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void removeRoleChangeListener(
        RoleChangeListener listener, Object object);

    /**
     * Adds a permission change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void addPermissionChangeListener(
        PermissionChangeListener listener, Object object);
    
    /**
     * Removes a permission change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void removePermissionChangeListener(
        PermissionChangeListener listener, Object object);

    // event firing //////////////////////////////////////////////////////////

    /**
     * Fires a permission assignment change event.
     *
     * @param assignment the <code>PermissionAssignment</code>.
     * @param added <code>true</code> if the assignment was added,
     * <code>false</code> if removed.
     */
    public void firePermissionAssignmentChangeEvent(
        PermissionAssignment assignment, boolean added);
    /** 
     * Fires a role assignment change envent.
     *
     * @param assignment the <code>RoleAssignment</code>.
     * @param added <code>true</code> if the assignment was added,
     * <code>false</code> if removed.
     */
    public void fireRoleAssignmentChangeEvent(
        RoleAssignment assignment, boolean added);

    /**
     * Fires a permission association change event.
     *
     * @param association the <code>PermissionAssociation</code>.
     * @param added <code>true</code> if the association was added,
     * <code>false</code> if removed.
     */
    public void firePermissionAssociationChangeEvent(
        PermissionAssociation association, boolean added);

    /** 
     * Fires a role implication change envent.
     *
     * @param implication the <code>RoleImplication</code>.
     * @param added <code>true</code> if the implication was added,
     * <code>false</code> if removed.
     */
    public void fireRoleImplicationChangeEvent(
        RoleImplication implication, boolean added);

    /** 
     * Fires a resource class inheritance change envent.
     *
     * @param inheritance the <code>ResourceClassInheritance</code>.
     * @param added <code>true</code> if the inheritance was added,
     * <code>false</code> if removed.
     */
    public void fireResourceClassInheritanceChangeEvent(
        ResourceClassInheritance inheritance, boolean added);

    /** 
     * Fires a resource class attributes change envent.
     *
     * @param attribute the <code>AttributesDefinition</code>.
     * @param added <code>true</code> if the attributes was added,
     * <code>false</code> if removed.
     */
    public void fireResourceClassAttributesChangeEvent(
        AttributeDefinition attribute, boolean added);

    /**
     * Fires a resource tree change event.
     *
     * @param inheritance inheritance inforamtion..
     * @param added <code>true</code> if the inheritacnce record was added,
     * <code>false</code> if removed.
     */
    public void fireResourceTreeChangeEvent(
        ResourceInheritance inheritance, boolean added);

    /**
     * Fires a resource tree change event.
     *
     * @param ownership ownership information.
     * @param added <code>true</code> if the ownership record was added,
     * <code>false</code> if removed.
     */
    public void fireResourceOwnershipChangeEvent(
        ResourceOwnership ownership, boolean added);

    /**
     * Fires a subject change event.
     *
     * @param subject the subject.
     */
    public void fireSubjectChangeEvent(Subject subject);
    
    /**
     * Fires a role change event.
     *
     * @param role the role.
     */
    public void fireRoleChangeEvent(Role role);

    /**
     * Fires a resource creation event.
     *
     * @param resource the resource.
     */
    public void fireResourceCreationEvent(Resource resource);

    /**
     * Fires a resource change event.
     *
     * @param resource the resource.
     * @param subject the subject that performed the change.
     */
    public void fireResourceChangeEvent(Resource resource, Subject subject);

	/**
	 * Fires a resource deletion event.
	 *
	 * @param resource the resource.
	 */
	public void fireResourceDeletionEvent(Resource resource);

	/**
	 * Fires a resource tree deletion event.
     *  
	 * @param resource the resource.
	 */
	public void fireResourceTreeDeletionEvent(Resource resource);
	
    /**
     * Fires a permission change event.
     *
     * @param permission the permission.
     */
    public void firePermissionChangeEvent(Permission permission);

    /**
     * Fires a resource class change event.
     *
     * @param resourceClass the resource class.
     */
    public void fireResourceClassChangeEvent(ResourceClass resourceClass);

    /**
     * Fires a attribute class change event.
     *
     * @param attributeClass the attribute class.
     */
    public void fireAttributeClassChangeEvent(AttributeClass attributeClass);

    /**
     * Fires a attribute definition change event.
     *
     * @param attributeDefinition the attribute definition.
     */
    public void fireAttributeDefinitionChangeEvent(AttributeDefinition attributeDefinition);
}
