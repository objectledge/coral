package org.objectledge.coral.query;

import java.util.Iterator;
import java.util.List;

import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.store.Resource;

/**
 * Represents the results of a query as tuples of Resources.
 * <p>
 * This interface allows you to view the results of the query as a sequential list of tuples of
 * Resources that match the query. It completely ignores the SELECT clause of the query, and allows
 * you to retrieve the desired properties of the objects yourself.
 * </p>
 * <p>
 * To view the results of the query as a list of tuples of Resource properties extracted using the
 * SELECT clause, call the {@link #getFiltered()} method and use the returned
 * {@link FilteredQueryResults} object instead.
 * </p>
 * 
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 * @version $Id: QueryResults.java,v 1.4 2006-04-26 11:10:07 rafal Exp $
 */
public interface QueryResults
    extends Iterable<QueryResults.Row>
{
    // result filtering //////////////////////////////////////////////////////

    /**
     * Returns the proxy object providing SELECT based Resource property
     * access. 
     *
     * <p>If the query did not have the SELECT clause id and pathname of
     * each Resource in the result row will be included.</p>
     *
     * @return filtered query results.
     * @throws IllegalStateException if the query contained no SELECT clause.
     */
    public FilteredQueryResults getFiltered()
        throws IllegalStateException;

    // iteration over results ////////////////////////////////////////////////

    /**
     * Returns an Iterator over a list of {@link QueryResults.Row} objects.
     *
     * <p>The iterator does not support <code>remove</code> operation.
     *
     * @return an Iterator over a list of {@link QueryResults.Row} objects.
     */
    public Iterator<Row> iterator();

    /**
     * Returns a list of {@link QueryResults.Row} objects.
     *
     * <p>This methods iterates over the whole result set and collects all the
     * rows in an ArrayList for subsequent random access.</p>
     *
     * @return a list of {@link QueryResults.Row} objects.
     */
    public List<Row> getList();

    // access to a colum of results at once (possibly memory consuming) //////

    /**
     * Returns the selected column of the result as Resource array.
     *
     * @param name the name of the column.
     * @return Resource array.
     * @throws IllegalArgumentException if no column by the specified name is
     *         present in the results.
     */
    public Resource[] getArray(String name)
        throws IllegalArgumentException;
    
    /**
     * Returns the selected column of the result as Resource array.
     *
     * @param index the index of the column (1 based).
     * @return Resource array.
     * @throws IndexOutOfBoundsException if the index if the index is out of
     *         1..columnCount range.
     */
    public Resource[] getArray(int index)
        throws IndexOutOfBoundsException;

    /**
     * Returns the selected column of the result as Resource List.
     *
     * @param name the name of the column.
     * @return Resource List.
     * @throws IllegalArgumentException if no column by the specified name is
     *         present in the results.
     */
    public List<? extends Resource> getList(String name)
        throws IllegalArgumentException;

    /**
     * Returns the selected column of the result as Resource List.
     *
     * @param index the index of the column (1 based).
     * @return Resource List.
     * @throws IndexOutOfBoundsException if the index if the index is out of
     *         1..columnCount range.
     */
    public List<? extends Resource> getList(int index)
        throws IndexOutOfBoundsException;

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
     * Returns the type of resources in the specified column.
     *
     * @param index the index of the column (1 based).
     * @return the type of the specified column.
     * @throws IndexOutOfBoundsException if the index if the index is out of
     *         1..columnCount range.
     */
    public ResourceClass<?> getColumnType(int index)
        throws IndexOutOfBoundsException;
    
    /**
     * Returns the type of resources in the specified column.
     *
     * @param name the name of the column.
     * @return the type of the specified column.
     * @throws IllegalArgumentException if no column by the specified name is
     *         present in the results.
     */
    public ResourceClass<?> getColumnType(String name)
        throws IllegalArgumentException;

    /**
     * Returns number of result rows. This is a potentially expensive operation.
     * 
     * @return number of result rows.
     */
    public int rowCount();

    // nested inteface ///////////////////////////////////////////////////////
    
    /**
     * Represens a single query result.
     */
    public interface Row
    {
        /**
         * Returns the selected column of the row.
         *
         * @param name the name of the column.
         * @return the Resource.
         * @throws IllegalArgumentException if no column by the specified name is
         *         present in the results.
         */
        public Resource get(String name)
            throws IllegalArgumentException;

        /**
         * Returns the id of selected column of the row.
         *
         * @param name the name of the column.
         * @return the resource id.
         * @throws IllegalArgumentException if no column by the specified name is
         *         present in the results.
         */
        public long getId(String name)
            throws IllegalArgumentException;
        
        /**
         * Returns the selected column of the row.
         *
         * @param index the index of the column (1 based).
         * @return the Resource.
         * @throws IndexOutOfBoundsException if the index if the index is out of
         *         1..columnCount range.
         */
        public Resource get(int index)
            throws IndexOutOfBoundsException;

        /**
         * Returns the id of a selected column of the row.
         *
         * @param index the index of the column (1 based).
         * @return the resource id.
         * @throws IndexOutOfBoundsException if the index if the index is out of
         *         1..columnCount range.
         */
        public long getId(int index)
            throws IndexOutOfBoundsException;

        /**
         * Returns the value from the only column of the row.
         *
         * @return the Resource
         * @throws IllegalStateException if there is more than one column in
         *         the query results.
         */
        public Resource get()
            throws IllegalStateException;

        /**
         * Returns the id from the only column of the row.
         *
         * @return the resource id
         * @throws IllegalStateException if there is more than one column in
         *         the query results.
         */
        public long getId()
            throws IllegalStateException;
            
        /**
         * Returns the contents of the row as Resource array.
         *
         * <p>The indices in the array are 0 based, on contrary to the general
         * indexing of QueryResults columns. Thus the valid indexes are 0
         * .. columnCount-1 inclusive.</p>
         * 
         * @return the contents of the current row as an array.
         */
        public Resource[] getArray();

        /**
         * Returns the contents of the row as resource id array.
         *
         * <p>The indices in the array are 0 based, on contrary to the general
         * indexing of QueryResults columns. Thus the valid indexes are 0
         * .. columnCount-1 inclusive.</p>
         * 
         * @return the contents of the current row as an Resource id array.
         */
        public long[] getIdArray();
   }
}
