package org.objectledge.coral.store;

import java.util.Date;

import org.objectledge.coral.entity.Entity;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.schema.UnknownAttributeException;
import org.objectledge.coral.security.PermissionAssignment;
import org.objectledge.coral.security.Role;
import org.objectledge.coral.security.Subject;

/**
 * Represents a resource.
 *
 * @version $Id: Resource.java,v 1.6 2005-03-18 10:26:09 rafal Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface Resource
    extends Entity
{
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
    public String getPath();
    
    /**
     * Returns the class this resource belongs to.
     *
     * @return the class this resource belongs to.
     */
    public ResourceClass<?> getResourceClass();

    /**
     * Returns the {@link Subject} that created this resource.
     *
     * @return the {@link Subject} that created this resource.
     */
    public Subject getCreatedBy();
    
    /**
     * Returns the creation time for this resource.
     *
     * @return the creation time for this resource.
     */
    public Date getCreationTime();

    /**
     * Returns the {@link Subject} that modified this resource most recently.
     *
     * @return the {@link Subject} that modified this resource most recently.
     */
    public Subject getModifiedBy();

    /**
     * Returns the last modification time for this resource.
     *
     * @return the last modification time for this resource.
     */
    public Date getModificationTime();

    /**
     * Returns the owner of the resource.
     *
     * @return the owner of the resource.
     */
    public Subject getOwner();

    /**
     * Returns the access control list for this resource.
     *
     * @return the access control list for this resource.
     */
    public PermissionAssignment[] getPermissionAssignments();

    /**
     * Returns the access control list entries for a specific role.
     *
     * @param role the role.
     * @return the access control list entries for a specific role.
     */
    public PermissionAssignment[] getPermissionAssignments(Role role);

    /**
     * Returns the parent resource.
     *
     * <p><code>null</code> is returned for top-level (root)
     * resources. Depending on the application one or more top-level resources
     * exist in the system.</p>
     *
     * @return the parent resource.
     */
    public Resource getParent();

    /**
     * Returns the parent resource's identifier.
     * 
     * <p>This method can be used to avoid lazy loading the parent resource in certain 
     * situations.</p>
     * 
     * @return the identifier of the parent resource or -1 if undefined.
     */
    public long getParentId();

    /**
     * Returns immediate children of the resource.
     * 
     * @return the immediate children of the resource.
     */
    public Resource[] getChildren();
    
    /** 
     * Checks if the specified attribute of the resource is defined.
     *
     * @param attribute the attribute to check.
     * @return <code>true</code> if the specified attribute is defined.
     * @throws UnknownAttributeException if <code>attribute</code> does not
     *         belong to the resource's class.
     */
    public boolean isDefined(AttributeDefinition<?> attribute)
        throws UnknownAttributeException;
    
    /**
     * Retrieves the value of a specific attribute.
     * 
     * @param attribute the attribute to retrieve.
     * @return the value of the attribute, or <code>null</code> if undefined.
     * @throws UnknownAttributeException if <code>attribute</code> does not
     *         belong to the resource's class.
     */
    public <T> T get(AttributeDefinition<T> attribute)
        throws UnknownAttributeException;
    
    /**
     * Retrieves the value of a specific attribute.
     * 
     * @param attribute the attribute to retrieve.
     * @param defaultValue value to be returned when the attribute value is undefined.
     * @return the value of the attribute, or <code>null</code> if undefined.
     * @throws UnknownAttributeException if <code>attribute</code> does not
     *         belong to the resource's class.
     */
    public <T> T get(AttributeDefinition<T> attribute, T defaultValue)
        throws UnknownAttributeException;
    
    /**
     * Sets the value of a specific attribute.
     * 
     * @param attribute the attribute to set.
     * @param value the value of the attribute.
     * @throws UnknownAttributeException if <code>attribute</code> does not
     *         belong to the resource's class.
     * @throws ModificationNotPermitedException if the attribute is
     *         <code>READONLY</code>.
     * @throws ValueRequiredException if <code>attribute</code> is
     *         <code>REQUIRED</code> and <code>value</code> is
     *         <code>null</code>.
     */
    public <T> void set(AttributeDefinition<T> attribute, T value)
        throws UnknownAttributeException, ModificationNotPermitedException,
        ValueRequiredException;

    /**
     * Removes the value of the specified attribute.
     *
     * @param attribute the attribute to remove.
     * @throws ValueRequiredException if the attribute is required for this
     *         resource type.
     * @throws UnknownAttributeException if <code>attribute</code> does not
     *         belong to the resource's class.
     */
    public void unset(AttributeDefinition<?> attribute)
        throws ValueRequiredException, UnknownAttributeException;
    
    /**
     * Sets the modified flag for the specified attribute.
     *
     * @param attribute the attribute to mark as modified.
     * @throws UnknownAttributeException if <code>attribute</code> does not
     *         belong to the resource's class.
     */
    public void setModified(AttributeDefinition<?> attribute)
        throws UnknownAttributeException;
    
    /**
     * Checks the modified flag for the specified resource.
     *
     * @param attribute the attribute to check.
     * @return <code>true</code> if the attribute was modified.
     * @throws UnknownAttributeException if <code>attribute</code> does not
     *         belong to the resource's class.
     */
    public boolean isModified(AttributeDefinition<?> attribute)
        throws UnknownAttributeException;

    /**
     * Updates the image of the resource in the persistent storage.
     */
    public void update();

    /**
     * Reverts the Resource object to the state present in the persistent
     * storage. 
     */
    public void revert();

    /**
     * Returns the security delegate object.
     *
     * <p>This method is of no intrest to the end-users. It's important to the
     * implementors of concrete resource classes only. It should return the same
     * <code>ResourceObject</code> that was passed to the {@link
     * org.objectledge.coral.schema.ResourceHandler#create(Resource,java.util.Map,
     * java.sql.Connection)} method upon creation of the resource.</p>
     *
     * @return the security delegate object.
     */
    public Resource getDelegate();
}
