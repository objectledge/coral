package org.objectledge.coral.schema;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.objectledge.coral.BackendException;
import org.objectledge.coral.InstantiationException;
import org.objectledge.coral.Instantiator;
import org.objectledge.coral.entity.AbstractEntity;
import org.objectledge.coral.event.AttributeClassChangeListener;
import org.objectledge.coral.event.CoralEventHub;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.OutputRecord;
import org.objectledge.database.persistence.Persistence;

/**
 * Represents an attribute type.
 *
 * @version $Id: AttributeClassImpl.java,v 1.11 2004-03-16 13:33:35 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class AttributeClassImpl<A>
    extends AbstractEntity
    implements AttributeClass<A>,
               AttributeClassChangeListener
{
    // Instance variables ///////////////////////////////////////////////////////////////////////
    
    /** Instantiator. */
    private Instantiator instantiator;
    
    /** The CoralEventHub. */
    private CoralEventHub coralEventHub;
    
    /** name of the Java class. */
    private String javaClassName;
    
    /** The associated Java class. */
    private Class<A> javaClass;

    /** name of the handler class. */
    private String handlerClassName;
    
    /** The associated {@link org.objectledge.coral.schema.AttributeHandler}. */
    private AttributeHandler<A> handler;

    /** The associated database table. */
    private String dbTable;
    
    // Initialization ///////////////////////////////////////////////////////////////////////////

    /**
     * Constructs a {@link AttributeClassImpl}.
     *
     * @param persistence the Peristence subsystem.
     * @param instantiator the Instantiator.
     * @param coralEventHub the CoralEventHub.
     */
    public AttributeClassImpl(Persistence persistence, Instantiator instantiator, 
        CoralEventHub coralEventHub)
    {
        super(persistence);
        this.instantiator = instantiator;
        this.coralEventHub = coralEventHub;
    }

    /**
     * Constructs a {@link AttributeClassImpl}.
     *
     * @param persistence the Peristence subsystem.
     * @param instantiator the Instantiator.
     * @param coralEventHub the CoralEventHub.
     * 
     * @param name the name of the attribute class.
     * @param javaClass the name of the Java class associated with this
     *        attribute class.
     * @param handlerClass the name of the AttributeHandler implementation
     *        responsible for this attribute class.
     * @param dbTable the name of the database table that holds the data of
     *        attributes belonging to this class.
     */
    AttributeClassImpl(Persistence persistence, Instantiator instantiator, 
        CoralEventHub coralEventHub, 
        String name, String javaClass, String handlerClass, String dbTable)
        throws JavaClassException
    {
        super(persistence, name);
        this.instantiator = instantiator;
        this.coralEventHub = coralEventHub;
        setDbTable(dbTable);
        setJavaClass(javaClass);
        setHandlerClass(handlerClass);
        coralEventHub.getInbound().addAttributeClassChangeListener(this, this);
    }

    // Persistent interface /////////////////////////////////////////////////////////////////////

    /** The key columns. */
    private static final String[] KEY_COLUMNS = { "attribute_class_id" };

    /**
     * Returns the name of the table this type is mapped to.
     *
     * @return the name of the table.
     */
    public String getTable()
    {
        return "coral_attribute_class";
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
        record.setString("db_table_name", dbTable);
        record.setString("java_class_name", javaClassName);
        record.setString("handler_class_name", handlerClassName);
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
        setDbTable(record.getString("db_table_name"));
        try
        {
            setJavaClass(record.getString("java_class_name"));
            setHandlerClass(record.getString("handler_class_name"));
        }
        catch(JavaClassException e)
        {
            throw new BackendException("Failed to load AttributeClass #"+id, e);
        }
        coralEventHub.getInbound().addAttributeClassChangeListener(this, this);
    }
    
    // AttributeClassChangeListener interface ///////////////////////////////////////////////////

    /**
     * Called when <code>AttributeClass</code>'s data change.
     *
     * @param attributeClass the attributeClass that changed.
     */
    public void attributeClassChanged(AttributeClass<?> attributeClass)
    {
        if(this.equals(attributeClass))
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

    // AttributeClass interface /////////////////////////////////////////////////////////////////

    /**
     * Returns the Java class that is associated with this resource attribute
     * type.
     *
     * @return the Java class that is associated with this resource attribute
     * type.
     */
    public Class<A> getJavaClass()
    {
        return javaClass;
    }

    /**
     * Returns the AttributeHandler implementaion that will manage the
     * attributes of that class.
     *
     * @return an <code>AttributeHandler</code> implementation.
     */
    public AttributeHandler<A> getHandler()
    {
        return handler;
    }

    /**
     * Return the name of a database table that holds the values of the
     * attributes of that type.
     *
     * @return the name of a database table that holds the values of the
     * attributes of that type.
     */
    public String getDbTable()
    {
        return dbTable;
    }
    
    // Package private setter methods ///////////////////////////////////////////////////////////

    /**
     * Sets the Java class that is associated with this resource attribute
     * type.
     *
     * @param className the Java class that is associated with this resource
     * attribute type.
     */
    void setJavaClass(String className)
        throws JavaClassException
    {
        javaClassName = className;
        try
        {
            javaClass = (Class<A>)instantiator.loadClass(className);
        }
        catch(ClassNotFoundException e)
        {
            throw new JavaClassException(e.getMessage(), e);
        }
    }

    /**
     * Sets the AttributeHandler implementaion that will manage the attributes
     * of that class. 
     *
     * @param className the name of an <code>AttributeHandler</code>
     * implementation. 
     */
    void setHandlerClass(String className)
        throws JavaClassException
    {
        handlerClassName = className;
        try
        {
            Class<AttributeHandler<A>> handlerClass = (Class<AttributeHandler<A>>)instantiator
                .loadClass(className);
            Map<Object, Object> additional = new HashMap<Object, Object>();
            additional.put(AttributeClass.class, this);
            handler = (AttributeHandler<A>)instantiator.newInstance(handlerClass, additional);
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
                                         "AttributeHandler interface", e);
        }
    }

    /**
     * Sets the name of a database table that holds the values of the
     * attributes of that type.
     *
     * @param dbTable the name of a database table that holds the values of the
     * attributes of that type.
     */
    void setDbTable(String dbTable)
    {
        this.dbTable = dbTable;
    }
}
