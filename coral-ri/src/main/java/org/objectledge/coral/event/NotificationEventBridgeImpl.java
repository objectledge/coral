package org.objectledge.coral.event;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.jcontainer.dna.Logger;
import org.objectledge.coral.BackendException;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeDefinitionImpl;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.schema.ResourceClassInheritance;
import org.objectledge.coral.schema.ResourceClassInheritanceImpl;
import org.objectledge.coral.security.CoralSecurity;
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
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ResourceInheritance;
import org.objectledge.coral.store.ResourceInheritanceImpl;
import org.objectledge.coral.store.ResourceOwnership;
import org.objectledge.coral.store.ResourceOwnershipImpl;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.PersistenceException;
import org.objectledge.database.persistence.PersistentFactory;
import org.objectledge.database.persistence.PicoPersistentFactory;
import org.objectledge.event.EventForwarder;
import org.objectledge.notification.Notification;
import org.objectledge.notification.NotificationReceiver;
import org.picocontainer.PicoContainer;

/**
 * The bridge between Notification service and ARLEventService
 *
 */
public class NotificationEventBridgeImpl
    extends CoralEventListener
    implements NotificationReceiver, CoralEventBridge
{
    // member objects ////////////////////////////////////////////////////////

    /** The notification service. */
    private Notification notification;
    
    /** The ARLEventService. */
    private CoralEventWhiteboard event;

    /** The Persistence subsystem. */
    private Persistence persistence;
    
    /** The dependency container for factories. */
    private PicoContainer dependencyContainer;
    
    /** The CoralSchema. */
    private CoralSchema coralSchema;
    
    /** The CoralSecurity. */
    private CoralSecurity coralSecurity;
    
    /** The CoralStore. */
    private CoralStore coralStore;
    
    /** The logger. */
    private Logger log;
    

    /** The notification channel. */
    private String channel;
    
    /** The channel prameter key ('channel') */
    public static final String CHANNEL_KEY = "channel";

    /** The default notification channel. ('ARL:1.0') */
    public static final String CHANNEL_DEFAULT = "ARL:1.0";

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
     * @param dependencyContainer the container of dependencies for the factories.
     * @param coralSchema the CoralSchema.
     * @param coralSecurity the CoralSecurity.
     * @param coralStore the CoralStore.
     * @param log the logger.
     */
    public NotificationEventBridgeImpl(Persistence persistence, Notification notification, 
        PicoContainer dependencyContainer, CoralSchema coralSchema, CoralSecurity coralSecurity,
        CoralStore coralStore, Logger log)
    {
        this.persistence = persistence;   
        this.notification = notification;
        this.dependencyContainer = dependencyContainer;
        this.coralSchema = coralSchema;
        this.coralSecurity = coralSecurity;
        this.coralStore = coralStore;
        this.log = log;
        
        this.attributeDefinitionFactory = new PicoPersistentFactory(dependencyContainer, 
            AttributeDefinitionImpl.class); 
        this.roleAssignmentFactory = 
            new PicoPersistentFactory(dependencyContainer, RoleAssignmentImpl.class);
        this.permissionAssignmentFactory = new PicoPersistentFactory(dependencyContainer, 
            PermissionAssignmentImpl.class);    
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
    public void attach(EventForwarder in, EventForwarder out)
    {
        event = new CoralEventWhiteboardImpl(in);
        this.register(new CoralEventWhiteboardImpl(out));
        notification.addReceiver(channel, this);
        connected = true;
    }
    
    /**
     * Detachs the event forwarders from the notification service.
     * 
     * @see #attach(EventForwarder,EventForwarder)
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
        buff.append("Remote ARL event: ");
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
                ResourceClass rc = coralSchema.getResourceClass(entity1);
                Permission p = coralSecurity.getPermission(entity2);
                PermissionAssociation pa = 
                    new PermissionAssociationImpl(coralSchema, coralSecurity, rc, p);
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
                    Resource res = coralStore.getResource(entity1);
                    Role r = coralSecurity.getRole(entity2);
                    Permission p = coralSecurity.getPermission(entity3);
                    pa = new PermissionAssignmentImpl(coralSecurity, coralStore, 
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
                    Subject s = coralSecurity.getSubject(entity1);
                    Role r = coralSecurity.getRole(entity2);
                    ra = new RoleAssignmentImpl(coralSecurity, null, s, r, false);
                }
                event.fireRoleAssignmentChangeEvent(ra, added);
            }
            else if("RoleImplicationChange".equals(type)) 
            {
                Role sup = coralSecurity.getRole(entity1);
                Role sub = coralSecurity.getRole(entity2);
                RoleImplication ri = new RoleImplicationImpl(coralSecurity, sup, sub);
                event.fireRoleImplicationChangeEvent(ri, added);
            }
            else if("ResourceClassInheritanceChange".equals(type)) 
            {
                ResourceClass p = coralSchema.getResourceClass(entity1);
                ResourceClass c = coralSchema.getResourceClass(entity2);
                ResourceClassInheritance rci = 
                    new ResourceClassInheritanceImpl(coralSchema, p, c);
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
                Resource p = coralStore.getResource(entity1);
                Resource c = coralStore.getResource(entity2);
                ResourceInheritance ri = 
                    new ResourceInheritanceImpl(p, c);
                event.fireResourceTreeChangeEvent(ri, added);
            }
            else if("ResourceOwnershipChange".equals(type)) 
            {
                Subject s = coralSecurity.getSubject(entity1);
                Resource r = coralStore.getResource(entity2);
                ResourceOwnership ro = 
                    new ResourceOwnershipImpl(s, r);
                event.fireResourceOwnershipChangeEvent(ro, added);
            }
            else if("SubjectChange".equals(type)) 
            {
                Subject s = coralSecurity.getSubject(entity1);
                event.fireSubjectChangeEvent(s);
            }
            else if("RoleChange".equals(type)) 
            {
                Role r = coralSecurity.getRole(entity1);
                event.fireRoleChangeEvent(r);
            }
            else if("PermissionChange".equals(type)) 
            {
                Permission p = coralSecurity.getPermission(entity1);
                event.firePermissionChangeEvent(p);
            }
            else if("ResourceCreation".equals(type)) 
            {
                Resource r = coralStore.getResource(entity1);
                event.fireResourceCreationEvent(r);
            }
            else if("ResourceChange".equals(type)) 
            {
                Resource r = coralStore.getResource(entity1);
                if(entity2 != -1)
                {
					Subject s = coralSecurity.getSubject(entity2);
					event.fireResourceChangeEvent(r, s);
                }
                else
                {
                	event.fireResourceChangeEvent(r, null);
                }
            }
			else if("ResourceDeletion".equals(type)) 
			{
				Resource r = coralStore.getResource(entity1);
				event.fireResourceDeletionEvent(r);
			}
            else if("ResourceClassChange".equals(type)) 
            {
                ResourceClass rc = coralSchema.getResourceClass(entity1);
                event.fireResourceClassChangeEvent(rc);
            }
            else if("AttributeClassChange".equals(type)) 
            {
                AttributeClass ac = coralSchema.getAttributeClass(entity1);
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

