package org.objectledge.coral.schema;

import java.util.Set;

import org.objectledge.coral.entity.Entity;
import org.objectledge.coral.security.Permission;
import org.objectledge.coral.security.PermissionAssociation;

/**
 * Represents a resource class.
 *
 * @version $Id: ResourceClass.java,v 1.6 2007-05-31 20:24:58 rafal Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface ResourceClass
    extends Entity
{
    /**
     * Returns the resource class flags.
     *
     * <p>Use {@link ResourceClassFlags} to decode flag values.
     *
     * @return resource class flags.
     */
    public int getFlags();

    /**
     * Returns the name of the Java class that is associated with this
     * resource class. 
     *
     * @return the Java class that is associated with this resource class.
     * type.
     */
    public String getJavaClassName();

    /**
     * Returns the Java class that is associated with this resource class.
     *
     * @return the Java class that is associated with this resource class.
     * type.
     */
    public Class<?> getJavaClass();

    /**
     * Returns the ResourceHandler implementaion that will manage the
     * resources of that class.
     *
     * @return an <code>ResourceHandler</code> implementation.
     */
    public ResourceHandler getHandler();

    /**
     * Return the name of a database table that holds the data of the
     * resources of that type.
     *
     * @return the name of a database table that holds the data of the
     * resource of that type, or <code>null</code> if not used.
     */
    public String getDbTable();

    /**
     * Returns attributes declared by this resource class.
     *
     * @return attributes declared by this resource class.
     */
    public AttributeDefinition<?>[] getDeclaredAttributes();

    /**
     * Returns all attributes declared by this resource class and it's parent
     * classes.  
     *
     * @return all attributes delcared by this resource class and it's parent
     * classes.  
     */
    public AttributeDefinition<?>[] getAllAttributes();

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
    public AttributeDefinition<?> getAttribute(String name)
        throws UnknownAttributeException;

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
    public boolean hasAttribute(String name);
    
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
        throws UnknownAttributeException;

    /**
     * Returns the maximum attribute index used at this moment by the resource class.
     * 
     * @return the maximum attribute index used at this moment by the resource class.
     */
    public int getMaxAttributeIndex();
    
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
    public ResourceClassInheritance[] getInheritance();

    /**
     * Returns the parent classes of this resource class.
     *
     * <p>Both direct and indirect parent classes will be reported.</p>
     * 
     * @return the parent classes of this resource class.
     */
    public ResourceClass[] getParentClasses();
    
    /**
     * Returns the direct parent classes of this resource class.
     *
     * @return the direct parent classes of this resource class.
     */
    public Set<ResourceClass> getDirectParentClasses();

    /**
     * Returns the child classes of this resource class.
     *
     * <p>Both direct and indirect child classes will be reported.</p>
     *
     * @return the child classes of this resource class.
     */
    public ResourceClass[] getChildClasses();

    /**
     * Returns the direct child classes of this resource class.
     *
     * @return the direct child classes of this resource class.
     */
    public Set<ResourceClass> getDirectChildClasses();    
    
    /**
     * Checks if the specifid class is a child class of this class.
     *
     * @param resourceClass the class to check.
     * @return <code>true</code> if the specifid class is a child class of
     * this class. 
     */
    public boolean isParent(ResourceClass resourceClass);

    /**
     * Returns the permission associations made for this class.
     *
     * @return the permission associations made for this class.
     */
    public PermissionAssociation[] getPermissionAssociations();

    /**
     * Returns the permissions associated with this resource class and it's
     * parent classes.
     *
     * @return the permissions associated with this resource class and it's
     * parent classes.
     */
    public Permission[] getPermissions();

    /**
     * Returns <code>true</code> if the specified permission is associated
     * with this resource class.
     *
     * @param permission the permission.
     * @return <code>true</code> if the specified permission is associated
     * with this resource class.
     */
    public boolean isAssociatedWith(Permission permission);
}
