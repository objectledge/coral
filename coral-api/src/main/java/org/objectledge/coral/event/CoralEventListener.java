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
 * Event multiplexer class.
 *
 * @version $Id: CoralEventListener.java,v 1.5 2004-02-27 15:20:18 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public abstract class CoralEventListener
    implements PermissionAssociationChangeListener,
               PermissionAssignmentChangeListener,
               RoleAssignmentChangeListener,
               RoleImplicationChangeListener,
               ResourceClassInheritanceChangeListener,
               ResourceClassAttributesChangeListener,
               ResourceTreeChangeListener,
               ResourceOwnershipChangeListener,
               SubjectChangeListener,
               RoleChangeListener,
               PermissionChangeListener,
               ResourceCreationListener,
               ResourceChangeListener,
               ResourceDeletionListener,
               ResourceTreeDeletionListener,
               ResourceClassChangeListener,
               AttributeClassChangeListener,
               AttributeDefinitionChangeListener
{
    // Memeber objects ///////////////////////////////////////////////////////
    
    /** The {@link CoralEventWhiteboard}. */
    private CoralEventWhiteboard whiteboard;

    // initialization ////////////////////////////////////////////////////////

    /**
     * Register the multiplexer with an <code>EventService</code>.
     *
     * @param whiteboard the event whiteboard.
     */
    public void register(CoralEventWhiteboard whiteboard)
    {
        whiteboard.addPermissionAssignmentChangeListener( 
            this, null);
        whiteboard.addPermissionAssociationChangeListener(
            this, null);
        whiteboard.addRoleAssignmentChangeListener(
            this, null);
        whiteboard.addRoleImplicationChangeListener(
            this, null);
        whiteboard.addResourceClassInheritanceChangeListener(
            this, null);
        whiteboard.addResourceClassAttributesChangeListener(
            this, null);
        whiteboard.addResourceTreeChangeListener(
            this, null);
        whiteboard.addResourceOwnershipChangeListener(
            this, null);
        whiteboard.addSubjectChangeListener(
            this, null);
        whiteboard.addRoleChangeListener(
            this, null);
        whiteboard.addPermissionChangeListener(
            this, null);
        whiteboard.addResourceCreationListener(
            this, null);
        whiteboard.addResourceChangeListener(
            this, null);
		whiteboard.addResourceDeletionListener(
			this, null);
        whiteboard.addResourceClassChangeListener(
            this, null);
        whiteboard.addAttributeClassChangeListener(
            this, null);
        whiteboard.addAttributeDefinitionChangeListener(
            this, null);
        this.whiteboard = whiteboard;
    }
    
    /**
     * Unregister the multiplexer from an <code>EventService</code>.
     */
    public void unregister()
    {
        if(whiteboard != null)
        {   
            whiteboard.removePermissionAssignmentChangeListener( 
                this, null);
            whiteboard.removePermissionAssociationChangeListener(
                this, null);
            whiteboard.removeRoleAssignmentChangeListener(
                this, null);
            whiteboard.removeRoleImplicationChangeListener(
                this, null);
            whiteboard.removeResourceClassInheritanceChangeListener(
                this, null);
            whiteboard.removeResourceClassAttributesChangeListener(
                this, null);
            whiteboard.removeResourceTreeChangeListener(
                this, null);
            whiteboard.removeResourceOwnershipChangeListener(
                this, null);
            whiteboard.removeSubjectChangeListener(
                this, null);
            whiteboard.removeRoleChangeListener(
                this, null);
            whiteboard.removePermissionChangeListener(
                this, null);
            whiteboard.removeResourceCreationListener(
                this, null);
            whiteboard.removeResourceChangeListener(
                this, null);
			whiteboard.removeResourceDeletionListener(
				this, null);
            whiteboard.removeResourceClassChangeListener(
                this, null);
            whiteboard.removeAttributeClassChangeListener(
                this, null);
            whiteboard.removeAttributeDefinitionChangeListener(
                this, null);
            whiteboard = null;
        }
    }    

    // processing method to be implemented by descendants ////////////////////
    
    /**
     * The event processing method to be implemented by concrete multiplexer
     * classess. 
     *
     * @param type the type of the event (same as the name of the interface
     *        with 'Listener' suffix removed).
     * @param entity1 the identifier of the first entity involved.
     * @param entity2 the identifier of the second entity involved or
     *        <code>1</code>.
     * @param entity3 the identifier of the third entity involved or
     *        <code>-1</code>
     * @param added the 'added' argument of the event methods
     */
    protected abstract void event(String type, long entity1, long entity2,
                                  long entity3, boolean added);
    
    // PermissionAssociationChangeListener interface /////////////////////////

    /**
     * Called when permission associations on a resource class / perimission
     * change. 
     *
     * @param association the permission association.
     * @param added <code>true</code> if the permission association was added,
     *        <code>false</code> if removed.
     */
    public void permissionsChanged(PermissionAssociation association, boolean added)
    {
        event("PermissionAssociationChange",
              association.getResourceClass().getId(),
              association.getPermission().getId(),
              -1,
              added);
    }
    
    // PermissionAssignmentChangeListener interface //////////////////////////

    /**
     * Called when permission assignemts on the <code>resource</code> change.
     *
     * @param assignment the permission assignment.
     * @param added <code>true</code> if the permission assignment was added,
     *        <code>false</code> if removed.
     */
    public void permissionsChanged(PermissionAssignment assignment, boolean added)
    {
        event("PermissionAssignmentChange",
              assignment.getResource().getId(),
              assignment.getRole().getId(),
              assignment.getPermission().getId(),
              added
        );
    }
    
    // RoleAssignmentChangeListener inteface /////////////////////////////////
    
    /**
     * Called when role assignemts on the <code>subject</code> change.
     *
     * @param ra the role assignment.
     * @param added <code>true</code> if the role assignment was added,
     *        <code>false</code> if removed.
     */
    public void rolesChanged(RoleAssignment ra, boolean added)
    {
        event("RoleAssignmentChange", 
              ra.getSubject().getId(),
              ra.getRole().getId(),
              -1,
              added
        );
    }

    // RoleImplicationChangeListener interface ///////////////////////////////
    
    /**
     * Called when role implications change.
     *
     * @param implication the {@link RoleImplication}.
     * @param added <code>true</code> if the implication was added,
     *        <code>false</code> if removed.
     */
    public void roleChanged(RoleImplication implication, boolean added)
    {
        event("RoleImplicationChange",
              implication.getSuperRole().getId(),
              implication.getSubRole().getId(),
              -1,
              added
        );
    }

    // ResourceClassInheritanceChangeListener inteface ///////////////////////

    /**
     * Called when resource class inheritance relationships change.
     *
     * @param inheritance the {@link ResourceClassInheritance}.
     * @param added <code>true</code> if the relationship was added,
     *        <code>false</code> if removed.
     */
    public void inheritanceChanged(ResourceClassInheritance inheritance, boolean added)
    {
        event("ResourceClassInheritanceChange", 
              inheritance.getParent().getId(),
              inheritance.getChild().getId(),
              -1,
              added
        );
    }

    // ResourceClassAttributesChangeListener interface ///////////////////////

    /**
     * Called when resource class attribute declarations change.
     *
     * @param attribute the {@link AttributeDefinition}.
     * @param added <code>true</code> if the attribute was added,
     *        <code>false</code> if removed.
     */
    public void attributesChanged(AttributeDefinition<?> attribute, boolean added)
    {
        event("ResourceClassAttributesChange",
              attribute.getId(),
              -1,
              -1,
              added);
    }
    
    // ResourceTreeChangeListener interface //////////////////////////////////
    
    /**
     * Called when the parent of the <code>resource</code> changes.
     *
     * @param item the resource inheritance record.
     * @param added <code>true</code> if the relationship was added,
     *        <code>false</code> if removed.
     */
    public void resourceTreeChanged(ResourceInheritance item, boolean added)
    {
        event("ResourceTreeChange",
              item.getParent().getId(),
              item.getChild().getId(),
              -1,
              added);
    }

    /**
     * Called when resource ownership changes.
     *
     * @param item the ownership information
     * @param added <code>true</code> if the relationship was added,
     *        <code>false</code> if removed.
     */
    public void resourceOwnershipChanged(ResourceOwnership item, boolean added)
    {
        event("ResourceOwnershipChange",
              item.getOwner().getId(),
              item.getResource().getId(),
              -1,
              added);
    }

    /**
     * Called when <code>Subject</code>'s data change.
     *
     * @param item the Subject that changed.
     */
    public void subjectChanged(Subject item)
    {
        event("SubjectChange",
              item.getId(),
              -1,
              -1,
              false);
    }
    
    /**
     * Called when <code>Role</code>'s data change.
     *
     * @param item the Role that changed.
     */
    public void roleChanged(Role item)
    {
        event("RoleChange",
              item.getId(),
              -1,
              -1,
              false);
    }

    /**
     * Called when <code>Permission</code>'s data change.
     *
     * @param item the Permission that changed.
     */
    public void permissionChanged(Permission item)
    {
        event("PermissionChange",
              item.getId(),
              -1,
              -1,
              false);
    }

    /**
     * Called when <code>Resource</code> is being created.
     *
     * @param resource the newly created resorce.
     */
    public void resourceCreated(Resource resource)
    {
        event("ResourceCreation",
              resource.getId(),
              -1,
              -1,
              false);
    }

    /**
     * Called when <code>Resource</code>'s data change.
     *
     * @param item the resource that changed.
     * @param subject the subject that performed the change.
     */
    public void resourceChanged(Resource item, Subject subject)
    {
        event("ResourceChange",
              item.getId(),
              subject != null ? subject.getId() : -1,
              -1,
              false);
    }

	/**
	 * Called when <code>Resource</code> is deleted.
	 *
	 * @param item the resource that was deleted.
	 */
	public void resourceDeleted(Resource item)
	{
		event("ResourceDeletion",
			  item.getId(),
			  -1,
			  -1,
			  false);
	}

    /**
     * Called when <code>Resource</code> is deleted.
     *
     * @param item the resource that was deleted.
     */
    public void resourceTreeDeleted(Resource item)
    {
        event("ResourceTreeDeletion",
              item.getId(),
              -1,
              -1,
              false);
    }

    /**
     * Called when <code>ResourceClass</code>'s data change.
     *
     * @param item the ResourceClass that changed.
     */
    public void resourceClassChanged(ResourceClass item)
    {
        event("ResourceClassChange",
              item.getId(),
              -1,
              -1,
              false);
    }

    /**
     * Called when <code>AttributeClass</code>'s data change.
     *
     * @param item the AttributeClass that changed.
     */
    public void attributeClassChanged(AttributeClass<?> item)
    {
        event("AttributeClassChange",
              item.getId(),
              -1,
              -1,
              false);
    }

    /**
     * Called when <code>AttributeDefinition</code>'s data change.
     *
     * @param item the AttributeDefinition that changed.
     */
    public void attributeDefinitionChanged(AttributeDefinition<?> item)
    {
        event("AttributeDefinitionChange",
              item.getId(),
              -1,
              -1,
              false);
    }
}

