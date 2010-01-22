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
 * Redirects events among inbound, outbound, and local forwarders.
 *
 * <p>Events fired on this forwarder will be passed to the local and outbound
 * forwarders, and listeners registered to this forwarder will be registerd to
 * local and inbound forwarders. Note that you shouldn't register to this
 * forwarder and fire events on it on the same time, because you would receive
 * echoes of your own events. Rather you should register to the redirector and
 * fire on outbound, or register on inbound and fire on the redirector.</p>
 */
public class CoralEventRedirector
    implements CoralEventWhiteboard
{
    // memeber objects ///////////////////////////////////////////////////////

    /** Inbound forwarder. */
    private CoralEventWhiteboard in;
    
    /** Local forwarder. */
    private CoralEventWhiteboard local;

    /** Outbound forwarder. */
    private CoralEventWhiteboard out;
    
    // initialization ////////////////////////////////////////////////////////

    /**
     * Constructs an event redirector.
     *
     * @param in the inbound forwarder.
     * @param local the local forwarder.
     * @param out the outbound forwarder.
     */
    public CoralEventRedirector(CoralEventWhiteboard in, CoralEventWhiteboard local, 
                              CoralEventWhiteboard out)
    {
        this.in = in;
        this.local = local;
        this.out = out;
    }

    // listener registration /////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public void addPermissionAssignmentChangeListener(
        PermissionAssignmentChangeListener listener, Object object)
    {
        in.addPermissionAssignmentChangeListener(listener, object);
        local.addPermissionAssignmentChangeListener(listener, object);
    }
    
    /**
     * {@inheritDoc}
     */
    public void removePermissionAssignmentChangeListener(
        PermissionAssignmentChangeListener listener, Object object)
    {
        in.removePermissionAssignmentChangeListener(listener, object);
        local.removePermissionAssignmentChangeListener(listener, object);
    }

    /**
     * {@inheritDoc}
     */
    public void addRoleAssignmentChangeListener(
        RoleAssignmentChangeListener listener, Object object)    
    {
        in.addRoleAssignmentChangeListener(listener, object);
        local.addRoleAssignmentChangeListener(listener, object);
    }
    
    /**
     * {@inheritDoc}
     */
    public void removeRoleAssignmentChangeListener(
        RoleAssignmentChangeListener listener, Object object)
    {
        in.removeRoleAssignmentChangeListener(listener, object);
        local.removeRoleAssignmentChangeListener(listener, object);
    }
    
    /**
     * {@inheritDoc}
     */
    public void addPermissionAssociationChangeListener(
        PermissionAssociationChangeListener listener, Object object)
    {
        in.addPermissionAssociationChangeListener(listener, object);
        local.addPermissionAssociationChangeListener(listener, object);
    }
    
    /**
     * {@inheritDoc}
     */
    public void removePermissionAssociationChangeListener(
        PermissionAssociationChangeListener listener, Object object)
    {
        in.removePermissionAssociationChangeListener(listener, object);
        local.removePermissionAssociationChangeListener(listener, object);
    }

    /**
     * {@inheritDoc}
     */
    public void addRoleImplicationChangeListener(
        RoleImplicationChangeListener listener, Object object)
    {
        in.addRoleImplicationChangeListener(listener, object);
        local.addRoleImplicationChangeListener(listener, object);
    }
    
    /**
     * {@inheritDoc}
     */
    public void removeRoleImplicationChangeListener(
        RoleImplicationChangeListener listener, Object object)
    {
        in.removeRoleImplicationChangeListener(listener, object);
        local.removeRoleImplicationChangeListener(listener, object);
    }

    /**
     * {@inheritDoc}
     */
    public void addResourceClassInheritanceChangeListener(
        ResourceClassInheritanceChangeListener listener, Object object)
    {
        in.addResourceClassInheritanceChangeListener(listener, object);
        local.addResourceClassInheritanceChangeListener(listener, object);
    }
    
    /**
     * {@inheritDoc}
     */
    public void removeResourceClassInheritanceChangeListener(
        ResourceClassInheritanceChangeListener listener, Object object)
    {
        in.removeResourceClassInheritanceChangeListener(listener, object);
        local.removeResourceClassInheritanceChangeListener(listener, object);
    }    

    /**
     * {@inheritDoc}
     */
    public void addResourceClassAttributesChangeListener(
        ResourceClassAttributesChangeListener listener, Object object)
    {
        in.addResourceClassAttributesChangeListener(listener, object);
        local.addResourceClassAttributesChangeListener(listener, object);
    }
    
    /**
     * {@inheritDoc}
     */
    public void removeResourceClassAttributesChangeListener(
        ResourceClassAttributesChangeListener listener, Object object)
    {
        in.removeResourceClassAttributesChangeListener(listener, object);
        local.removeResourceClassAttributesChangeListener(listener, object);
    }

    /**
     * {@inheritDoc}
     */
    public void addResourceTreeChangeListener(
        ResourceTreeChangeListener listener, Object object)
    {
        in.addResourceTreeChangeListener(listener, object);
        local.addResourceTreeChangeListener(listener, object);
    }
    
    /**
     * {@inheritDoc}
     */
    public void removeResourceTreeChangeListener(
        ResourceTreeChangeListener listener, Object object)
    {
        in.removeResourceTreeChangeListener(listener, object);
        local.removeResourceTreeChangeListener(listener, object);
    }

    /**
     * {@inheritDoc}
     */
    public void addResourceOwnershipChangeListener(
        ResourceOwnershipChangeListener listener, Object object)
    {
        in.addResourceOwnershipChangeListener(listener, object);
        local.addResourceOwnershipChangeListener(listener, object);
    }
    
    /**
     * {@inheritDoc}
     */
    public void removeResourceOwnershipChangeListener(
        ResourceOwnershipChangeListener listener, Object object)
    {
        in.removeResourceOwnershipChangeListener(listener, object);
        local.removeResourceOwnershipChangeListener(listener, object);
    }

    /**
     * {@inheritDoc}
     */
    public void addResourceClassChangeListener(
        ResourceClassChangeListener listener, Object object)
    {
        in.addResourceClassChangeListener(listener, object);
        local.addResourceClassChangeListener(listener, object);
    }
    
    /**
     * {@inheritDoc}
     */
    public void removeResourceClassChangeListener(
        ResourceClassChangeListener listener, Object object)
    {
        in.removeResourceClassChangeListener(listener, object);
        local.removeResourceClassChangeListener(listener, object);
    }

    /**
     * {@inheritDoc}
     */
    public void addAttributeClassChangeListener(
        AttributeClassChangeListener listener, Object object)
    {
        in.addAttributeClassChangeListener(listener, object);
        local.addAttributeClassChangeListener(listener, object);
    }
    
    /**
     * {@inheritDoc}
     */
    public void removeAttributeClassChangeListener(
        AttributeClassChangeListener listener, Object object)
    {
        in.removeAttributeClassChangeListener(listener, object);
        local.removeAttributeClassChangeListener(listener, object);
    }

    /**
     * {@inheritDoc}
     */
    public void addAttributeDefinitionChangeListener(
        AttributeDefinitionChangeListener listener, Object object)
    {
        in.addAttributeDefinitionChangeListener(listener, object);
        local.addAttributeDefinitionChangeListener(listener, object);
    }
    
    /**
     * {@inheritDoc}
     */
    public void removeAttributeDefinitionChangeListener(
        AttributeDefinitionChangeListener listener, Object object)
    {
        in.removeAttributeDefinitionChangeListener(listener, object);
        local.removeAttributeDefinitionChangeListener(listener, object);
    }

    /**
     * {@inheritDoc}
     */
    public void addResourceCreationListener(
        ResourceCreationListener listener, Object object)
    {
        in.addResourceCreationListener(listener, object);
        local.addResourceCreationListener(listener, object);
    }
    
    /**
     * {@inheritDoc}
     */
    public void removeResourceCreationListener(
        ResourceCreationListener listener, Object object)
    {
        in.removeResourceCreationListener(listener, object);
        local.removeResourceCreationListener(listener, object);
    }

    /**
     * {@inheritDoc}
     */
    public void addResourceChangeListener(
        ResourceChangeListener listener, Object object)
    {
        in.addResourceChangeListener(listener, object);
        local.addResourceChangeListener(listener, object);
    }
    
    /**
     * {@inheritDoc}
     */
    public void removeResourceChangeListener(
        ResourceChangeListener listener, Object object)
    {
        in.removeResourceChangeListener(listener, object);
        local.removeResourceChangeListener(listener, object);
    }

    /**
     * {@inheritDoc}
     */
	public void addResourceDeletionListener(
		ResourceDeletionListener listener, Object object)
	{
		in.addResourceDeletionListener(listener, object);
		local.addResourceDeletionListener(listener, object);
	}
    
    /**
     * {@inheritDoc}
     */
	public void removeResourceDeletionListener(
		ResourceDeletionListener listener, Object object)
	{
		in.removeResourceDeletionListener(listener, object);
		local.removeResourceDeletionListener(listener, object);
	}

    /**
     * {@inheritDoc}
     */
	public void addResourceTreeDeletionListener(
		ResourceTreeDeletionListener listener, Object object)
	{
		in.addResourceTreeDeletionListener(listener, object);
		local.addResourceTreeDeletionListener(listener, object);
	}
    
    /**
     * {@inheritDoc}
     */
	public void removeResourceTreeDeletionListener(
		ResourceTreeDeletionListener listener, Object object)
	{
		in.removeResourceTreeDeletionListener(listener, object);
		local.removeResourceTreeDeletionListener(listener, object);
	}

    /**
     * {@inheritDoc}
     */
    public void addSubjectChangeListener(
        SubjectChangeListener listener, Object object)
    {
        in.addSubjectChangeListener(listener, object);
        local.addSubjectChangeListener(listener, object);
    }

    
    /**
     * {@inheritDoc}
     */
    public void removeSubjectChangeListener(
        SubjectChangeListener listener, Object object)
    {
        in.removeSubjectChangeListener(listener, object);
        local.removeSubjectChangeListener(listener, object);
    }
    
    /**
     * {@inheritDoc}
     */
    public void addRoleChangeListener(
        RoleChangeListener listener, Object object)
    {
        in.addRoleChangeListener(listener, object);
        local.addRoleChangeListener(listener, object);
    }
    
    /**
     * {@inheritDoc}
     */
    public void removeRoleChangeListener(
        RoleChangeListener listener, Object object)
    {
        in.removeRoleChangeListener(listener, object);
        local.removeRoleChangeListener(listener, object);
    }

    /**
     * {@inheritDoc}
     */
    public void addPermissionChangeListener(
        PermissionChangeListener listener, Object object)
    {
        in.addPermissionChangeListener(listener, object);
        local.addPermissionChangeListener(listener, object);
    }
    
    /**
     * {@inheritDoc}
     */
    public void removePermissionChangeListener(
        PermissionChangeListener listener, Object object)
    {
        in.removePermissionChangeListener(listener, object);
        local.removePermissionChangeListener(listener, object);
    }

    // event firing //////////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public void firePermissionAssignmentChangeEvent(
        PermissionAssignment item, boolean added)
    {
        local.firePermissionAssignmentChangeEvent(item, added);
        out.firePermissionAssignmentChangeEvent(item, added);
    }

    /**
     * {@inheritDoc}
     */
    public void fireRoleAssignmentChangeEvent(
        RoleAssignment item, boolean added)
    {
        local.fireRoleAssignmentChangeEvent(item, added);
        out.fireRoleAssignmentChangeEvent(item, added);
    }

    /**
     * {@inheritDoc}
     */
    public void firePermissionAssociationChangeEvent(
        PermissionAssociation item, boolean added)
    {
        local.firePermissionAssociationChangeEvent(item, added);
        out.firePermissionAssociationChangeEvent(item, added);
    }

    /**
     * {@inheritDoc}
     */
    public void fireRoleImplicationChangeEvent(
        RoleImplication item, boolean added)
    {
        local.fireRoleImplicationChangeEvent(item, added);
        out.fireRoleImplicationChangeEvent(item, added);
    }

    /**
     * {@inheritDoc}
     */
    public void fireResourceClassInheritanceChangeEvent(
        ResourceClassInheritance item, boolean added)
    {
        local.fireResourceClassInheritanceChangeEvent(item, added);
        out.fireResourceClassInheritanceChangeEvent(item, added);
    }

    /**
     * {@inheritDoc}
     */
    public void fireResourceClassAttributesChangeEvent(
        AttributeDefinition item, boolean added)
    {
        local.fireResourceClassAttributesChangeEvent(item, added);
        out.fireResourceClassAttributesChangeEvent(item, added);
    }

    /**
     * {@inheritDoc}
     */
    public void fireResourceTreeChangeEvent(
        ResourceInheritance item, boolean added)
    {
        local.fireResourceTreeChangeEvent(item, added);
        out.fireResourceTreeChangeEvent(item, added);
    }

    /**
     * {@inheritDoc}
     */
    public void fireResourceOwnershipChangeEvent(
        ResourceOwnership item, boolean added)
    {
        local.fireResourceOwnershipChangeEvent(item, added);
        out.fireResourceOwnershipChangeEvent(item, added);
    }

    /**
     * {@inheritDoc}
     */
    public void fireSubjectChangeEvent(Subject item)
    {
        local.fireSubjectChangeEvent(item);
        out.fireSubjectChangeEvent(item);
    }

    /**
     * {@inheritDoc}
     */
    public void fireRoleChangeEvent(Role item)
    {
        local.fireRoleChangeEvent(item);
        out.fireRoleChangeEvent(item);
    }

    /**
     * {@inheritDoc}
     */
    public void fireResourceCreationEvent(Resource item)
    {
        local.fireResourceCreationEvent(item);
        out.fireResourceCreationEvent(item);
    }
    
    /**
     * {@inheritDoc}
     */
    public void fireResourceChangeEvent(Resource item, Subject subject)
    {
        local.fireResourceChangeEvent(item, subject);
        out.fireResourceChangeEvent(item, subject);
    }

    /**
     * {@inheritDoc}
     */
	public void fireResourceDeletionEvent(Resource item)
	{
		local.fireResourceDeletionEvent(item);
		out.fireResourceDeletionEvent(item);
	}

    /**
     * {@inheritDoc}
     */
	public void fireResourceTreeDeletionEvent(Resource item)
	{
		local.fireResourceTreeDeletionEvent(item);
		out.fireResourceTreeDeletionEvent(item);
	}
	
    /**
     * {@inheritDoc}
     */
    public void firePermissionChangeEvent(Permission item)
    {
        local.firePermissionChangeEvent(item);
        out.firePermissionChangeEvent(item);
    }

    /**
     * {@inheritDoc}
     */
    public void fireResourceClassChangeEvent(ResourceClass item)
    {
        local.fireResourceClassChangeEvent(item);
        out.fireResourceClassChangeEvent(item);
    }

    /**
     * {@inheritDoc}
     */
    public void fireAttributeClassChangeEvent(AttributeClass item)
    {
        local.fireAttributeClassChangeEvent(item);
        out.fireAttributeClassChangeEvent(item);
    }

    /**
     * {@inheritDoc}
     */
    public void fireAttributeDefinitionChangeEvent(AttributeDefinition item)
    {
        local.fireAttributeDefinitionChangeEvent(item);
        out.fireAttributeDefinitionChangeEvent(item);
    }
}
