package org.objectledge.coral.event;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.jcontainer.dna.Logger;
import org.objectledge.coral.BackendException;
import org.objectledge.coral.CoralCore;
import org.objectledge.coral.Instantiator;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeDefinitionImpl;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.schema.ResourceClassInheritance;
import org.objectledge.coral.schema.ResourceClassInheritanceImpl;
import org.objectledge.coral.security.Permission;
import org.objectledge.coral.security.PermissionAssignment;
import org.objectledge.coral.security.PermissionAssignmentImpl;
import org.objectledge.coral.security.PermissionAssociation;
import org.objectledge.coral.security.PermissionAssociationImpl;
import org.objectledge.coral.security.Role;
import org.objectledge.coral.security.RoleAssignment;
import org.objectledge.coral.security.RoleAssignmentImpl;
import org.objectledge.coral.security.RoleImplication;
import org.objectledge.coral.security.RoleImplicationImpl;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ResourceInheritance;
import org.objectledge.coral.store.ResourceInheritanceImpl;
import org.objectledge.coral.store.ResourceOwnership;
import org.objectledge.coral.store.ResourceOwnershipImpl;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.PersistenceException;
import org.objectledge.database.persistence.PersistentFactory;
import org.objectledge.event.EventWhiteboard;
import org.objectledge.notification.Notification;
import org.objectledge.notification.NotificationReceiver;

/**
 * The bridge between Notification service and CoralEventWhiteboard
 *
 */
