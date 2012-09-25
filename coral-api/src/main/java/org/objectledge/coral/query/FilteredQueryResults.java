package org.objectledge.coral.query;

import java.util.Iterator;
import java.util.List;

import org.objectledge.coral.schema.AttributeClass;

/**
 * Represents the results of a query as tuples of Resource properties.
 *
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 * @version $Id: FilteredQueryResults.java,v 1.4 2006-04-26 11:10:07 rafal Exp $
 */
public interface FilteredQueryResults
    extends Iterable<FilteredQueryResults.Row>
{
    // iteration over results ////////////////////////////////////////////////

    /**
     * Returns an Iterator over a list of {@link FilteredQueryResults.Row} objects.
     *
     * <p>The iterator does not support <code>remove</code> operation.
     *
     * @return an Iterator over a list of {@link FilteredQueryResults.Row} objects.
     */
    public Iterator<Row> iterator();

    /**
     * Returns a list of {@link FilteredQueryResults.Row} objects.
     *
     * <p>This methods iterates over the whole result set and collects all the
     * rows in an ArrayList for subsequent random access.</p>
     *
     * @return a list of {@link FilteredQueryResults.Row} objects.
     */
    public List<Row> getList();

    // results metadata //////////////////////////////////////////////////////

    /**
     * Returns the total number of columns in the results.
     *
     * <p>Note that columns are numbered 1 .. columnCount inclusive, just like
     * in <code>java.sql.ResultSet</code>.</p>
     * 
     * @return the  total number of columns in the results.
     */
    public int getColumnCount();
    
    /**
     * Returns the name of the specified result column.
     *
     * @param index the index of the column (1 based).
     * @return the name of the specified column.
     * @throws IndexOutOfBoundsException if the index if the index is out of
     *         1..columnCount range.
     */
    public String getColumnName(int index)
        throws IndexOutOfBoundsException;
    
    /**
     * Returns the index of the specified result column.
     *
     * @param name the name of the column.
     * @return the index of the specified column.
     * @throws IllegalArgumentException if no column by the specified name is
     *         present in the results.
     */
    public int getColumnIndex(String name)
        throws IllegalArgumentException;

    /**
     * Returns the type of attribute in the specified column.
     *
     * @param index the index of the column (1 based).
     * @return the type of the specified column.
     * @throws IndexOutOfBoundsException if the index if the index is out of
     *         1..columnCount range.
     */
    public AttributeClass<?> getColumnType(int index)
        throws IndexOutOfBoundsException;    

    // nested interface //////////////////////////////////////////////////////
    
    /**
     * Represents a single query result.
     */
    public interface Row
    {
        /**
         * Returns the selected column of the row.
         *
         * @param name the name of the column.
         * @return the value.
         * @throws IllegalArgumentException if no column by the specified name is
         *         present in the results.
         */
        public Object get(String name)
            throws IllegalArgumentException;
        
        /**
         * Returns the selected column of the row.
         *
         * @param index the index of the column (1 based).
         * @return the value.
         * @throws IndexOutOfBoundsException if the index if the index is out of
         *         1..columnCount range.
         */
        public Object get(int index)
            throws IndexOutOfBoundsException;

        /**
         * Returns the value from the only column of the row.
         *
         * @return the value.
         * @throws IllegalStateException if there is more than one column in
         *         the query results.
         */
        public Object get()
            throws IllegalStateException;

        /**
         * Returns the contents of the row as array.
         *
         * <p>The indices in the array are 0 based, on contrary to the general
         * indexing of QueryResults columns. Thus the valid indexes are 0
         * .. columnCount-1 inclusive.</p>
         * 
         * @return the current row contents.
         */
        public Object[] getArray();
    }
}
