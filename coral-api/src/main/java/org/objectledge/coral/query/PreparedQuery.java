package org.objectledge.coral.query;

import org.objectledge.coral.entity.Entity;
import org.objectledge.coral.schema.AttributeClass;

/**
 * Represents a query that is prepared once, and may be executed multiple
 * times.
 *
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 * @version $Id: PreparedQuery.java,v 1.3 2004-05-14 11:19:10 fil Exp $
 */
public interface PreparedQuery
{
    // query execution ///////////////////////////////////////////////////////

    /**
     * Execute the query.
     *
     * @return query results.
     * @throws IllegalStateException when not all positional parameters in the
     *         query have definite values.
     */
    public QueryResults execute()
        throws IllegalStateException;
    
    /**
     * Execute the query.
     * 
     * @param parameters the query positional parameter values.
     * @return query results.
     * @throws IllegalStateException when there are too few values in the
     *         parameters array, or the array contains <code>null</code>
     *         values.
     */
    public QueryResults execute(Object[] parameters)
        throws IllegalStateException;
    
    // value setting /////////////////////////////////////////////////////////

    /**
     * Assigns a value to the positional parameter.
     * 
     * @param index the parameter index.
     * @param value the parameter value.
     * @throws IndexOutOfBoundsException if there is parameter by the specified index in the query
     * @throws IllegalArgumentException if a <code>null</code> value is used.
     */
    public void setParameter(int index, Entity value)
        throws IndexOutOfBoundsException, IllegalArgumentException;

    /**
     * Assigns a value to the positional parameter.
     * <p>
     * The AttributeClass of the value must be known, because it's the HandlerClass that provides
     * the external representation of the value that may be used in the query.
     * </p>
     * 
     * @param index the parameter index.
     * @param type the AttributeClass the value belongs to.
     * @param value the parameter value.
     * @throws IndexOutOfBoundsException if there is parameter by the specified index in the query
     * @throws IllegalArgumentException if a <code>null</code> value is used.
     */
    public <T> void setParameter(int index, AttributeClass<T> type, T value)
        throws IndexOutOfBoundsException, IllegalArgumentException;

    // convenience methods for the well known attribute types ////////////////
    
    /**
     * Assigns a value to the positional parameter.
     * 
     * @param index the parameter index.
     * @param value the parameter value.
     * @throws IndexOutOfBoundsException if there is parameter by the specified index in the query
     */
    public void setParameter(int index, boolean value)
        throws IndexOutOfBoundsException;
        
    /**
     * Assigns a value to the positional parameter.
     * 
     * @param index the parameter index.
     * @param value the parameter value.
     * @throws IndexOutOfBoundsException if there is parameter by the specified index in the query
     */
    public void setParameter(int index, int value)
        throws IndexOutOfBoundsException;    

    /**
     * Assigns a value to the positional parameter.
     * 
     * @param index the parameter index.
     * @param value the parameter value.
     * @throws IndexOutOfBoundsException if there is parameter by the specified index in the query
     */
    public void setParameter(int index, long value)
        throws IndexOutOfBoundsException;

    /**
     * Assigns a value to the positional parameter.
     * 
     * @param index the parameter index.
     * @param value the parameter value.
     * @throws IndexOutOfBoundsException if there is parameter by the specified index in the query
     */
    public void setParameter(int index, String value)
        throws IndexOutOfBoundsException;
}
