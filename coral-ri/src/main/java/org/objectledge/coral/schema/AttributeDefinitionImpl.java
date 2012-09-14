package org.objectledge.coral.schema;

import java.sql.SQLException;

import org.objectledge.coral.BackendException;
import org.objectledge.coral.CoralCore;
import org.objectledge.coral.entity.AbstractEntity;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.event.AttributeDefinitionChangeListener;
import org.objectledge.coral.event.CoralEventHub;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.OutputRecord;
import org.objectledge.database.persistence.Persistence;

/**
 * Represents a concrete attribute of an resource class.
 *
 * @version $Id: AttributeDefinitionImpl.java,v 1.14 2004-12-23 02:22:36 rafal Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class AttributeDefinitionImpl<T>
    extends AbstractEntity
    implements AttributeDefinition<T>,
               AttributeDefinitionChangeListener
{
    // Instance variables ///////////////////////////////////////////////////////////////////////

    /** The CoralEventHub. */
    private CoralEventHub coralEventHub;
    
    /** The component hub. */
    private CoralCore coral;

    /** The class of this attribute. */
    private AttributeClass<T> attributeClass;
    
    /** The resource class that declares this attribute. */
    private ResourceClass<?> declaringClass;

    /** The value domain constraint. */
    private String domain;
    
    /** The flags of this attribute. */
    private int flags;
    
    // Initialization ///////////////////////////////////////////////////////////////////////////

    /**
     * Constructs a {@link AttributeDefinitionImpl}.
     *
     * @param persistence the Peristence subsystem.
     * @param coralEventHub the event hub.
     * @param coral the component hub.
     */
    public AttributeDefinitionImpl(Persistence persistence, CoralEventHub coralEventHub, 
        CoralCore coral)
    {
        super(persistence);
        this.coralEventHub = coralEventHub;
        this.coral = coral;
    }

    /**
     * Constructs a {@link AttributeDefinitionImpl}.
     *
     * @param persistence the Peristence subsystem.
     * @param coralEventHub the CoralEventHub.
     * @param coral the component hub,
     * 
     * @param name the name of this attribute.
     * @param attributeClass the class of this attribute.
     * @param domain the value domain constraint.
     * @param flags the flags of this attribute.
     */
    public AttributeDefinitionImpl(Persistence persistence, CoralEventHub coralEventHub,
        CoralCore coral, 
        String name, AttributeClass<T> attributeClass, String domain, int flags)
    {
        super(persistence, name);
        this.coralEventHub = coralEventHub;
        this.coral = coral;
        this.attributeClass = attributeClass;
        this.declaringClass = null;
        this.domain = domain;
        this.flags = flags;
        coralEventHub.getInbound().addAttributeDefinitionChangeListener(this, this);
    }
    
    // Persistent interface /////////////////////////////////////////////////////////////////////

    /** The key columns */
    private static final String[] KEY_COLUMNS = { "attribute_definition_id" };    

    /**
     * Returns the name of the table this type is mapped to.
     *
     * @return the name of the table.
     */
    public String getTable()
    {
        return "coral_attribute_definition";
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
        record.setLong("attribute_class_id", attributeClass.getId());
        record.setLong("resource_class_id", declaringClass.getId());
        if(domain != null)
        {
            record.setString("domain", domain);
        }
        else
        {
            record.setNull("domain");
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
        long attributeClassId = record.getLong("attribute_class_id");
        try
        {
            AttributeClass<T> attributeClass = (AttributeClass<T>)coral.getSchema()
                .getAttributeClass(attributeClassId);
            this.attributeClass = attributeClass;
        }
        catch(EntityDoesNotExistException e)
        {
            throw new SQLException("Failed to load AttributeDefinition #" + id, e);
        }
        long declaringClassId = record.getLong("resource_class_id");
        try
        {
            this.declaringClass = coral.getSchema().getResourceClass(declaringClassId);
        }
        catch(EntityDoesNotExistException e)
        {
            throw new SQLException("Failed to load AttributeDefinition #" + id, e);
        }
        if(record.isNull("domain"))
        {
            domain = null;
        }
        else
        {
            domain = record.getString("domain");
        }
        this.flags = record.getInteger("flags");
        coralEventHub.getInbound().addAttributeDefinitionChangeListener(this, this);
    }

    // AttributeDefinitionChangeListener interface //////////////////////////////////////////////

    /**
     * Called when <code>AttributeDefinition</code>'s data change.
     *
     * @param attributeDefinition the attributeDefinition that changed.
     */
    public void attributeDefinitionChanged(AttributeDefinition<?> attributeDefinition)
    {
        if(this.equals(attributeDefinition))
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

    // AttributeDefinition interface ////////////////////////////////////////////////////////////

    /**
     * Returns the class of this attribute.
     *
     * @return the class of this attribute.
     */
    public AttributeClass<T> getAttributeClass()
    {
        return attributeClass;
    }

    /**
     * Returns the resource class this attribute belongs to.
     *
     * @return the resource class this attribute belongs to.
     */
    public ResourceClass<?> getDeclaringClass()
    {
        return declaringClass;
    }

    /**
     * Returns the value domain constraint for the attribute.
     *
     * @return the value domain constraint for the attribute.
     */
    public String getDomain()
    {
        return domain;
    }

    /**
     * Returns the flags of this attribute.
     *
     * @return the flags of this attribute.
     */
    public int getFlags()
    {
        return flags;
    }
    
    // Setter methods ///////////////////////////////////////////////////////////////////////////

    /**
     * Sets the resource class this attribute belongs to.
     *
     * @param declaringClass the resource class this attribute belongs to.
     */
    void setDeclaringClass(ResourceClass<?> declaringClass)
    {
        this.declaringClass = declaringClass;
    }

    /**
     * Sets the flags of this attribute.
     *
     * @param flags the flags of this attribute.
     */
    void setFlags(int flags)
    {
        this.flags = flags;
    }
    
    /** 
     * Sets the name of this attribute.
     * 
	 * <p>Needed because AbstractEntityt.setName(String) is not visible in this package.</p>
     *
     * @param name the new name of the attribute.
     */
    void setAttributeName(String name)
    {
        this.name = name;    
    }
    
    /**
     * Sets the domain of this attribute.
     *
     * @param domain the new domain of the attribute. 
     */
    void setDomain(String domain)
    {
        this.domain = domain;
    }
}
