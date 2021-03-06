package org.objectledge.coral.store;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.objectledge.coral.BackendException;
import org.objectledge.coral.CoralCore;
import org.objectledge.coral.entity.AbstractEntity;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.event.CoralEventHub;
import org.objectledge.coral.event.PermissionAssignmentChangeListener;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.schema.UnknownAttributeException;
import org.objectledge.coral.security.PermissionAssignment;
import org.objectledge.coral.security.Role;
import org.objectledge.coral.security.Subject;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.OutputRecord;
import org.objectledge.database.persistence.Persistence;

/**
 * The ''security delegate'' Resource implementation.
 *
 * <p>This class deals with identity, parent-child relationships and security
 * aspects of the <code>Resource</code> interface. Attribute-related methods
 * of this class throw <code>UnsupportedOperationException</code>. Instances
 * of this class are passed to 
 * {@link org.objectledge.coral.schema.ResourceHandler#create(Resource,Map,java.sql.Connection)} 
 * and {@link org.objectledge.coral.schema.ResourceHandler#retrieve(Resource,
 * java.sql.Connection,Object)}.</p>
 *
 * @version $Id: ResourceImpl.java,v 1.28 2008-01-01 20:20:27 rafal Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class ResourceImpl
    extends AbstractEntity
    implements Resource,
               PermissionAssignmentChangeListener
{
    // Instance variables ////////////////////////////////////////////////////////////////////////

    /** The component hub. */
    private CoralCore coral;

    /** The CoralEventHub. */
    private CoralEventHub coralEventHub;

    /** The class of this resource. */
    private ResourceClass resourceClass;
    
    /** Parent resource id (may be -1). */
    private long parentId = -1;
    
    /** The parent resource (may be null). */
    private ResourceRef parent;
    
    /** The resource's creator. */
    private Subject creator;
    
    /** The resource's owner. */
    private Subject owner;
    
    /** The subject that performed most recent modification. */
    private Subject modifier;
    
    /** Resource's creation time. */
    private Date created;
    
    /** Resource's last modification time. */
    private Date modified;

    /** The permission assignments made on this resource. */
    private Set assignments;
    
    /** The permission assignments made on this resource for different
     * roles. */
    private Map assignmentsByRole = new WeakHashMap();

    /**
     * Mapping of builtin attributes to methods.
     */
    private static Map builtinAttributes = new HashMap();

    /**
     * Fillin the {@link #builtinAttributes} map.
     */
    static
    {
        try
        {
            Class[] noArg = new Class[0];
            Class impl = ResourceImpl.class;
            builtinAttributes.
                put("name", impl.getMethod("getName", noArg));
            builtinAttributes.
                put("id", impl.getMethod("getId", noArg));
            builtinAttributes.
                put("path",impl.getMethod("getPath", noArg));
            builtinAttributes.
                put("resource_class", impl.getMethod("getResourceClass", noArg));
            builtinAttributes.
                put("parent", impl.getMethod("getParent", noArg));
            builtinAttributes.
                put("owner", impl.getMethod("getOwner", noArg));
            builtinAttributes.
                put("created_by", impl.getMethod("getCreatedBy", noArg));
            builtinAttributes.
                put("modified_by", impl.getMethod("getModifiedBy", noArg));        
            builtinAttributes.
                put("creation_time", impl.getMethod("getCreationTime", noArg));
            builtinAttributes.
                put("modification_time", impl.getMethod("getModificationTime", noArg));
        }
        catch(NoSuchMethodException e)
        {
            throw new BackendException("failed to resolve class method", e);
        }
    }

    // Initialization ///////////////////////////////////////////////////////////////////////////

    /**
     * Constructs a blank Resource object for reading data in.
     *
     * @param persistence the Persistence subsystem.
     * @param coral the component hub.
     * @param coralEventHub the event hub.
     */
    public ResourceImpl(Persistence persistence, CoralCore coral, CoralEventHub coralEventHub)
    {
        super(persistence);
        this.coral = coral;
        this.coralEventHub = coralEventHub;
    }
    
    /**
     * Constructs a new security delegate Resource object.
     *
     * @param persistence the Persistence subsystem.
     * @param coral the component hub.
     * @param coralEventHub the event hub.
     * 
     * @param name the name of the new resource.
     * @param resourceClass the resource class of the new resource.
     * @param parent the parent resource (may be <code>null</code>)
     * @param creator the Subject that creates the resource.
     */
    public ResourceImpl(Persistence persistence, CoralCore coral, CoralEventHub coralEventHub,
        String name, ResourceClass resourceClass, Resource parent, Subject creator)
    {
        super(persistence, name);
        this.coral = coral;
        this.coralEventHub = coralEventHub;

        this.resourceClass = resourceClass;
        this.parent = new ResourceRef(parent, coral);
        if(parent != null)
        {
            this.parentId = parent.getId();
        } 
        this.creator = creator;
        this.owner = creator;
        this.modifier = creator;
        this.created = new Date();
        this.modified = this.created;
    }
    
    /**
     * Compares this ResourceImpl to another object.
     * 
     * <p>This methods takes into account the fact that the argument may be another resource 
     * security delegate, or an actual resource, and returns <code>true</code> if the argument
     * refers to the same logical resource.</p>
     * 
     * @return true if obj is non-null, an implementation of Resource interface and refers to 
     * the same logical resource.
     */
    public boolean equals(Object obj) 
    {
        if(obj instanceof Resource) 
        {
            Resource delegate = ((Resource)obj).getDelegate();
            if(delegate == null) 
            {
                return super.equals(obj);
            }
            else
            {
                return super.equals(delegate);
            }
        }
        return false;
    }
    
    /**
     * Returns a String representation of this object.
     *
     * <p> This method is overriden to augument debugging. The format of the representation is as 
     * following: 
     * <blockquote>
     *   <code>javaClass name #id @identity</code>
     * </blockquote>
     * Where:
     * <ul>
     *   <li><code>javaClass</code> is the actual implementation class of the object</li>
     *   <li><code>path</code> is the path of the resource as returned by the {@link #getPath()} 
     *     method.</li>
     *   <li><code>id</code> is the identifier of the resource as returned by the {@link #getId()}
     *     method.</li> 
     *   <li><code>idenity</code> is the object instance's identity hashcode as retured by the
     *     <code>System.getIdentityHashCode(Object)</code> function.</li>
     *  </ul>
     *  </p>
     * 
     * @return a String representation of this object.
     */
    public String toString()
    {
        StringBuilder buff = new StringBuilder();
        buff.append(getClass().getName());
        buff.append(" delegate for ");
        buff.append(getPath());
        buff.append(" #");
        buff.append(getIdString());
        buff.append(" @");
        buff.append(Integer.toString(System.identityHashCode(this), 16));
        return buff.toString();
    }
    

    // Persitent interface //////////////////////////////////////////////////////////////////////
    
    /** The key columns */
    private static final String[] KEY_COLUMNS = { "resource_id" };
    
    /**
     * Returns the name of the table this type is mapped to.
     *
     * @return the name of the table.
     */
    public String getTable()
    {
        return "coral_resource";
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
     * @throws SQLException if there is a problem storing field values.
     */
    public void getData(OutputRecord record)
        throws SQLException
    {
        super.getData(record);
        record.setLong("resource_class_id", resourceClass.getId());
        if(parentId != -1)
        {
            record.setLong("parent", parentId);
        }
        else
        {
            record.setNull("parent");
        }
        record.setLong("created_by", creator.getId());
        record.setTimestamp("creation_time", created);
        record.setLong("owned_by", owner.getId());
        record.setLong("modified_by", modifier.getId());
        record.setTimestamp("modification_time", modified);
    }

    /**
     * Loads the fields of the object from the specified record.
     *
     * <p>You need to call <code>setData</code> of your superclasses if they
     * are <code>Persistent</code>.</p>
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
            long resourceClassId = record.getLong("resource_class_id");
            resourceClass = coral.getSchema().getResourceClass(resourceClassId);
            if(!record.isNull("parent"))
            {
                parentId = record.getLong("parent");
            }
            long creatorId = record.getLong("created_by");
            creator = coral.getSecurity().getSubject(creatorId);
            created = record.getDate("creation_time");
            long ownerId = record.getLong("owned_by");
            owner = coral.getSecurity().getSubject(ownerId);
            long modifierId = record.getLong("modified_by");
            modifier = coral.getSecurity().getSubject(modifierId);
            modified = record.getDate("modification_time");
        }
        catch(EntityDoesNotExistException e)
        {
            throw new SQLException("Failed to load Resource #"+id, e);
        }
    }

    // Resource interface ////////////////////////////////////////////////////

    /**
     * Returns the path name of the resource.
     *
     * <p>The path name is composed of the names of all of the resource's
     * parents, separated by / characters. If the top level parent (resource
     * that has <code>null</code> parent) is the 'root' resource #1, the
     * pathname will start with a /. Please note that the pathname can also
     * denote other resources than this one, unless all resources in your
     * system have unique names.</p>
     *
     * @return the pathname of the resource.
     */
    public String getPath()
    {
        StringBuilder buff = new StringBuilder();
        Resource r = this;
        while(r != null)
        {
            if(r.getId() != 1)
            {
                buff.insert(0, r.getName());
            }
            r = r.getParent();
            if(r != null || this.getId() == 1)
            {
                buff.insert(0, '/');
            }
        }
        return buff.toString();
    }

    /**
     * Returns the class this resource belongs to.
     *
     * @return the class this resource belongs to.
     */
    public ResourceClass getResourceClass()
    {
        return resourceClass;
    }

    /**
     * Returns the {@link Subject} that created this resource.
     *
     * @return the {@link Subject} that created this resource.
     */
    public Subject getCreatedBy()
    {
        return creator;
    }
    
    /**
     * Returns the creation time for this resource.
     *
     * @return the creation time for this resource.
     */
    public Date getCreationTime()
    {
        return created;
    }

    /**
     * Returns the {@link Subject} that modified this resource most recently.
     *
     * @return the {@link Subject} that modified this resource most recently.
     */
    public Subject getModifiedBy()
    {
        return modifier;
    }

    /**
     * Returns the last modification time for this resource.
     *
     * @return the last modification time for this resource.
     */
    public Date getModificationTime()
    {
        return modified;
    }

    /**
     * Returns the owner of the resource.
     *
     * @return the owner of the resource.
     */
    public Subject getOwner()
    {
        return owner;
    }

    /**
     * Returns the access control list for this resource.
     *
     * @return the access control list for this resource.
     */
    public PermissionAssignment[] getPermissionAssignments()
    {
        buildAssignmentSet();
        Set snapshot = assignments;
        PermissionAssignment[] result = new PermissionAssignment[snapshot.size()];
        snapshot.toArray(result);
        return result;
    }

    /**
     * Returns the access control list entries for a specific role.
     * 
     * @param role the role.
     * @return the access control list entries for a specific role.
     */
    public PermissionAssignment[] getPermissionAssignments(Role role)
    {
        synchronized(assignmentsByRole)
        {
            Set currentAssignments = buildAssignmentSet(role);
            PermissionAssignment[] result = new PermissionAssignment[currentAssignments.size()];
            currentAssignments.toArray(result);
            return result;
        }
    }

    /**
     * Returns the parent resource.
     *
     * <p><code>null</code> is returned for top-level (root)
     * resources. Depending on the application one or more top-level resources
     * exist in the system.</p>
     *
     * @return the parent resource.
     */
    public Resource getParent()
    {
        if(parent == null)
        {
            if(parentId != -1)
            {
                try
                {
                    parent = new ResourceRef(coral.getStore().getResource(parentId), coral);
                }
                catch(EntityDoesNotExistException e)
                {
                    throw new BackendException("corrupted data parent resource #"+parentId+
                        " does not exist", e);
                }
            }
            else
            {
                parent = new ResourceRef(null, coral);
            }
        }
        try
        {
            return parent.get();
        }
        catch(EntityDoesNotExistException e)
        {
            throw new BackendException("in-memory data incosistency", e);
        }
    }

    /**
     * Returns the parent resource's identifier.
     * 
     * <p>This method can be used to avoid lazy loading the parent resource in certain 
     * situations.</p>
     * 
     * @return the identifier of the parent resource or -1 if undefined.
     */
    public long getParentId()
    {
        return parentId;
    }

    /**
     * Returns immediate children of the resource.
     * 
     * @return the immediate children of the resource.
     */
    public Resource[] getChildren()
    {
        try
        {
            return coral.getStore().getResource(coral.getStore().getResource(getId()));
        }
        catch(EntityDoesNotExistException e)
        {
            throw new BackendException("Unexpected inconsitency", e);
        }
    }
    
    /**
     * Returns immediate children of the resource of the specific type.
     * 
     * @param type desired type
     * @return the immediate children of the resource.
     */
    @SuppressWarnings("unchecked")
    public <T extends Resource> List<T> getChildren(Class<T> type)
    {
        Resource[] allChildren = getChildren();
        List<T> filteredChildren = new ArrayList<T>();
        for(Resource child : allChildren)
        {
            if(type.isAssignableFrom(child.getClass()))
            {
                filteredChildren.add((T)child);
            }
        }
        return filteredChildren;
    }

    /** 
     * Checks if the specified attribute of the resource is defined.
     *
     * @param attribute the attribute to check.
     * @return <code>true</code> if the specified attribute is defined.
     * @throws UnknownAttributeException if <code>attribute</code> does not
     *         belong to the resource's class.
     */
    public boolean isDefined(AttributeDefinition attribute) 
        throws UnknownAttributeException
    {
        if(builtinAttributes.containsKey(attribute.getName()))
        {
            if(attribute.getName().equals("parent"))
            {
                return parent != null;
            }
            else
            {
                return true;
            }
        }
        else
        {
            throw new UnknownAttributeException("not a builtin attribute: '"+
                                                  attribute.getName()+"'");
        }
    }
    
    /**
     * Retrieves the value of a specific attribute.
     * 
     * @param attribute the attribute to retrieve.
     * @return the value of the attribute, or <code>null</code> if undefined.
     * @throws UnknownAttributeException if <code>attribute</code> does not
     *         belong to the resource's class.
     */
    public Object get(AttributeDefinition attribute)
        throws UnknownAttributeException
    {
        if(!builtinAttributes.containsKey(attribute.getName()))
        {
            throw new UnknownAttributeException(attribute.getName()+
            		" declared in "+attribute.getDeclaringClass().getName()+
            		" is not a builtin attribute ");
        }
        Method method = (Method)builtinAttributes.get(attribute.getName());
        try
        {
            return method.invoke(this, new Object[0]);
        }
        catch(Exception e)
        {
            throw new BackendException("failed to invoke method for builtin attribute "+
                attribute.getName(), e);
        }
    }
    
    /**
     * Retrieves the value of a specific attribute.
     * 
     * @param attribute the attribute to retrieve.
     * @return the value of the attribute, or <code>null</code> if undefined.
     * @throws UnknownAttributeException if <code>attribute</code> does not
     *         belong to the resource's class.
     */
    public Object get(AttributeDefinition attribute, Object defaultValue)
        throws UnknownAttributeException
    {
    	Object value = get(attribute);
    	return value != null ? value : defaultValue;
    }
    
    /**
     * Sets the value of a specific attribute.
     * 
     * @param attribute the attribute to set.
     * @param value the value of the attribute.
     * @throws UnknownAttributeException if <code>attribute</code> does not
     *         belong to the resource's class.
     * @throws ModificationNotPermitedException if the attribute is
     *         <code>READONLY</code>.
     */
    public void set(AttributeDefinition attribute, Object value)
        throws UnknownAttributeException, ModificationNotPermitedException
    {
        if(builtinAttributes.containsKey(attribute.getName()))
        {
            throw new IllegalArgumentException("builtin attribute "+attribute.getName()+
                                               "cannot be modified with set method");    
        }
        else
        {
            throw new UnknownAttributeException("not a builtin attribute");
        }
    }

    /**
     * Removes the value of the specified attribute.
     *
     * @param attribute the attribute to remove.
     * @throws ValueRequiredException if the attribute is required for this
     *         resource type.
     * @throws UnknownAttributeException if <code>attribute</code> does not
     *         belong to the resource's class.
     */
    public void unset(AttributeDefinition attribute)
        throws ValueRequiredException, UnknownAttributeException
    {
        if(builtinAttributes.containsKey(attribute.getName()))
        {
            throw new IllegalArgumentException("builtin attribute "+attribute.getName()+
                                               " cannot be removed with unset method");    
        }
        else
        {
            throw new UnknownAttributeException("not a builtin attribute");
        }
    }
    
    /**
     * Sets the modified flag for the specified attribute.
     *
     * @param attribute the attribute to mark as modified.
     * @throws UnknownAttributeException if <code>attribute</code> does not
     *         belong to the resource's class.
     */
    public void setModified(AttributeDefinition attribute)
        throws UnknownAttributeException
    {
        if(builtinAttributes.containsKey(attribute.getName()))
        {
            throw new IllegalArgumentException("builtin attribute "+attribute.getName()+
                                               "cannot be modified this way");    
        }
        else
        {
            throw new UnknownAttributeException("not a builtin attribute");
        }
    }
    
    /**
     * Checks the modified flag for the specified resource.
     *
     * @param attribute the attribute to check.
     * @return <code>true</code> if the attribute was modified.
     * @throws UnknownAttributeException if <code>attribute</code> does not
     *         belong to the resource's class.
     */
    public boolean isModified(AttributeDefinition attribute)
        throws UnknownAttributeException
    {
        if(builtinAttributes.containsKey(attribute.getName()))
        {
            return false;
        }
        else
        {
            throw new UnknownAttributeException("not a builtin attribute");
        }
    }

    /**
     * Updates the image of the resource in the persistent storage.
     */
    public void update()
    {
        modifier = coral.getCurrentSubject();
        modified = new Date();
        try
        {
            persistence.save(this);
        }
        catch(SQLException e)
        {
            throw new BackendException("failed to update resource's persitent image", e);
        }
        try
        {
            Resource impl = coral.getStore().getResource(getId());
            coralEventHub.getGlobal().fireResourceChangeEvent(impl, modifier);
        }
        catch(EntityDoesNotExistException e)
        {
            throw new BackendException("inconsistent data", e);
        }
    }

    /**
     * Reverts the Resource object to the state present in the persistent
     * storage. 
     */
    public void revert()
    {
        throw new UnsupportedOperationException("this is a security delegate object");        
    }

    /**
     * Returns the security delegate object.
     *
     * @return the security delegate object.
     */
    public Resource getDelegate()
    {
        return null;
    }

    // PermissionAssignmentChangeListener interface //////////////////////////

    /**
     * Called when permission assignemts on the <code>resource</code> change.
     *
     * @param assignment the permission assignment.
     * @param added <code>true</code> if the permission was added,
     *        <code>false</code> if removed.
     */
    public void permissionsChanged(PermissionAssignment assignment, boolean added)
    {
        if(assignment.getResource().getId() != id)
        {
            return;
        }
        if(assignments != null)
        {
            synchronized(assignments)
            {
                if(added)
                {
                    assignments.add(assignment);
                }
                else
                {
                    assignments.remove(assignment);
                }
            }
        }
        synchronized(assignmentsByRole)
        {
            Set pas = (Set)assignmentsByRole.get(assignment.getRole());
            if(pas != null)
            {
                if(added)
                {
                    pas.add(assignment);
                }
                else
                {
                    pas.remove(assignment);
                }
            }
        }
    }

    // package private setters //////////////////////////////////////////////////////////////////

    /**
     * Sets the owner of the resource.
     *
     * @param owner the owner of the resource.
     */
    void setOwner(Subject owner)
    {
        this.owner = owner;
    }

    /**
     * Changes the creator of the resource (used when deleting subjects).
     * 
     * @param creator updated creator reference.
     */
    void setCreaedBy(Subject creator)
    {
        this.creator = creator;
    }

    /**
     * Changes the last modifier of the resource (used when deleting subjects).
     * 
     * @param modifier updated modifier reference.
     */
    void setModified(Subject modifier, Date modificationTime)
    {
        this.modifier = modifier;
        this.modified = modificationTime;
    }

    /**
     * Sets the name of the resource.
	 *
	 * <p>Needed because AbstractEntityt.setName(String) is not visible in this package.</p>
     *
     * @param name the new name of the resource.
     */
    void setResourceName(String name)
    {
        this.name = name;
    }

    /**
     * Sets the parent of the resource.
     *
     * @param parent the parent of the resource.
     */
    void setParent(Resource parent)
    {
        this.parent = new ResourceRef(parent, coral);
        if(parent != null)
        {
        	parentId = parent.getId();
        }
        else
        {
        	parentId = -1;
        }
    }

    // private //////////////////////////////////////////////////////////////////////////////////
    
    private void buildAssignmentSet()
    {
        if(assignments == null)
        {
            assignments = coral.getRegistry().getPermissionAssignments(this);
            try
            {
                Resource impl = coral.getStore().getResource(getId());
                coralEventHub.getGlobal().addPermissionAssignmentChangeListener(this, impl);
            }
            catch(Exception e)
            {
                throw new BackendException("inconsistent data", e);
            }
        }
    }

    private Set buildAssignmentSet(Role role)
    {
        buildAssignmentSet();
        synchronized(assignments)
        {
            Set pas = (Set)assignmentsByRole.get(role);
            if(pas == null)
            {
                pas = new HashSet();
                Iterator i = assignments.iterator();
                while(i.hasNext())
                {
                    PermissionAssignment pa = (PermissionAssignment)i.next();
                    if(role.equals(pa.getRole()))
                    {
                        pas.add(pa);
                    }
                }
                assignmentsByRole.put(role, pas);
            }
            return pas;
        }          
    }
}
