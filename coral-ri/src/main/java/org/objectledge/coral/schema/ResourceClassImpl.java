package org.objectledge.coral.schema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.OutputRecord;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.PersistenceException;

/**
 * Represents a resource class.
 *
 * @version $Id: ResourceClassImpl.java,v 1.20 2005-01-19 08:00:53 rafal Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class ResourceClassImpl
    extends AbstractEntity
    implements ResourceClass,
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
    private ResourceHandler handler;

    /** class flags. */
    private int flags;

    /** db table. */
    private String dbTable;

    /** Inheritance records. Describes direct relationships. */
    private Set inheritance;

    /** The parent classes. Contains direct and indirect parents. */
    private Set parentClasses;

    /** The child classes. Contains direct and indirect children. */
    private Set childClasses;
    
    /** The declared attributes. */
    private Set declaredAttributes;

    /** The associated permissions. */
    private Set permissions;

    /** The permission associations. */
    private Set permissionAssociations;
    
    /** The attributes keyed by name. Contains declared and inherited attributes. */
    private Map attributeMap;

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
        setJavaClass(javaClass);
        setDbTable(dbTable);
        setHandlerClass(handlerClass);
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
     *
     * <p>You need to call <code>getData</code> of your superclasses if they
     * are <code>Persistent</code>.</p>
     *
     * @param record the record to store state into.
     * @throws PersistenceException if there is a problem storing field values.
     */
    public void getData(OutputRecord record)
        throws PersistenceException
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
     *
     * <p>You need to call <code>setData</code> of your superclasses if they
     * are <code>Persistent</code>.</p>
     * 
     * @param record the record to read state from.
     * @throws PersistenceException if there is a problem loading field values.
     */
    public void setData(InputRecord record)
        throws PersistenceException
    {
        super.setData(record);
        try
        {
            setJavaClass(record.getString("java_class_name"));
            if(!record.isNull("db_table_name"))
            {
                setDbTable(record.getString("db_table_name"));
            }
            setHandlerClass(record.getString("handler_class_name"));
            setFlags(record.getInteger("flags"));
        }
        catch(JavaClassException e)
        {
            throw new PersistenceException("Failed to load ResourceClass #"+id, e);
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
            catch(PersistenceException e)
            {
                throw new BackendException("failed to revert entity state");
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
        buildDeclaredAttributeSet();
        // copy on write
        Set snapshot = declaredAttributes;
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
        buildAttributeMap();
        // copy on write
        Map snapshot = attributeMap;
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
        buildAttributeMap();
        Map snapshot = attributeMap;
        AttributeDefinition attr = (AttributeDefinition)snapshot.get(name);
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
        buildAttributeMap();
        // copy on write
        Map snapshot = attributeMap;
        return snapshot.containsKey(name);
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
        buildInheritance();
        // copy on write
        Set snapshot = inheritance;
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
        buildParentClassSet();
        // copy on write
        Set snapshot = parentClasses;
        ResourceClass[] result = new ResourceClass[snapshot.size()];
        snapshot.toArray(result);
        return result;
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
        buildChildClassSet();
        // copy on write
        Set snapshot = childClasses;
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
        buildChildClassSet();
        // copy on write
        Set snapshot = childClasses;
        ResourceClass[] result = new ResourceClass[snapshot.size()];
        snapshot.toArray(result);
        return result;
    }

    /**
     * Returns the permissions associated with this resource class.
     *
     * @return the permissions associated with this resource class.
     */
    public Permission[] getPermissions()
    {
        buildPermissionSet();
        // copy on write
        Set snapshot = permissions;
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
        buildPermissionAssociationSet();
        // copy on write
        Set snapshot = permissionAssociations;
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
        buildPermissionSet();
        // copy on write
        Set snapshot = permissions;
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
            // copy on write
            Set inheritanceCopy = (Set)((HashSet)inheritance).clone();
            if(added)
            {
                inheritanceCopy.add(item);
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
                inheritanceCopy.remove(item);
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
            inheritance = inheritanceCopy;
        }
        // flush cached information
        childClasses = null;
        parentClasses = null;
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
        ResourceClass rc = attribute.getDeclaringClass();
        if(rc.equals(this) || parentClasses.contains(rc))
        {
            if(rc.equals(this))
            {
                Set declaredAttributesCopy = (Set)((HashSet)declaredAttributes).clone();
                if(added)
                {
                    declaredAttributesCopy.add(attribute);
                }
                else
                {
                    declaredAttributesCopy.remove(attribute);
                }
                declaredAttributes = declaredAttributesCopy;
            }
            Map attributeMapCopy = (Map)((HashMap)attributeMap).clone();
            if(added)
            {
                attributeMapCopy.put(attribute.getName(), attribute);
            }
            else
            {
                attributeMapCopy.remove(attribute.getName());
            }
            attributeMap = attributeMapCopy;
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
     */
    void setJavaClass(String className)
        throws JavaClassException
    {
        javaClassName = className;
        try
        {
            javaClass = instantiator.loadClass(className);
        }
        catch(ClassNotFoundException e)
        {
            javaClass = null;
            throw new JavaClassException(e.getMessage(), e);
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
            Class handlerClass = instantiator.loadClass(className);
            Map additional = new HashMap();
            additional.put(ResourceClass.class, this);
            handler = (ResourceHandler)instantiator.newInstance(handlerClass, additional);
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
    private synchronized void buildInheritance()
    {
        if(inheritance == null)
        {
            inheritance = coral.getRegistry().getResourceClassInheritance(this);
            coralEventHub.getGlobal().addResourceClassInheritanceChangeListener(this, this);
        }
    }
    
    /**
     * Initializes {@link #parentClasses} set if neccessary.
     */
    private synchronized void buildParentClassSet()
    {
        if(parentClasses == null)
        {
            buildInheritance();
            Set pc = new HashSet();
            ArrayList stack = new ArrayList();
            stack.add(this);
            
            while(stack.size() > 0)
            {
                ResourceClass rc = (ResourceClass)stack.remove(stack.size()-1);
                Set rcis;
                if(rc.equals(this))
                {
                    rcis = inheritance;
                }
                else
                {
                    pc.add(rc);
                    rcis = coral.getRegistry().getResourceClassInheritance(rc);
                    coralEventHub.getGlobal().addResourceClassInheritanceChangeListener(this, rc);
                    coralEventHub.getGlobal().addResourceClassAttributesChangeListener(this, rc);
                    coralEventHub.getGlobal().addPermissionAssociationChangeListener(this, rc);
                }
                Iterator i = rcis.iterator();
                while(i.hasNext())
                {
                    ResourceClassInheritance ir =
                        (ResourceClassInheritance)i.next();
                    if(ir.getChild().equals(rc))
                    {
                        stack.add(ir.getParent());
                    }
                }
            }
            parentClasses = pc;
        }
    }
    

    /**
     * Initializes {@link #childClasses} set if neccessary.
     */
    private synchronized void buildChildClassSet()
    {
        if(childClasses == null)
        {
            buildInheritance();
            Set cc = new HashSet();
            ArrayList stack = new ArrayList();
            stack.add(this);
            
            while(stack.size() > 0)
            {
                ResourceClass rc = (ResourceClass)stack.remove(stack.size()-1);
                Set rcis;
                if(rc.equals(this))
                {
                    rcis = inheritance;
                }
                else
                {
                    cc.add(rc);
                    rcis = coral.getRegistry().getResourceClassInheritance(rc);
                    coralEventHub.getGlobal().addResourceClassInheritanceChangeListener(this, rc);
                }
                Iterator i = rcis.iterator();
                while(i.hasNext())
                {
                    ResourceClassInheritance ir =
                        (ResourceClassInheritance)i.next();
                    if(ir.getParent().equals(rc))
                    {
                        stack.add(ir.getChild());
                    }
                }
            }
            childClasses = cc;
        }
    }

    /**
     * Initializes {@link #declaredAttributes} if neccessary.
     */
    private synchronized void buildDeclaredAttributeSet()
    {
        if(declaredAttributes == null)
        {
            declaredAttributes = coral.getRegistry().getDeclaredAttributes(this);
            coralEventHub.getGlobal().addResourceClassAttributesChangeListener(this, this);
        }   
    }
    
    /**
     * Initializes {@link #permissions} if neccessary.
     */
    private synchronized void buildPermissionSet()
    {    
        if(permissions == null)
        {
            buildPermissionAssociationSet();
            buildParentClassSet();
            Set temp = new HashSet();
            temp.addAll(permissionAssociations);
            Iterator i = parentClasses.iterator();
            while(i.hasNext())
            {
                ResourceClassImpl rc = (ResourceClassImpl)i.next();
                rc.buildPermissionAssociationSet();
                temp.addAll(rc.permissionAssociations);
            }
            permissions = new HashSet();
            i = temp.iterator();
            while(i.hasNext())
            {
                PermissionAssociation pa = (PermissionAssociation)i.next();
                permissions.add(pa.getPermission());
            }
        }   
    }

    /**
     * Initalizes {@link #permissionAssociations} if neccessary.
     */
    private synchronized void buildPermissionAssociationSet()
    {
        if(permissionAssociations == null)
        {
            permissionAssociations = coral.getRegistry().getPermissionAssociations(this);
            coralEventHub.getGlobal().addPermissionAssociationChangeListener(this, this);
        }
    }

    /**
     * Initailizes {@link #attributeMap} if neccessary.
     */
    private synchronized void buildAttributeMap()
    {
        if(attributeMap == null)
        {
            buildParentClassSet();
            buildDeclaredAttributeSet();
            
            attributeMap = new HashMap();
            Set ads = declaredAttributes;
            Iterator i = ads.iterator();
            while(i.hasNext())
            {
                AttributeDefinition attr = (AttributeDefinition)i.next();
                coralEventHub.getGlobal().addAttributeDefinitionChangeListener(this, attr);
                attributeMap.put(attr.getName(), attr);
            }
            Set rcs = parentClasses;
            Iterator j = rcs.iterator();
            while(j.hasNext())
            {
                ResourceClassImpl rc = (ResourceClassImpl)j.next();
                synchronized(rc)
                {
                    rc.buildAttributeMap();
                    coralEventHub.getGlobal().addResourceClassAttributesChangeListener(this, rc);
                    coralEventHub.getGlobal().addResourceClassInheritanceChangeListener(this, rc);
                    i = rc.attributeMap.values().iterator();
                    while(i.hasNext())
                    {
                        AttributeDefinition attr = (AttributeDefinition)i.next();
                        coralEventHub.getGlobal().addAttributeDefinitionChangeListener(this, attr);
                        attributeMap.put(attr.getName(), attr);
                    }
                }
            }
        }
    }
}
