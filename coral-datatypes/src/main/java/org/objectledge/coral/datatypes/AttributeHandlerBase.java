package org.objectledge.coral.datatypes;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

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
import org.objectledge.database.DatabaseUtils;

/**
 * An abstract base class for {@link AttributeHandler} implementations.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: AttributeHandlerBase.java,v 1.6 2004-12-23 04:10:58 rafal Exp $
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

    /** SimpleDateFormat pattern used for parsing date strings. */
    public static final String DATE_FORMAT = "yyyy/MM/dd";
    
    /** SimpleDateFormat pattern used for parsing date + time strings. */
    public static final String DATE_TIME_FORMAT = "yyyy/MM/dd HH:mm:ss";
    
    /** If date specification contains this substring DATE_TIME_FORMAT will be used for parsing,
     *  otherwise DATE_FORMAT will be used. */
    public static final String DATE_TIME_INDICATOR = ":";
    
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
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    public boolean shouldRetrieveAfterCreate()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public int getSupportedConditions()
    {
        return CONDITION_NONE;
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    public boolean supportsExternalString()
    {
        return false;
    }

    // value conversion //////////////////////////////////////////////////////

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
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
     * {@inheritDoc}
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
     * {@inheritDoc}
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
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    public boolean isComposite()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean containsResourceReferences()
    {
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    public Resource[] getResourceReferences(Object value)
    {
        throw new UnsupportedOperationException(attributeClass.getName()+
            " is not a referential attribute type");
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean clearResourceReferences(Object value)
    {
        throw new UnsupportedOperationException(attributeClass.getName()+
            " is not a referential attribute type");
    }

    // protected /////////////////////////////////////////////////////////////

    /**
     * Return the name of the table.
     * 
     * @return the table name.
     */
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
     * @param id the record id.
     * @param stmt the sql statement.
     * @throws EntityDoesNotExistException if not exists.
     * @throws SQLException if occured.
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
     * 
     * @param string the input string.
     * @return the escaped string.
     */
    protected String escape(String string)
    {
        return DatabaseUtils.escapeSqlString(string);
    }

	/**
	 * Unescape unicode escapes.
     * @param string the input string.
     * @return the unescaped string.
	 */
	protected String unescape(String string)
	{
		return DatabaseUtils.unescapeSqlString(string);
	}
    
    /**
     * Parse date/time string.
     * 
     * @param string the date string.
     * @return the date.
     * @throws IllegalArgumentException if the string does not conform to the expected pattern.
     */
    protected Date parseDate(String string)
        throws IllegalArgumentException
    {
        String pattern = string.contains(DATE_TIME_INDICATOR) ? DATE_TIME_FORMAT : DATE_FORMAT;
        /** DateFormat is not thread-safe */
        DateFormat format = new SimpleDateFormat(pattern);
        try
        {
            return format.parse(string);
        }
        catch(ParseException e)
        {
            throw new IllegalArgumentException(string+" does not conform to "+pattern+" pattern",
                e);
        }
    }

    /**
     * Return a new Comparator, or null if not supported.
     * 
     * @return the comparator.
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
        /**
         * {@inheritDoc}
         */
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
        /**
         * {@inheritDoc}
         */
        public int compare(Object o1, Object o2)
        {
            Entity e1 = (Entity)o1;
            Entity e2 = (Entity)o2;
            return e1.toString().compareTo(e2.toString());
        }
    }
}
