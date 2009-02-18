package org.objectledge.coral.schema;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Comparator;

import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.store.ConstraintViolationException;
import org.objectledge.coral.store.Resource;

/**
 * Performs low-level data manipulation on attribute instances.
 * 
 * <p>The handler provides information about attribute class that
 * could be more declaratively expressed as attribute class flags.
 * The relevant methods are:
 * <ul>
 *   <li>{@link #isComposite()}</li>
 *   <li>{@link #containsResourceReferences()}</li>
 *   <li>{@link #shouldRetrieveAfterCreate()}</li>
 *   <li>{@link #supportsExternalString()}</li>
 *   <li>{@link #getSupportedConditions()}</li>
 * </ul>
 * </p>
 * 
 * @version $Id: AttributeHandler.java,v 1.3 2005-01-20 10:54:21 rafal Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface AttributeHandler
{
    // constants /////////////////////////////////////////////////////////////

    /** This attribute type does not support comparisons of any kind. */
    public static int CONDITION_NONE = 0x00;

    /** This attribute type supports equality comparisons. */
    public static int CONDITION_EQUALITY = 0x01;
    
    /** This attribute type supports ordering comparisons. */
    public static int CONDITION_COMPARISON = 0x02;

    /** This attribute type supports approximation comparisons. */
    public static int CONDITION_APPROXIMATION = 0x04;

    // data manipulation /////////////////////////////////////////////////////

    // NOTE - AttributeClass needs to be passed to the constrcutor

    /**
     * Preloads all values of the attribute from the database.
     * 
     * @param conn database connection.
     * @throws SQLException if database operation fails.
     */
    public void preload(Connection conn)
        throws SQLException;

    /**
     * Creates a new attribute instance.
     *
     * @param value the value of the attribute.
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform
     *        the operation as a part of a JDBC transaction.
     * @return the identifier of the new attribute.
     * @throws SQLException in case of database problems. The caller metod
     *         should consider rolling back the whole transaction.
     */
    public long create(Object value, Connection conn)
        throws SQLException;

    /**
     * Retrieves an attribute value.
     *
     * @param id the identifier of the attribute.
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform
     *        the operation as a part of a JDBC transaction.
     * @return the retrieved attribute object.
     * @throws EntityDoesNotExistException if the attribute with specified id
     *         does not exist. 
     * @throws SQLException in case of database problems. The caller metod
     *         should consider rolling back the whole transaction.
     */
    public Object retrieve(long id, Connection conn)
        throws EntityDoesNotExistException, SQLException;

    /**
     * Modifies an existing attribute.
     *
     * @param id the identifier of the attribute.
     * @param value the value of the attribute.
     * @param conn the JDBC <code>Connection</code> to use. Needed to perform
     *        the operation as a part of a JDBC transaction.
     * @throws EntityDoesNotExistException if the attribute with specified id
     *         does not exist. 
     * @throws SQLException in case of database problems. The caller metod
     *         should consider rolling back the whole transaction.
     */
    public void update(long id, Object value, Connection conn)
        throws EntityDoesNotExistException, SQLException;

    /**
     * Removes an existing attribute.
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
        throws EntityDoesNotExistException, SQLException;

    // meta information //////////////////////////////////////////////////////

    /**
     * Provideds a hint to the ResorceHandler class that it should not cache
     * the data value used to create the attribute, but re-retrieve the value
     * from the AttributeHandler and cache that one instead.
     *
     * @return <code>false</code> if the data value used for creating the
     *         resource can be safely cached.
     */
    public boolean shouldRetrieveAfterCreate();

    /**
     * Provides information about comparison operations supported by the
     * attribute type.
     *
     * <p>The returned value is a bitwise sum of the CONDITION_*
     * constants.</p>
     * @return information about comparison operations supported by the
     * attribute type.
     */
    public int getSupportedConditions();
    
    /**
     * Returns a <code>java.util.Comparator</code> that can be used for
     * comparing attribute values.
     *
     * <p>The returned object's must be written to be safely used by multiple
     * threads at the same time.</p>
     *
     * @return a <code>Comparator</code> or <code>null</code> if the
     *          attribute class does not have sensible comparison semantics.
     */
    public Comparator getComparator();

    /**
     * Retruns <code>true</code> if the {@toExternalString()} is supported for
     * this attribute type.
     *
     * @return Retruns <code>true</code> if the {@toExternalString()} is
     *         supported for this attribute type.
     */
    public boolean supportsExternalString();

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
    public Object toAttributeValue(Object object);

    /**
     * Converts an attribute value into a human readable string.
     *
     * @param value the value to convert.
     * @return a human readable string.
     */
    public String toPrintableString(Object value);
    
    /**
     * Converts an attribute value into a string representation suitable for
     * using in queries against the underlying data store, like a relational
     * database.
     *
     * @param value the value to convert.
     * @return a string representation suitable for using in queries agains
     *         the underlying data store.
     */
    public String toExternalString(Object value);

    // value domain //////////////////////////////////////////////////////////

    /**
     * Check if the domain constraint is well formed.
     *
     * @param domain value domain constraint.
     */
    public void checkDomain(String domain);
    
    /**
     * Check if an attribute value fullfills a domain constraint.
     *
     * @param domain value domain constraint.
     * @param value an attribute value.
     * @throws ConstraintViolationException if the value does not fulfill the constraint.
     */
    public void checkDomain(String domain, Object value)
        throws ConstraintViolationException;
        
    // integrity constraints ////////////////////////////////////////////////
    
    /**
     * Is a composite attribute (wraps the actual values iside an object).
     * 
     * @return <code>true</code> if the attribute is composite. 
     */
    public boolean isComposite();
    
    /**
     * Checks if the attributes of this type can impose integrity constraints 
     * on the data store.
     * 
     * @return <code>true</code> if the attribute can impose constraints on the
     * data store. 
     */
    public boolean containsResourceReferences();
    
    /**
     * Returns the resources referenced by this attribute.
     * 
     * @param value the attribute value.
     * @return resources referenced by this attribute.
     */
    public Resource[] getResourceReferences(Object value);
    
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
    public boolean clearResourceReferences(Object value);
}
