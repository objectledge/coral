package org.objectledge.coral.datatypes;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;

import org.objectledge.coral.entity.Entity;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.AttributeHandler;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.security.CoralSecurity;
import org.objectledge.coral.store.ConstraintViolationException;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.Resource;
import org.objectledge.database.Database;
import org.objectledge.utils.StringUtils;

/**
 * An abstract base class for {@link AttributeHandler} implementations.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: AttributeHandlerBase.java,v 1.2 2004-03-09 15:46:49 fil Exp $
 */
public abstract class AttributeHandlerBase
    implements AttributeHandler
{
    // member objects ////////////////////////////////////////////////////////

    /** The database. */
    protected Database database = null;

    /** The store. */
    protected CoralStore coralStore = null;

    /** The schema. */
    protected CoralSchema coralSchema = null;

    /** The security. */
    protected CoralSecurity coralSecurity = null;

    /** The attribute class. */
    protected AttributeClass attributeClass = null;
    
    /** The attribute object type. */
    protected Class attributeType = null;

    /** The attribute value comparator. */
    protected Comparator comparator = null;

    /** <code>true</code> if attribute value comparison is not supported. */
    protected boolean comparatorNotSupported = false;

    /**
     * The base constructor.
     * 
     * @param database the database.
     * @param coralStore the store.
     * @param coralSecurity the security.
     * @param coralSchema the scheam.
     * @param attributeClass the attribute class.
     */
    public AttributeHandlerBase(Database database, CoralStore coralStore,
                                 CoralSecurity coralSecurity, CoralSchema coralSchema,
                                 AttributeClass attributeClass)
    {
        this.attributeClass = attributeClass;
        this.attributeType = attributeClass.getJavaClass();
        this.database = database; 
        this.coralStore = coralStore;
        this.coralSecurity = coralSecurity;
        this.coralSchema = coralSchema;
    }

    /**
     * Removes an existing attribute.
     *
     * <p>This method is implemented here because it is identical for all
     * generic attributes.</p>
     *
     * @param id the identifier of the attribute.
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform
     *        the operation as a part of a JDBC transaction.
     * @throws SQLException in case of database problems. The caller metod
     *         should consider rolling back the whole transaction.
     * @throws EntityDoesNotExistException if the attribute with specified id
     *         does not exist. 
     */
    public void delete(long id, Connection conn)
        throws EntityDoesNotExistException, SQLException
    {
        Statement stmt = conn.createStatement();
        checkExists(id, stmt);
        stmt.execute(
            "DELETE FROM "+getTable()+" WHERE data_key = "+id
        );
    }

    // meta information //////////////////////////////////////////////////////

    /**
     * Provideds a hint to the ResorceHandler class that it should not cache
     * the data value used to create the attribute, but re-retrieve the value
     * from the AttributeHandler and cache that one instead.
     *
     * <p>For all common attribute classes the returned value is
     * <code>false</code>.</p>
     *
     * @return <code>false</code> if the data value used for creating the
     *         resource can be safely cached.
     */
    public boolean shouldRetrieveAfterCreate()
    {
        return false;
    }

    /**
     * Provides information about comparison operations supported by the
     * attribute type.
     *
     * <p>The returned value is a bitwise sum of the CONDITION_*
     * constants.</p>
     * @return information about comparison operations supported by the
     * attribute type.
     */
    public int getSupportedConditions()
    {
        return CONDITION_NONE;
    }

    /**
     * Returns a <code>java.util.Comparator</code> that can be used for
     * comparing attribute values.
     *
     * <p>The returned object's must be written to be safely used by multiple
     * threads at the same time.</p>
     *
     * @returns a <code>Comparator</code> or <code>null</code> if the
     *          attribute class does not have sensible comparison semantics.
     */
    public Comparator getComparator()
    {
        if(comparator == null || comparatorNotSupported)
        {
            comparator = createComparator();
            if(comparator == null)
            {
                comparatorNotSupported = true;
            }
        }
        return comparator;
    }

    /**
     * Retruns <code>true</code> if the {@toExternalString()} is supported for
     * this attribute type.
     *
     * @return Retruns <code>true</code> if the {@toExternalString()} is
     *         supported for this attribute type.
     */
    public boolean supportsExternalString()
    {
        return false;
    }

    // value conversion //////////////////////////////////////////////////////

    /**
     * Returns an attribute represenation of the passed argument.
     *
     * <p>If the passed object is of expeced type, or it's subclass, it is
     * returned unchanged. If it is of type that the AttributeHandler
     * recognizes as a suitable for conversion, the conversion will be
     * performed and the resulting object will be returned. Otherwise,
     * IllegalArgumentException will be thrown.</p>
     * 
     * @param object the object to be checked/converted. 
     * @return a native representation of the object.
     */
    public Object toAttributeValue(Object object)
    {
        Object result = null;
        if(object == null)
        {
            throw new IllegalArgumentException("can't convert null value");
        }
        if(attributeType.isInstance(object))
        {
            result = object;
        }
        if(result == null && object instanceof String)
        {
            result = fromString((String)object);
        }
        if(result == null)
        {
            result = fromObject(object);
        }
        if(result != null)
        {
            return result;
        }
        else
        {
            throw new IllegalArgumentException(
                "The attribute handler class "+getClass().getName()+
                " was unable to provide an representation of "+
                object.getClass().getName()+" as "+
                attributeType.getName());
        }
    }

    /**
     * Converts an attribute value into a human readable string.
     *
     * @param value the value to convert.
     * @return a human readable string.
     */
    public String toPrintableString(Object value)
    {
        checkValue(value);
        if(value instanceof Entity)
        {
            return ((Entity)value).getName();
        }
        else
        {
            return value.toString();
        }   
    }
    
    /**
     * Converts an attribute value into a string representation suitable for
     * using in queries against the underlying data store, like a relational
     * database.
     *
     * @param value the value to convert.
     * @return a string representation suitable for using in queries agains
     *         the underlying data store.
     */
    public String toExternalString(Object value)
    {
        checkValue(value);
        if(value instanceof Entity)
        {
            return Long.toString(((Entity)value).getId());
        }
        else
        {
            throw new UnsupportedOperationException("unable to provide "+
                                                    "an external representation of "+
                                                    attributeType.getName());
        }   
    }

    // value domain //////////////////////////////////////////////////////////

    /**
     * Check if the domain constraint is well formed.
     *
     * @param domain value domain constraint.
     */
    public void checkDomain(String domain)
    {
        if(domain != null)
        {
            throw new IllegalArgumentException(attributeClass.getName()+" does not support"+
                                               " domain constraints ");
        }
    }
    
    /**
     * Check if an attribute value fullfills a domain constraint.
     *
     * @param domain value domain constraint.
     * @param value an attribute value.
     * @throws ConstraintViolationExcepion if the value does not fulfill the constraint.
     */
    public void checkDomain(String domain, Object value)
        throws ConstraintViolationException
    {
        if(domain != null)
        {
            throw new IllegalArgumentException(attributeClass.getName()+" does not support "+
                                               " domain constaraints ");
        }        
        if(!attributeType.isInstance(value))
        {
            throw new IllegalArgumentException("value is "+value.getClass().getName()+
                                               ", "+attributeType.getName()+
                                               " expected");
        }
    }

    // integrity constraints ////////////////////////////////////////////////    

    /**
     * Is a composite attribute (wraps the actual values iside an object).
     */
    public boolean isComposite()
    {
        return false;
    }

    /**
     * Checks if the attributes of this type can impose integrity constraints 
     * on the data store.
     * 
     * @return <code>true</code> if the attribute can impose constraints on the
     * data store. 
     */
    public boolean containsResourceReferences()
    {
        return false;
    }
    
    /**
     * Returns the resources referenced by this attribute.
     * 
     * @param value the attribute value.
     * @return resources referenced by this attribute.
     * */
    public Resource[] getResourceReferences(Object value)
    {
        throw new UnsupportedOperationException(attributeClass.getName()+
            " is not a referential attribute type");
    }
    
    /**
     * Removes all resource attributes from the attribute value.
     * 
     * <p>This method may be called during deletion of a group of 
     * interdependant resources.</p>
     * 
     * @param value attribute value.
     * @return <code>true</code> if the attribute value should be
     *         removed form the resource.
     */
    public boolean clearResourceReferences(Object value)
    {
        throw new UnsupportedOperationException(attributeClass.getName()+
            " is not a referential attribute type");
    }

    // protected /////////////////////////////////////////////////////////////

    protected String getTable()
    {
        return attributeClass.getDbTable();
    }

    /**
     * Converts a string into an attribute object.
     *
     * <p>The default attribute handler implementation does not support this
     * conversion (returns <code>null</code>).</p>
     * 
     * @param string the string to convert.
     * @return the attribute object, or <code>null</code> if conversion not
     *         supported. 
     */
    protected Object fromString(String string)
    {
        return null;
    }
    
    /**
     * Converts a Java object into an attribute object.
     *
     * <p>The default attribute handler implementation does not support this
     * conversion (returns <code>null</code>).</p>
     * 
     * @param object the object to convert.
     * @return the attribute object, or <code>null</code> if conversion not
     *         supported. 
     */
    protected Object fromObject(Object object)
    {
        return null;
    }

    /**
     * Checks if the specified record exists in the table.
     *
     * @param id the record id
     */
    protected void checkExists(long id, Statement stmt)
        throws EntityDoesNotExistException, SQLException
    {
        ResultSet rs = stmt.executeQuery(
            "SELECT data_key FROM "+getTable()+" WHERE data_key = "+id
        );
        if(!rs.next())
        {
            throw new EntityDoesNotExistException(
                "Item #"+id+" does not exist in table "+getTable());
        }
    }

    /**
     * Checks if a value is non-null and castable to the attributeType.
     *
     * @param value the value to check.
     */
    protected void checkValue(Object value)
    {
        if(value == null)
        {
            throw new IllegalArgumentException("can't convert null value");
        }
        if(!attributeType.isInstance(value))
        {
            throw new IllegalArgumentException("value is "+value.getClass().getName()+
                                               ", "+attributeType.getName()+
                                               " expected");
        }
    }

    /**
     * Backslash escape the \ and ' characters.
     */
    protected String escape(String string)
    {
        return StringUtils.backslashEscape(StringUtils.escapeNonASCIICharacters(string), "'\\");
    }

	/**
	 * Unescape unicode escapes.
	 */
	protected String unescape(String string)
	{
		return StringUtils.expandUnicodeEscapes(string);
	}

    /**
     * Return a new Comparator, or null if not supported.
     */
    protected Comparator createComparator()
    {
        if(Comparable.class.isAssignableFrom(attributeType))
        {
            return new ComparableComparator();
        }
        if(Entity.class.isAssignableFrom(attributeType))
        {
            return new EntityComparator();   
        }
        return null;
    }

    /**
     * A comparator for objects that support Comparable interface.
     *
     * <p>Silly, but useful.</p>
     */
    public static class ComparableComparator
        implements Comparator
    {
        public int compare(Object o1, Object o2)
        {
            return ((Comparable)o1).compareTo(o2);
        }
    }

    /**
     * Compares Coral entities by name.
     */
    public static class EntityComparator
        implements Comparator
    {
        public int compare(Object o1, Object o2)
        {
            Entity e1 = (Entity)o1;
            Entity e2 = (Entity)o2;
            return e1.toString().compareTo(e2.toString());
        }
    }
}