public class NotificationEventBridgeImpl
    extends CoralEventListener
    implements NotificationReceiver, CoralEventBridge
{
    // member objects ////////////////////////////////////////////////////////

    /** The notification service. */
    private Notification notification;
    
    /** The CoralEventWhiteboard */
    private CoralEventWhiteboard event;

    /** The Persistence subsystem. */
    private Persistence persistence;
    
    /** The component instantiator. */
    private Instantiator instantiator;
        
    /** The component hub. */
    private CoralCore coral;
    
    /** The logger. */
    private Logger log;
    

    /** The notification channel. */
    private String channel;
    
    /** The channel prameter key ('channel') */
    public static final String CHANNEL_KEY = "channel";

    /** The default notification channel. ('CORAL:1.0') */
    public static final String CHANNEL_DEFAULT = "CORAL:1.0";

    /** Flow control variable. */
    private boolean connected = false;

    /** The <code>PersistentFactory</code> for <code>AttributeDefinition</code>
     * objects. */
    private PersistentFactory attributeDefinitionFactory;

    /** The <code>PersistentFactory</code> for <code>RoleAssignment</code>
     * objects. */
    private PersistentFactory roleAssignmentFactory; 

    /** The <code>PersistentFactory</code> for <code>PermissionAssignment</code>
     * objects. */
    private PersistentFactory permissionAssignmentFactory;

    // initialization ///////////////////////////////////////////////////////
    
    /**
     * Constructs an event bridge instance.
     * 
     * @param persistence the Peristence substem.
     * @param notification the Notification subsystem.
     * @param instantiator the component instantiator.
     * @param coral the component hub.
     * @param log the logger.
     */
    public NotificationEventBridgeImpl(Persistence persistence, Notification notification, 
        Instantiator instantiator, CoralCore coral, Logger log)
    {
        this.persistence = persistence;   
        this.notification = notification;
        this.coral = coral;
        this.log = log;
        
        this.attributeDefinitionFactory = instantiator.
            getPersistentFactory(AttributeDefinitionImpl.class); 
        this.roleAssignmentFactory = instantiator.
            getPersistentFactory(RoleAssignmentImpl.class);
        this.permissionAssignmentFactory = instantiator.
            getPersistentFactory(PermissionAssignmentImpl.class);    
        // TODO make this configurable 
        channel = CHANNEL_DEFAULT;
    }
    
    /**
     * Attaches the provided event forwarders to the notification service.
     *
     * @param in events coming from the notification service will be fired on
     *        this forwarder.
     * @param out events coming from this forwarder will be sent to the
     *        notification service.
     */
    public void attach(EventWhiteboard in, EventWhiteboard out)
    {
        event = new CoralEventWhiteboardImpl(in);
        this.register(new CoralEventWhiteboardImpl(out));
        notification.addReceiver(channel, this);
        connected = true;
    }
    
    /**
     * Detachs the event forwarders from the notification service.
     * 
     * @see #attach(EventWhiteboard,EventWhiteboard)
     */
    public void detach()
    {
        connected = false; 
        this.unregister();
        notification.removeReceiver(channel, this);
    }
    
    // NotificationReceiver interface ////////////////////////////////////////

    /**
     * Called when a notification is received.
     *
     * @param channel the name of the channel.
     * @param message the notification.
     */
    public void receive(String channel, byte[] message)
    {
        if(!connected)
        {
            return;
        }
        try
        {
            ByteArrayInputStream bais = new ByteArrayInputStream(message);
            DataInputStream in = new DataInputStream(bais);
            String type = in.readUTF();
            long entity1 = in.readLong();
            long entity2 = in.readLong();
            long entity3 = in.readLong();
            boolean added = in.readBoolean();
            receive(type, entity1, entity2, entity3, added);
        }
        catch(IOException e)
        {
            throw new BackendException("failed to decode event", e);
        }
    }

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
    public void event(String type, long entity1, long entity2,
                      long entity3, boolean added)
    {
        if(!connected)
        {
            return;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        byte[] msg;
        try
        {
            out.writeUTF(type);
            out.writeLong(entity1);
            out.writeLong(entity2);
            out.writeLong(entity3);
            out.writeBoolean(added);
            out.flush();
            msg = baos.toByteArray();
        }
        catch(IOException e)
        {
            throw new BackendException("failed to encode event", e);
        }
        notification.sendNotification(channel, msg, false);
    }

    /**
     * Process event coming from the remote side.
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
    public void receive(String type, long entity1, long entity2,
                        long entity3, boolean added)
    {
        // log
        StringBuffer buff = new StringBuffer();
        buff.append("Remote Coral event: ");
        buff.append(type);
        buff.append("(#");
        buff.append(entity1);
        if(entity2 != -1L)
        {
            buff.append(", #");
            buff.append(entity2);
            if(entity3 != -1)
            {
                buff.append(", #");
                buff.append(entity3);
            }
        }
        buff.append(added ? ", true)" : ", false)");
        log.info(buff.toString());
        // dispatch
        try
        {
            
            if("PermissionAssociationChange".equals(type))
            {
                ResourceClass rc = coral.getSchema().getResourceClass(entity1);
                Permission p = coral.getSecurity().getPermission(entity2);
                PermissionAssociation pa = 
                    new PermissionAssociationImpl(coral, rc, p);
                event.firePermissionAssociationChangeEvent(pa, added);
            }
            else if("PermissionAssignmentChange".equals(type))
            {
                PermissionAssignment pa = null;
                if(added)
                {
                    pa = (PermissionAssignment)persistence.load(
                        "resource_id = "+entity1+" AND "+
                        "role_id = "+entity2+" AND "+
                        "permission_id = "+entity3,
                        permissionAssignmentFactory
                    );
                }
                else
                {
                    Resource res = coral.getStore().getResource(entity1);
                    Role r = coral.getSecurity().getRole(entity2);
                    Permission p = coral.getSecurity().getPermission(entity3);
                    pa = new PermissionAssignmentImpl(coral, 
                        null, res, r, p, false);
                }
                event.firePermissionAssignmentChangeEvent(pa, added);
            } 
            else if("RoleAssignmentChange".equals(type)) 
            {
                RoleAssignment ra = null;
                if(added)
                {
                    ra = (RoleAssignment)persistence.load(
                        "subject_id = "+entity1+" AND "+
                        "role_id = "+entity2,
                        roleAssignmentFactory
                    );
                }
                else
                {
                    Subject s = coral.getSecurity().getSubject(entity1);
                    Role r = coral.getSecurity().getRole(entity2);
                    ra = new RoleAssignmentImpl(coral, null, s, r, false);
                }
                event.fireRoleAssignmentChangeEvent(ra, added);
            }
            else if("RoleImplicationChange".equals(type)) 
            {
                Role sup = coral.getSecurity().getRole(entity1);
                Role sub = coral.getSecurity().getRole(entity2);
                RoleImplication ri = new RoleImplicationImpl(coral, sup, sub);
                event.fireRoleImplicationChangeEvent(ri, added);
            }
            else if("ResourceClassInheritanceChange".equals(type)) 
            {
                ResourceClass p = coral.getSchema().getResourceClass(entity1);
                ResourceClass c = coral.getSchema().getResourceClass(entity2);
                ResourceClassInheritance rci = 
                    new ResourceClassInheritanceImpl(coral, p, c);
                event.fireResourceClassInheritanceChangeEvent(rci, added);
            }
            else if("ResourceClassAttributesChange".equals(type)) 
            {
                AttributeDefinition a = (AttributeDefinition)persistence.load(
                    entity1, attributeDefinitionFactory);
                event.fireResourceClassAttributesChangeEvent(a, added);
            }
            else if("ResourceTreeChange".equals(type)) 
            {
                Resource p = coral.getStore().getResource(entity1);
                Resource c = coral.getStore().getResource(entity2);
                ResourceInheritance ri = 
                    new ResourceInheritanceImpl(p, c);
                event.fireResourceTreeChangeEvent(ri, added);
            }
            else if("ResourceOwnershipChange".equals(type)) 
            {
                Subject s = coral.getSecurity().getSubject(entity1);
                Resource r = coral.getStore().getResource(entity2);
                ResourceOwnership ro = 
                    new ResourceOwnershipImpl(s, r);
                event.fireResourceOwnershipChangeEvent(ro, added);
            }
            else if("SubjectChange".equals(type)) 
            {
                Subject s = coral.getSecurity().getSubject(entity1);
                event.fireSubjectChangeEvent(s);
            }
            else if("RoleChange".equals(type)) 
            {
                Role r = coral.getSecurity().getRole(entity1);
                event.fireRoleChangeEvent(r);
            }
            else if("PermissionChange".equals(type)) 
            {
                Permission p = coral.getSecurity().getPermission(entity1);
                event.firePermissionChangeEvent(p);
            }
            else if("ResourceCreation".equals(type)) 
            {
                Resource r = coral.getStore().getResource(entity1);
                event.fireResourceCreationEvent(r);
            }
            else if("ResourceChange".equals(type)) 
            {
                Resource r = coral.getStore().getResource(entity1);
                if(entity2 != -1)
                {
					Subject s = coral.getSecurity().getSubject(entity2);
					event.fireResourceChangeEvent(r, s);
                }
                else
                {
                	event.fireResourceChangeEvent(r, null);
                }
            }
			else if("ResourceDeletion".equals(type)) 
			{
				Resource r = coral.getStore().getResource(entity1);
				event.fireResourceDeletionEvent(r);
			}
            else if("ResourceClassChange".equals(type)) 
            {
                ResourceClass rc = coral.getSchema().getResourceClass(entity1);
                event.fireResourceClassChangeEvent(rc);
            }
            else if("AttributeClassChange".equals(type)) 
            {
                AttributeClass ac = coral.getSchema().getAttributeClass(entity1);
                event.fireAttributeClassChangeEvent(ac);
            }
            else if("AttributeDefinictionChange".equals(type)) 
            {
                AttributeDefinition a = (AttributeDefinition)persistence.
                    load(entity1, attributeDefinitionFactory);
                event.fireAttributeDefinitionChangeEvent(a);
            }
            else
            {
                throw new BackendException("unknown event "+type);
            }
        }
        catch(EntityDoesNotExistException e)
        {
            throw new BackendException("invalid entity id", e);
        }
        catch(PersistenceException e)
        {
            throw new BackendException("invalid entity id", e);
        }
    }
}

