package org.objectledge.coral.entity;

// JDBC
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.jcontainer.dna.ConfigurationException;
import org.jcontainer.dna.Logger;
import org.objectledge.cache.CacheFactory;
import org.objectledge.coral.BackendException;
import org.objectledge.coral.CoralCore;
import org.objectledge.coral.Instantiator;
import org.objectledge.coral.event.CoralEventHub;
import org.objectledge.coral.event.PermissionAssignmentChangeListener;
import org.objectledge.coral.event.PermissionAssociationChangeListener;
import org.objectledge.coral.event.ResourceClassAttributesChangeListener;
import org.objectledge.coral.event.ResourceClassInheritanceChangeListener;
import org.objectledge.coral.event.RoleAssignmentChangeListener;
import org.objectledge.coral.event.RoleImplicationChangeListener;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.AttributeClassImpl;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.AttributeDefinitionImpl;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.schema.ResourceClassImpl;
import org.objectledge.coral.schema.ResourceClassInheritance;
import org.objectledge.coral.schema.ResourceClassInheritanceImpl;
import org.objectledge.coral.security.Permission;
import org.objectledge.coral.security.PermissionAssignment;
import org.objectledge.coral.security.PermissionAssignmentImpl;
import org.objectledge.coral.security.PermissionAssociation;
import org.objectledge.coral.security.PermissionAssociationImpl;
import org.objectledge.coral.security.PermissionImpl;
import org.objectledge.coral.security.Role;
import org.objectledge.coral.security.RoleAssignment;
import org.objectledge.coral.security.RoleAssignmentImpl;
import org.objectledge.coral.security.RoleImpl;
import org.objectledge.coral.security.RoleImplication;
import org.objectledge.coral.security.RoleImplicationImpl;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.security.SubjectImpl;
import org.objectledge.coral.store.Resource;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.PersistenceException;
import org.objectledge.database.persistence.Persistent;
import org.objectledge.database.persistence.PersistentFactory;

