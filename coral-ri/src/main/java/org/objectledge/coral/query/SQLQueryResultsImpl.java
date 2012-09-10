package org.objectledge.coral.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.objectledge.coral.BackendException;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.Resource;

/**
 * Represents the results of a query as tuples of Resources. This is an implementation of
 * QueryResults over an java.sql.ResultSet containing tuples of resource identifiers.
 * 
 * @author <a href="rkrzewsk@ngo.pl">Rafal Krzewski</a>
 * @version $Id: SQLQueryResultsImpl.java,v 1.3 2005-05-05 08:27:04 rafal Exp $
 */
public class SQLQueryResultsImpl
    implements QueryResults
{
    // instance variables ////////////////////////////////////////////////////

    /** The FROM list (type, alias) */
    private String[][] from;
    
    /** The SELECT list */
    private String[] select;

    /**
     * A list of long[] arrays containing identifiers of resources in consecutive tuples of the
     * result.
     */
    private ArrayList<long[]> resultList = null;

    /** A mapping of column names into indices */
    private Map<String, Integer> nameIndex = new HashMap<String, Integer>();

    /** Types of resources in each column. */
    private ResourceClass<?>[] columnType;

    /** The Coral store. */
    private CoralStore store;

    /** The Coral schema. */
    private CoralSchema schema;
    
    // initialization ////////////////////////////////////////////////////////

    /**
     * Constructs a SQLQueryResultsImpl.
     *
     * @param schema the Coral Schema.
     * @param store the Coral Store.
     * @param resultSet the ResultSet
     * @param from the FROM list.
     * @param select the SELECT list, or <code>null</code>
     */
    public SQLQueryResultsImpl(CoralSchema schema, CoralStore store, ResultSet resultSet, 
        String[][] from, String[] select)
    {
        this.store = store;
        this.schema = schema;
        
        this.from = from;
        this.select = select;
        columnType = new ResourceClass[from.length];
        try
        {
            for(int i=0; i<from.length; i++)
            {
                nameIndex.put(from[i][1], new Integer(i+1));
                if(from[i][0] != null)
                {
                    columnType[i] = schema.getResourceClass(from[i][0]);
                }
                else
                {
                    columnType[i] = null;
                }
            }
        }
        catch(EntityDoesNotExistException e)
        {
            throw new BackendException("resource class missing", e);
        }
        fillList(resultSet);
    }

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
        throws IllegalStateException
    {
        if(select == null)
        {
            throw new IllegalStateException("the query contains no SELECT clause");
        }
        return new FilteredQueryResultsImpl(schema, this, select);
    }

    // iteration over results ////////////////////////////////////////////////

    /**
     * Returns an Iterator over a list of {@link QueryResults.Row} objects.
     *
     * <p>The iterator does not support <code>remove</code> operation.
     *
     * @return an Iterator over a list of {@link QueryResults.Row} objects.
     */
    public Iterator<QueryResults.Row> iterator()
    {
        return new Iterator<QueryResults.Row>()
            {
                private int i=0;
                
                public boolean hasNext()
                {
                    return i < resultList.size();
                }
                        
                public QueryResults.Row next()
                {
                    if(i<resultList.size())
                    {
                        return new RowImpl(resultList.get(i++));
                    }
                    else
                    {
                        throw new NoSuchElementException();
                    }
                }
                        
                public void remove()
                {
                    throw new UnsupportedOperationException();
                }
            };
    }

    /**
     * Returns a list of {@link QueryResults.Row} objects.
     *
     * <p>This methods iterates over the whole result set and collects all the
     * rows in an ArrayList for subsequent random access.</p>
     *
     * @return a list of {@link QueryResults.Row} objects.
     */
    public List<QueryResults.Row> getList()
    {
        ArrayList<QueryResults.Row> temp = new ArrayList<QueryResults.Row>();
        Iterator<QueryResults.Row> i = iterator();
        while(i.hasNext())
        {
            temp.add(i.next());
        }
        return temp;
    }

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
        throws IllegalArgumentException
    {
        return getArray(getColumnIndex(name));
    }
    
    /**
     * Returns the selected column of the result as Resource array.
     *
     * @param index the index of the column (1 based).
     * @return Resource array.
     * @throws IndexOutOfBoundsException if the index if the index is out of
     *         1..columnCount range.
     */
    public Resource[] getArray(int index)
        throws IndexOutOfBoundsException
    {
        if(index < 1 || index > from.length)
        {
            throw new IndexOutOfBoundsException("index "+index+"requested "+
                                                "range 1.."+from.length);
        }
        Resource[] result = new Resource[resultList.size()];
        try
        {
            for(int i=0; i<result.length; i++)
            {
                result[i] = store.getResource(((long[])resultList.get(i))[index-1]);
            }
            return result;
        }
        catch(EntityDoesNotExistException e)
        {
            throw new BackendException("resource missing", e);
        }
    }

    /**
     * Returns the selected column of the result as Resource List.
     *
     * @param name the name of the column.
     * @return Resource List.
     * @throws IllegalArgumentException if no column by the specified name is
     *         present in the results.
     */
    public List<Resource> getList(String name)
        throws IllegalArgumentException
    {
        return getList(getColumnIndex(name));
    }

    /**
     * Returns the selected column of the result as Resource List.
     *
     * @param index the index of the column (1 based).
     * @return Resource List.
     * @throws IndexOutOfBoundsException if the index if the index is out of
     *         1..columnCount range.
     */
    public List<Resource> getList(int index)
        throws IndexOutOfBoundsException
    {
        if(index < 1 || index > from.length)
        {
            throw new IndexOutOfBoundsException("index "+index+"requested "+
                                                "range 1.."+from.length);
        }
        List<Resource> result = new ArrayList<Resource>(resultList.size());
        try
        {
            for(int i=0; i<resultList.size(); i++)
            {
                result.add(store.getResource((resultList.get(i))[index - 1]));
            }
            return result;
        }
        catch(EntityDoesNotExistException e)
        {
            throw new BackendException("resource missing", e);
        }
    }

    // results metadata //////////////////////////////////////////////////////

    /**
     * Returns the total number of columns in the results.
     *
     * <p>Note that columns are numbered 1 .. columnCount inclusive, just like
     * in <code>java.sql.ResultSet</code>.</p>
     * 
     * @return the  total number of columns in the results.
     */
    public int getColumnCount()
    {
        return from.length;
    }
    
    /**
     * Returns the name of the specified result column.
     *
     * @param index the index of the column (1 based).
     * @return the name of the specified column.
     * @throws IndexOutOfBoundsException if the index if the index is out of
     *         1..columnCount range.
     */
    public String getColumnName(int index)
        throws IndexOutOfBoundsException
    {
        if(index < 1 || index > from.length)
        {
            throw new IndexOutOfBoundsException("index "+index+"requested "+
                                                "range 1.."+from.length);
        }
        return from[index-1][1];
    }
    
    /**
     * Returns the index of the specified result column.
     *
     * @param name the name of the column.
     * @return the index of the specified column.
     * @throws IllegalArgumentException if no column by the specified name is
     *         present in the results.
     */
    public int getColumnIndex(String name)
        throws IllegalArgumentException
    {
        Integer i = (Integer)nameIndex.get(name);
        if(i == null)
        {
            throw new IllegalArgumentException("no column named "+name);
        }
        else
        {
            return i.intValue();
        }
    }

    /**
     * Returns the type of resources in the specified column.
     *
     * @param index the index of the column (1 based).
     * @return the type of the specified column.
     * @throws IndexOutOfBoundsException if the index if the index is out of
     *         1..columnCount range.
     */
    public ResourceClass<?> getColumnType(int index)
        throws IndexOutOfBoundsException
    {
        if(index < 1 || index > from.length)
        {
            throw new IndexOutOfBoundsException("index "+index+"requested "+
                                                "range 1.."+from.length);
        }
        return columnType[index-1];
    }
    
    /**
     * Returns the type of resources in the specified column.
     *
     * @param name the name of the column.
     * @return the type of the specified column.
     * @throws IllegalArgumentException if no column by the specified name is
     *         present in the results.
     */
    public ResourceClass<?> getColumnType(String name)
        throws IllegalArgumentException
    {
        Integer i = (Integer)nameIndex.get(name);
        if(i == null)
        {
            throw new IllegalArgumentException("no column named "+name);
        }
        else
        {
            return getColumnType(i.intValue());
        }
    }

    // implementation ////////////////////////////////////////////////////////

    /**
     * Fills the result list with data.
     */
    private void fillList(ResultSet resultSet)
    {
        try
        {
            resultList = new ArrayList<long[]>();
            while(resultSet.next())
            {
                long[] row = new long[from.length];
                for(int i=0; i<from.length; i++)
                {
                    row[i] = resultSet.getLong(i+1);
                }
                resultList.add(row);
            }
        }
        catch(SQLException e)
        {
            throw new BackendException("failed to read data", e);
        }
    }
    
    // Row implementation ////////////////////////////////////////////////////
    
    /**
     * Represents a single query result.
     */
    public class RowImpl
        implements QueryResults.Row
    {
        // instance variables ////////////////////////////////////////////////

        /** The ids of the resources in the row. */
        private long[] ids;
        
        // initialization ////////////////////////////////////////////////////
        
        /**
         * Constructs a row. 
         *
         * @param ids The ids of the resources in the row.
         */
        public RowImpl(long[] ids)
        {
            this.ids = ids;
        }

        /**
         * Returns the selected column of the row.
         *
         * @param name the name of the column.
         * @return the Resource.
         * @throws IllegalArgumentException if no column by the specified name is
         *         present in the results.
         */
        public Resource get(String name)
            throws IllegalArgumentException
        {
            return get(getColumnIndex(name));
        }

        /**
         * Returns the id of selected column of the row.
         *
         * @param name the name of the column.
         * @return the resource id.
         * @throws IllegalArgumentException if no column by the specified name is
         *         present in the results.
         */
        public long getId(String name)
            throws IllegalArgumentException
        {
            return getId(getColumnIndex(name));
        }

        
        /**
         * Returns the selected column of the row.
         *
         * @param index the index of the column (1 based).
         * @return the Resource.
         * @throws IndexOutOfBoundsException if the index if the index is out of
         *         1..columnCount range.
         */
        public Resource get(int index)
            throws IndexOutOfBoundsException
        {
            if(index < 1 || index > from.length)
            {
                throw new IndexOutOfBoundsException("index "+index+"requested "+
                                                    "range 1.."+from.length);
            }
            try
            {
                return store.getResource(ids[index-1]);
            }
            catch(EntityDoesNotExistException e)
            {
                throw new BackendException("resource missing", e);
            }
        }

        /**
         * Returns the id of a selected column of the row.
         *
         * @param index the index of the column (1 based).
         * @return the resource id.
         * @throws IndexOutOfBoundsException if the index if the index is out of
         *         1..columnCount range.
         */
        public long getId(int index)
            throws IndexOutOfBoundsException
        {
            if(index < 1 || index > from.length)
            {
                throw new IndexOutOfBoundsException("index "+index+"requested "+
                                                    "range 1.."+from.length);
            }
            return ids[index-1];
        }

        /**
         * Returns the value from the only column of the row.
         *
         * @return the Resource
         * @throws IllegalStateException if there is more than one column in
         *         the query results.
         */
        public Resource get()
            throws IllegalStateException
        {
            if(from.length > 1)
            {
                throw new IllegalStateException("more than 1 column");
            }
            return get(1);
        }

        /**
         * Returns the id from the only column of the row.
         *
         * @return the resource id
         * @throws IllegalStateException if there is more than one column in
         *         the query results.
         */
        public long getId()
            throws IllegalStateException
        {
            if(from.length > 1)
            {
                throw new IllegalStateException("more than 1 column");
            }
            return getId(1);
        }
        
        /**
         * Returns the contents of the row as Resource array.
         *
         * <p>The indices in the array are 0 based, on contrary to the general
         * indexing of QueryResults columns. Thus the valid indexes are 0
         * .. columnCount-1 inclusive.</p>
         * 
         * @return the contents of the row as Resource array.
         */
        public Resource[] getArray()
        {
            Resource[] result = new Resource[from.length];
            for(int i=1; i<=result.length; i++)
            {
                result[i-1] = get(i);
            }
            return result;
        }

        /**
         * Returns the contents of the row as resource id array.
         *
         * <p>The indices in the array are 0 based, on contrary to the general
         * indexing of QueryResults columns. Thus the valid indexes are 0
         * .. columnCount-1 inclusive.</p>
         * 
         * @return the contents of the row as resource id array.
         */
        public long[] getIdArray()
        {
            long[] result = new long[ids.length];
            System.arraycopy(ids, 0, result, 0, ids.length); 
            return result;
        }
    }
}
