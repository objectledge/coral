package org.objectledge.coral.schema;

import org.objectledge.coral.BackendException;
import org.objectledge.coral.entity.AbstractEntity;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.event.AttributeDefinitionChangeListener;
import org.objectledge.coral.event.CoralEventHub;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.OutputRecord;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.PersistenceException;

/**
 * Represents a concrete attribute of an resource class.
 *
 * @version $Id: AttributeDefinitionImpl.java,v 1.6 2004-02-25 13:07:20 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class AttributeDefinitionImpl
    extends AbstractEntity
    implements AttributeDefinition,
               AttributeDefinitionChangeListener
{
    // Instance variables ///////////////////////////////////////////////////////////////////////

    /** The CoralEventHub. */
    private CoralEventHub coralEventHub;
    
    /** The CoralSchema. */
    private CoralSchema coralSchema;

    /** The class of this attribute. */
    private AttributeClass attributeClass;
    
    /** The resource class that declares this attribute. */
    private ResourceClass declaringClass;

    /** The value domain constraint. */
    private String domain;
    
    /** The flags of this attribute. */
    private int flags;
    
    // Initialization ///////////////////////////////////////////////////////////////////////////

    /**
     * Constructs a {@link AttributeDefinitionImpl}.
     *
     * @param persistence the Peristence subsystem.
     * @param coralEventHub the CoralEventHub.
     */
    AttributeDefinitionImpl(Persistence persistence, CoralEventHub coralEventHub, 
        CoralSchema coralSchema)
    {
        super(persistence);
        this.coralEventHub = coralEventHub;
        this.coralSchema = coralSchema;
    }

    /**
     * Constructs a {@link AttributeDefinitionImpl}.
     *
     * @param persistence the Peristence subsystem.
     * @param coralEventHub the CoralEventHub.
     * 
     * @param name the name of this attribute.
     * @param attributeClass the class of this attribute.
     * @param domain the value domain constraint.
     * @param flags the flags of this attribute.
     */
    AttributeDefinitionImpl(Persistence persistence, CoralEventHub coralEventHub, 
        CoralSchema coralSchema,
        String name, AttributeClass attributeClass, String domain, int flags)
    {
        super(persistence, name);
        this.coralEventHub = coralEventHub;
        this.coralSchema = coralSchema;
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
        return "arl_attribute_definition";
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
        long attributeClassId = record.getLong("attribute_class_id");
        try
        {
            this.attributeClass = coralSchema.getAttributeClass(attributeClassId);
        }
        catch(EntityDoesNotExistException e)
        {
            throw new PersistenceException("Failed to load AttributeDefinition #"+id, e);
        }
        long declaringClassId = record.getLong("resource_class_id");
        try
        {
            this.declaringClass = coralSchema.getResourceClass(declaringClassId);
        }
        catch(EntityDoesNotExistException e)
        {
            throw new PersistenceException("Failed to load AttributeDefinition #"+id, e);
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
    public void attributeDefinitionChanged(AttributeDefinition attributeDefinition)
    {
        if(this.equals(attributeDefinition))
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

    // AttributeDefinition interface ////////////////////////////////////////////////////////////

    /**
     * Returns the class of this attribute.
     *
     * @return the class of this attribute.
     */
    public AttributeClass getAttributeClass()
    {
        return attributeClass;
    }

    /**
     * Returns the resource class this attribute belongs to.
     *
     * @return the resource class this attribute belongs to.
     */
    public ResourceClass getDeclaringClass()
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
    public void setDeclaringClass(ResourceClass declaringClass)
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
}