/**
 * Manages persistence of {@link Entity}, {@link Assignment} and {@link
 * Association} objects.
 * 
 * @version $Id: CoralRegistryImpl.java,v 1.6 2004-03-09 14:33:30 zwierzem Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class CoralRegistryImpl
    implements CoralRegistry,
               PermissionAssociationChangeListener,
               PermissionAssignmentChangeListener,
               RoleAssignmentChangeListener,
               RoleImplicationChangeListener,
               ResourceClassInheritanceChangeListener,
               ResourceClassAttributesChangeListener
{
    // Member objects ////////////////////////////////////////////////////////

    /** The {@link PersistenceService}. */
    private Persistence persistence;
    
    /** The logger. */
    private Logger log;
    
    /** The event hub. */
    private CoralEventHub coralEventHub;
    
    /** The component hub. */
    private CoralCore coral;
    
    // Factory classes

    /** The <code>PersistentFactory</code> for <code>ResourceClassInheritance</code> objects. */
    private PersistentFactory resourceClassInheritanceFactory ; 

    /** The <code>PersistentFactory</code> for <code>AttributeDefinition</code> objects. */
    private PersistentFactory attributeDefinitionFactory; 

    /** The <code>PersistentFactory</code> for <code>RoleImplication</code> objects. */
    private PersistentFactory roleImplicationFactory; 

    /** The <code>PersistentFactory</code> for <code>RoleAssignment</code> objects. */
    private PersistentFactory roleAssignmentFactory;      

    /** The <code>PersistentFactory</code> for <code>PermissionAssociation</code> objects. */
    private PersistentFactory permissionAssociationFactory;      

    /** The <code>PersistentFactory</code> for <code>PermissionAssignment</code> objects. */
    private PersistentFactory permissionAssignmentFactory;      

    /** The <code>PersistentFactory</code> for <code>Subject</code> objects. Used by 
     *  {@link #getSubordinates(Subject)} method only.*/
    private PersistentFactory subjectFactory;      

    // caches

    /** lock object for resourceClassInheritanceByResourceClass and 
     *  attributeDefinitionByResourceClass */
    private Object resourceClassLock = new Object();

    /** <code>ResourceClassInheritance</code> <code>Set</code> by <code>ResourceClass</code> */
    private Map resourceClassInheritanceByResourceClass;

    /** Weak mapping of <code>ResourceClass</code> objects into sets of 
     *  <code>AttributeDefinition</code> objects. */
    private Map attributeDefinitionByResourceClass;

    /** lock object for roleImplicationByRole and roleAssignmentByRole */
    private Object roleLock = new Object();

    /** Weak mapping of <code>Role</code> objects into sets of <code>RoleImplication</code> 
     *  objects. */
    private Map roleImplicationByRole;

    /** Weak mapplig of <code>Role</code> objects into sets of <code>RoleAssignment</code> objects.
     */
    private Map roleAssignmentByRole;

    /** Weak mapplig of <code>Subject</code> objects into sets of <code>RoleAssignment</code> 
     * objects. */
    private Map roleAssignmentBySubject;

    /** lock object for permissionAssociationByResourceClass permissionAssociationByPermission 
     *  permissionAssignmentByResource and permissionAssignmentByRole */
    private Object permissionLock = new Object();

    /** Weak mapping of <code>ResourceClass</code> objects into sets of
     *  <code>PermissionAssociation</code> objects. */
    private Map permissionAssociationByResourceClass;
    
    /** Weak mapping of <code>Permission</code> objects into sets of
     *  <code>PermissionAssociation</code> objects. */
    private Map permissionAssociationByPermission;

    /** Weak mapping of <code>Resource</code> objects into sets of
     *  <code>PermissionAssignments</code> */
    private Map permissionAssignmentByResource;

    /** Weak mapping of <code>Role</code> objects into sets of
     *  <code>PermissionAssignments</code> objects. */
    private Map permissionAssignmentByRole;

    // Registires

    /** <code>ResourceClass</code> registry. */
    private EntityRegistry resourceClassRegistry;
    
    /** <code>AttributeClass</code> registry. */
    private EntityRegistry attributeClassRegistry;
    
    /** <code>AttributeDefinition</code> registry. */
    private EntityRegistry attributeDefinitionRegistry;

    /** <code>Permission</code> registry. */
    private EntityRegistry permissionRegistry;

    /** <code>Role</code> registry. */
    private EntityRegistry roleRegistry;

    /** <code>Subject</code> registry. */
    private EntityRegistry subjectRegistry;

    // Initialization ////////////////////////////////////////////////////////

    /**
     * Constructs the {@link RegistryService} implementation.
     * 
     * @param persistence the persistence subsystem
     * @param cacheFactory the cache factory.
     * @param coralEventHub the event hub.
     * @param coral the component hub.
     * @param instantiator component instantiator.
     * @param log the logger.
     * @throws ConfigurationException if the configuration is invalid.
     */
    public CoralRegistryImpl(Persistence persistence, 
        CacheFactory cacheFactory, CoralEventHub coralEventHub, CoralCore coral, 
        Instantiator instantiator, Logger log)
        throws ConfigurationException
    {
        this.persistence = persistence;
        this.coralEventHub = coralEventHub;
        this.coral = coral;
        this.log = log;
        
        setupCaches();
        setupFactories(instantiator);
        setupRegistries(cacheFactory, instantiator);
        setupListener();
    }

    /**
     * Sets up the instance chaces for reflection objects.
     */
    private void setupCaches()
    {
        attributeDefinitionByResourceClass = new WeakHashMap();
        resourceClassInheritanceByResourceClass = new WeakHashMap();

        roleImplicationByRole = new WeakHashMap();

        roleAssignmentBySubject = new WeakHashMap();
        roleAssignmentByRole = new WeakHashMap();

        permissionAssociationByResourceClass = new WeakHashMap();
        permissionAssociationByPermission = new WeakHashMap();

        permissionAssignmentByRole = new WeakHashMap();
        permissionAssignmentByResource = new WeakHashMap();
    }

    private void setupFactories(Instantiator instantiator)
    {
        resourceClassInheritanceFactory = 
            instantiator.getPersistentFactory(ResourceClassInheritanceImpl.class); 

        attributeDefinitionFactory =
            instantiator.getPersistentFactory(AttributeDefinitionImpl.class); 

        roleImplicationFactory = 
            instantiator.getPersistentFactory(RoleImplicationImpl.class); 

        roleAssignmentFactory =
            instantiator.getPersistentFactory(RoleAssignmentImpl.class);      

        permissionAssociationFactory = 
            instantiator.getPersistentFactory(PermissionAssociationImpl.class);      

        permissionAssignmentFactory = 
            instantiator.getPersistentFactory(PermissionAssignmentImpl.class);      

        subjectFactory = 
            instantiator.getPersistentFactory(SubjectImpl.class);      
    }
    
    /**
     * Setup the entity registry.
     */
    private void setupRegistries(CacheFactory cacheFactory, Instantiator instantiator)
        throws ConfigurationException
    {
        resourceClassRegistry = new EntityRegistry(persistence, cacheFactory, 
            instantiator, log, "resource class", ResourceClassImpl.class);
        attributeClassRegistry = new EntityRegistry(persistence, cacheFactory, 
            instantiator, log, "attribute class", AttributeClassImpl.class);
        attributeDefinitionRegistry = new EntityRegistry(persistence, cacheFactory, 
            instantiator, log, "attribute definition", AttributeDefinitionImpl.class);
        permissionRegistry = new EntityRegistry(persistence, cacheFactory, 
            instantiator, log, "permission", PermissionImpl.class);
        roleRegistry = new EntityRegistry(persistence, cacheFactory, 
            instantiator, log, "role", RoleImpl.class);
        subjectRegistry = new EntityRegistry(persistence, cacheFactory, 
            instantiator, log,  "subject", SubjectImpl.class);
    }   

    /**
     * Registers as the listener for ARL events.
     */
    private void setupListener()
    {
        coralEventHub.getInbound().addPermissionAssociationChangeListener(this, null);
        coralEventHub.getInbound().addPermissionAssignmentChangeListener(this, null);
        coralEventHub.getInbound().addRoleAssignmentChangeListener(this, null);
        coralEventHub.getInbound().addRoleImplicationChangeListener(this, null);
        coralEventHub.getInbound().addResourceClassInheritanceChangeListener(this, null);
        coralEventHub.getInbound().addResourceClassAttributesChangeListener(this, null);
    }

    // Schema - AttributeClass ///////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public AttributeClass[] getAttributeClass()
    {
        Set all = attributeClassRegistry.get();
        AttributeClass[] result = new AttributeClass[all.size()];
        all.toArray(result);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public AttributeClass getAttributeClass(long id)
        throws EntityDoesNotExistException
    {
        return (AttributeClass)attributeClassRegistry.get(id);
    }
    
    /**
     * {@inheritDoc}
     */
    public AttributeClass getAttributeClass(String name)
        throws EntityDoesNotExistException
    {
        try
        {
            return (AttributeClass)attributeClassRegistry.getUnique(name);
        }
        catch(AmbigousEntityNameException e)
        {
            throw new BackendException("integrity constranits corrupted", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addAttributeClass(AttributeClass item)
        throws EntityExistsException
    {
        attributeClassRegistry.addUnique(item);
    }

    /**
     * {@inheritDoc}
     */
    public void renameAttributeClass(AttributeClass item, String name)
        throws EntityExistsException
    {
        attributeClassRegistry.renameUnique(item, name);
    }
    
    /**
     * {@inheritDoc}
     */
    public void deleteAttributeClass(AttributeClass item)
        throws EntityInUseException
    {
        boolean shouldCommit = false;
        try
        {
            shouldCommit = persistence.getDatabase().beginTransaction();
            int attrs = persistence.count("arl_attribute_definition", 
                                              "attribute_class_id = "+item.getId());
            if(attrs > 0)
            {
                throw new EntityInUseException("Attribute class "+item.getName()+" is used by "+
                                                   attrs+" attributes");
            }
            
            attributeClassRegistry.delete(item);
            
            persistence.getDatabase().commitTransaction(shouldCommit);
        }
        catch(PersistenceException e)
        {
            try
            {
                persistence.getDatabase().rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            throw new BackendException("failed to query attribute class references", e);
        }
        catch(BackendException ex)
        {
            try
            {
                persistence.getDatabase().rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            throw ex;
        }
        catch(Exception e)
        {
            try
            {
                persistence.getDatabase().rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            throw new BackendException("failed to delete attribute class", e);
        }
    }

    // Schema - ResourceClass ////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public ResourceClass[] getResourceClass()
    {
        Set all = resourceClassRegistry.get();
        ResourceClass[] result = new ResourceClass[all.size()];
        all.toArray(result);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public ResourceClass getResourceClass(long id)
        throws EntityDoesNotExistException
    {
        return (ResourceClass)resourceClassRegistry.get(id);
    }
    
    /**
     * {@inheritDoc}
     */
    public ResourceClass getResourceClass(String name)
        throws EntityDoesNotExistException
    {
        try
        {
            return (ResourceClass)resourceClassRegistry.getUnique(name);
        }
        catch(AmbigousEntityNameException e)
        {
            throw new BackendException("integrity constranits corrupted", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addResourceClass(ResourceClass item)
        throws EntityExistsException
    {
        resourceClassRegistry.addUnique(item);
    }
    
    /**
     * {@inheritDoc}
     */
    public void deleteResourceClass(ResourceClass item)
        throws EntityInUseException
    {
        boolean shouldCommit = false;
        try
        {
            shouldCommit = persistence.getDatabase().beginTransaction();
            // Check for resources
            int resources = persistence.count("arl_resource", 
                                                  "resource_class_id = "+item.getId());
            if(resources > 0)
            {
                throw new EntityInUseException(resources+" resources of the class "+
                                               item.getName()+" exist");
            }

            int children = persistence.count("arl_resource_class_inheritance",
                                                 "parent = "+item.getId());
            if(children > 0)
            {
                throw new EntityInUseException(children+" child classes of the class "+
                                               item.getName()+" exist");
            }
            
            AttributeDefinition[] attributes = item.getDeclaredAttributes();
            for(int i=0; i<attributes.length; i++)
            {
                coral.getSchema().deleteAttribute(item, attributes[i]);
            }
            ResourceClassInheritance[] rci = item.getInheritance();
            for(int i=0; i<rci.length; i++)
            {
                if(rci[i].getChild().equals(item))
                {
                    coral.getSchema().deleteParentClass(item, rci[i].getParent());
                }
            }
            Permission[] perms = item.getPermissions();
            for(int i=0; i<perms.length; i++)
            {
                coral.getSecurity().deletePermission(item, perms[i]);
            }
            resourceClassRegistry.delete(item);
            persistence.getDatabase().commitTransaction(shouldCommit);
        }
        catch(PersistenceException e)
        {
            try
            {
                persistence.getDatabase().rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            throw new BackendException("failed to query resource class references", e);
        }
        catch(BackendException ex)
        {
            try
            {
                persistence.getDatabase().rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            throw ex;
        }
        catch(Exception e)
        {
            try
            {
                persistence.getDatabase().rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            throw new BackendException("failed to delete resource class", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void renameResourceClass(ResourceClass item, String name)
        throws EntityExistsException
    {
        resourceClassRegistry.renameUnique(item, name);
    }

    // Schema - AttributeDefinition //////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public Set getDeclaredAttributes(ResourceClass owner)
    {
        synchronized(resourceClassLock)
        {
            HashSet items = (HashSet)attributeDefinitionByResourceClass.get(owner);
            if(items == null)
            {
                items = new HashSet();
                attributeDefinitionByResourceClass.put(owner, items);
                List list;
                try
                {
                    list = persistence.load("resource_class_id = "+owner.getId(),
                                            attributeDefinitionFactory);
                }
                catch(PersistenceException e)
                {
                    throw new BackendException("Failed to load AttributeDefinitions", e);
                }
                attributeDefinitionRegistry.resolve(list, items);
            }
            return (Set)items.clone();
        }
    }

    /**
     * {@inheritDoc}
     */    
    public AttributeDefinition[] getAttributeDefinition()
    {
        synchronized(resourceClassLock)
        {
            Set defs = attributeDefinitionRegistry.get();
            for(Iterator i = defs.iterator(); i.hasNext(); )
            {
                AttributeDefinition def = (AttributeDefinition)i.next();
                ResourceClass owner = def.getDeclaringClass();
                Set items = (Set)attributeDefinitionByResourceClass.get(owner);
                if(items == null)
                {
                    items = new HashSet();
                    attributeDefinitionByResourceClass.put(owner, items);
                }
                items.add(def);
            }
            AttributeDefinition[] result = new AttributeDefinition[defs.size()];
            defs.toArray(result);
            return result;
        }
    }

    /**
     * {@inheritDoc}
     */
    public AttributeDefinition getAttributeDefinition(long id)
        throws EntityDoesNotExistException
    {
        return (AttributeDefinition)attributeDefinitionRegistry.get(id);
    }

    /**
     * {@inheritDoc}
     */
    public void addAttributeDefinition(AttributeDefinition item)
    {
        synchronized(resourceClassLock)
        {
            attributeDefinitionRegistry.add(item);
            ResourceClass owner = item.getDeclaringClass();
            Set items = (Set)attributeDefinitionByResourceClass.get(owner);
            if(items == null)
            {
                items = new HashSet();
                attributeDefinitionByResourceClass.put(owner, items);
            }
            items.add(item);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void renameAttributeDefinition(AttributeDefinition item, String name)
    {
        attributeDefinitionRegistry.rename(item, name);
    }

    /**
     * {@inheritDoc}
     */
    public void deleteAttributeDefinition(AttributeDefinition item)
    {
        synchronized(resourceClassLock)
        {
            attributeDefinitionRegistry.delete(item);
            ResourceClass owner = item.getDeclaringClass();
            Set items = (Set)attributeDefinitionByResourceClass.get(owner);
            if(items != null)
            {
                items.remove(item);
            }
        }
    }       

    // Schema - ResourceClassInheritance /////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public Set getResourceClassInheritance(ResourceClass owner)
    {
        synchronized(resourceClassLock)
        {
            HashSet items = (HashSet)resourceClassInheritanceByResourceClass.get(owner);
            if(items == null)
            {
                items = new HashSet();
                resourceClassInheritanceByResourceClass.put(owner, items);
                List list;
                try
                {
                    list = persistence.load("parent = "+owner.getId()+" OR "+
                                            "child = "+owner.getId(),
                                            resourceClassInheritanceFactory);
                }
                catch(PersistenceException e)
                {
                    throw new BackendException("Failed to load ResourceClassInheritances", e);
                }
                Iterator i = list.iterator();
                while(i.hasNext())
                {
                    ResourceClassInheritance item = (ResourceClassInheritance)i.next();
                    items.add(item);
                }
            }
            return (Set)items.clone();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addResourceClassInheritance(ResourceClassInheritance item)
    {
        synchronized(resourceClassLock)
        {
            try
            {
                persistence.save((Persistent)item);
            }
            catch(PersistenceException e)
            {
                throw new BackendException("Failed to save ResourceClassInheritance", e);
            }
            ResourceClass parent = item.getParent();
            Set items = (Set)resourceClassInheritanceByResourceClass.get(parent);
            if(items == null)
            {
                items = new HashSet();
                resourceClassInheritanceByResourceClass.put(parent, items);
            }
            items.add(item);
            ResourceClass child = item.getChild();
            items = (Set)resourceClassInheritanceByResourceClass.get(child);
            if(items == null)
            {
                items = new HashSet();
                resourceClassInheritanceByResourceClass.put(child, items);
            }
            items.add(item);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void deleteResourceClassInheritance(ResourceClassInheritance item)
    {
        synchronized(resourceClassLock)
        {
            try
            {
                persistence.delete((Persistent)item);
            }
            catch(PersistenceException e)
            {
                throw new BackendException("Failed to delete ResourceClassInheritance", e);
            }
            ResourceClass parent = item.getParent();
            Set items = (Set)resourceClassInheritanceByResourceClass.get(parent);
            if(items != null)
            {
                items.remove(item);
            }
            ResourceClass child = item.getChild();
            items = (Set)resourceClassInheritanceByResourceClass.get(child);
            if(items != null)
            {
                items.remove(item);
            }
        }
    }       
    
    // Security - Subject ////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public Subject[] getSubject()
    {
        Set all = subjectRegistry.get();
        Subject[] result = new Subject[all.size()];
        all.toArray(result);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public Subject getSubject(long id)
        throws EntityDoesNotExistException
    {
        return (Subject)subjectRegistry.get(id);
    }
    
    /**
     * {@inheritDoc}
     */
    public Subject getSubject(String name)
        throws EntityDoesNotExistException
    {
        try
        {
            return (Subject)subjectRegistry.getUnique(name);
        }
        catch(AmbigousEntityNameException e)
        {
            throw new BackendException("integrity constranits corrupted", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addSubject(Subject item)
        throws EntityExistsException
    {
        subjectRegistry.addUnique(item);
    }
    
    /**
     * {@inheritDoc}
     */
    public void deleteSubject(Subject item)
        throws EntityInUseException
    {
        boolean shouldCommit = false;
        try
        {
            shouldCommit = persistence.getDatabase().beginTransaction();
            // check for subordinates
            int subordinates = persistence.count("arl_subject", 
                                       "supervisor = "+item.getId());
            if(subordinates > 0)
            {
                throw new EntityInUseException(item.getName()+" has "+
                                               subordinates+" subordinates");
            }
            // check for created resources
            int created = persistence.count("arl_resource", 
                                  "created_by = "+item.getId());
            if(created > 0)
            {
                throw new EntityInUseException(item.getName()+
                                               " has created "+created+" resources");
            }
            // check for owned resources
            int owned = persistence.count("arl_resource", 
                                "owned_by = "+item.getId());
            if(owned > 0)
            {
                throw new EntityInUseException(item.getName()+
                                               " owns "+owned+" resources");
            }
            // check for modified resources
            int modified = persistence.count("arl_resource",
                                   "modified_by = "+item.getId());
            if(modified > 0)
            {
                    throw new EntityInUseException(item.getName()+
                                                   " has modified "+modified+" resources");
            }
            // check for granted role assignments
            int grantedRoles = persistence.count("arl_role_assignment", 
                 "grantor = "+item.getId());
            if(grantedRoles > 0)
            {
                throw new EntityInUseException(item.getName()+
                    " has made "+grantedRoles+" role grants");
            }
            // check for granted permission assignments
            int grantedPermissions = persistence.count("arl_permission_assignment", 
                 "grantor = "+item.getId());
            if(grantedPermissions > 0)
            {
                throw new EntityInUseException(item.getName()+
                    " has made "+grantedPermissions+" permission grants");
            }
                
            subjectRegistry.delete(item);
            persistence.getDatabase().commitTransaction(shouldCommit);
        }
        catch(PersistenceException e)
        {
            try
            {
                persistence.getDatabase().rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            throw new BackendException("failed to query subject references", e);
        }
        catch(BackendException ex)
        {
            try
            {
                persistence.getDatabase().rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            throw ex;
        }
        catch(Exception e)
        {
            try
            {
                persistence.getDatabase().rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            throw new BackendException("Failed to delete Subject", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void renameSubject(Subject item, String name)
        throws EntityExistsException
    {
        subjectRegistry.renameUnique(item, name);
    }

    // Security - Role ///////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public Role[] getRole()
    {
        Set all = roleRegistry.get();
        Role[] result = new Role[all.size()];
        all.toArray(result);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public Role getRole(long id)
        throws EntityDoesNotExistException
    {
        return (Role)roleRegistry.get(id);        
    }
    
    /**
     * {@inheritDoc}
     */
    public Role[] getRole(String name)
    {
        Set all = roleRegistry.get(name);
        Role[] result = new Role[all.size()];
        all.toArray(result);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public Role getUniqueRole(String name)
        throws IllegalStateException
    {
        try
        {
            return (Role)roleRegistry.getUnique(name);
        }
        catch(AmbigousEntityNameException e)
        {
            throw new IllegalStateException("role name "+name+" is not unique");
        }
        catch(EntityDoesNotExistException e)
        {
            throw new IllegalStateException("role "+name+" does not exist");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addRole(Role item)
    {
        roleRegistry.add(item);
    }
    
    /**
     * {@inheritDoc}
     */
    public void deleteRole(Role item)
        throws EntityInUseException
    {
        boolean shouldCommit = false;
        try
        {
            shouldCommit = persistence.getDatabase().beginTransaction();
            // check for sub roles
            int subRoles = persistence.count("arl_role_implication", 
                                                 "super_role = "+item.getId());
            if(subRoles > 0)
            {
                throw new EntityInUseException("Role "+item.getName()+" has "+subRoles+
                                               " subroles");
            }
            // check for role assignments
            int subjects = persistence.count("arl_role_assignment", 
                                                 "role_id = "+item.getId());
            if(subjects > 0)
            {
                throw new EntityInUseException("Role "+item.getName()+" has been assigned to "+
                                               subjects+" subjects");
            }
            // check for permission assignemts
            int assignments = persistence.count("arl_permission_assignment", 
                                                    "role_id = "+item.getId());
            if(assignments > 0)
            {
                throw new EntityInUseException("Role "+item.getName()+" received "+
                                               assignments+" permssion assignments");
            }

            roleRegistry.delete(item);
            persistence.getDatabase().commitTransaction(shouldCommit);
        }
        catch(PersistenceException e)
        {
            try
            {
                persistence.getDatabase().rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            throw new BackendException("failed to query role references", e);
        }
        catch(BackendException ex)
        {
            try
            {
                persistence.getDatabase().rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            throw ex;
        }
        catch(Exception e)
        {
            try
            {
                persistence.getDatabase().rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            throw new BackendException("Failed to delete Role", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void renameRole(Role item, String name)
    {
        roleRegistry.rename(item, name);
    }

    // Security - Permission /////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public Permission[] getPermission()
    {
        Set all = permissionRegistry.get();
        Permission[] result = new Permission[all.size()];
        all.toArray(result);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public Permission getPermission(long id)
        throws EntityDoesNotExistException
    {
        return (Permission)permissionRegistry.get(id);
    }
    
    /**
     * {@inheritDoc}
     */
    public Permission[] getPermission(String name)
    {
        Set all = permissionRegistry.get(name);
        Permission[] result = new Permission[all.size()];
        all.toArray(result);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public Permission getUniquePermission(String name)
        throws IllegalStateException
    {
        try
        {
           return (Permission)permissionRegistry.getUnique(name);
        }
        catch(AmbigousEntityNameException e)
        {
            throw new IllegalStateException("permission name "+name+" is not unique");
        }
        catch(EntityDoesNotExistException e)
        {
            throw new IllegalStateException("permission "+name+" does not exist");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addPermission(Permission item)
    {
        permissionRegistry.add(item);
    }
    
    /**
     * {@inheritDoc}
     */
    public void deletePermission(Permission item)
        throws EntityInUseException
    {
        boolean shouldCommit = false;
        try
        {
            shouldCommit = persistence.getDatabase().beginTransaction();
            // check for assignments
            int assignments = persistence.count("arl_permission_assignment", 
                                                    "permission_id = "+item.getId());
            if(assignments > 0)
            {
                throw new EntityInUseException("Permission "+item.getName()+" has "+
                                               assignments+" active assignments");
            }
            // check for associations
            int associations = persistence.count("arl_permission_association",
                                                     "permission_id = "+item.getId());
            if(assignments > 0)
            {
                    throw new EntityInUseException("Permission "+item.getName()+" is associated "+
                                                   "with "+associations+" resource classes");
            }
            
            permissionRegistry.delete(item);
            persistence.getDatabase().commitTransaction(shouldCommit);
        }
        catch(PersistenceException e)
        {
            try
            {
                persistence.getDatabase().rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            throw new BackendException("failed to query permission references", e);
        }
        catch(BackendException ex)
        {
            try
            {
                persistence.getDatabase().rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            throw ex;
        }
        catch(Exception e)
        {
            try
            {
                persistence.getDatabase().rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            throw new BackendException("Failed to delete Permission", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void renamePermission(Permission item, String name)
    {
        permissionRegistry.rename(item, name);
    }

    // Security - RoleImplication ////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public Set getRoleImplications(Role owner)
    {
        synchronized(roleLock)
        {
            HashSet items = (HashSet)roleImplicationByRole.get(owner);
            if(items == null)
            {
                items = new HashSet();
                roleImplicationByRole.put(owner, items);
                List list;
                try
                {
                    list = persistence.load("super_role = "+owner.getId()+" OR "+
                                            "sub_role = "+owner.getId(),
                                            roleImplicationFactory);
                }
                catch(PersistenceException e)
                {
                    throw new BackendException("Failed to load RoleImplications", e);
                }
                Iterator i = list.iterator();
                while(i.hasNext())
                {
                    RoleImplication item = (RoleImplication)i.next();
                    items.add(item);
                }
            }
            return (Set)items.clone();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addRoleImplication(RoleImplication item)
    {
        synchronized(roleLock)
        {
            try
            {
                persistence.save((Persistent)item);
            }
            catch(PersistenceException e)
            {
                throw new BackendException("Failed to save RoleImplication", e);
            }
            Role superRole = item.getSuperRole();
            Set items = (Set)roleImplicationByRole.get(superRole);
            if(items == null)
            {
                items = new HashSet();
                roleImplicationByRole.put(superRole, items);
            }
            items.add(item);
            Role subRole = item.getSubRole();
            items = (Set)roleImplicationByRole.get(subRole);
            if(items == null)
            {
                items = new HashSet();
                roleImplicationByRole.put(subRole, items);
            }
            items.add(item);
        }
        coralEventHub.getGlobal().fireRoleImplicationChangeEvent(item, true);
    }

    /**
     * {@inheritDoc}
     */
    public void deleteRoleImplication(RoleImplication item)
    {
        synchronized(roleLock)
        {
            try
            {
                persistence.delete((Persistent)item);
            }
            catch(PersistenceException e)
            {
                throw new BackendException("Failed to delete RoleImplication", e);
            }
            Role superRole = item.getSuperRole();
            Set items = (Set)roleImplicationByRole.get(superRole);
            if(items != null)
            {
                items.remove(item);
            }
            Role subRole = item.getSubRole();
            items = (Set)roleImplicationByRole.get(subRole);
            if(items != null)
            {
                items.remove(item);
            }
        }
        coralEventHub.getGlobal().fireRoleImplicationChangeEvent(item, false);
    }       
    
    // Security - RoleAssignment /////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public Set getRoleAssignments(Subject owner)
    {
        synchronized(roleLock)
        {
            HashSet items = (HashSet)roleAssignmentBySubject.get(owner);
            if(items == null)
            {
                items = new HashSet();
                roleAssignmentBySubject.put(owner, items);
                List list;
                try
                {
                    list = persistence.load("subject_id = "+owner.getId(),
                                            roleAssignmentFactory);
                }
                catch(PersistenceException e)
                {
                    throw new BackendException("Failed to load RoleAssignments", e);
                }
                Iterator i = list.iterator();
                while(i.hasNext())
                {
                    RoleAssignment item = (RoleAssignment)i.next();
                    items.add(item);
                }
            }
            return (Set)items.clone();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Set getRoleAssignments(Role owner)
    {
        synchronized(roleLock)
        {
            HashSet items = (HashSet)roleAssignmentByRole.get(owner);
            if(items == null)
            {
                items = new HashSet();
                roleAssignmentByRole.put(owner, items);
                List list;
                try
                {
                    list = persistence.load("role_id = "+owner.getId(),
                                            roleAssignmentFactory);
                }
                catch(PersistenceException e)
                {
                    throw new BackendException("Failed to load RoleAssignments", e);
                }
                Iterator i = list.iterator();
                while(i.hasNext())
                {
                    RoleAssignment item = (RoleAssignment)i.next();
                    items.add(item);
                }
            }
            return (Set)items.clone();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addRoleAssignment(RoleAssignment item)
    {
        synchronized(roleLock)
        {
            try
            {
                persistence.save((Persistent)item);
            }
            catch(PersistenceException e)
            {
                throw new BackendException("Failed to save RoleAssignment", e);
            }
            Set itemsBySubject = (Set)roleAssignmentBySubject.get(item.getSubject());
            if(itemsBySubject != null)
            {
                itemsBySubject.add(item);
            }
            Set itemsByRole = (Set)roleAssignmentByRole.get(item.getRole());
            if(itemsByRole != null)
            {
                itemsByRole.add(item);
            }
        }
        coralEventHub.getGlobal().fireRoleAssignmentChangeEvent(item, true);
    }

    /**
     * {@inheritDoc}
     */
    public void deleteRoleAssignment(RoleAssignment item)
    {
        synchronized(roleLock)
        {
            try
            {
                persistence.delete((Persistent)item);
            }
            catch(PersistenceException e)
            {
                throw new BackendException("Failed to delete RoleAssignment", e);
            }
            Set itemsBySubject = (Set)roleAssignmentBySubject.get(item.getSubject());
            if(itemsBySubject != null)
            {
                itemsBySubject.remove(item);
            }
            Set itemsByRole = (Set)roleAssignmentByRole.get(item.getRole());
            if(itemsByRole != null)
            {
                itemsByRole.remove(item);
            }
        }
        coralEventHub.getGlobal().fireRoleAssignmentChangeEvent(item, false);
    }       

    // Security - PermissionAssociation //////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public Set getPermissionAssociations(ResourceClass owner)
    {
        synchronized(permissionLock)
        {
            HashSet items = (HashSet)permissionAssociationByResourceClass.get(owner);
            if(items == null)
            {
                items = new HashSet();
                permissionAssociationByResourceClass.put(owner, items);
                List list;
                try
                {
                    list = persistence.load("resource_class_id = "+owner.getId(),
                                            permissionAssociationFactory);
                }
                catch(PersistenceException e)
                {
                    throw new BackendException("Failed to load PermissionAssociations", e);
                }
                Iterator i = list.iterator();
                while(i.hasNext())
                {
                    PermissionAssociation item = (PermissionAssociation)i.next();
                    items.add(item);
                }
            }
            return (Set)items.clone();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Set getPermissionAssociations(Permission owner)
    {
        synchronized(permissionLock)
        {
            HashSet items = (HashSet)permissionAssociationByPermission.get(owner);
            if(items == null)
            {
                items = new HashSet();
                permissionAssociationByPermission.put(owner, items);
                List list;
                try
                {
                    list = persistence.load("permission_id = "+owner.getId(),
                                            permissionAssociationFactory);
                }
                catch(PersistenceException e)
                {
                    throw new BackendException("Failed to load PermissionAssociations", e);
                }
                Iterator i = list.iterator();
                while(i.hasNext())
                {
                    PermissionAssociation item = (PermissionAssociation)i.next();
                    items.add(item);
                }
            }
            return (Set)items.clone();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addPermissionAssociation(PermissionAssociation item)
    {
        synchronized(permissionLock)
        {
            try
            {
                persistence.save((Persistent)item);
            }
            catch(PersistenceException e)
            {
                throw new BackendException("Failed to save PermissionAssociation", e);
            }
            Set itemsByResourceClass = (Set)permissionAssociationByResourceClass.
                get(item.getResourceClass());
            if(itemsByResourceClass != null)
            {
                itemsByResourceClass.add(item);
            }
            Set itemsByPermission = (Set)permissionAssociationByPermission.
                get(item.getPermission());
            if(itemsByPermission != null)
            {
                itemsByPermission.add(item);
            }
        }
        coralEventHub.getGlobal().firePermissionAssociationChangeEvent(item, true);
    }

    /**
     * {@inheritDoc}
     */
    public void deletePermissionAssociation(PermissionAssociation item)
    {
        synchronized(permissionLock)
        {
            try
            {
                persistence.delete((Persistent)item);
            }
            catch(PersistenceException e)
            {
                throw new BackendException("Failed to delete PermissionAssociation", e);
            }
            Set itemsByResourceClass = (Set)permissionAssociationByResourceClass.
                get(item.getResourceClass());
            if(itemsByResourceClass != null)
            {
                itemsByResourceClass.remove(item);
            }
            Set itemsByPermission = (Set)permissionAssociationByPermission.
                get(item.getPermission());
            if(itemsByPermission != null)
            {
                itemsByPermission.remove(item);
            }
        }
        coralEventHub.getGlobal().firePermissionAssociationChangeEvent(item, false);
    }       

    // Security - PermissionAssignment ///////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public Set getPermissionAssignments(Resource owner)
    {
        synchronized(permissionLock)
        {
            HashSet items = (HashSet)permissionAssignmentByResource.get(owner);
            if(items == null)
            {
                items = new HashSet();
                permissionAssignmentByResource.put(owner, items);
                List list;
                try
                {
                    list = persistence.load("resource_id = "+owner.getId(),
                                            permissionAssignmentFactory);
                }
                catch(PersistenceException e)
                {
                    throw new BackendException("Failed to load PermissionAssignments", e);
                }
                Iterator i = list.iterator();
                while(i.hasNext())
                {
                    PermissionAssignment item = (PermissionAssignment)i.next();
                    items.add(item);
                }
            }
            return (Set)items.clone();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Set getPermissionAssignments(Role owner)
    {
        synchronized(permissionLock)
        {
            HashSet items = (HashSet)permissionAssignmentByRole.get(owner);
            if(items == null)
            {
                items = new HashSet();
                permissionAssignmentByRole.put(owner, items);
                List list;
                try
                {
                    list = persistence.load("role_id = "+owner.getId(),
                                            permissionAssignmentFactory);
                }
                catch(PersistenceException e)
                {
                    throw new BackendException("Failed to load PermissionAssignments", e);
                }
                Iterator i = list.iterator();
                while(i.hasNext())
                {
                    PermissionAssignment item = (PermissionAssignment)i.next();
                    items.add(item);
                }
            }
            return (Set)items.clone();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addPermissionAssignment(PermissionAssignment item)
    {
        synchronized(permissionLock)
        {
            try
            {
                persistence.save((Persistent)item);
            }
            catch(PersistenceException e)
            {
                throw new BackendException("Failed to save PermissionAssignment", e);
            }
            Resource owner = item.getResource();
            Set items = (Set)permissionAssignmentByResource.get(owner);
            if(items != null)
            {
                items.add(item);
            }
            Role role = item.getRole();
            items = (Set)permissionAssignmentByRole.get(role);
            if(items != null)
            {
                items.add(item);
            }
        }
        coralEventHub.getGlobal().firePermissionAssignmentChangeEvent(item, true);
    }

    /**
     * {@inheritDoc}
     */
    public void deletePermissionAssignment(PermissionAssignment item)
    {
        synchronized(permissionLock)
        {
            try
            {
                persistence.delete((Persistent)item);
            }
            catch(PersistenceException e)
            {
                throw new BackendException("Failed to delete PermissionAssignment", e);
            }
            Resource owner = item.getResource();
            Set items = (Set)permissionAssignmentByResource.get(owner);
            if(items != null)
            {
                items.remove(item);
            }
            Role role = item.getRole();
            items = (Set)permissionAssignmentByRole.get(role);
            if(items != null)
            {
                items.remove(item);
            }
        }
        coralEventHub.getGlobal().firePermissionAssignmentChangeEvent(item, false);
    }       

    // PermissionAssociationChangeListener interface /////////////////////////

    /**
     * {@inheritDoc}
     */
    public void permissionsChanged(PermissionAssociation item, boolean added)
    {
        synchronized(permissionLock)
        {
            if(added)
            {
                Set itemsByResourceClass = (Set)permissionAssociationByResourceClass.
                    get(item.getResourceClass());
                if(itemsByResourceClass != null)
                {
                    itemsByResourceClass.add(item);
                }
                Set itemsByPermission = (Set)permissionAssociationByPermission.
                    get(item.getPermission());
                if(itemsByPermission != null)
                {
                    itemsByPermission.add(item);
                }
            }
            else
            {
                Set itemsByResourceClass = (Set)permissionAssociationByResourceClass.
                    get(item.getResourceClass());
                if(itemsByResourceClass != null)
                {
                    itemsByResourceClass.remove(item);
                }
                Set itemsByPermission = (Set)permissionAssociationByPermission.
                    get(item.getPermission());
                if(itemsByPermission != null)
                {
                    itemsByPermission.remove(item);
                }
            }
        }
    }
    
    // PermissionAssignmentChangeListener interface //////////////////////////

    /**
     * {@inheritDoc}
     */
    public void permissionsChanged(PermissionAssignment item, boolean added)
    {
        synchronized(permissionLock)
        {
            Set itemsByResource = (Set)permissionAssignmentByResource.get(item.getResource());
            Set itemsByRole = (Set)permissionAssignmentByRole.get(item.getRole());
            if(added)
            {
                if(itemsByResource != null)
                {
                    itemsByResource.add(item);
                }
                if(itemsByRole != null)
                {
                    itemsByRole.add(item);
                }
            }
            else
            {
                if(itemsByResource != null)
                {
                    itemsByResource.remove(item);
                }
                if(itemsByRole != null)
                {
                    itemsByRole.remove(item);
                }
            }
        }
    }
    
    // RoleAssignmentChangeListener inteface /////////////////////////////////
    
    /**
     * {@inheritDoc}
     */
    public void rolesChanged(RoleAssignment item, boolean added)
    {
        synchronized(roleLock)
        {
            if(added)
            {
                Set itemsBySubject = (Set)roleAssignmentBySubject.get(item.getSubject());
                if(itemsBySubject != null)
                {
                    itemsBySubject.add(item);
                }
                Set itemsByRole = (Set)roleAssignmentByRole.get(item.getRole());
                if(itemsByRole != null)
                {
                    itemsByRole.add(item);
                }
            }
            else
            {
                Set itemsBySubject = (Set)roleAssignmentBySubject.get(item.getSubject());
                if(itemsBySubject != null)
                {
                    itemsBySubject.remove(item);
                }
                Set itemsByRole = (Set)roleAssignmentByRole.get(item.getRole());
                if(itemsByRole != null)
                {
                    itemsByRole.remove(item);
                }
            }
        }
    }

    // RoleImplicationChangeListener interface ///////////////////////////////
    
    /**
     * {@inheritDoc}
     */
    public void roleChanged(RoleImplication item, boolean added)
    {
        synchronized(roleLock)
        {
            Role superRole = item.getSuperRole();
            Set items = (Set)roleImplicationByRole.get(superRole);
            if(added)
            {
                if(items == null)
                {
                    items = new HashSet();
                    roleImplicationByRole.put(superRole, items);
                }
                items.add(item);
            }
            else
            {
                if(items != null)
                {
                    items.remove(item);
                }
            }
            Role subRole = item.getSuperRole();
            items = (Set)roleImplicationByRole.get(subRole);
            if(added)
            {
                if(items == null)
                {
                    items = new HashSet();
                    roleImplicationByRole.put(subRole, items);
                }
                items.add(item);
            }
        }
    }

    // ResourceClassInheritanceChangeListener inteface ///////////////////////

    /**
     * {@inheritDoc}
     */
    public void inheritanceChanged(ResourceClassInheritance item, boolean added)
    {
        synchronized(resourceClassLock)
        {
            ResourceClass parent = item.getParent();
            Set items = (Set)resourceClassInheritanceByResourceClass.get(parent);
            if(added)
            {
                if(items == null)
                {
                    items = new HashSet();
                    resourceClassInheritanceByResourceClass.put(parent, items);
                }
                items.add(item);
            }
            else
            {
                if(items != null)
                {
                    items.remove(item);
                }
            }
            ResourceClass child = item.getChild();
            items = (Set)resourceClassInheritanceByResourceClass.get(child);
            if(added)
            {
                if(items == null)
                {
                    items = new HashSet();
                    resourceClassInheritanceByResourceClass.put(child, items);
                }
                items.add(item);
            }
            else
            {
                if(items != null)
                {
                    items.remove(item);
                }
            }
        }
    }

    // ResourceClassAttributesChangeListener interface ///////////////////////

    /**
     * {@inheritDoc}
     */
    public void attributesChanged(AttributeDefinition item, boolean added)
    {
        synchronized(resourceClassLock)
        {
            ResourceClass owner = item.getDeclaringClass();
            Set items = (Set)attributeDefinitionByResourceClass.get(owner);
            if(added)
            {
                if(items == null)
                {
                    items = new HashSet();
                    attributeDefinitionByResourceClass.put(owner, items);
                }
                items.add(item);
            }
            else
            {
                if(items != null)
                {
                    items.remove(item);
                }
            }
        }
    }    

    // Non-cached cross-reference information ////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public RoleAssignment[] getGrantedRoleAssignments(Subject subject)
    {
        try
        {
            List list = persistence.load("grantor = "+subject.getId(),
                                             roleAssignmentFactory);
            RoleAssignment[] result = new RoleAssignment[list.size()];
            list.toArray(result);
            return result;
        }
        catch(PersistenceException e)
        {
            throw new BackendException("failed to load role assignments", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public PermissionAssignment[] getGrantedPermissionAssignments(Subject subject)
    {
        try
        {
            List list = persistence.load("grantor = "+subject.getId(),
                                             permissionAssignmentFactory);
            PermissionAssignment[] result = new PermissionAssignment[list.size()];
            list.toArray(result);
            return result;
        }
        catch(PersistenceException e)
        {
            throw new BackendException("failed to load permission assignments", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Resource[] getCreatedResources(Subject subject)
    {
        try
        {
            List list = persistence.load("created_by = "+subject.getId(),
                                             roleAssignmentFactory);
            Resource[] result = new Resource[list.size()];
            for(int i=0; i<list.size(); i++)
            {
                result[i] = coral.getStore().getResource(((Resource)list.get(i)).getId());
            }
            return result;
        }
        catch(PersistenceException e)
        {
            throw new BackendException("failed to load role assignments", e);
        }
        catch(EntityDoesNotExistException e)
        {
            throw new BackendException("internal error", e);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public Resource[] getOwnedResources(Subject subject)
    {
        try
        {
            List list = persistence.load("owned_by = "+subject.getId(),
                                             roleAssignmentFactory);
            ArrayList temp = new ArrayList();
            ArrayList stack = new ArrayList();
            for(int i=0; i<list.size(); i++)
            {
                stack.add(coral.getStore().getResource(((Resource)list.get(i)).getId()));
            }
            while(stack.size() > 0)
            {
                Resource r = (Resource)stack.remove(stack.size()-1);
                temp.add(r);
                Resource[] subs = coral.getStore().getResource(r);
                for(int i=0; i<subs.length; i++)
                {
                    stack.add(subs[i]);
                }
            }
            Resource[] result = new Resource[temp.size()];
            temp.toArray(result);
            return result;
        }
        catch(PersistenceException e)
        {
            throw new BackendException("failed to load role assignments", e);
        }
        catch(EntityDoesNotExistException e)
        {
            throw new BackendException("internal error", e);
        }
    }
}
