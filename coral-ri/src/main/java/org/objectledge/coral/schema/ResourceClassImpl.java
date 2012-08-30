package org.objectledge.coral.schema;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.objectledge.collections.ImmutableHashMap;
import org.objectledge.collections.ImmutableHashSet;
import org.objectledge.collections.ImmutableMap;
import org.objectledge.collections.ImmutableSet;
import org.objectledge.coral.BackendException;
import org.objectledge.coral.CoralCore;
import org.objectledge.coral.InstantiationException;
import org.objectledge.coral.Instantiator;
import org.objectledge.coral.entity.AbstractEntity;
import org.objectledge.coral.event.AttributeDefinitionChangeListener;
import org.objectledge.coral.event.CoralEventHub;
import org.objectledge.coral.event.PermissionAssociationChangeListener;
import org.objectledge.coral.event.ResourceClassAttributesChangeListener;
import org.objectledge.coral.event.ResourceClassChangeListener;
import org.objectledge.coral.event.ResourceClassInheritanceChangeListener;
import org.objectledge.coral.security.Permission;
import org.objectledge.coral.security.PermissionAssociation;
import org.objectledge.coral.store.Resource;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.OutputRecord;
import org.objectledge.database.persistence.Persistence;

/**
 * Represents a resource class.
 *
 * @version $Id: ResourceClassImpl.java,v 1.27 2007-05-31 20:24:54 rafal Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class ResourceClassImpl<T extends Resource>
    extends AbstractEntity
    implements ResourceClass<T>,
               ResourceClassInheritanceChangeListener,
               ResourceClassAttributesChangeListener,
               AttributeDefinitionChangeListener,
               PermissionAssociationChangeListener,
               ResourceClassChangeListener

{
    // Instance variables ///////////////////////////////////////////////////////////////////////

    /** The CoralEventHub. */
    private CoralEventHub coralEventHub;

    /** Instantiator. */
    private Instantiator instantiator;
    
    /** The component hub. */
    private CoralCore coral;
    
    /** The name of the Java class associated with this resource class */
    private String javaClassName;

    /** The Java class associated with this resource class */
    private Class javaClass;
    
    /** The name of the handler class. */
    private String handlerClassName;
    
    /** The {@link org.objectledge.coral.schema.ResourceHandler} implementation that is responsible
     * for this resource class. */
    private ResourceHandler<T> handler;

    /** class flags. */
    private int flags;

    /** db table. */
    private String dbTable;

    /** Inheritance records. Describes direct relationships. */
    private ImmutableSet<ResourceClassInheritance> inheritance;

    /** The parent classes. Contains direct and indirect parents. */
    private ImmutableSet<ResourceClass<?>> parentClasses;
    
    /** The direct parent classes. */
    private ImmutableSet<ResourceClass<?>> directParentClasses;

    /** The child classes. Contains direct and indirect children. */
    private ImmutableSet<ResourceClass<?>> childClasses;
    
    /** The direct child classes. */
    private ImmutableSet<ResourceClass<?>> directChildClasses;
    
    /** The declared attributes. */
    private ImmutableSet<AttributeDefinition<?>> declaredAttributes;

    /** The associated permissions. */
    private ImmutableSet<Permission> permissions;

    /** The permission associations. */
    private ImmutableSet<PermissionAssociation> permissionAssociations;
    
    /** The attributes keyed by name. Contains declared and inherited attributes. */
    private ImmutableMap<String, AttributeDefinition<?>> attributeMap;

    /** Attribute index table. */
    private final AtomicReference<AttributeIndexTable> indexTable = new AtomicReference<AttributeIndexTable>();

    // Initialization ///////////////////////////////////////////////////////////////////////////

    /**
     * Constructs a {@link ResourceClassImpl}.
     * 
     * @param persistence the Peristence subsystem.
     * @param instantiator the Instantiator.
     * @param coralEventHub the event hub.
     * @param coral the component hub.
     */
    public ResourceClassImpl(Persistence persistence, Instantiator instantiator, 
        CoralEventHub coralEventHub, CoralCore coral)
    {
        super(persistence);
        this.instantiator = instantiator;
        this.coral = coral;
        this.coralEventHub = coralEventHub;
    }
    
    /**
     * Constructs a {@link ResourceClassImpl}.
     *
     * @param persistence the Peristence subsystem.
     * @param instantiator the Instantiator.
     * @param coralEventHub the event hub.
     * @param coral the component hub.
     * 
     * @param name the name of the resource class.
     * @param javaClass the name of the Java class associated with this
     *        resource class.
     * @param handlerClass the name of the ResourceHandler implementation
     *        responsible for this resource class.
     * @param dbTable the database table name.
     * @param flags resource class flags.
     * @throws JavaClassException if javaClass or handlerClass could not be loaded or instantiated.
     */
    public ResourceClassImpl(Persistence persistence, Instantiator instantiator, 
        CoralEventHub coralEventHub, CoralCore coral,
        String name, String javaClass, String handlerClass, String dbTable, int flags)
        throws JavaClassException
    {
        super(persistence, name);
        this.instantiator = instantiator;
        this.coral= coral;
        this.coralEventHub = coralEventHub;
        setDbTable(dbTable);
        setHandlerClass(handlerClass);
        setJavaClass(javaClass, true);
        setFlags(flags);
        coralEventHub.getInbound().addResourceClassChangeListener(this, this);
    }
    
    // Persistent interface /////////////////////////////////////////////////////////////////////

    /** The key columns */
    private static final String[] KEY_COLUMNS = { "resource_class_id" };
    
    /**
     * Returns the name of the table this type is mapped to.
     *
     * @return the name of the table.
     */
    public String getTable()
    {
        return "coral_resource_class";
    }
    
    
    /** 
     * Returns the names of the key columns.
     *
     * @return the names of the key columns.
     */
    public String[] getKeyColumns()
    {
        return KEY_COLUMNS;
    }

    /**
     * Stores the fields of the object into the specified record.
     * <p>
     * You need to call <code>getData</code> of your superclasses if they are
     * <code>Persistent</code>.
     * </p>
     * 
     * @param record the record to store state into.
     * @throws SQLException if there is a problem storing field values.
     */
    public void getData(OutputRecord record)
        throws SQLException
    {
        super.getData(record);
        record.setString("java_class_name", javaClassName);
        record.setString("handler_class_name", handlerClassName);
        if(dbTable != null)
        {
            record.setString("db_table_name", dbTable);
        }
        else
        {
            record.setNull("db_table_name");
        }
        record.setInteger("flags", flags);
    }

    /**
     * Loads the fields of the object from the specified record.
     * <p>
     * You need to call <code>setData</code> of your superclasses if they are
     * <code>Persistent</code>.
     * </p>
     * 
     * @param record the record to read state from.
     * @throws SQLException if there is a problem loading field values.
     */
    public void setData(InputRecord record)
        throws SQLException
    {
        super.setData(record);
        try
        {
            setHandlerClass(record.getString("handler_class_name"));
            setJavaClass(record.getString("java_class_name"), true);
            if(!record.isNull("db_table_name"))
            {
                setDbTable(record.getString("db_table_name"));
            }
            setFlags(record.getInteger("flags"));
        }
        catch(JavaClassException e)
        {
            throw new SQLException("Failed to load ResourceClass #" + id, e);
        }
        coralEventHub.getInbound().addResourceClassChangeListener(this, this);
    }

    // ResourceClassChangeListener interface ////////////////////////////////////////////////////

    /**
     * Called when <code>ResourceClass</code>'s data change.
     *
     * @param resourceClass the resourceClass that changed.
     */
    public void resourceClassChanged(ResourceClass resourceClass)
    {
        if(this.equals(resourceClass))
        {
            try
            {
                persistence.revert(this);
            }
            catch(SQLException e)
            {
                throw new BackendException("failed to revert entity state", e);
            }
        }
    }

    // ResourceClass interface //////////////////////////////////////////////////////////////////
    
    /**
     * Returns the name of the Java class that is associated with this
     * resource class. 
     *
     * @return the Java class that is associated with this resource class.
     * type.
     */
    public String getJavaClassName()
    {
        return javaClassName;
    }

    /**
     * Returns the Java class that is associated with this resource class.
     * type.
     *
     * @return the Java class that is associated with this resource class.
     * type.
     */
    public Class getJavaClass()
    {
        if(javaClass != null)
        {
            return javaClass;
        }
        else
        {
            throw new BackendException("implementation class "+javaClassName+
                                       " is missing or has linkage problems");
        }
    }

    /**
     * Returns the ResourceHandler implementaion that will manage the
     * resources of that class.
     *
     * @return an <code>ResourceHandler</code> implementation.
     */
    public ResourceHandler getHandler()
    {
        return handler;
    }

    /**
     * Return the name of a database table that holds the data of the
     * resources of that type.
     *
     * @return the name of a database table that holds the data of the
     * resource of that type, or <code>null</code> if not used.
     */
    public String getDbTable()
    {
        return dbTable;
    }

    /**
     * Returns the resource class flags.
     *
     * <p>Use {@link org.objectledge.coral.schema.ResourceClassFlags} to decode flag values.
     *
     * @return resource class flags.
     */
    public int getFlags()
    {
        return flags;
    }

    /**
     * Returns attributes declared by this resource class.
     *
     * @return attributes declared by this resource class.
     */
    public AttributeDefinition[] getDeclaredAttributes()
    {
        ImmutableSet<AttributeDefinition<?>> snapshot = buildDeclaredAttributeSet();
        AttributeDefinition[] result = new AttributeDefinition[snapshot.size()];
        snapshot.toArray(result);
        return result;
    }

    /**
     * Returns all attributes declared by this resource class and it's parent
     * classes.  
     *
     * @return all attributes delcared by this resource class and it's parent
     * classes.  
     */
    public AttributeDefinition[] getAllAttributes()
    {
        ImmutableMap<String, AttributeDefinition<?>> snapshot = buildAttributeMap();
        AttributeDefinition[] result = new AttributeDefinition[snapshot.size()];
        snapshot.values().toArray(result);
        return result;
    }

    /**
     * Returns an attribute with a specified name.
     *
     * <p>Note that the attribute may belong to a parent class of this
     * resource class.</p>
     *
     * @param name the name of the attribute.
     * @return the attribute definition object.
     * @throws UnknownAttributeException if the resource class does not have
     *         an attribute of the specififed class.
     */
    public AttributeDefinition getAttribute(String name)
        throws UnknownAttributeException
    {
        ImmutableMap<String, AttributeDefinition<?>> snapshot = buildAttributeMap();
        AttributeDefinition<?> attr = snapshot.get(name);
        if(attr == null)
        {
            throw new UnknownAttributeException("resource class "+getName()+
                                                " does not have "+name+" attribute");
        }
        return attr;
    }

    /**
     * Checks it the class has an attribute with the specified name.
     *
     * <p>Note that the attribute may belong to a parent class of this
     * resource class.</p>
     *
     * @param name the name of the attribute.
     * @return <code>true</code> if the class has an attribute with the
     *         specified name.
     */
    public boolean hasAttribute(String name)
    {
        ImmutableMap<String, AttributeDefinition<?>> snapshot = buildAttributeMap();
        return snapshot.containsKey(name);
    }
    
    /**
     * Returns the index of the attribute definition within a resource class.
     * <p>
     * Index of the attribute definition is an integer greater or equal to 0. It is defined for all
     * declared and inherited attributes of the class. It is guaranteed to remain constant through
     * the entire runtime of a Coral instance however it may change across different Coral
     * instantiations. An attempt will be made to keep indexes as small as possible.
     * </p>
     * <p>
     * This mechanism is provided to enable datatypes implementations to use Java arrays as a
     * space-effective means of storing resource attribute values.
     * </p>
     * 
     * @param attr the attribute.
     * @return the index of the attribute definition within a resource class.
     * @throws UnknownAttributeException if the attribute was not declared by the class or one of
     *         it's parent classes.
     */
    public int getAttributeIndex(AttributeDefinition<?> attr)
        throws UnknownAttributeException
    {
        return getAttributeIndexTable().getIndex(attr);        
    }
    
    /**
     * Returns the maximum attribute index used at this moment by the resource class.
     * 
     * @return the maximum attribute index used at this moment by the resource class.
     */
    public int getMaxAttributeIndex() 
    {
        return getAttributeIndexTable().getMaxIndex();
    }

    /**
     * Returns information about inheritance relationships this class is
     * involved in. 
     *
     * <p>You need to use this method to acquire information about direct
     * relationships, as {@link #getParentClasses()} and {@link
     * #getChildClasses()} return both both directly and indireclty related
     * classes.</p>
     *
     * @return the inheritance relationships this class is involved in.
     */
    public ResourceClassInheritance[] getInheritance()
    { 
        ImmutableSet<ResourceClassInheritance> snapshot = buildInheritance();
        ResourceClassInheritance[] result = new ResourceClassInheritance[snapshot.size()];
        snapshot.toArray(result);
        return result;
    }

    /**
     * Returns the parent classes of this resource class.
     *
     * @return the parent classes of this resource class.
     */
    public ResourceClass[] getParentClasses()
    {
        ImmutableSet<ResourceClass<?>> snapshot = buildParentClassSet();
        ResourceClass[] result = new ResourceClass[snapshot.size()];
        snapshot.toArray(result);
        return result;
    }    
    
    /**
     * Returns the direct parent classes of this resource class.
     *
     * @return the direct parent classes of this resource class.
     */    
    public Set<ResourceClass<?>> getDirectParentClasses()
    {
        return buildDirectParentClassSet().unmodifiableSet();
    }

    /**
     * Checks if the specifid class is a child class of this class.
     *
     * @param resourceClass the class to check.
     * @return <code>true</code> if the specifid class is a child class of
     * this class. 
     */
    public boolean isParent(ResourceClass resourceClass)
    {
        ImmutableSet<ResourceClass<?>> snapshot = buildChildClassSet();
        return snapshot.contains(resourceClass);
    }

    /**
     * Returns the child classes of this resource class.
     *
     * <p>Both direct and indirect child classes will be reported.</p>
     *
     * @return the child classes of this resource class.
     */
    public ResourceClass[] getChildClasses()
    {
        ImmutableSet<ResourceClass<?>> snapshot = buildChildClassSet();
        ResourceClass[] result = new ResourceClass[snapshot.size()];
        snapshot.toArray(result);
        return result;
    }

    /**
     * Returns the direct child classes of this resource class.
     *
     * @return the direct child classes of this resource class.
     */    
    public Set<ResourceClass<?>> getDirectChildClasses()
    {
        return buildDirectChildClassSet().unmodifiableSet();
    }    
    
    /**
     * Returns the permissions associated with this resource class.
     *
     * @return the permissions associated with this resource class.
     */
    public Permission[] getPermissions()
    {
        ImmutableSet<Permission> snapshot = buildPermissionSet();
        Permission[] result = new Permission[snapshot.size()];
        snapshot.toArray(result);
        return result;
    }

    /**
     * Returns the permission associations made for this class.
     *
     * @return the permission associations made for this class.
     */
    public PermissionAssociation[] getPermissionAssociations()
    {
        ImmutableSet<PermissionAssociation> snapshot = buildPermissionAssociationSet();
        PermissionAssociation[] result = new PermissionAssociation[snapshot.size()];
        snapshot.toArray(result);
        return result;
    }

    /**
     * Returns <code>true</code> if the specified permission is associated
     * with this resource class.
     *
     * @param permission the permission.
     * @return <code>true</code> if the specified permission is associated
     * with this resource class.
     */
    public boolean isAssociatedWith(Permission permission)
    {
        ImmutableSet<Permission> snapshot = buildPermissionSet();
        return snapshot.contains(permission);
    }

    // ResourceClassInheritanceChangeListener interface /////////////////////////////////////////

    /**
     * Called when resource class inheritance relationships change.
     *
     * @param item the {@link org.objectledge.coral.schema.ResourceClassInheritance}.
     * @param added <code>true</code> if the relationship was added,
     *        <code>false</code> if removed.
     */
    public synchronized void inheritanceChanged(ResourceClassInheritance item, 
                                                boolean added)
    {
        // direct relationship added / removed
        if(item.getChild().equals(this) || item.getParent().equals(this))
        {
            if(added)
            {
                inheritance = inheritance.add(item);
                if(item.getChild().equals(this))
                {
                    coralEventHub.getGlobal().
                        addResourceClassInheritanceChangeListener(this, item.getParent());
                    coralEventHub.getGlobal().
                        addResourceClassAttributesChangeListener(this, item.getParent());
                    coralEventHub.getGlobal().
                        addPermissionAssociationChangeListener(this, item.getParent());
                }
                else
                {
                    coralEventHub.getGlobal().
                        addResourceClassInheritanceChangeListener(this, item.getChild());
                }
            }
            else
            {
                inheritance = inheritance.remove(item);
                if(item.getChild().equals(this))
                {
                    coralEventHub.getGlobal().
                        removeResourceClassInheritanceChangeListener(this, item.getParent());
                    coralEventHub.getGlobal().
                        removeResourceClassAttributesChangeListener(this, item.getParent());
                    coralEventHub.getGlobal().
                        removePermissionAssociationChangeListener(this, item.getParent());
                }
                else
                {
                    coralEventHub.getGlobal().
                        removeResourceClassInheritanceChangeListener(this, item.getChild());
                }
            }
        }
        if(item.getChild().equals(this) || item.getChild().isParent(this) && added)
        {
            List<AttributeDefinition<?>> l = new ArrayList<AttributeDefinition<?>>();
            // arrays and generics don't mix :/
            for(AttributeDefinition<?> ad : item.getParent().getAllAttributes())
            {
                l.add(ad);
            }
            expandAttributeIndexTable(l);
        }
        // flush cached information
        childClasses = null;
        directChildClasses = null;
        parentClasses = null;
        directParentClasses = null;
        attributeMap = null;
        permissions = null;
    }

    // ResourceClassAttributesChangeListener interface //////////////////////////////////////////

    /**
     * Called when resource class attribute declarations change.
     *
     * @param attribute the {@link org.objectledge.coral.schema.AttributeDefinition}.
     * @param added <code>true</code> if the attribute was added,
     *        <code>false</code> if removed.
     */
    public synchronized void attributesChanged(AttributeDefinition attribute, 
                                               boolean added)
    {
        ResourceClass<?> rc = attribute.getDeclaringClass();
        if(rc.equals(this) || parentClasses.contains(rc))
        {
            if(rc.equals(this))
            {
                if(added)
                {
                    declaredAttributes = declaredAttributes.add(attribute);
                }
                else
                {
                    declaredAttributes = declaredAttributes.remove(attribute);
                }
            }
            if(added)
            {
                attributeMap = attributeMap.put(attribute.getName(), attribute);
                expandAttributeIndexTable(attribute);
            }
            else
            {
                attributeMap = attributeMap.remove(attribute.getName());
            }
        }
    }
    
    // AttributeDefinitionChangeListener ////////////////////////////////////////////////////////
    
    /**
     * Called when <code>AttributeDefinition</code>'s data change.
     *
     * @param attributeDefinition the attribute that changed.
     */
    public synchronized void attributeDefinitionChanged(AttributeDefinition attributeDefinition)
    {
        if(attributeMap.containsValue(attributeDefinition))
        {
             if(!attributeMap.containsKey(attributeDefinition.getName()))
             {
                 attributeMap.values().remove(attributeDefinition);
                 attributeMap.put(attributeDefinition.getName(), attributeDefinition);
             }
        }
    }    

    // PermissionAssignmentChangeListener interface /////////////////////////////////////////////

    /**
     * Called when permission associations on a resource class / perimission change.
     *
     * @param association the permission association.
     * @param added <code>true</code> if the permission was added,
     *        <code>false</code> if removed.
     */
    public synchronized void permissionsChanged(PermissionAssociation association, 
                                                boolean added) 
    {
        if(permissions != null)
        {
            if(added)
            {
                permissions.add(association.getPermission());
            }   
            else
            {
                permissions.remove(association.getPermission());
            }
        }
        if(association.getResourceClass().equals(this) && permissionAssociations != null)
        {
            if(added)
            {
                permissionAssociations.add(association);
            }
            else
            {
                permissionAssociations.remove(association);
            }
        }
    }
    
    // Package private setter methods ///////////////////////////////////////////////////////////

    /**
     * Sets the name of the class.
     * 
	 * <p>Needed because AbstractEntityt.setName(String) is not visible in this package.</p>
     *
     * @param name the new name of the class.
     */
    void setClassName(String name)
    {
        this.name = name;
    }

    /**
     * Sets the Java class that is associated with this resource class.
     *
     * @param className the Java class that is associated with this resource
     * class.
     * @param fallbackAllowed when class is not available and fallback is allowed, handler provided
     * introspection-only implementation will be used, otherwise exception will be thrown.
     */
    void setJavaClass(String className, boolean fallbackAllowed)
        throws JavaClassException
    {
        javaClassName = className;
        try
        {
            javaClass = instantiator.loadClass(className);
        }
        catch(ClassNotFoundException e)
        {
            if(fallbackAllowed && this.handler != null)
            {
                javaClass = handler.getFallbackResourceImplClass();
                coral.getLog().warn(
                    className + "is not available. Falling back to handler default: "
                        + javaClass.getName(), e);
            }
            else
            {
                throw new JavaClassException(className + "is not available", e);                
            }
        }
    }

    /**
     * Sets the ResourceHandler implementaion that will manage the resources
     * of that class. 
     *
     * @param className the name of an <code>ResourceHandler</code>
     * implementation. 
     */
    void setHandlerClass(String className)
        throws JavaClassException
    {
        this.handlerClassName = className;
        try
        {
            Class<?> handlerClass = instantiator.loadClass(className);
            Map<Class<?>, Object> additional = new HashMap<Class<?>, Object>();
            additional.put(ResourceClass.class, this);
            handler = (ResourceHandler<T>)instantiator.newInstance(handlerClass, additional);
        }
        catch(ClassNotFoundException e)
        {
            throw new JavaClassException(e.getMessage(), e);
        }
        catch(InstantiationException e)
        {
            throw new JavaClassException(e.getMessage(), e.getCause());
        }
        catch(ClassCastException e)
        {
            throw new JavaClassException(className+" does not implement "+
                                         "ResourceHandler interface", e);
        }
    }

    /**
     * Sets the name of a database table that holds the data of the
     * resources of that type.
     *
     * @param dbTable the name of a database table that holds the data of the
     * resource of that type, or <code>null</code> if not used.
     */
    void setDbTable(String dbTable)
    {
        this.dbTable = dbTable;
    }

    /**
     * Sets the resource class flags.
     *
     * <p>Use {@link org.objectledge.coral.schema.ResourceClassFlags} to decode flag values.
     *
     * @param flags the resource class flags.
     */
    void setFlags(int flags)
    {
        this.flags = flags;
    }

    // Private //////////////////////////////////////////////////////////////////////////////////

    /**
     * Initializes {@link #inheritance} set if neccessary.
     */
    private synchronized ImmutableSet<ResourceClassInheritance> buildInheritance()
    {
        if(inheritance == null)
        {
            inheritance = new ImmutableHashSet<ResourceClassInheritance>(coral.getRegistry().getResourceClassInheritance(this));
            coralEventHub.getGlobal().addResourceClassInheritanceChangeListener(this, this);
        }
        return inheritance;
    }
    
    /**
     * Initializes {@link #parentClasses} set if neccessary.
     * @return 
     */
    private synchronized ImmutableSet<ResourceClass<?>> buildParentClassSet()
    {
        if(parentClasses == null)
        {
            Set<ResourceClass<?>> pc = new HashSet<ResourceClass<?>>();
            ArrayList<ResourceClass<?>> stack = new ArrayList<ResourceClass<?>>();
            stack.add(this);
            
            while(stack.size() > 0)
            {
                ResourceClass<?> rc = stack.remove(stack.size()-1);
                ImmutableSet<ResourceClassInheritance> rcis;
                if(rc.equals(this))
                {
                    rcis = buildInheritance();
                }
                else
                {
                    pc.add(rc);
                    rcis = new ImmutableHashSet<ResourceClassInheritance>(coral.getRegistry().getResourceClassInheritance(rc));
                    coralEventHub.getGlobal().addResourceClassInheritanceChangeListener(this, rc);
                    coralEventHub.getGlobal().addResourceClassAttributesChangeListener(this, rc);
                    coralEventHub.getGlobal().addPermissionAssociationChangeListener(this, rc);
                }
                for(ResourceClassInheritance ir : rcis)
                {
                    if(ir.getChild().equals(rc))
                    {
                        stack.add(ir.getParent());
                    }
                }

            }
            parentClasses = new ImmutableHashSet<ResourceClass<?>>(pc);
        }
        return parentClasses;
    }
    
    /**
     * Initializes {@link #directParentClasses} set if necessary.
     * @return 
     */
    private synchronized ImmutableSet<ResourceClass<?>> buildDirectParentClassSet()
    {
        if(directParentClasses == null)
        {
            buildInheritance();
            Set<ResourceClass<?>> dpc = new HashSet<ResourceClass<?>>();
            for(ResourceClassInheritance ir : inheritance)
            {
                if(ir.getChild().equals(this))
                {
                    dpc.add(ir.getParent());
                }                
            }
            directParentClasses = new ImmutableHashSet<ResourceClass<?>>(dpc);
        }
        return directParentClasses;
    }
    
    /**
     * Initializes {@link #childClasses} set if necessary.
     * @return 
     */
    private synchronized ImmutableSet<ResourceClass<?>> buildChildClassSet()
    {
        if(childClasses == null)
        {
            Set<ResourceClass<?>> cc = new HashSet<ResourceClass<?>>();
            ArrayList<ResourceClass<?>> stack = new ArrayList<ResourceClass<?>>();
            stack.add(this);
            
            while(stack.size() > 0)
            {
                ResourceClass<?> rc = stack.remove(stack.size()-1);
                ImmutableSet<ResourceClassInheritance> rcis;
                if(rc.equals(this))
                {
                    rcis = buildInheritance();
                }
                else
                {
                    cc.add(rc);
                    rcis = new ImmutableHashSet<ResourceClassInheritance>(coral.getRegistry().getResourceClassInheritance(rc));
                    coralEventHub.getGlobal().addResourceClassInheritanceChangeListener(this, rc);
                }
                for(ResourceClassInheritance ir : rcis)
                {
                    if(ir.getParent().equals(rc))
                    {
                        stack.add(ir.getChild());
                    }
                }
            }
            childClasses = new ImmutableHashSet<ResourceClass<?>>(cc);
        }
        return childClasses;
    }
    
    /**
     * Initializes {@link #directChildClasses} set if necessary.
     * @return 
     */
    private synchronized ImmutableSet<ResourceClass<?>> buildDirectChildClassSet()
    {
        if(directChildClasses == null)
        {
            buildInheritance();
            Set<ResourceClass<?>> dcc = new HashSet<ResourceClass<?>>();
            for(ResourceClassInheritance ir : inheritance)
            {
                if(ir.getParent().equals(this))
                {
                    dcc.add(ir.getChild());
                }                
            }
            directChildClasses = new ImmutableHashSet<ResourceClass<?>>(dcc);
        }
        return directChildClasses;
    }

    /**
     * Initializes {@link #declaredAttributes} if necessary.
     * @return 
     */
    private synchronized ImmutableSet<AttributeDefinition<?>> buildDeclaredAttributeSet()
    {
        if(declaredAttributes == null)
        {
            declaredAttributes = new ImmutableHashSet<AttributeDefinition<?>>(coral.getRegistry().getDeclaredAttributes(this));
            coralEventHub.getGlobal().addResourceClassAttributesChangeListener(this, this);
        }
        return declaredAttributes;
    }
    
    /**
     * Initializes {@link #permissions} if necessary.
     * @return 
     */
    private synchronized ImmutableSet<Permission> buildPermissionSet()
    {    
        if(permissions == null)
        {
            Set<Permission> temp = new HashSet<Permission>();
            for(PermissionAssociation pa : buildPermissionAssociationSet())
            {
                temp.add(pa.getPermission());
            }
            for(ResourceClass<?> rc : buildParentClassSet())
            {
                ImmutableSet<PermissionAssociation> rcpa = ((ResourceClassImpl<?>)rc).buildPermissionAssociationSet();
                for(PermissionAssociation pa : rcpa)
                {
                    temp.add(pa.getPermission());
                }
            }
            permissions = new ImmutableHashSet<Permission>(temp);
        } 
        return permissions;
    }

    /**
     * Initializes {@link #permissionAssociations} if necessary.
     * @return 
     */
    private synchronized ImmutableSet<PermissionAssociation> buildPermissionAssociationSet()
    {
        if(permissionAssociations == null)
        {
            permissionAssociations = new ImmutableHashSet<PermissionAssociation>(coral.getRegistry().getPermissionAssociations(this));
            coralEventHub.getGlobal().addPermissionAssociationChangeListener(this, this);
        }
        return permissionAssociations;
    }

    /**
     * Initializes {@link #attributeMap} if necessary.
     * @return 
     */
    private synchronized ImmutableMap<String, AttributeDefinition<?>> buildAttributeMap()
    {
        if(attributeMap == null)
        {
            Map<String, AttributeDefinition<?>> tempAttributeMap = new HashMap<String, AttributeDefinition<?>>();
            for(AttributeDefinition<?> attr : buildDeclaredAttributeSet())
            {
                coralEventHub.getGlobal().addAttributeDefinitionChangeListener(this, attr);
                tempAttributeMap.put(attr.getName(), attr);
            }
            
            for(ResourceClass<?> rc : buildParentClassSet())
            {
                synchronized(rc)
                {
                    ImmutableMap<String, AttributeDefinition<?>> rcam = ((ResourceClassImpl<?>)rc).buildAttributeMap();
                    coralEventHub.getGlobal().addResourceClassAttributesChangeListener(this, rc);
                    coralEventHub.getGlobal().addResourceClassInheritanceChangeListener(this, rc);
                    for(AttributeDefinition<?> attr : rcam.values())
                    {
                        coralEventHub.getGlobal().addAttributeDefinitionChangeListener(this, attr);
                        tempAttributeMap.put(attr.getName(), attr);
                    }
                }
            }
            attributeMap = new ImmutableHashMap<String, AttributeDefinition<?>>(tempAttributeMap);
        }
        return attributeMap;
    }
    
    private AttributeIndexTable getAttributeIndexTable()
    {
        AttributeIndexTable t = indexTable.get();
        if(t == null)
        {
            // note: multiple threads may enter this block concurrently
            t = new AttributeIndexTable(buildAttributeMap().values());
            if(!indexTable.compareAndSet(null, t))
            {
                // we're late to the party, pick up table initialized by other thread
                t = indexTable.get();
            }
        }
        return t;
    }
    
    private void expandAttributeIndexTable(AttributeDefinition<?> attr)
    {
        AttributeIndexTable o;
        AttributeIndexTable n;
        // serialize table modifications by retrying the update until it completes uninterrupted
        do
        {
            o = indexTable.get();
            if(o == null)
            {
                return;
            }
            // immutable pattern - create new table by extending previous one with the attribute
            n = new AttributeIndexTable(o, attr);
        }
        while(!indexTable.compareAndSet(o, n));
    }
    
    private void expandAttributeIndexTable(List<AttributeDefinition<?>> attrs) 
    {
        AttributeIndexTable o;
        AttributeIndexTable n;
        do
        {
            o = indexTable.get();
            if(o == null)
            {
                return;
            }
            n = new AttributeIndexTable(o, attrs);
        }
        while(!indexTable.compareAndSet(o, n));
    }
}
