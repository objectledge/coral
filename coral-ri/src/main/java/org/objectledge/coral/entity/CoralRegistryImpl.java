package org.objectledge.coral.entity;

// JDBC
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.jcontainer.dna.ConfigurationException;
import org.jcontainer.dna.Logger;
import org.objectledge.cache.CacheFactory;
import org.objectledge.collections.ImmutableHashSet;
import org.objectledge.collections.ImmutableSet;
import org.objectledge.coral.BackendException;
import org.objectledge.coral.CoralCore;
import org.objectledge.coral.Instantiator;
import org.objectledge.coral.PreloadingParticipant;
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
import org.objectledge.database.persistence.Persistent;
import org.objectledge.database.persistence.PersistentFactory;

/**
 * Manages persistence of {@link Entity}, {@link Assignment} and {@link Association} objects.
 * 
 * @version $Id: CoralRegistryImpl.java,v 1.14 2008-01-02 00:31:03 rafal Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class CoralRegistryImpl
    implements CoralRegistry, PermissionAssociationChangeListener,
    PermissionAssignmentChangeListener, RoleAssignmentChangeListener,
    RoleImplicationChangeListener, ResourceClassInheritanceChangeListener,
    ResourceClassAttributesChangeListener, PreloadingParticipant
{

    // Member objects ////////////////////////////////////////////////////////

    /** The {@link Persistence}. */
    private Persistence persistence;

    /** The logger. */
    private Logger log;

    /** The event hub. */
    private CoralEventHub coralEventHub;

    /** The component hub. */
    private CoralCore coral;

    // Factory classes

    /** The <code>PersistentFactory</code> for <code>ResourceClassInheritance</code> objects. */
    private PersistentFactory<ResourceClassInheritanceImpl> resourceClassInheritanceFactory;

    /** The <code>PersistentFactory</code> for <code>AttributeDefinition</code> objects. */
    @SuppressWarnings("rawtypes")
    private PersistentFactory<AttributeDefinitionImpl> attributeDefinitionFactory;

    /** The <code>PersistentFactory</code> for <code>RoleImplication</code> objects. */
    private PersistentFactory<RoleImplicationImpl> roleImplicationFactory;

    /** The <code>PersistentFactory</code> for <code>RoleAssignment</code> objects. */
    private PersistentFactory<RoleAssignmentImpl> roleAssignmentFactory;

    /** The <code>PersistentFactory</code> for <code>PermissionAssociation</code> objects. */
    private PersistentFactory<PermissionAssociationImpl> permissionAssociationFactory;

    /** The <code>PersistentFactory</code> for <code>PermissionAssignment</code> objects. */
    private PersistentFactory<PermissionAssignmentImpl> permissionAssignmentFactory;

    // caches

    /**
     * lock object for resourceClassInheritanceByResourceClass and
     * attributeDefinitionByResourceClass
     */
    private Object resourceClassLock = new Object();

    /** <code>ResourceClassInheritance</code> <code>Set</code> by <code>ResourceClass</code> */
    private Map<ResourceClass<?>, Set<ResourceClassInheritance>> resourceClassInheritanceByResourceClass;

    /**
     * Weak mapping of <code>ResourceClass</code> objects into sets of
     * <code>AttributeDefinition</code> objects.
     */
    private Map<ResourceClass<?>, Set<AttributeDefinition<?>>> attributeDefinitionByResourceClass;

    /** lock object for roleImplicationByRole and roleAssignmentByRole */
    private Object roleLock = new Object();

    /**
     * Weak mapping of <code>Role</code> objects into sets of <code>RoleImplication</code> objects.
     */
    private Map<Role, Set<RoleImplication>> roleImplicationByRole;

    /**
     * Weak mapplig of <code>Role</code> objects into sets of <code>RoleAssignment</code> objects.
     */
    private Map<Role, Set<RoleAssignment>> roleAssignmentByRole;

    /**
     * Weak mapplig of <code>Subject</code> objects into sets of <code>RoleAssignment</code>
     * objects.
     */
    private Map<Subject, Set<RoleAssignment>> roleAssignmentBySubject;

    /**
     * lock object for permissionAssociationByResourceClass permissionAssociationByPermission
     * permissionAssignmentByResource and permissionAssignmentByRole
     */
    private Object permissionLock = new Object();

    /**
     * Weak mapping of <code>ResourceClass</code> objects into sets of
     * <code>PermissionAssociation</code> objects.
     */
    private Map<ResourceClass<?>, Set<PermissionAssociation>> permissionAssociationByResourceClass;

    /**
     * Weak mapping of <code>Permission</code> objects into sets of
     * <code>PermissionAssociation</code> objects.
     */
    private Map<Permission, Set<PermissionAssociation>> permissionAssociationByPermission;

    /**
     * Weak mapping of <code>Resource</code> objects into sets of <code>PermissionAssignments</code>
     */
    private Map<Resource, Set<PermissionAssignment>> permissionAssignmentByResource;

    /**
     * Weak mapping of <code>Role</code> objects into sets of <code>PermissionAssignments</code>
     * objects.
     */
    private Map<Role, Set<PermissionAssignment>> permissionAssignmentByRole;

    // Registires

    /** <code>ResourceClass</code> registry. */
    private EntityRegistry<ResourceClassImpl<?>> resourceClassRegistry;

    /** <code>AttributeClass</code> registry. */
    private EntityRegistry<AttributeClassImpl<?>> attributeClassRegistry;

    /** <code>AttributeDefinition</code> registry. */
    private EntityRegistry<AttributeDefinitionImpl<?>> attributeDefinitionRegistry;

    /** <code>Permission</code> registry. */
    private EntityRegistry<PermissionImpl> permissionRegistry;

    /** <code>Role</code> registry. */
    private EntityRegistry<RoleImpl> roleRegistry;

    /** <code>Subject</code> registry. */
    private EntityRegistry<SubjectImpl> subjectRegistry;

    // Initialization ////////////////////////////////////////////////////////

    /**
     * Constructs the {@link CoralRegistry} implementation.
     * 
     * @param persistence the persistence subsystem
     * @param cacheFactory the cache factory.
     * @param coralEventHub the event hub.
     * @param coral the component hub.
     * @param instantiator component instantiator.
     * @param log the logger.
     * @throws ConfigurationException if the configuration is invalid.
     */
    public CoralRegistryImpl(Persistence persistence, CacheFactory cacheFactory,
        CoralEventHub coralEventHub, CoralCore coral, Instantiator instantiator, Logger log)
        throws ConfigurationException
    {
        this.persistence = persistence;
        this.coralEventHub = coralEventHub;
        this.coral = coral;
        this.log = log;

        setupCaches(cacheFactory);
        setupFactories(instantiator);
        setupRegistries(cacheFactory, instantiator);
        setupListener();
    }

    /**
     * Sets up the instance chaces for reflection objects.
     */
    private void setupCaches(CacheFactory cacheFactory)
    {
        attributeDefinitionByResourceClass = new WeakHashMap<>();
        resourceClassInheritanceByResourceClass = new WeakHashMap<>();

        roleImplicationByRole = new WeakHashMap<>();

        roleAssignmentBySubject = new WeakHashMap<>();
        roleAssignmentByRole = new WeakHashMap<>();

        permissionAssociationByResourceClass = new WeakHashMap<>();
        permissionAssociationByPermission = new WeakHashMap<>();

        permissionAssignmentByRole = new WeakHashMap<>();
        permissionAssignmentByResource = new WeakHashMap<>();

        cacheFactory
            .registerForPeriodicExpunge((WeakHashMap<?, ?>)attributeDefinitionByResourceClass);
        cacheFactory
            .registerForPeriodicExpunge((WeakHashMap<?, ?>)resourceClassInheritanceByResourceClass);
        cacheFactory.registerForPeriodicExpunge((WeakHashMap<?, ?>)roleImplicationByRole);
        cacheFactory.registerForPeriodicExpunge((WeakHashMap<?, ?>)roleAssignmentBySubject);
        cacheFactory.registerForPeriodicExpunge((WeakHashMap<?, ?>)roleAssignmentByRole);
        cacheFactory
            .registerForPeriodicExpunge((WeakHashMap<?, ?>)permissionAssociationByResourceClass);
        cacheFactory
            .registerForPeriodicExpunge((WeakHashMap<?, ?>)permissionAssociationByPermission);
        cacheFactory.registerForPeriodicExpunge((WeakHashMap<?, ?>)permissionAssignmentByRole);
        cacheFactory.registerForPeriodicExpunge((WeakHashMap<?, ?>)permissionAssignmentByResource);
    }

    private void setupFactories(Instantiator instantiator)
    {
        resourceClassInheritanceFactory = instantiator
            .getPersistentFactory(ResourceClassInheritanceImpl.class);

        attributeDefinitionFactory = instantiator
            .getPersistentFactory(AttributeDefinitionImpl.class);

        roleImplicationFactory = instantiator.getPersistentFactory(RoleImplicationImpl.class);

        roleAssignmentFactory = instantiator.getPersistentFactory(RoleAssignmentImpl.class);

        permissionAssociationFactory = instantiator
            .getPersistentFactory(PermissionAssociationImpl.class);

        permissionAssignmentFactory = instantiator
            .getPersistentFactory(PermissionAssignmentImpl.class);
    }

    /**
     * Setup the entity registry.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void setupRegistries(CacheFactory cacheFactory, Instantiator instantiator)
        throws ConfigurationException
    {
        resourceClassRegistry = new EntityRegistry(persistence, cacheFactory, instantiator, log,
            "resource class", ResourceClassImpl.class);
        attributeClassRegistry = new EntityRegistry(persistence, cacheFactory, instantiator, log,
            "attribute class", AttributeClassImpl.class);
        attributeDefinitionRegistry = new EntityRegistry(persistence, cacheFactory, instantiator,
            log, "attribute definition", AttributeDefinitionImpl.class);
        permissionRegistry = new EntityRegistry<>(persistence, cacheFactory, instantiator, log,
            "permission", PermissionImpl.class);
        roleRegistry = new EntityRegistry<>(persistence, cacheFactory, instantiator, log, "role",
            RoleImpl.class);
        subjectRegistry = new EntityRegistry<>(persistence, cacheFactory, instantiator, log,
            "subject", SubjectImpl.class);
    }

    /**
     * Registers as the listener for Coral events.
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
    public ImmutableSet<AttributeClass<?>> getAllAttributeClasses()
    {
        Set<AttributeClass<?>> s = new HashSet<AttributeClass<?>>();
        for(AttributeClass<?> ac : attributeClassRegistry.get())
        {
            s.add(ac);
        }
        return new ImmutableHashSet<AttributeClass<?>>(s);
    }

    /**
     * {@inheritDoc}
     */
    public AttributeClass<?> getAttributeClass(long id)
        throws EntityDoesNotExistException
    {
        return (AttributeClass<?>)attributeClassRegistry.get(id);
    }

    /**
     * {@inheritDoc}
     */
    public AttributeClass<?> getAttributeClass(String name)
        throws EntityDoesNotExistException
    {
        try
        {
            return (AttributeClass<?>)attributeClassRegistry.getUnique(name);
        }
        catch(AmbigousEntityNameException e)
        {
            throw new BackendException("integrity constranits corrupted", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addAttributeClass(AttributeClass<?> item)
        throws EntityExistsException
    {
        attributeClassRegistry.addUnique((AttributeClassImpl<?>)item);
    }

    /**
     * {@inheritDoc}
     */
    public void renameAttributeClass(AttributeClass<?> item, String name)
        throws EntityExistsException
    {
        attributeClassRegistry.renameUnique((AttributeClassImpl<?>)item, name);
    }

    /**
     * {@inheritDoc}
     */
    public void deleteAttributeClass(AttributeClass<?> item)
        throws EntityInUseException
    {
        boolean shouldCommit = false;
        try
        {
            shouldCommit = persistence.getDatabase().beginTransaction();
            int attrs = persistence.count("coral_attribute_definition", "attribute_class_id = "
                + item.getIdString());
            if(attrs > 0)
            {
                throw new EntityInUseException("Attribute class " + item.getName() + " is used by "
                    + attrs + " attributes");
            }

            attributeClassRegistry.delete((AttributeClassImpl<?>)item);

            persistence.getDatabase().commitTransaction(shouldCommit);
        }
        catch(SQLException e)
        {
            try
            {
                persistence.getDatabase().rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            throw new BackendException("Failed to query references to " + item, e);
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
            throw new BackendException("Failed to delete " + item, e);
        }
    }

    // Schema - ResourceClass ////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public ImmutableSet<ResourceClass<?>> getAllResourceClasses()
    {
        Set<ResourceClass<?>> s = new HashSet<ResourceClass<?>>();
        for(ResourceClass<?> rc : resourceClassRegistry.get())
        {
            s.add(rc);
        }
        return new ImmutableHashSet<ResourceClass<?>>(s);
    }

    /**
     * {@inheritDoc}
     */
    public ResourceClass<?> getResourceClass(long id)
        throws EntityDoesNotExistException
    {
        return (ResourceClass<?>)resourceClassRegistry.get(id);
    }

    /**
     * {@inheritDoc}
     */
    public ResourceClass<?> getResourceClass(String name)
        throws EntityDoesNotExistException
    {
        try
        {
            return (ResourceClass<?>)resourceClassRegistry.getUnique(name);
        }
        catch(AmbigousEntityNameException e)
        {
            throw new BackendException("integrity constranits corrupted", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addResourceClass(ResourceClass<?> item)
        throws EntityExistsException
    {
        resourceClassRegistry.addUnique((ResourceClassImpl<?>)item);
    }

    /**
     * {@inheritDoc}
     */
    public void deleteResourceClass(ResourceClass<?> item)
        throws EntityInUseException
    {
        boolean shouldCommit = false;
        try
        {
            shouldCommit = persistence.getDatabase().beginTransaction();
            // Check for resources
            int resources = persistence.count("coral_resource",
                "resource_class_id = " + item.getIdString());
            if(resources > 0)
            {
                throw new EntityInUseException(resources + " resources of the class "
                    + item.getName() + " exist");
            }

            int children = persistence.count("coral_resource_class_inheritance",
                "parent = " + item.getIdString());
            if(children > 0)
            {
                throw new EntityInUseException(children + " child classes of the class "
                    + item.getName() + " exist");
            }

            AttributeDefinition<?>[] attributes = item.getDeclaredAttributes();
            for(int i = 0; i < attributes.length; i++)
            {
                coral.getSchema().deleteAttribute(item, attributes[i]);
            }
            ResourceClassInheritance[] rci = item.getInheritance();
            for(int i = 0; i < rci.length; i++)
            {
                if(rci[i].getChild().equals(item))
                {
                    coral.getSchema().deleteParentClass(item, rci[i].getParent());
                }
            }
            PermissionAssociation[] perms = item.getPermissionAssociations();
            for(int i = 0; i < perms.length; i++)
            {
                coral.getSecurity().deletePermission(item, perms[i].getPermission());
            }
            resourceClassRegistry.delete((ResourceClassImpl<?>)item);
            persistence.getDatabase().commitTransaction(shouldCommit);
        }
        catch(SQLException e)
        {
            try
            {
                persistence.getDatabase().rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            throw new BackendException("Failed to query references to " + item, e);
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
            throw new BackendException("Failed to delete " + item, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void renameResourceClass(ResourceClass<?> item, String name)
        throws EntityExistsException
    {
        resourceClassRegistry.renameUnique((ResourceClassImpl<?>)item, name);
    }

    // Schema - AttributeDefinition //////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Set<AttributeDefinition<?>> getDeclaredAttributes(ResourceClass<?> owner)
    {
        synchronized(resourceClassLock)
        {
            HashSet items = (HashSet)attributeDefinitionByResourceClass.get(owner);
            if(items == null)
            {
                try
                {
                    List list = persistence.load(attributeDefinitionFactory,
                        "resource_class_id = ?", owner.getId());
                    items = new HashSet<AttributeDefinition<?>>();
                    attributeDefinitionRegistry.resolve(list, items);
                    attributeDefinitionByResourceClass.put(owner, items);
                }
                catch(SQLException e)
                {
                    throw new BackendException("Failed to load AttributeDefinitions for " + owner,
                        e);
                }
            }
            return (Set<AttributeDefinition<?>>)items.clone();
        }
    }

    /**
     * {@inheritDoc}
     */
    public ImmutableSet<AttributeDefinition<?>> getAllAttributeDefinitions()
    {
        synchronized(resourceClassLock)
        {
            Set<? extends AttributeDefinition<?>> defs = attributeDefinitionRegistry.get();
            for(Iterator<? extends AttributeDefinition<?>> i = defs.iterator(); i.hasNext();)
            {
                AttributeDefinition<?> def = i.next();
                ResourceClass<?> owner = def.getDeclaringClass();
                Set<AttributeDefinition<?>> items = attributeDefinitionByResourceClass.get(owner);
                if(items == null)
                {
                    items = new HashSet<>();
                    attributeDefinitionByResourceClass.put(owner, items);
                }
                items.add(def);
            }
            return new ImmutableHashSet<AttributeDefinition<?>>(defs);
        }
    }

    /**
     * {@inheritDoc}
     */
    public AttributeDefinition<?> getAttributeDefinition(long id)
        throws EntityDoesNotExistException
    {
        return (AttributeDefinition<?>)attributeDefinitionRegistry.get(id);
    }

    /**
     * {@inheritDoc}
     */
    public void addAttributeDefinition(AttributeDefinition<?> item)
    {
        synchronized(resourceClassLock)
        {
            attributeDefinitionRegistry.add((AttributeDefinitionImpl<?>)item);
            ResourceClass<?> owner = item.getDeclaringClass();
            Set<AttributeDefinition<?>> items = attributeDefinitionByResourceClass.get(owner);
            if(items == null)
            {
                items = new HashSet<>();
                attributeDefinitionByResourceClass.put(owner, items);
            }
            items.add(item);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void renameAttributeDefinition(AttributeDefinition<?> item, String name)
    {
        attributeDefinitionRegistry.rename((AttributeDefinitionImpl<?>)item, name);
    }

    /**
     * {@inheritDoc}
     */
    public void deleteAttributeDefinition(AttributeDefinition<?> item)
    {
        synchronized(resourceClassLock)
        {
            attributeDefinitionRegistry.delete((AttributeDefinitionImpl<?>)item);
            ResourceClass<?> owner = item.getDeclaringClass();
            Set<AttributeDefinition<?>> items = attributeDefinitionByResourceClass.get(owner);
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
    public Set<ResourceClassInheritance> getResourceClassInheritance(ResourceClass<?> owner)
    {
        synchronized(resourceClassLock)
        {
            HashSet<ResourceClassInheritance> items = (HashSet<ResourceClassInheritance>)resourceClassInheritanceByResourceClass
                .get(owner);
            if(items == null)
            {
                try
                {
                    List<? extends ResourceClassInheritance> list = persistence.load(
                        resourceClassInheritanceFactory, "parent = ? OR child = ?", owner.getId(),
                        owner.getId());
                    items = new HashSet<>(list);
                    resourceClassInheritanceByResourceClass.put(owner, items);
                }
                catch(SQLException e)
                {
                    throw new BackendException("Failed to load ResourceClassInheritance for "
                        + owner, e);
                }
            }
            return (Set<ResourceClassInheritance>)items.clone();
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
            catch(SQLException e)
            {
                throw new BackendException("Failed to save " + item.toString(), e);
            }
            ResourceClass<?> parent = item.getParent();
            Set<ResourceClassInheritance> items = resourceClassInheritanceByResourceClass
                .get(parent);
            if(items == null)
            {
                items = new HashSet<>();
                resourceClassInheritanceByResourceClass.put(parent, items);
            }
            items.add(item);
            ResourceClass<?> child = item.getChild();
            items = resourceClassInheritanceByResourceClass.get(child);
            if(items == null)
            {
                items = new HashSet<>();
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
            catch(SQLException e)
            {
                throw new BackendException("Failed to delete " + item, e);
            }
            ResourceClass<?> parent = item.getParent();
            Set<ResourceClassInheritance> items = resourceClassInheritanceByResourceClass
                .get(parent);
            if(items != null)
            {
                items.remove(item);
            }
            ResourceClass<?> child = item.getChild();
            items = resourceClassInheritanceByResourceClass.get(child);
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
        Set<? extends Subject> all = subjectRegistry.get();
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
        subjectRegistry.addUnique((SubjectImpl)item);
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
            // check for created resources
            int created = persistence.count("coral_resource", "created_by = " + item.getIdString());
            if(created > 0)
            {
                throw new EntityInUseException(item.getName() + " has created " + created
                    + " resources");
            }
            // check for owned resources
            int owned = persistence.count("coral_resource", "owned_by = " + item.getIdString());
            if(owned > 0)
            {
                throw new EntityInUseException(item.getName() + " owns " + owned + " resources");
            }
            // check for modified resources
            int modified = persistence.count("coral_resource",
                "modified_by = " + item.getIdString());
            if(modified > 0)
            {
                throw new EntityInUseException(item.getName() + " has modified " + modified
                    + " resources");
            }
            // check for granted role assignments
            int grantedRoles = persistence.count("coral_role_assignment",
                "grantor = " + item.getIdString());
            if(grantedRoles > 0)
            {
                throw new EntityInUseException(item.getName() + " has made " + grantedRoles
                    + " role grants");
            }
            // check for granted permission assignments
            int grantedPermissions = persistence.count("coral_permission_assignment", "grantor = "
                + item.getIdString());
            if(grantedPermissions > 0)
            {
                throw new EntityInUseException(item.getName() + " has made " + grantedPermissions
                    + " permission grants");
            }

            subjectRegistry.delete((SubjectImpl)item);
            persistence.getDatabase().commitTransaction(shouldCommit);
        }
        catch(SQLException e)
        {
            try
            {
                persistence.getDatabase().rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            throw new BackendException("Failed to query references to " + item, e);
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
            throw new BackendException("Failed to delete " + item, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void renameSubject(Subject item, String name)
        throws EntityExistsException
    {
        subjectRegistry.renameUnique((SubjectImpl)item, name);
    }

    // Security - Role ///////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public Role[] getRole()
    {
        Set<RoleImpl> all = roleRegistry.get();
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
        Set<RoleImpl> all = roleRegistry.get(name);
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
            throw new IllegalStateException("role name " + name + " is not unique");
        }
        catch(EntityDoesNotExistException e)
        {
            throw new IllegalStateException("role " + name + " does not exist");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addRole(Role item)
    {
        roleRegistry.add((RoleImpl)item);
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
            int subRoles = persistence.count("coral_role_implication",
                "super_role = " + item.getIdString());
            if(subRoles > 0)
            {
                throw new EntityInUseException("Role " + item.getName() + " has " + subRoles
                    + " subroles");
            }
            // check for role assignments
            int subjects = persistence.count("coral_role_assignment",
                "role_id = " + item.getIdString());
            if(subjects > 0)
            {
                throw new EntityInUseException("Role " + item.getName() + " has been assigned to "
                    + subjects + " subjects");
            }
            // check for permission assignemts
            int assignments = persistence.count("coral_permission_assignment",
                "role_id = " + item.getIdString());
            if(assignments > 0)
            {
                throw new EntityInUseException("Role " + item.getName() + " received "
                    + assignments + " permssion assignments");
            }

            roleRegistry.delete((RoleImpl)item);
            persistence.getDatabase().commitTransaction(shouldCommit);
        }
        catch(SQLException e)
        {
            try
            {
                persistence.getDatabase().rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            throw new BackendException("Failed to query references to " + item, e);
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
            throw new BackendException("Failed to delete " + item, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void renameRole(Role item, String name)
    {
        roleRegistry.rename((RoleImpl)item, name);
    }

    // Security - Permission /////////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public Permission[] getPermission()
    {
        Set<PermissionImpl> all = permissionRegistry.get();
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
        Set<PermissionImpl> all = permissionRegistry.get(name);
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
            throw new IllegalStateException("permission name " + name + " is not unique");
        }
        catch(EntityDoesNotExistException e)
        {
            throw new IllegalStateException("permission " + name + " does not exist");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addPermission(Permission item)
    {
        permissionRegistry.add((PermissionImpl)item);
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
            int assignments = persistence.count("coral_permission_assignment", "permission_id = "
                + item.getIdString());
            if(assignments > 0)
            {
                throw new EntityInUseException("Permission " + item.getName() + " has "
                    + assignments + " active assignments");
            }
            // check for associations
            int associations = persistence.count("coral_permission_association", "permission_id = "
                + item.getIdString());
            if(assignments > 0)
            {
                throw new EntityInUseException("Permission " + item.getName() + " is associated "
                    + "with " + associations + " resource classes");
            }

            permissionRegistry.delete((PermissionImpl)item);
            persistence.getDatabase().commitTransaction(shouldCommit);
        }
        catch(SQLException e)
        {
            try
            {
                persistence.getDatabase().rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            throw new BackendException("Failed to query references to " + item, e);
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
            throw new BackendException("Failed to delete " + item, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void renamePermission(Permission item, String name)
    {
        permissionRegistry.rename((PermissionImpl)item, name);
    }

    // Security - RoleImplication ////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public Set<RoleImplication> getRoleImplications(Role owner)
    {
        synchronized(roleLock)
        {
            HashSet<RoleImplication> items = (HashSet<RoleImplication>)roleImplicationByRole
                .get(owner);
            if(items == null)
            {
                try
                {
                    List<? extends RoleImplication> list = persistence.load(roleImplicationFactory,
                        "super_role = ? OR sub_role = ?", owner.getId(), owner.getId());
                    items = new HashSet<>(list);
                    roleImplicationByRole.put(owner, items);
                }
                catch(SQLException e)
                {
                    throw new BackendException("Failed to load RoleImplications for " + owner, e);
                }
            }
            return (Set<RoleImplication>)items.clone();
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
            catch(SQLException e)
            {
                throw new BackendException("Failed to save " + item.toString(), e);
            }
            Role superRole = item.getSuperRole();
            Set<RoleImplication> items = roleImplicationByRole.get(superRole);
            if(items == null)
            {
                items = new HashSet<>();
                roleImplicationByRole.put(superRole, items);
            }
            items.add(item);
            Role subRole = item.getSubRole();
            items = roleImplicationByRole.get(subRole);
            if(items == null)
            {
                items = new HashSet<>();
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
            catch(SQLException e)
            {
                throw new BackendException("Failed to delete " + item, e);
            }
            Role superRole = item.getSuperRole();
            Set<RoleImplication> items = roleImplicationByRole.get(superRole);
            if(items != null)
            {
                items.remove(item);
            }
            Role subRole = item.getSubRole();
            items = roleImplicationByRole.get(subRole);
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
    public Set<RoleAssignment> getRoleAssignments(Subject owner)
    {
        synchronized(roleLock)
        {
            HashSet<RoleAssignment> items = (HashSet<RoleAssignment>)roleAssignmentBySubject
                .get(owner);
            if(items == null)
            {
                try
                {
                    List<? extends RoleAssignment> list = persistence.load(roleAssignmentFactory,
                        "subject_id = ?", owner.getId());
                    items = new HashSet<>(list);
                    roleAssignmentBySubject.put(owner, items);
                }
                catch(SQLException e)
                {
                    throw new BackendException("Failed to load RoleAssignments for " + owner, e);
                }
            }
            return (Set<RoleAssignment>)items.clone();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Set<RoleAssignment> getRoleAssignments(Role owner)
    {
        synchronized(roleLock)
        {
            HashSet<RoleAssignment> items = (HashSet<RoleAssignment>)roleAssignmentByRole
                .get(owner);
            if(items == null)
            {
                try
                {
                    List<? extends RoleAssignment> list = persistence.load(roleAssignmentFactory,
                        "role_id = ?", owner.getId());
                    items = new HashSet<>(list);
                    roleAssignmentByRole.put(owner, items);
                }
                catch(SQLException e)
                {
                    throw new BackendException("Failed to load RoleAssignments for " + owner, e);
                }
            }
            return (Set<RoleAssignment>)items.clone();
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
            catch(SQLException e)
            {
                throw new BackendException("Failed to save " + item.toString(), e);
            }
            Set<RoleAssignment> itemsBySubject = roleAssignmentBySubject.get(item.getSubject());
            if(itemsBySubject != null)
            {
                itemsBySubject.add(item);
            }
            Set<RoleAssignment> itemsByRole = roleAssignmentByRole.get(item.getRole());
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
            catch(SQLException e)
            {
                throw new BackendException("Failed to delete " + item, e);
            }
            Set<RoleAssignment> itemsBySubject = roleAssignmentBySubject.get(item.getSubject());
            if(itemsBySubject != null)
            {
                itemsBySubject.remove(item);
            }
            Set<RoleAssignment> itemsByRole = roleAssignmentByRole.get(item.getRole());
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
    public Set<PermissionAssociation> getPermissionAssociations(ResourceClass<?> owner)
    {
        synchronized(permissionLock)
        {
            HashSet<PermissionAssociation> items = (HashSet<PermissionAssociation>)permissionAssociationByResourceClass
                .get(owner);
            if(items == null)
            {
                try
                {
                    List<? extends PermissionAssociation> list = persistence.load(
                        permissionAssociationFactory, "resource_class_id = ?", owner.getId());
                    items = new HashSet<>(list);
                    permissionAssociationByResourceClass.put(owner, items);
                }
                catch(SQLException e)
                {
                    throw new BackendException(
                        "Failed to load PermissionAssociations for " + owner, e);
                }
            }
            return (Set<PermissionAssociation>)items.clone();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Set<PermissionAssociation> getPermissionAssociations(Permission owner)
    {
        synchronized(permissionLock)
        {
            HashSet<PermissionAssociation> items = (HashSet<PermissionAssociation>)permissionAssociationByPermission
                .get(owner);
            if(items == null)
            {
                try
                {
                    List<? extends PermissionAssociation> list = persistence.load(
                        permissionAssociationFactory, "permission_id = ?", owner.getId());
                    items = new HashSet<>(list);
                    permissionAssociationByPermission.put(owner, items);
                }
                catch(SQLException e)
                {
                    throw new BackendException(
                        "Failed to load PermissionAssociations for " + owner, e);
                }
            }
            return (Set<PermissionAssociation>)items.clone();
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
            catch(SQLException e)
            {
                throw new BackendException("Failed to save " + item.toString(), e);
            }
            Set<PermissionAssociation> itemsByResourceClass = permissionAssociationByResourceClass
                .get(item.getResourceClass());
            if(itemsByResourceClass != null)
            {
                itemsByResourceClass.add(item);
            }
            Set<PermissionAssociation> itemsByPermission = permissionAssociationByPermission
                .get(item.getPermission());
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
            catch(SQLException e)
            {
                throw new BackendException("Failed to delete " + item, e);
            }
            Set<PermissionAssociation> itemsByResourceClass = permissionAssociationByResourceClass
                .get(item.getResourceClass());
            if(itemsByResourceClass != null)
            {
                itemsByResourceClass.remove(item);
            }
            Set<PermissionAssociation> itemsByPermission = permissionAssociationByPermission
                .get(item.getPermission());
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
    public Set<PermissionAssignment> getPermissionAssignments(Resource owner)
    {
        synchronized(permissionLock)
        {
            HashSet<PermissionAssignment> items = (HashSet<PermissionAssignment>)permissionAssignmentByResource
                .get(owner);
            if(items == null)
            {
                try
                {
                    List<? extends PermissionAssignment> list = persistence.load(
                        permissionAssignmentFactory, "resource_id = ?", owner.getId());
                    items = new HashSet<>(list);
                    permissionAssignmentByResource.put(owner, items);
                }
                catch(SQLException e)
                {
                    throw new BackendException("Failed to load PermissionAssignments for " + owner,
                        e);
                }
            }
            return (Set<PermissionAssignment>)items.clone();
        }
    }

    /**
     * {@inheritDoc}
     */
    public Set<PermissionAssignment> getPermissionAssignments(Role owner)
    {
        synchronized(permissionLock)
        {
            HashSet<PermissionAssignment> items = (HashSet<PermissionAssignment>)permissionAssignmentByRole
                .get(owner);
            if(items == null)
            {
                try
                {
                    List<? extends PermissionAssignment> list = persistence.load(
                        permissionAssignmentFactory, "role_id = ?", owner.getId());
                    items = new HashSet<>(list);
                    permissionAssignmentByRole.put(owner, items);
                }
                catch(SQLException e)
                {
                    throw new BackendException("Failed to load PermissionAssignments for " + owner,
                        e);
                }
            }
            return (Set<PermissionAssignment>)items.clone();
        }
    }

    public Set<PermissionAssignment> getPermissionAssigments(Permission owner)
    {
        // this is rarely used - no caching
        List<PermissionAssignmentImpl> list;
        try
        {
            list = persistence
                .load(permissionAssignmentFactory, "permission_id = ?", owner.getId());
        }
        catch(SQLException e)
        {
            throw new BackendException("Failed to load PermissionAssignments for " + owner, e);
        }
        return new HashSet<PermissionAssignment>(list);
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
            catch(SQLException e)
            {
                throw new BackendException("Failed to save " + item.toString(), e);
            }
            Resource owner = item.getResource();
            Set<PermissionAssignment> items = permissionAssignmentByResource.get(owner);
            if(items != null)
            {
                items.add(item);
            }
            Role role = item.getRole();
            items = permissionAssignmentByRole.get(role);
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
            catch(SQLException e)
            {
                throw new BackendException("Failed to delete " + item, e);
            }
            Resource owner = item.getResource();
            Set<PermissionAssignment> items = permissionAssignmentByResource.get(owner);
            if(items != null)
            {
                items.remove(item);
            }
            Role role = item.getRole();
            items = permissionAssignmentByRole.get(role);
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
                Set<PermissionAssociation> itemsByResourceClass = permissionAssociationByResourceClass
                    .get(item.getResourceClass());
                if(itemsByResourceClass != null)
                {
                    itemsByResourceClass.add(item);
                }
                Set<PermissionAssociation> itemsByPermission = permissionAssociationByPermission
                    .get(item.getPermission());
                if(itemsByPermission != null)
                {
                    itemsByPermission.add(item);
                }
            }
            else
            {
                Set<PermissionAssociation> itemsByResourceClass = permissionAssociationByResourceClass
                    .get(item.getResourceClass());
                if(itemsByResourceClass != null)
                {
                    itemsByResourceClass.remove(item);
                }
                Set<PermissionAssociation> itemsByPermission = permissionAssociationByPermission
                    .get(item.getPermission());
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
            Set<PermissionAssignment> itemsByResource = permissionAssignmentByResource.get(item
                .getResource());
            Set<PermissionAssignment> itemsByRole = permissionAssignmentByRole.get(item.getRole());
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
                Set<RoleAssignment> itemsBySubject = roleAssignmentBySubject.get(item.getSubject());
                if(itemsBySubject != null)
                {
                    itemsBySubject.add(item);
                }
                Set<RoleAssignment> itemsByRole = roleAssignmentByRole.get(item.getRole());
                if(itemsByRole != null)
                {
                    itemsByRole.add(item);
                }
            }
            else
            {
                Set<RoleAssignment> itemsBySubject = roleAssignmentBySubject.get(item.getSubject());
                if(itemsBySubject != null)
                {
                    itemsBySubject.remove(item);
                }
                Set<RoleAssignment> itemsByRole = roleAssignmentByRole.get(item.getRole());
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
            Set<RoleImplication> items = roleImplicationByRole.get(superRole);
            if(added)
            {
                if(items == null)
                {
                    items = new HashSet<>();
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
            items = roleImplicationByRole.get(subRole);
            if(added)
            {
                if(items == null)
                {
                    items = new HashSet<>();
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
            ResourceClass<?> parent = item.getParent();
            Set<ResourceClassInheritance> items = resourceClassInheritanceByResourceClass
                .get(parent);
            if(added)
            {
                if(items == null)
                {
                    items = new HashSet<>();
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
            ResourceClass<?> child = item.getChild();
            items = resourceClassInheritanceByResourceClass.get(child);
            if(added)
            {
                if(items == null)
                {
                    items = new HashSet<>();
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
    public void attributesChanged(AttributeDefinition<?> item, boolean added)
    {
        synchronized(resourceClassLock)
        {
            ResourceClass<?> owner = item.getDeclaringClass();
            Set<AttributeDefinition<?>> items = attributeDefinitionByResourceClass.get(owner);
            if(added)
            {
                if(items == null)
                {
                    items = new HashSet<>();
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
            List<? extends RoleAssignment> list = persistence.load(roleAssignmentFactory,
                "grantor = ?", subject.getId());
            RoleAssignment[] result = new RoleAssignment[list.size()];
            list.toArray(result);
            return result;
        }
        catch(SQLException e)
        {
            throw new BackendException("Failed to load RoleAssignments for " + subject, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public PermissionAssignment[] getGrantedPermissionAssignments(Subject subject)
    {
        try
        {
            List<? extends PermissionAssignment> list = persistence.load(
                permissionAssignmentFactory, "grantor = ?", subject.getId());
            PermissionAssignment[] result = new PermissionAssignment[list.size()];
            list.toArray(result);
            return result;
        }
        catch(SQLException e)
        {
            throw new BackendException("Failed to load PermissionAssignments for " + subject, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Resource[] getCreatedResources(Subject subject)
    {
        try
        {
            List<? extends RoleAssignment> list = persistence.load(roleAssignmentFactory,
                "created_by = ?", subject.getId());
            Resource[] result = new Resource[list.size()];
            for(int i = 0; i < list.size(); i++)
            {
                result[i] = coral.getStore().getResource(((Resource)list.get(i)).getId());
            }
            return result;
        }
        catch(SQLException e)
        {
            throw new BackendException("Failed to load RoleAssignments for " + subject, e);
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
            // WHOOPS it appears that there is a long-standing bug here, but the code is not used
            List list = persistence.load(roleAssignmentFactory, "owned_by = ?", subject.getId());
            List<Resource> temp = new ArrayList<>();
            List<Resource> stack = new ArrayList<>();
            for(int i = 0; i < list.size(); i++)
            {
                stack.add(coral.getStore().getResource(((Resource)list.get(i)).getId()));
            }
            while(stack.size() > 0)
            {
                Resource r = stack.remove(stack.size() - 1);
                temp.add(r);
                Resource[] subs = coral.getStore().getResource(r);
                for(int i = 0; i < subs.length; i++)
                {
                    stack.add(subs[i]);
                }
            }
            Resource[] result = new Resource[temp.size()];
            temp.toArray(result);
            return result;
        }
        catch(SQLException e)
        {
            throw new BackendException("Failed to load RoleAssignments " + subject, e);
        }
        catch(EntityDoesNotExistException e)
        {
            throw new BackendException("internal error", e);
        }
    }

    // preloading ///////////////////////////////////////////////////////////////////////////////

    private static final int[] STARTUP_PHASES = { 1, 3 };

    /**
     * {@inheritDoc}
     */
    public int[] getPhases()
    {
        return STARTUP_PHASES;
    }

    /**
     * {@inheritDoc}
     */
    public void preloadData(int phase)
    {
        if(phase == 1)
        {
            preloadRegistryPhase1();
        }
        else if(phase == 3)
        {
            preloadRegistryPhase2();
        }
        else
        {
            throw new IllegalArgumentException("unexpected phase " + phase);
        }
    }

    private void preloadRegistryPhase1()
    {
        long time = System.currentTimeMillis();
        log.info("preloading entity registry phase 1");
        log.info("preloading attribute classes");
        attributeClassRegistry.get();
        log.info("preloading resource classes");
        resourceClassRegistry.get();
        log.info("preloading attribute definitions");
        attributeDefinitionRegistry.get();
        preloadAttributeDefinitions();
        log.info("preloading resource class inheritance");
        preloadResourceClassInheritance();
        log.info("preloading roles");
        roleRegistry.get();
        log.info("preloading subjects");
        subjectRegistry.get();
        log.info("preloading permissions");
        permissionRegistry.get();
        log.info("preloading role implications");
        preloadRoleImplications();
        log.info("preloading role assignments");
        preloadRoleAssignments();
        log.info("preloading permission associations");
        preloadPermissionAssociations();
        time = System.currentTimeMillis() - time;
        log.info("finished preloading registry phase 1 in " + time + "ms");
    }

    private void preloadRegistryPhase2()
    {
        long time = System.currentTimeMillis();
        log.info("preloading entity registry phase 2");
        log.info("preloading permission assignments");
        preloadPermissionAssignments();
        time = System.currentTimeMillis() - time;
        log.info("finished preloading registry phase 2 in " + time + "ms");
    }

    private void preloadResourceClassInheritance()
    {
        synchronized(resourceClassLock)
        {
            List<? extends ResourceClassInheritance> list;
            try
            {
                list = persistence.load(resourceClassInheritanceFactory);
            }
            catch(SQLException e)
            {
                throw new BackendException("Failed to load ResuourceClassInheritance", e);
            }
            Iterator<? extends ResourceClassInheritance> i = list.iterator();
            while(i.hasNext())
            {
                ResourceClassInheritance item = i.next();
                Set<ResourceClassInheritance> items = resourceClassInheritanceByResourceClass
                    .get(item.getParent());
                if(items == null)
                {
                    items = new HashSet<>();
                    resourceClassInheritanceByResourceClass.put(item.getParent(), items);
                }
                items.add(item);
                items = resourceClassInheritanceByResourceClass.get(item.getChild());
                if(items == null)
                {
                    items = new HashSet<>();
                    resourceClassInheritanceByResourceClass.put(item.getChild(), items);
                }
                items.add(item);
            }
            Iterator<? extends ResourceClass<?>> j = resourceClassRegistry.get().iterator();
            while(j.hasNext())
            {
                ResourceClass<?> rc = j.next();
                if(!resourceClassInheritanceByResourceClass.containsKey(rc))
                {
                    resourceClassInheritanceByResourceClass.put(rc,
                        new HashSet<ResourceClassInheritance>());
                }
            }
        }
    }

    private void preloadAttributeDefinitions()
    {
        synchronized(resourceClassLock)
        {
            Iterator<? extends AttributeDefinition<?>> i = attributeDefinitionRegistry.get()
                .iterator();
            while(i.hasNext())
            {
                AttributeDefinition<?> item = i.next();
                ResourceClass<?> owner = item.getDeclaringClass();
                Set<AttributeDefinition<?>> items = attributeDefinitionByResourceClass.get(owner);
                if(items == null)
                {
                    items = new HashSet<>();
                    attributeDefinitionByResourceClass.put(owner, items);
                }
                items.add(item);
            }
            Iterator<? extends ResourceClass<?>> j = resourceClassRegistry.get().iterator();
            while(j.hasNext())
            {
                ResourceClass<?> rc = j.next();
                if(!attributeDefinitionByResourceClass.containsKey(rc))
                {
                    attributeDefinitionByResourceClass.put(rc,
                        new HashSet<AttributeDefinition<?>>());
                }
            }
        }
    }

    private void preloadRoleImplications()
    {
        synchronized(roleLock)
        {
            List<? extends RoleImplication> list;
            try
            {
                list = persistence.load(roleImplicationFactory);
            }
            catch(SQLException e)
            {
                throw new BackendException("Failed to load RoleImplications", e);
            }
            Iterator<? extends RoleImplication> i = list.iterator();
            while(i.hasNext())
            {
                RoleImplication item = i.next();
                Set<RoleImplication> items = roleImplicationByRole.get(item.getSubRole());
                if(items == null)
                {
                    items = new HashSet<>();
                    roleImplicationByRole.put(item.getSubRole(), items);
                }
                items.add(item);
                items = roleImplicationByRole.get(item.getSuperRole());
                if(items == null)
                {
                    items = new HashSet<>();
                    roleImplicationByRole.put(item.getSuperRole(), items);
                }
                items.add(item);
            }
            Iterator<? extends Role> j = roleRegistry.get().iterator();
            while(j.hasNext())
            {
                Role r = (Role)j.next();
                if(!roleImplicationByRole.containsKey(r))
                {
                    roleImplicationByRole.put(r, new HashSet<RoleImplication>());
                }
            }
        }
    }

    private void preloadRoleAssignments()
    {
        synchronized(roleLock)
        {
            List<? extends RoleAssignment> list;
            try
            {
                list = persistence.load(roleAssignmentFactory);
            }
            catch(SQLException e)
            {
                throw new BackendException("Failed to load RoleAssignments", e);
            }
            Iterator<? extends RoleAssignment> i = list.iterator();
            while(i.hasNext())
            {
                RoleAssignment item = i.next();
                Set<RoleAssignment> items = roleAssignmentBySubject.get(item.getSubject());
                if(items == null)
                {
                    items = new HashSet<>();
                    roleAssignmentBySubject.put(item.getSubject(), items);
                }
                items.add(item);
                items = roleAssignmentByRole.get(item.getRole());
                if(items == null)
                {
                    items = new HashSet<>();
                    roleAssignmentByRole.put(item.getRole(), items);
                }
                items.add(item);
            }
            Iterator<? extends Subject> j = subjectRegistry.get().iterator();
            while(j.hasNext())
            {
                Subject s = j.next();
                if(!roleAssignmentBySubject.containsKey(s))
                {
                    roleAssignmentBySubject.put(s, new HashSet<RoleAssignment>());
                }
            }
            Iterator<? extends Role> k = roleRegistry.get().iterator();
            while(k.hasNext())
            {
                Role r = k.next();
                if(!roleAssignmentByRole.containsKey(r))
                {
                    roleAssignmentByRole.put(r, new HashSet<RoleAssignment>());
                }
            }
        }
    }

    private void preloadPermissionAssociations()
    {
        synchronized(permissionLock)
        {
            List<? extends PermissionAssociation> list;
            try
            {
                list = persistence.load(permissionAssociationFactory);
            }
            catch(SQLException e)
            {
                throw new BackendException("Failed to load PermissionAssociations", e);
            }
            Iterator<? extends PermissionAssociation> i = list.iterator();
            while(i.hasNext())
            {
                PermissionAssociation item = i.next();
                Set<PermissionAssociation> items = permissionAssociationByResourceClass.get(item
                    .getResourceClass());
                if(items == null)
                {
                    items = new HashSet<>();
                    permissionAssociationByResourceClass.put(item.getResourceClass(), items);
                }
                items.add(item);
                items = permissionAssociationByPermission.get(item.getPermission());
                if(items == null)
                {
                    items = new HashSet<>();
                    permissionAssociationByPermission.put(item.getPermission(), items);
                }
                items.add(item);
            }
            Iterator<? extends ResourceClass<?>> j = resourceClassRegistry.get().iterator();
            while(j.hasNext())
            {
                ResourceClass<?> rc = j.next();
                if(!permissionAssociationByResourceClass.containsKey(rc))
                {
                    permissionAssociationByResourceClass.put(rc,
                        new HashSet<PermissionAssociation>());
                }
            }
            Iterator<? extends Permission> k = permissionRegistry.get().iterator();
            while(k.hasNext())
            {
                Permission p = k.next();
                if(!permissionAssociationByPermission.containsKey(p))
                {
                    permissionAssociationByPermission.put(p, new HashSet<PermissionAssociation>());
                }
            }
        }
    }

    private void preloadPermissionAssignments()
    {
        synchronized(permissionLock)
        {
            List<? extends PermissionAssignment> list;
            try
            {
                list = persistence.load(permissionAssignmentFactory);
            }
            catch(SQLException e)
            {
                throw new BackendException("Failed to load PermissionAssignments", e);
            }
            Iterator<? extends PermissionAssignment> i = list.iterator();
            while(i.hasNext())
            {
                PermissionAssignment item = i.next();
                Set<PermissionAssignment> items = permissionAssignmentByResource.get(item
                    .getResource());
                if(items == null)
                {
                    items = new HashSet<>();
                    permissionAssignmentByResource.put(item.getResource(), items);
                }
                items.add(item);
                items = permissionAssignmentByRole.get(item.getRole());
                if(items == null)
                {
                    items = new HashSet<>();
                    permissionAssignmentByRole.put(item.getRole(), items);
                }
                items.add(item);
            }
            Iterator<? extends Resource> j = Arrays.asList(coral.getStore().getResource())
                .iterator();
            while(j.hasNext())
            {
                Resource r = j.next();
                if(!permissionAssignmentByResource.containsKey(r))
                {
                    permissionAssignmentByResource.put(r, new HashSet<PermissionAssignment>());
                }
            }
            Iterator<? extends Role> k = roleRegistry.get().iterator();
            while(k.hasNext())
            {
                Role r = k.next();
                if(!permissionAssignmentByRole.containsKey(r))
                {
                    permissionAssignmentByRole.put(r, new HashSet<PermissionAssignment>());
                }
            }
        }
    }

}
