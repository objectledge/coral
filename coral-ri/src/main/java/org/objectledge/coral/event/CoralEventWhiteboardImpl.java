package org.objectledge.coral.event;

import java.lang.reflect.Method;

import org.objectledge.coral.BackendException;
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
import org.objectledge.event.EventWhiteboard;

/**
 * Convenience wrapper around <code>EventService</code> implementation.
 *
 * @version $Id: CoralEventWhiteboardImpl.java,v 1.5 2004-03-09 15:46:47 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class CoralEventWhiteboardImpl
    implements CoralEventWhiteboard
{
    // Member objects ////////////////////////////////////////////////////////

    /** The {@link EventWhiteboard}. */
    private EventWhiteboard whiteboard;

    /** {@link
     * PermissionAssociationChangeListener#permissionsChanged(PermissionAssociation,boolean)} */
    private Method permissionAssociationChange;

    /** {@link
     * PermissionAssignmentChangeListener#permissionsChanged(PermissionAssignment,boolean)} */
    private Method permissionAssignmentChange;
    
    /** {@link
     * RoleAssignmentChangeListener#rolesChanged(RoleAssignment,boolean)} */
    private Method roleAssignmentChange;
    
    /** {@link
     * RoleImplicationChangeListener#roleChanged(RoleImplication,boolean)} */
    private Method roleImplicationChange;
    
    /** {@link
     * ResourceClassInheritanceChangeListener#inheritanceChanged(ResourceClassInheritance,boolean)}
     */
    private Method resourceClassInheritanceChange;
    
    /** {@link
     * ResourceClassAttributesChangeListener#attributesChanged(AttributeDefinition,boolean)} */
    private Method resourceClassAttributesChange;

    /** {@link
     * ResourceTreeChangeListener#resourceTreeChanged(ResourceInheritance,boolean)} */
    private Method resourceTreeChange;

    /** {@link
     * ResourceOwnershipChangeListener#resourceOwnershipChanged(ResourceOwnership,boolean)} */
    private Method resourceOwnershipChange;
    
    /** {@link
     * SubjectChangeListener#subjectChanged(Subject)} */
    private Method subjectChange;
    
    /** {@link
     * RoleChangeListener#roleChanged(Role)} */
    private Method roleChange;

    /** {@link
     * PermissionChangeListener#permissionChanged(Permission)} */
    private Method permissionChange;

    /** {@link
     * ResourceCreationListener#resourceCreated(Resource)} */
    private Method resourceCreation;

    /** {@link
     * ResourceChangeListener#resourceChanged(Resource,Subject)} */
    private Method resourceChange;

	/** {@link
	 * ResourceDeletionListener#resourceDeleted(Resource)} */
	private Method resourceDeletion;
	
	/** {@link
	 * ResourceTreeDeletionListener#resourceDeleted(Resource)} */
	private Method resourceTreeDeletion;
	
    /** {@link
     * ResourceClassChangeListener#resourceClassChanged(ResourceClass)} */
    private Method resourceClassChange;
    
    /** {@link
     * AttributeClassChangeListener#attributeClassChanged(AttributeClass)} */
    private Method attributeClassChange;

    /** {@link
     * AttributeDefinitionChangeListener#attributeDefinitionChanged(AttributeDefinition)} */
    private Method attributeDefinitionChange;

    // Initialization ////////////////////////////////////////////////////////

    /**
     * Constructs the {@link CoralEventWhiteboard} implementation.
     *
     * @param event an event whiteboard.
     */
    CoralEventWhiteboardImpl(EventWhiteboard event)
    {
        this.whiteboard = event;
        
        try
        {
            permissionAssociationChange = PermissionAssociationChangeListener.class.
                getMethod("permissionsChanged", 
                          new Class[] {PermissionAssociation.class, Boolean.TYPE});
            permissionAssignmentChange = PermissionAssignmentChangeListener.class.
                getMethod("permissionsChanged", 
                          new Class[] {PermissionAssignment.class, Boolean.TYPE});
            roleAssignmentChange = RoleAssignmentChangeListener.class.
                getMethod("rolesChanged", 
                          new Class[] {RoleAssignment.class, Boolean.TYPE});
            roleImplicationChange = RoleImplicationChangeListener.class.
                getMethod("roleChanged", 
                          new Class[] {RoleImplication.class, Boolean.TYPE});
            resourceClassInheritanceChange = ResourceClassInheritanceChangeListener.class.
                getMethod("inheritanceChanged", 
                          new Class[] {ResourceClassInheritance.class, Boolean.TYPE});
            resourceClassAttributesChange = ResourceClassAttributesChangeListener.class.
                getMethod("attributesChanged", 
                          new Class[] {AttributeDefinition.class, Boolean.TYPE});
            resourceTreeChange = ResourceTreeChangeListener.class.
                getMethod("resourceTreeChanged", 
                          new Class[] {ResourceInheritance.class, Boolean.TYPE});
            resourceOwnershipChange = ResourceOwnershipChangeListener.class.
                getMethod("resourceOwnershipChanged",
                          new Class[] {ResourceOwnership.class, Boolean.TYPE});
            subjectChange = SubjectChangeListener.class.
                getMethod("subjectChanged",
                          new Class[] {Subject.class});
            roleChange = RoleChangeListener.class.
                getMethod("roleChanged",
                          new Class[] {Role.class});
            permissionChange = PermissionChangeListener.class.
                getMethod("permissionChanged",
                          new Class[] {Permission.class});
            resourceCreation = ResourceCreationListener.class.
                getMethod("resourceCreated",
                          new Class[] {Resource.class});
            resourceChange = ResourceChangeListener.class.
                getMethod("resourceChanged",
                          new Class[] {Resource.class, Subject.class});
			resourceDeletion = ResourceDeletionListener.class.
				getMethod("resourceDeleted",
						  new Class[] {Resource.class});
			resourceTreeDeletion = ResourceTreeDeletionListener.class.
							getMethod("resourceTreeDeleted",
									  new Class[] {Resource.class});
            resourceClassChange = ResourceClassChangeListener.class.
                getMethod("resourceClassChanged",
                          new Class[] {ResourceClass.class});
            attributeClassChange = AttributeClassChangeListener.class.
                getMethod("attributeClassChanged",
                          new Class[] {AttributeClass.class});
            attributeDefinitionChange = AttributeDefinitionChangeListener.class.
                getMethod("attributeDefinitionChanged",
                          new Class[] {AttributeDefinition.class});
        }
        catch(NoSuchMethodException e)
        {
            throw new BackendException("Failed to reflect listener interfaces", e);
        }
    }

    // CoralEventWhiteboard interface ///////////////////////////////////////////////////////////

    // listener registration /////////////////////////////////////////////////

    /**
     * Adds a permission assignment change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void addPermissionAssignmentChangeListener(
        PermissionAssignmentChangeListener listener, Object object)
    {
        whiteboard.addListener(PermissionAssignmentChangeListener.class, listener, object);
    }
    
    /**
     * Removes a permission assignment change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void removePermissionAssignmentChangeListener(
        PermissionAssignmentChangeListener listener, Object object)
    {
        whiteboard.removeListener(PermissionAssignmentChangeListener.class, listener, object);
    }

    /**
     * Adds a role assignment change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void addRoleAssignmentChangeListener(
        RoleAssignmentChangeListener listener, Object object)
    {
        whiteboard.addListener(RoleAssignmentChangeListener.class, listener, object);
    }
    
    /**
     * Removes a role assignment change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void removeRoleAssignmentChangeListener(
        RoleAssignmentChangeListener listener, Object object)
    {
        whiteboard.removeListener(RoleAssignmentChangeListener.class, listener, object);
    }

    /**
     * Adds a permission association change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void addPermissionAssociationChangeListener(
        PermissionAssociationChangeListener listener, Object object)
    {
        whiteboard.addListener(PermissionAssociationChangeListener.class, listener, object);
    }
    
    /**
     * Removes a permission association change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void removePermissionAssociationChangeListener(
        PermissionAssociationChangeListener listener, Object object)
    {
        whiteboard.removeListener(PermissionAssociationChangeListener.class, listener, object);
    }

    /**
     * Adds a role implication change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void addRoleImplicationChangeListener(
        RoleImplicationChangeListener listener, Object object)
    {
        whiteboard.addListener(RoleImplicationChangeListener.class, listener, object);
    }
    
    /**
     * Removes a role implication change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void removeRoleImplicationChangeListener(
        RoleImplicationChangeListener listener, Object object)
    {
        whiteboard.removeListener(RoleImplicationChangeListener.class, listener, object);
    }

    /**
     * Adds a resource class inheritance change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void addResourceClassInheritanceChangeListener(
        ResourceClassInheritanceChangeListener listener, Object object)
    {
        whiteboard.addListener(ResourceClassInheritanceChangeListener.class, listener, object);
    }
    
    /**
     * Removes a resource class inheritance change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void removeResourceClassInheritanceChangeListener(
        ResourceClassInheritanceChangeListener listener, Object object)
    {
        whiteboard.removeListener(ResourceClassInheritanceChangeListener.class, listener, object);
    }

    /**
     * Adds a resource class attributes change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void addResourceClassAttributesChangeListener(
        ResourceClassAttributesChangeListener listener, Object object)
    {
        whiteboard.addListener(ResourceClassAttributesChangeListener.class, listener, object);
    }
    
    /**
     * Removes a resource class attributes change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void removeResourceClassAttributesChangeListener(
        ResourceClassAttributesChangeListener listener, Object object)
    {
        whiteboard.removeListener(ResourceClassAttributesChangeListener.class, listener, object);
    }

    /**
     * Adds a resource tree change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void addResourceTreeChangeListener(
        ResourceTreeChangeListener listener, Object object)
    {
        whiteboard.addListener(ResourceTreeChangeListener.class, listener, object);
    }
    
    /**
     * Removes a resource tree change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void removeResourceTreeChangeListener(
        ResourceTreeChangeListener listener, Object object)
    {
        whiteboard.removeListener(ResourceTreeChangeListener.class, listener, object);
    }

    /**
     * Adds a resource ownership change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void addResourceOwnershipChangeListener(
        ResourceOwnershipChangeListener listener, Object object)
    {
        whiteboard.addListener(ResourceOwnershipChangeListener.class, listener, object);
    }
    
    /**
     * Removes a resource ownership change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void removeResourceOwnershipChangeListener(
        ResourceOwnershipChangeListener listener, Object object)
    {
        whiteboard.removeListener(ResourceOwnershipChangeListener.class, listener, object);
    }

    /**
     * Adds a resource class change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void addResourceClassChangeListener(
        ResourceClassChangeListener listener, Object object)
    {
        whiteboard.addListener(ResourceClassChangeListener.class, listener, object);
    }
    
    /**
     * Removes a resource class change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void removeResourceClassChangeListener(
        ResourceClassChangeListener listener, Object object)
    {
        whiteboard.removeListener(ResourceClassChangeListener.class, listener, object);
    }

    /**
     * Adds a attribute class change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void addAttributeClassChangeListener(
        AttributeClassChangeListener listener, Object object)
    {
        whiteboard.addListener(AttributeClassChangeListener.class, listener, object);
    }
    
    /**
     * Removes a attribute class change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void removeAttributeClassChangeListener(
        AttributeClassChangeListener listener, Object object)
    {
        whiteboard.removeListener(AttributeClassChangeListener.class, listener, object);
    }

    /**
     * Adds a attribute definition change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void addAttributeDefinitionChangeListener(
        AttributeDefinitionChangeListener listener, Object object)
    {
        whiteboard.addListener(AttributeDefinitionChangeListener.class, listener, object);
    }
    
    /**
     * Removes a attribute definition change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void removeAttributeDefinitionChangeListener(
        AttributeDefinitionChangeListener listener, Object object)
    {
        whiteboard.removeListener(AttributeDefinitionChangeListener.class, listener, object);
    }

    /**
     * Adds a resource change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void addResourceCreationListener(
        ResourceCreationListener listener, Object object)
    {
        whiteboard.addListener(ResourceCreationListener.class, listener, object);
    }
    
    /**
     * Removes a resource creation listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void removeResourceCreationListener(
        ResourceCreationListener listener, Object object)
    {
        whiteboard.removeListener(ResourceCreationListener.class, listener, object);
    }

    /**
     * Adds a resource change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void addResourceChangeListener(
        ResourceChangeListener listener, Object object)
    {
        whiteboard.addListener(ResourceChangeListener.class, listener, object);
    }
    
    /**
     * Removes a resource change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void removeResourceChangeListener(
        ResourceChangeListener listener, Object object)
    {
        whiteboard.removeListener(ResourceChangeListener.class, listener, object);
    }

	/**
	 * Adds a resource deletion listener.
	 *
	 * @param listener the listener object.
	 * @param object the object the listener is recieving notifications on, or
	 *        <code>null</code> for all objects.
	 */
	public void addResourceDeletionListener(
		ResourceDeletionListener listener, Object object)
	{
		whiteboard.addListener(ResourceDeletionListener.class, listener, object);
	}

	/**
	 * Removes a resource deletion listener.
	 *
	 * @param listener the listener object.
	 * @param object the object the listener is recieving notifications on, or
	 *        <code>null</code> for all objects.
	 */
	public void removeResourceDeletionListener(
		ResourceDeletionListener listener, Object object)
	{
		whiteboard.removeListener(ResourceDeletionListener.class, listener, object);
	}

	/**
	 * Adds a resource tree deletion listener.
	 *
	 * @param listener the listener object.
	 * @param object the object the listener is recieving notifications on, or
	 *        <code>null</code> for all objects.
	 */
	public void addResourceTreeDeletionListener(
		ResourceTreeDeletionListener listener, Object object)
	{
		whiteboard.addListener(ResourceTreeDeletionListener.class, listener, object);
	}
    
	/**
	 * Removes a resource tree deletion listener.
	 *
	 * @param listener the listener object.
	 * @param object the object the listener is recieving notifications on, or
	 *        <code>null</code> for all objects.
	 */
	public void removeResourceTreeDeletionListener(
		ResourceTreeDeletionListener listener, Object object)
	{
		whiteboard.removeListener(ResourceTreeDeletionListener.class, listener, object);
	}
	
    /**
     * Adds a subject change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void addSubjectChangeListener(
        SubjectChangeListener listener, Object object)
    {
        whiteboard.addListener(SubjectChangeListener.class, listener, object);
    }
    
    /**
     * Removes a subject change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void removeSubjectChangeListener(
        SubjectChangeListener listener, Object object)
    {
        whiteboard.removeListener(SubjectChangeListener.class, listener, object);
    }
    
    /**
     * Adds a role change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void addRoleChangeListener(
        RoleChangeListener listener, Object object)
    {
        whiteboard.addListener(RoleChangeListener.class, listener, object);
    }
    
    /**
     * Removes a role change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void removeRoleChangeListener(
        RoleChangeListener listener, Object object)
    {
        whiteboard.removeListener(RoleChangeListener.class, listener, object);
    }

    /**
     * Adds a permission change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void addPermissionChangeListener(
        PermissionChangeListener listener, Object object)
    {
        whiteboard.addListener(PermissionChangeListener.class, listener, object);
    }
    
    /**
     * Removes a permission change listener.
     *
     * @param listener the listener object.
     * @param object the object the listener is recieving notifications on, or
     *        <code>null</code> for all objects.
     */
    public void removePermissionChangeListener(
        PermissionChangeListener listener, Object object)
    {
        whiteboard.removeListener(PermissionChangeListener.class, listener, object);
    }

    // event firing //////////////////////////////////////////////////////////

    /**
     * Fires a permission assignment change event.
     *
     * @param assignment the <code>PermissionAssignment</code>.
     * @param added <code>true</code> if the assignment was added,
     * <code>false</code> if removed.
     */
    public void firePermissionAssignmentChangeEvent(
        PermissionAssignment assignment, boolean added)
    {
        Object[] args = new Object[] { assignment, new Boolean(added) };
                
        whiteboard.fireEvent(permissionAssignmentChange, args, assignment.getResource());
        whiteboard.fireEvent(permissionAssignmentChange, args, assignment.getRole());
        whiteboard.fireEvent(permissionAssignmentChange, args, assignment.getPermission());
        whiteboard.fireEvent(permissionAssignmentChange, args, null);
    }
    
    /** 
     * Event.Fires a role assignment change envent.
     *
     * @param assignment the <code>RoleAssignment</code>.
     * @param added <code>true</code> if the assignment was added,
     * <code>false</code> if removed.
     */
    public void fireRoleAssignmentChangeEvent(
        RoleAssignment assignment, boolean added)
    {
        Object[] args = new Object[] { assignment, new Boolean(added) };
                
        whiteboard.fireEvent(roleAssignmentChange, args, assignment.getSubject());
        whiteboard.fireEvent(roleAssignmentChange, args, assignment.getRole());
        whiteboard.fireEvent(roleAssignmentChange, args, null);
    }

    /**
     * Event.Fires a permission association change event.
     *
     * @param association the <code>PermissionAssociation</code>.
     * @param added <code>true</code> if the association was added,
     * <code>false</code> if removed.
     */
    public void firePermissionAssociationChangeEvent(
        PermissionAssociation association, boolean added)
    {
        Object[] args = new Object[] { association, new Boolean(added) };
                
        whiteboard.fireEvent(permissionAssociationChange, args, association.getResourceClass());
        whiteboard.fireEvent(permissionAssociationChange, args, association.getPermission());
        whiteboard.fireEvent(permissionAssociationChange, args, null);
    }

    /** 
     * Event.Fires a role implication change envent.
     *
     * @param implication the <code>RoleImplication</code>.
     * @param added <code>true</code> if the implication was added,
     * <code>false</code> if removed.
     */
    public void fireRoleImplicationChangeEvent(
        RoleImplication implication, boolean added)
    {
        Object[] args = new Object[] { implication, new Boolean(added) };
                
        whiteboard.fireEvent(roleImplicationChange, args, implication.getSuperRole());
        whiteboard.fireEvent(roleImplicationChange, args, implication.getSubRole());
        whiteboard.fireEvent(roleImplicationChange, args, null);
    }

    /** 
     * Event.Fires a resource class inheritance change envent.
     *
     * @param inheritance the <code>ResourceClassInheritance</code>.
     * @param added <code>true</code> if the inheritance was added,
     * <code>false</code> if removed.
     */
    public void fireResourceClassInheritanceChangeEvent(
        ResourceClassInheritance inheritance, boolean added)
    {
        Object[] args = new Object[] { inheritance, new Boolean(added) };
                
        whiteboard.fireEvent(resourceClassInheritanceChange, args, inheritance.getParent());
        whiteboard.fireEvent(resourceClassInheritanceChange, args, inheritance.getChild());
        whiteboard.fireEvent(resourceClassInheritanceChange, args, null);
    }

    /** 
     * Event.Fires a resource class attributes change envent.
     *
     * @param attribute the <code>AttributesDefinition</code>.
     * @param added <code>true</code> if the attributes was added, <code>false</code> if removed.
     */
    public void fireResourceClassAttributesChangeEvent(
        AttributeDefinition attribute, boolean added)
    {
        Object[] args = new Object[] { attribute, new Boolean(added) };
                
        whiteboard.fireEvent(resourceClassAttributesChange, args, attribute.getDeclaringClass());
        whiteboard.fireEvent(resourceClassAttributesChange, args, null);
    }

    /**
     * Event.Fires a resource tree change event.
     *
     * @param inheritance inheritance information.
     * @param added <code>true</code> if the inheritace record was added, <code>false</code> if
     * removed.
     */
    public void fireResourceTreeChangeEvent(
        ResourceInheritance inheritance, boolean added)
    {
        Object[] args = new Object[] { inheritance, new Boolean(added) };
                
        whiteboard.fireEvent(resourceTreeChange, args, inheritance.getParent());
        whiteboard.fireEvent(resourceTreeChange, args, inheritance.getChild());
        whiteboard.fireEvent(resourceTreeChange, args, null);
    }

    /**
     * Event.Fires a resource ownership change event.
     *
     * @param ownership ownership information.
     * @param added <code>true</code> if the ownership record was added, <code>false</code> if
     * removed.
     */
    public void fireResourceOwnershipChangeEvent(
        ResourceOwnership ownership, boolean added)
    {
        Object[] args = new Object[] { ownership, new Boolean(added) };
                
        whiteboard.fireEvent(resourceOwnershipChange, args, ownership.getOwner());
        whiteboard.fireEvent(resourceOwnershipChange, args, ownership.getResource());
        whiteboard.fireEvent(resourceOwnershipChange, args, null);
    }

    /**
     * Fires a subject change event.
     *
     * @param subject the subject.
     */
    public void fireSubjectChangeEvent(Subject subject)
    {
        Object[] args = new Object[] { subject };
        
        whiteboard.fireEvent(subjectChange, args, subject);
        whiteboard.fireEvent(subjectChange, args, null);
    }   
    
    /**
     * Fires a role change event.
     *
     * @param role the role.
     */
    public void fireRoleChangeEvent(Role role)
    {
        Object[] args = new Object[] { role };
        
        whiteboard.fireEvent(roleChange, args, role);
        whiteboard.fireEvent(roleChange, args, null);
    }   

    /**
     * Fires a resource deletion event.
     *
     * @param resource the resource.
     */
    public void fireResourceCreationEvent(Resource resource)
    {
        Object[] args = new Object[] { resource };
        
        if(resource.getParent() != null)
        {
            whiteboard.fireEvent(resourceCreation, args, resource.getParent());
        }
        whiteboard.fireEvent(resourceCreation, args, resource.getResourceClass());
        ResourceClass[] parents = resource.getResourceClass().getParentClasses();
        for(int i=0; i<parents.length; i++)
        {
            whiteboard.fireEvent(resourceCreation, args, parents[i]);
        }
        whiteboard.fireEvent(resourceCreation, args, null);
    }   

    /**
     * Fires a resource change event.
     *
     * @param resource the resource.
     * @param subject the subject that performed the change.
     */
    public void fireResourceChangeEvent(Resource resource, Subject subject)
    {
        Object[] args = new Object[] { resource, subject };
        
        whiteboard.fireEvent(resourceChange, args, resource);
        whiteboard.fireEvent(resourceChange, args, resource.getResourceClass());
        ResourceClass[] parents = resource.getResourceClass().getParentClasses();
        for(int i=0; i<parents.length; i++)
        {
			whiteboard.fireEvent(resourceChange, args, parents[i]);
        }
        whiteboard.fireEvent(resourceChange, args, null);
    }   

	/**
	 * Fires a resource deletion event.
	 *
	 * @param resource the resource.
	 */
	public void fireResourceDeletionEvent(Resource resource)
	{
		Object[] args = new Object[] { resource };
        
		whiteboard.fireEvent(resourceDeletion, args, resource);
		whiteboard.fireEvent(resourceDeletion, args, resource.getResourceClass());
		ResourceClass[] parents = resource.getResourceClass().getParentClasses();
		for(int i=0; i<parents.length; i++)
		{
			whiteboard.fireEvent(resourceDeletion, args, parents[i]);
		}
		whiteboard.fireEvent(resourceDeletion, args, null);
	}   

	/**
	 * Fires a resource tree deletion event.
	 *
	 * @param resource the resource.
	 */
	public void fireResourceTreeDeletionEvent(Resource resource)
	{
		Object[] args = new Object[] { resource };
		whiteboard.fireEvent(resourceTreeDeletion, args, resource);
		whiteboard.fireEvent(resourceTreeDeletion, args, resource.getResourceClass());
		ResourceClass[] parents = resource.getResourceClass().getParentClasses();
		for(int i=0; i<parents.length; i++)
		{
			whiteboard.fireEvent(resourceTreeDeletion, args, parents[i]);
		}
		whiteboard.fireEvent(resourceTreeDeletion, args, null);
	}
	
    /**
     * Fires a permission change event.
     *
     * @param permission the permission.
     */
    public void firePermissionChangeEvent(Permission permission)
    {
        Object[] args = new Object[] { permission };
        
        whiteboard.fireEvent(permissionChange, args, permission);
        whiteboard.fireEvent(permissionChange, args, null);
    }   

    /**
     * Fires a resource class change event.
     *
     * @param resourceClass the resource class.
     */
    public void fireResourceClassChangeEvent(ResourceClass resourceClass)
    {
        Object[] args = new Object[] { resourceClass };
        
        whiteboard.fireEvent(resourceClassChange, args, resourceClass);
        whiteboard.fireEvent(resourceClassChange, args, null);
    }   

    /**
     * Fires a attribute class change event.
     *
     * @param attributeClass the attribute class.
     */
    public void fireAttributeClassChangeEvent(AttributeClass attributeClass)
    {
        Object[] args = new Object[] { attributeClass };
        
        whiteboard.fireEvent(attributeClassChange, args, attributeClass);
        whiteboard.fireEvent(attributeClassChange, args, null);
    }   

    /**
     * Fires a attribute definition change event.
     *
     * @param attributeDefinition the attribute definition.
     */
    public void fireAttributeDefinitionChangeEvent(AttributeDefinition attributeDefinition)
    {
        Object[] args = new Object[] { attributeDefinition };
        
        whiteboard.fireEvent(attributeDefinitionChange, args, attributeDefinition);
        whiteboard.fireEvent(attributeDefinitionChange, args, null);
    }   
}
