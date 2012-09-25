package org.objectledge.coral.schema;

import java.util.Map;

import org.objectledge.collections.ImmutableSet;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.entity.EntityExistsException;
import org.objectledge.coral.entity.EntityFactory;
import org.objectledge.coral.entity.EntityInUseException;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ValueRequiredException;

/**
 * Manages {@link ResourceClass}es and their associated entities.
 *
 * @version $Id: CoralSchema.java,v 1.6 2009-01-30 13:43:52 rafal Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface CoralSchema
{
    // Attribute classes /////////////////////////////////////////////////////

    /**
     * Returns all {@link AttributeClass}es defined in the system.
     *
     * @return all {@link AttributeClass}es defined in the system.
     */
    public ImmutableSet<AttributeClass<?>> getAllAttributeClasses();

    /**
     * Returns the {@link AttributeClass} with a specific identifier.
     *
     * @param id the identifier.
     * @return the <code>AttributeClass</code>.
     * @throws EntityDoesNotExistException if the <code>AttributeClass</code>
     *         with the specified identifier does not exist.
     */
    public AttributeClass<?> getAttributeClass(long id)
        throws EntityDoesNotExistException;
    
    /**
     * Returns the attribute class with the specified name.
     *
     * @param name the name.
     * @return the <code>AttributeClass</code>s with the given name.
     * @throws EntityDoesNotExistException if the <code>AttributeClass</code>
     *         with the specified name does not exist.
     */
    public AttributeClass<?> getAttributeClass(String name)
        throws EntityDoesNotExistException;

    /**
     * Returns the attribute class with the specified name.
     * 
     * @param name the name.
     * @return the <code>AttributeClass</code>s with the given name.
     * @throws EntityDoesNotExistException if the <code>AttributeClass</code> with the specified
     *         name does not exist.
     */
    public <A> AttributeClass<A> getAttributeClass(String name, Class<A> javaClass)
        throws EntityDoesNotExistException;

    /**
     * Creates an {@link AttributeClass}.
     *
     * @param name the name of the class
     * @param javaClass the Java type corresponding to this attribute class.
     * @param handlerClass the {@link AttributeHandler} implementation that will
     *        manage the instances of this attributes class.
     * @param dbTable the database table that will hold the data for the
     *        attributes of that class (allows sharing handler classes betweeen
     *        attribute types).
     * @return a newly created <code>AttributeClass</code>
     * @throws EntityExistsException if an attribute class with specified name
     *         already exists.
     * @throws JavaClassException if the <code>javaClass</code> or
     *         <code>handlerClass</code> attributes don't specify valid Java
     *         classes.
     */
    public AttributeClass<?> createAttributeClass(String name, String javaClass, 
                                               String handlerClass, String dbTable)
        throws EntityExistsException, JavaClassException;

    /**
     * Removes an {@link AttributeClass}.
     *
     * @param attributeClass the {@link AttributeClass}.
     * @throws EntityInUseException if there is a {@link ResourceClass} that
     *         uses the specified <code>AttributeClass</code>.
     */
    public void deleteAttributeClass(AttributeClass<?> attributeClass)
        throws EntityInUseException;

    /**
     * Renames an attributeClass.
     *
     * @param attributeClass the attribute class to rename.
     * @param name the new name.
     * @throws EntityExistsException if an attribute class with that name already
     *         exists in the system.
     */
    public void setName(AttributeClass<?> attributeClass, String name)
        throws EntityExistsException;

    /**
     * Sets the Java class corresponding to an <code>AttributeClass</code>.
     *
     * @param attributeClass the {@link AttributeClass}.
     * @param javaClass the Java class corresponding to an
     *        <code>AttributClass</code> 
     * @throws JavaClassException if the <code>javaClass</code> does not specify
     *         a valid JavaClass.
     */
    public void setJavaClass(AttributeClass<?> attributeClass, String javaClass)
        throws JavaClassException;

    /**
     * Sets the {@link AttributeHandler} implementation that will manage the
     * attributes of that class. 
     *
     * @param attributeClass the {@link AttributeClass}.
     * @param handlerClass the {@link AttributeHandler} implementation that will
     *        manage the attributes of that class.
     * @throws JavaClassException if the <code>handlerClass</code> does not
     *         specify a valid JavaClass.
     */
    public void setHandlerClass(AttributeClass<?> attributeClass, 
                                String handlerClass)
        throws JavaClassException;
    
    /**
     * Sets the database table that will hold the data for the attributes of
     * that class. 
     *
     * @param attributeClass the {@link AttributeClass}.
     * @param dbTable the database table that will hold the data for the
     *        attributes of that class (allows sharing handler classes betweeen
     *        attribute types).
     */
    public void setDbTable(AttributeClass<?> attributeClass, String dbTable);

    // Attribute instances ///////////////////////////////////////////////////

    /**
     * Creates an attribute instance.
     * 
     * @param name the name of the new Attribute.
     * @param attributeClass the class of the new attribute.
     * @param dbColumn name of database column, may be {@code null} in which case, attribute
     *        {@code name} is used as column name.
     * @param domain the value domain constraint.
     * @param flags the flags of the new Attribute.
     * @return a newly created attribute instance.
     */
    public <T> AttributeDefinition<T> createAttribute(String name,
        AttributeClass<T> attributeClass, String dbColumn, String domain, int flags);

    /**
     * Returns all attributes defined by classes in the system.
     * 
     * @return all attributes defined by classes in the system.
     */
    public ImmutableSet<AttributeDefinition<?>> getAllAttributes();

    /**
     * Returns an attribute definition with the specified id.
     * 
     * @param id the attribute definition identifier.
     * @return the attribute definition.
     * @throws EntityDoesNotExistException if no attribute with given id exists.
     */
    public AttributeDefinition<?> getAttribute(long id)
        throws EntityDoesNotExistException;

    /**
     * Removes an attribute from it's containg <code>ResourceClass</code>.
     *
     * <p>If the <code>AttributeDefinition</code> object wasn't attached to any
     * <code>ResourceClass</code> this call has no effect.</p>
     *
     * @param resourceClass the resource class to modify.
     * @param attribute the attribute to remove.
     * @throws IllegalArgumentException if the attribute is not declared
     *        by the resourceClass.
     */
    public void deleteAttribute(ResourceClass<?> resourceClass, AttributeDefinition<?> attribute)
        throws IllegalArgumentException;

    /**
     * Renames an attribute.
     *
     * @param attribute the attribute to rename.
     * @param name the new name.
     * @throws SchemaIntegrityException if the <code>ResourceClass</code> already
     *         has an attribute with the specified name.
     */
    public void setName(AttributeDefinition<?> attribute, String name)
        throws SchemaIntegrityException;

    /**
     * Changes the database column name of the attribute.
     * 
     * @param attribute the attribute to modify.
     * @param dbColumn name of database column, may be {@code null} in which case, attribute
     *        {@code name} is used as column name.
     */
    public void setDbColumn(AttributeDefinition<?> attribute, String dbColumn);

    /**
     * Changes the domain of the attribute.
     * 
     * @param attribute the attribute to modify.
     * @param domain the new domain of the attribute.
     */
    public void setDomain(AttributeDefinition<?> attribute, String domain);

    /**
     * Changes the flags of the attribute.
     *
     * @param attribute the attribute to modify.
     * @param flags the new value of flags.
     */
    public void setFlags(AttributeDefinition<?> attribute, int flags);
                        
    // Resource classes //////////////////////////////////////////////////////

    /**
     * Returns all {@link ResourceClass}es defined in the system.
     *
     * @return all {@link ResourceClass}es defined in the system.
     */
    public ImmutableSet<ResourceClass<?>> getAllResourceClasses();

    /**
     * Returns the {@link ResourceClass} with a specific identifier.
     *
     * @param id the identifier.
     * @return the <code>ResourceClass</code>.
     * @throws EntityDoesNotExistException if the <code>ResourceClass</code>
     *         with the specified identifier does not exist.
     */
    public ResourceClass<?> getResourceClass(long id)
        throws EntityDoesNotExistException;
    
    /**
     * Returns the {@link ResourceClass} object with the specified name.
     *
     * @param name the name.
     * @return the  <code>ResourceClass</code> with the given name.
     * @throws EntityDoesNotExistException if the <code>ResourceClass</code>
     *         with the specified name does not exist.
     */
    public ResourceClass<?> getResourceClass(String name)
        throws EntityDoesNotExistException;

    /**
     * Returns the {@link ResourceClass} object with the specified name.
     *
     * @param name the name.
     * @return the  <code>ResourceClass</code> with the given name.
     * @throws EntityDoesNotExistException if the <code>ResourceClass</code>
     *         with the specified name does not exist.
     */
    public <T extends Resource> ResourceClass<T> getResourceClass(String name, Class<T> rClass)
        throws EntityDoesNotExistException;

    
    /**
     * Creates an resource class.
     *
     * @param name the name of the class
     * @param javaClass the Java type corresponding to this resource class.
     * @param handlerClass the {@link ResourceHandler} implementation that will
     *        manage the instances of that resource class.
     * @param dbTable the name a database table that holds the data of the
     *        resource of that type, or <code>null</code> if not used.
     * @param flags resource class flags.
     * @return a newly created <code>ResourceClass</code>
     * @throws EntityExistsException if an resource class with specified name
     *         already exists.
     * @throws JavaClassException if the <code>javaClass</code> or
     *         <code>handlerClass</code> attributes don't specify valid Java
     *         classes.
     */
    public ResourceClass<?> createResourceClass(String name, String javaClass,
                                             String handlerClass, String dbTable, 
                                             int flags)
        throws EntityExistsException, JavaClassException;

    /**
     * Removes an {@link ResourceClass}.
     *
     * @param resourceClass the {@link ResourceClass}.
     * @throws EntityInUseException if there are any instances of this
     *         <code>ResourceClass</code> in the system.
     */
    public void deleteResourceClass(ResourceClass<?> resourceClass)
        throws EntityInUseException;

    /**
     * Renames an <code>ResourceClass</code>.
     *
     * @param resourceClass <code>ResourceClass</code> the resource class to rename.
     * @param name the new name.
     * @throws EntityExistsException if a resource class with that name already
     *         exists in the system.
     */
    public void setName(ResourceClass<?> resourceClass, String name)
        throws EntityExistsException;

    /**
     * Changes the flags of the resource class.
     *
     * @param resourceClass the resource class to modify.
     * @param flags the new value of flags.
     */
    public void setFlags(ResourceClass<?> resourceClass, int flags);

    /**
     * Sets the Java class corresponding to an <code>ResourceClass</code>.
     *
     * @param resourceClass the {@link ResourceClass}.
     * @param javaClass the Java class corresponding to an
     *        <code>AttributClass</code> 
     * @throws JavaClassException if the <code>javaClass</code> does not specify
     *         a valid JavaClass.
     */
    public void setJavaClass(ResourceClass<?> resourceClass, String javaClass)
        throws JavaClassException;

    /**
     * Sets the {@link ResourceHandler} implementation that will manage the
     * resources of that class. 
     *
     * @param resourceClass the {@link ResourceClass}.
     * @param handlerClass the {@link ResourceHandler} implementation that will
     *        manage the resources of that class.
     * @throws JavaClassException if the <code>handlerClass</code> does not
     *         specify a valid JavaClass.
     */
    public void setHandlerClass(ResourceClass<?> resourceClass, 
                                String handlerClass)
        throws JavaClassException;

    /**
     * Sets the database table that will hold the data for the resource of
     * that class. 
     *
     * @param resourceClass the {@link ResourceClass}.
     * @param dbTable the database table that will hold the data for the
     *        resources of that class (allows sharing handler classes betweeen
     *        resource types).
     */
    public void setDbTable(ResourceClass<?> resourceClass, String dbTable);

    /**
     * Adds an attribute to a {@link ResourceClass}.
     *
     * @param resourceClass the <code>ResourceClass</code> to modify.
     * @param attribute the attribute to add.
     * @param value initial value of the attribute that should be set for
     *        existing resources of this class. May be <code>null</code> for
     *        non-REQUIRED attributes.
     * @throws SchemaIntegrityException if performing the operation would
     *         introduce schema incositencies.
     * @throws ValueRequiredException if <code>null</code> value was provided
     *         for a REQUIRED attribute.
     */
    public <T> void addAttribute(ResourceClass<?> resourceClass, AttributeDefinition<T> attribute,
        T value)
        throws SchemaIntegrityException, ValueRequiredException;
    
    /**
     * Creats a child--parent relationship between two
     * resource classes.
     *
     * @param child the child <code>ResourceClass</code>.
     * @param parent the parent <code>ResourceClass</code>.
     * @param attributes initial values of the parent class' attributes that
     *        should be set for existing instances of the child class. Values
     *        are keyed with {@link AttributeDefinition} objects, not
     *        attribute names! If no initial values are set, this parameter
     *        may be <code>null</code>.
     * @throws CircularDependencyException if the <code>child</code> is
     *         actually a parent of <code>parent</code>
     * @throws SchemaIntegrityException if performing the operation would
     *         introduce schema incositencies.
     * @throws ValueRequiredException if values for any of parent class
     *         REQUIRED attributes are missing from <code>attributes</code>
     *         map. 
     */
    public void addParentClass(ResourceClass<?> child, ResourceClass<?> parent, 
        Map<AttributeDefinition<?>, ?> attributes)
        throws CircularDependencyException, SchemaIntegrityException, 
               ValueRequiredException;

    /**
     * Removes a child--parent relationship between two
     * resource classes.
     *
     * @param child the child <code>ResourceClass</code>.
     * @param parent the parent <code>ResourceClass</code>.
     * @throws IllegalArgumentException if <code>parent</code> is not really a
     *         parent of the <code>child</code>.
     */
    public void deleteParentClass(ResourceClass<?> child, ResourceClass<?> parent)
        throws IllegalArgumentException;
    
    /**
     * Provides an EntityFactory for AttributeClasses.
     * 
     * @return  an EntityFactory for AttributeClasses.
     */
    public EntityFactory<AttributeClass<?>> getAttributeClassFactory();

    /**
     * Provides an EntityFactory for AttributeDefinitions.
     * 
     * @return  an EntityFactory for AttributeDefinitions.
     */
    public EntityFactory<AttributeDefinition<?>> getAttributeDefinitionFactory();
    

    /**
     * Provides an EntityFactory for AttributeClasses.
     * 
     * @return  an EntityFactory for AttributeClasses.
     */
    public EntityFactory<ResourceClass<?>> getResourceClassFactory();
} 
