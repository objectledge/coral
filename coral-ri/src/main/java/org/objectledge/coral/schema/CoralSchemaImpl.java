package org.objectledge.coral.schema;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import org.jcontainer.dna.Logger;
import org.objectledge.coral.BackendException;
import org.objectledge.coral.CoralCore;
import org.objectledge.coral.Instantiator;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.entity.EntityExistsException;
import org.objectledge.coral.entity.EntityInUseException;
import org.objectledge.coral.event.CoralEventHub;
import org.objectledge.coral.store.ValueRequiredException;
import org.objectledge.database.Database;
import org.objectledge.database.DatabaseUtils;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.PersistenceException;
import org.objectledge.database.persistence.Persistent;

/**
 * Manages {@link ResourceClass}es and their associated entities.
 *
 * @version $Id: CoralSchemaImpl.java,v 1.5 2004-03-05 08:24:26 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class CoralSchemaImpl
    implements CoralSchema
{
    // Instance variables ////////////////////////////////////////////////////////////////////////

    private Database database;

    private Persistence persistence;
    
    private Instantiator instantiator;

    private CoralCore coral;
    
    private CoralEventHub coralEventHub;
    
    private Logger log;

    // Initialization ////////////////////////////////////////////////////////////////////////////

    /**
     * Constructs the {@link SchemaService} implementation.
     * 
     * @param database the database.
     * @param persistence the persistence subsystem.
     * @param instantiator the instantiator.
     * @param coral the component hub.
     * @param coralEventHub the event hub.
     * @param log the logger.
     */
    public CoralSchemaImpl(Database database, Persistence persistence, Instantiator instantiator,
        CoralCore coral, CoralEventHub coralEventHub, Logger log)
    {
        this.database = database;
        this.persistence = persistence;
        this.instantiator = instantiator;
        this.coral= coral;
        this.coralEventHub = coralEventHub;
        this.log = log;
    }

    // Attribute classes /////////////////////////////////////////////////////

    /**
     * Returns all {@link AttributeClass}es defined in the system.
     *
     * @return all {@link AttributeClass}es defined in the system.
     */
    public AttributeClass[] getAttributeClass()
    {
        return coral.getRegistry().getAttributeClass();
    }

    /**
     * Returns the {@link AttributeClass} with a specific identifier.
     *
     * @param id the identifier.
     * @return the <code>AttributeClass</code>.
     * @throws EntityDoesNotExistException if the <code>AttributeClass</code>
     *         with the specified identifier does not exist.
     */
    public AttributeClass getAttributeClass(long id)
        throws EntityDoesNotExistException
    {
        return coral.getRegistry().getAttributeClass(id);
    }
    
    /**
     * Returns the attribute class with the specified name.
     *
     * @param name the name.
     * @return the <code>AttributeClass</code> with the given name.
     * @throws EntityDoesNotExistException if the <code>AttributeClass</code>
     *         with the specified name does not exist.
     */
    public AttributeClass getAttributeClass(String name)
        throws EntityDoesNotExistException
    {
        return coral.getRegistry().getAttributeClass(name);
    }

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
    public AttributeClass createAttributeClass(String name, String javaClass, 
                                               String handlerClass, String dbTable)
        throws EntityExistsException, JavaClassException
    {
        AttributeClass attributeClass = new AttributeClassImpl(persistence, instantiator, 
            coralEventHub, 
            name, javaClass, handlerClass, dbTable);
        coral.getRegistry().addAttributeClass(attributeClass);
        return attributeClass;
    }

    /**
     * Removes an {@link AttributeClass}.
     *
     * @param attributeClass the {@link AttributeClass}.
     * @throws EntityInUseException if there is a {@link ResourceClass} that
     *         uses the specified <code>AttributeClass</code>.
     */
    public void deleteAttributeClass(AttributeClass attributeClass)
        throws EntityInUseException
    {
        coral.getRegistry().deleteAttributeClass(attributeClass);
    }

    /**
     * Renames an attributeClass.
     *
     * @param attributeClass the attribute class to rename.
     * @param name the new name.
     * @throws EntityExistsException if another attribute class with that name exists.
     */
    public void setName(AttributeClass attributeClass, String name)
        throws EntityExistsException
    {
        coral.getRegistry().renameAttributeClass(attributeClass, name);
        coralEventHub.getOutbound().fireAttributeClassChangeEvent(attributeClass);
    }

    /**
     * Sets the Java class corresponding to an <code>AttributeClass</code>
     *
     * @param attributeClass the {@link AttributeClass}.
     * @param javaClass the Java class corresponding to an
     *        <code>AttributClass</code> 
     * @throws JavaClassException if the <code>javaClass</code> does not specify
     *         a valid JavaClass.
     */
    public void setJavaClass(AttributeClass attributeClass, String javaClass)
        throws JavaClassException
    {
        ((AttributeClassImpl)attributeClass).setJavaClass(javaClass);
        try
        {
            persistence.save((Persistent)attributeClass);
        }
        catch(PersistenceException e)
        {
            throw new BackendException("Failed to update AttributeClass", e);
        }
        coralEventHub.getOutbound().fireAttributeClassChangeEvent(attributeClass);
    }

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
    public void setHandlerClass(AttributeClass attributeClass, 
                                String handlerClass)
        throws JavaClassException
    {
        ((AttributeClassImpl)attributeClass).setHandlerClass(handlerClass);
        try
        {
            persistence.save((Persistent)attributeClass);
        }
        catch(PersistenceException e)
        {
            throw new BackendException("Failed to update AttributeClass", e);
        }
        coralEventHub.getOutbound().fireAttributeClassChangeEvent(attributeClass);
    }
    
    /**
     * Sets the database table that will hold the data for the attributes of
     * that class. 
     *
     * @param attributeClass the {@link AttributeClass}.
     * @param dbTable the database table that will hold the data for the
     *        attributes of that class (allows sharing handler classes betweeen
     *        attribute types).
     */
    public void setDbTable(AttributeClass attributeClass, String dbTable)
    {
        ((AttributeClassImpl)attributeClass).setDbTable(dbTable);
        try
        {
            persistence.save((Persistent)attributeClass);
        }
        catch(PersistenceException e)
        {
            throw new BackendException("Failed to update AttributeClass", e);
        }
        coralEventHub.getOutbound().fireAttributeClassChangeEvent(attributeClass);
    }

    // Attribute instances ///////////////////////////////////////////////////

    /**
     * Returns all attributes defined by classes in the system.
     * 
     * @return all attributes defined by classes in the system.
     */
    public AttributeDefinition[] getAttribute()
    {
        return coral.getRegistry().getAttributeDefinition();
    }

    /**
     * Returns an attribute definition with the specified id.
     * 
     * @param id attribute definition identifier.
     * @return the attribute definition.
     * @throws EntityDoesNotExistException if no such attribute definition exists.
     */
    public AttributeDefinition getAttribute(long id)
        throws EntityDoesNotExistException
    {
        return coral.getRegistry().getAttributeDefinition(id);
    }

    /**
     * Creates an attribute instance.
     *
     * @param name the name of the new Attribute.
     * @param attributeClass the class of the new attribute.
     * @param domain the value domain constraint.
     * @param flags the flags of the new Attribute.
     * @return a new attribute instance.
     */
    public AttributeDefinition createAttribute(String name, AttributeClass attributeClass,
                                               String domain, int flags)
    {
        // check domain specified for well-formedness
        attributeClass.getHandler().checkDomain(domain);
        return new AttributeDefinitionImpl(persistence, coralEventHub, this, 
            name, attributeClass, domain, flags);
    }

    /**
     * Renames an attribute.
     *
     * @param attribute the attribute to rename.
     * @param name the new name.
     * @throws SchemaIntegrityException if the <code>ResourceClass</code> already
     *         has an attribute with the specified name.
     */
    public void setName(AttributeDefinition attribute, String name)
        throws SchemaIntegrityException
    {
        if(attribute.getDeclaringClass() != null)
        {
            checkName(attribute.getDeclaringClass(), name);
        }
        String oldName = attribute.getName();
        try
        {
            coral.getRegistry().renameAttributeDefinition(attribute, name);
            coralEventHub.getOutbound().fireAttributeDefinitionChangeEvent(attribute);
        }
        catch(Exception e)
        {
            ((AttributeDefinitionImpl)attribute).setName(oldName);
            throw new BackendException("failed to update attribute's persitent image", e);
        }
    }

    /**
     * Changes the domain of the attribute.
     *
     * TODO check if existing attribute values meet the requested domain constraint.
     *
     * @param attribute the attribute to modify.
     * @param domain the new domain of the attirbute.
     */
    public void setDomain(AttributeDefinition attribute, String domain)
    {
        // check domain specified for well-formedness
        attribute.getAttributeClass().getHandler().checkDomain(domain);
        ((AttributeDefinitionImpl)attribute).setDomain(domain);
        try
        {
            persistence.save((Persistent)attribute);
        }
        catch(PersistenceException e)
        {
            throw new BackendException("Failed to update Attribute", e);
        }
        coralEventHub.getOutbound().fireAttributeDefinitionChangeEvent(attribute);
    }

    /**
     * Changes the flags of the attribute.
     *
     * @param attribute the attribute to modify.
     * @param flags the new value of flags.
     */
    public void setFlags(AttributeDefinition attribute, int flags)
    {
        ((AttributeDefinitionImpl)attribute).setFlags(flags);
        try
        {
            persistence.save((Persistent)attribute);
        }
        catch(PersistenceException e)
        {
            throw new BackendException("Failed to update Attribute", e);
        }
        coralEventHub.getOutbound().fireAttributeDefinitionChangeEvent(attribute);
    }
                        
    // Resource classes //////////////////////////////////////////////////////

    /**
     * Returns all {@link ResourceClass}es defined in the system.
     *
     * @return all {@link ResourceClass}es defined in the system.
     */
    public ResourceClass[] getResourceClass()
    {
        return coral.getRegistry().getResourceClass();
    }

    /**
     * Returns the {@link ResourceClass} with a specific identifier.
     *
     * @param id the identifier.
     * @return the <code>ResourceClass</code>.
     * @throws EntityDoesNotExistException if the <code>ResourceClass</code>
     *         with the specified identifier does not exist.
     */
    public ResourceClass getResourceClass(long id)
        throws EntityDoesNotExistException
    {
        return coral.getRegistry().getResourceClass(id);
    }
    
    /**
     * Returns the {@link ResourceClass} object with the specified name.
     *
     * @param name the name.
     * @return the <code>ResourceClass</code> with the given name.
     * @throws EntityDoesNotExistException if the <code>ResourceClass</code>
     *         with the specified name does not exist.
     */
    public ResourceClass getResourceClass(String name)
        throws EntityDoesNotExistException
    {
        return coral.getRegistry().getResourceClass(name);
    }

    /**
     * Creates an resource class.
     *
     * @param name the name of the class.
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
    public ResourceClass createResourceClass(String name, String javaClass,
                                             String handlerClass, String dbTable, 
                                             int flags)
        throws EntityExistsException, JavaClassException
    {
        ResourceClass resourceClass = new ResourceClassImpl(persistence, instantiator, 
            coralEventHub, coral.getRegistry(), 
            name, javaClass, handlerClass, dbTable, flags);
        coral.getRegistry().addResourceClass(resourceClass);
        return resourceClass;
    }

    /**
     * Removes an {@link ResourceClass}.
     *
     * @param resourceClass the {@link ResourceClass}.
     * @throws EntityInUseException if there are any instances of this
     *         <code>ResourceClass</code> in the system.
     */
    public void deleteResourceClass(ResourceClass resourceClass)
        throws EntityInUseException
    {
        coral.getRegistry().deleteResourceClass(resourceClass);
    }

    /**
     * Renames an <code>ResourceClass</code>.
     *
     * @param resourceClass the resource class to rename.
     * @param name the new name.
     * @throws EntityExistsException if another resource class with this name exists.
     */
    public void setName(ResourceClass resourceClass, String name)
        throws EntityExistsException
    {
        coral.getRegistry().renameResourceClass(resourceClass, name);
        coralEventHub.getOutbound().fireResourceClassChangeEvent(resourceClass);
    }

    /**
     * Sets the Java class corresponding to an <code>ResourceClass</code>
     *
     * @param resourceClass the {@link ResourceClass}.
     * @param javaClass the Java class corresponding to an
     *        <code>AttributClass</code> 
     * @throws JavaClassException if the <code>javaClass</code> does not specify
     *         a valid JavaClass.
     */
    public void setJavaClass(ResourceClass resourceClass, String javaClass)
        throws JavaClassException
    {
        ((ResourceClassImpl)resourceClass).setJavaClass(javaClass);
        try
        {
            persistence.save((Persistent)resourceClass);
        }
        catch(PersistenceException e)
        {
            throw new BackendException("Failed to save ResourceClass", e);
        }
        coralEventHub.getOutbound().fireResourceClassChangeEvent(resourceClass);
    }

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
    public void setHandlerClass(ResourceClass resourceClass, 
                                String handlerClass)
        throws JavaClassException
    {
        ((ResourceClassImpl)resourceClass).setHandlerClass(handlerClass);
        try
        {
            persistence.save((Persistent)resourceClass);
        }
        catch(PersistenceException e)
        {
            throw new BackendException("Failed to save ResourceClass", e);
        }
        coralEventHub.getOutbound().fireResourceClassChangeEvent(resourceClass);
    }

    /**
     * Changes the flags of the resource class.
     *
     * @param resourceClass the resource class to modify.
     * @param flags the new value of flags.
     */
    public void setFlags(ResourceClass resourceClass, int flags)
    {
        ((ResourceClassImpl)resourceClass).setFlags(flags);
        try
        {
            persistence.save((Persistent)resourceClass);
        }
        catch(PersistenceException e)
        {
            throw new BackendException("Failed to update resource class", e);
        }
        coralEventHub.getOutbound().fireResourceClassChangeEvent(resourceClass);
    }

    /**
     * Adds an attribute to a {@link ResourceClass}.
     *
     * @param resourceClass the <code>ResourceClass</code> to modify.
     * @param attribute the attribute to add.
     * @param value the initial value.
     * @throws SchemaIntegrityException if performing the operation would
     *         introduce schema incositencies.
     * @throws ValueRequiredException if the attribute has REQUIRED flag set, but no initial value
     *         was given.
     */
    public void addAttribute(ResourceClass resourceClass, 
                             AttributeDefinition attribute, Object value)
        throws SchemaIntegrityException, ValueRequiredException
    {
        checkName(resourceClass, attribute.getName());
        Connection conn = null;
        boolean shouldCommit = false;
        try
        {
            shouldCommit = database.beginTransaction();
            conn = database.getConnection();
            ((AttributeDefinitionImpl)attribute).setDeclaringClass(resourceClass);
            coral.getRegistry().addAttributeDefinition(attribute);
            coralEventHub.getLocal().fireResourceClassAttributesChangeEvent(attribute, true);
            resourceClass.getHandler().addAttribute(attribute, value, conn);
            database.commitTransaction(shouldCommit);
            coralEventHub.getOutbound().fireResourceClassAttributesChangeEvent(attribute, true);
        }
        catch(SQLException e)
        {
            coralEventHub.getLocal().fireResourceClassAttributesChangeEvent(attribute, false);
            ((AttributeDefinitionImpl)attribute).setDeclaringClass(null);
            try
            {
                database.rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            throw new BackendException("failed to add attribute", e);
        }
        finally
        {
            DatabaseUtils.close(conn);
        }
    }
    
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
    public void deleteAttribute(ResourceClass resourceClass, AttributeDefinition attribute)
        throws IllegalArgumentException
    {
        if(!resourceClass.equals(attribute.getDeclaringClass()))
        {
            throw new IllegalArgumentException(resourceClass.getName()+" does not declare "+
                                               attribute.getName()+" attribute");
        }
        Connection conn = null;
        boolean shouldCommit = false;
        try
        {
            conn = database.getConnection();
            shouldCommit = database.beginTransaction();
            coralEventHub.getLocal().fireResourceClassAttributesChangeEvent(attribute, false);
            attribute.getDeclaringClass().getHandler().
                deleteAttribute(attribute, conn);
            coral.getRegistry().deleteAttributeDefinition(attribute);
            database.commitTransaction(shouldCommit);
            coralEventHub.getOutbound().fireResourceClassAttributesChangeEvent(attribute, false);
        }
        catch(BackendException ex)
        {
            coralEventHub.getLocal().fireResourceClassAttributesChangeEvent(attribute, true);
            try
            {
                database.rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            throw ex;
        }
        catch(Exception e)
        {
            coralEventHub.getLocal().fireResourceClassAttributesChangeEvent(attribute, true);
            try
            {
                database.rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            throw new BackendException("failed to remove attribute values from existing resources",
                 e);
        }
        finally
        {
            DatabaseUtils.close(conn);
        }
    }

    /**
     * Creats a child--parent relationship between two
     * resource classes.
     *
     * @param child the child <code>ResourceClass</code>.
     * @param parent the parent <code>ResourceClass</code>.
     * @param attributes initial values of parent class attributes.
     * 
     * @throws CircularDependencyException if the <code>child</code> is
     *         actually a parent of <code>parent</code>
     * @throws SchemaIntegrityException if performing the operation would
     *         introduce schema incositencies.
     * @throws ValueRequiredException if <code>null</code> value was provided
     *         for a REQUIRED attribute.
     */
    public void addParentClass(ResourceClass child, ResourceClass parent, Map attributes)
        throws CircularDependencyException, SchemaIntegrityException, ValueRequiredException
    {
        if(parent.isParent(child))
        {
            throw new IllegalArgumentException("resource class"+parent.getName()+" is already "+
                "a parent of "+child.getClass());
        }
        if(child.isParent(parent))
        {
            throw new CircularDependencyException("resource class "+child.getName()+
                " is a parent of class "+parent.getName());
        }
        if((parent.getFlags() & ResourceClassFlags.FINAL) != 0)
        {
            throw new IllegalArgumentException("cannot extend FINAL class "+parent.getName());
        }
        AttributeDefinition[] attrs = parent.getAllAttributes();
        for(int i=0; i<attrs.length; i++)
        {
            String name = attrs[i].getName();
            checkName(child, name);
            ResourceClass[] ra = child.getParentClasses();
            for(int j=0; j<ra.length; j++)
            {
                checkName(ra[j], name);
            }
            ra = child.getChildClasses();
            for(int j=0; j<ra.length; j++)
            {
                checkName(ra[j], name);
            }
        }
        Connection conn = null;
        boolean shouldCommit = false;
        ResourceClassInheritance relationship = null;
        try
        {
            conn = database.getConnection();
            shouldCommit = database.beginTransaction();
            relationship = new ResourceClassInheritanceImpl(this,
                parent, child);
            coral.getRegistry().addResourceClassInheritance(relationship);
            coralEventHub.getLocal().fireResourceClassInheritanceChangeEvent(relationship, true);
            child.getHandler().addParentClass(parent, attributes, conn);
            database.commitTransaction(shouldCommit);
            coralEventHub.getOutbound().fireResourceClassInheritanceChangeEvent(relationship, true);
        }
        catch(BackendException ex)
        {
            coralEventHub.getLocal().fireResourceClassInheritanceChangeEvent(relationship, false);
            try
            {
                database.rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            throw ex;
        }
        catch(Exception e)
        {
            coralEventHub.getLocal().fireResourceClassInheritanceChangeEvent(relationship, false);
            try
            {
                database.rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            throw new BackendException("failed to set initial attribute values for existing "+
                "resources", e);
        }
        finally
        {
            DatabaseUtils.close(conn);
        }
    }

    /**
     * Removes a child--parent relationship between two
     * resource classes.
     *
     * @param child the child <code>ResourceClass</code>.
     * @param parent the parent <code>ResourceClass</code>.
     * @throws IllegalArgumentException if <code>parent</code> is not really a
     *         parent of the <code>child</code>.
     */
    public void deleteParentClass(ResourceClass child, ResourceClass parent)
        throws IllegalArgumentException
    {
        if(!parent.isParent(child))
        {
            throw new IllegalArgumentException("resource class "+child.getName()+
                                               " is not a child of "+
                                               " class "+parent.getName());
        }
        Connection conn = null;
        boolean shouldCommit = false;
        ResourceClassInheritance relationship = null;
        try
        {
            conn = database.getConnection();
            shouldCommit = database.beginTransaction();
            relationship = new ResourceClassInheritanceImpl(this, 
                parent, child);
            coralEventHub.getLocal().fireResourceClassInheritanceChangeEvent(relationship, false);
            child.getHandler().deleteParentClass(parent, conn);
            coral.getRegistry().deleteResourceClassInheritance(relationship);
            database.commitTransaction(shouldCommit);
            coralEventHub.getOutbound().fireResourceClassInheritanceChangeEvent(relationship, 
                false);
        }
        catch(BackendException ex)
        {
            coralEventHub.getLocal().fireResourceClassInheritanceChangeEvent(relationship, true);
            try
            {
                database.rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            throw ex;
        }
        catch(Exception e)
        {
            coralEventHub.getLocal().fireResourceClassInheritanceChangeEvent(relationship, true);
            try
            {
                database.rollbackTransaction(shouldCommit);
            }
            catch(SQLException ee)
            {
                log.error("rollback failed", ee);
            }
            throw new BackendException("failed to remove attribute values from existing resources",
                e);
        }
        finally
        {
            DatabaseUtils.close(conn);
        }
    }

    // private ///////////////////////////////////////////////////////////////

    private void checkName(ResourceClass rClass, String name)
        throws SchemaIntegrityException
    {
        AttributeDefinition attr;
        if(rClass.hasAttribute(name))
        {
            attr = rClass.getAttribute(name);
            if(attr.getDeclaringClass().equals(rClass))
            {
                throw new SchemaIntegrityException("resource class "+rClass.getName()+
                                                " already declares attribute named "+
                                                name);
            }
            else
            {
                throw new SchemaIntegrityException("resource class "+rClass.getName()+
                                                " already inherits attribute named "+
                                                name+" from class "+
                                                attr.getDeclaringClass().getName());
            }
        }
        ResourceClass[] children = rClass.getChildClasses();
        for(int i=0; i<children.length; i++)
        {
            ResourceClass child = children[i];
            if(rClass.hasAttribute(name))
            {
                attr = rClass.getAttribute(name);
                if(attr.getDeclaringClass().equals(children))
                {
                    throw new SchemaIntegrityException("resource class "+child.getName()+
                                                    " which is a descendant of "+rClass.getName()+
                                                    " already declares attribute named "+
                                                    name);
                }
                else
                {
                    throw new SchemaIntegrityException("resource class "+child.getName()+
                                                    " which is a descendant of "+rClass.getName()+
                                                    " already inherits attribute named "+
                                                    name+" from class "+
                                                    attr.getDeclaringClass().getName());
                }
            }
        }
    }
} 
