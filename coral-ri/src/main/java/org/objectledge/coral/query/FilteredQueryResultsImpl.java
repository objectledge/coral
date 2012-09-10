package org.objectledge.coral.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.AttributeClass;
import org.objectledge.coral.schema.AttributeDefinition;
import org.objectledge.coral.schema.CoralSchema;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.schema.UnknownAttributeException;
import org.objectledge.coral.store.Resource;

/**
 * Represents the results of a query as tuples of Resource properties.
 *
 * <p>This is a generic implementation that can be used with any {@link
 * QueryResults implementation}
 * 
 * @author <a href="rkrzewsk@ngo.pl">Rafal Krzewski</a>
 * @version $Id: FilteredQueryResultsImpl.java,v 1.3 2004-12-22 07:54:49 rafal Exp $
 */
public class FilteredQueryResultsImpl
    implements FilteredQueryResults
{
    // instance variables ////////////////////////////////////////////////////

    /** The QueryResults. */
    private QueryResults results;

    /** the SELECT list. */
    private String[] select;

    /** Resource indices corresponding to each result column. */
    private int[] columnIndex;
    
    /** AttributeDefinitions corresponding to each result column. */
    private AttributeDefinition<?>[] columnAttribute;

    /** Name to index map. (String->Integer) */
    private Map<String, Integer> nameIndex = new HashMap<String, Integer>();
    
    // initialization ////////////////////////////////////////////////////////

    /**
     * Creates a FilteredQueryResultsImpl object.
     *
     * @param coralSchema Coral Schema.
     * @param results the QueryResults.
     * @param select the SELECT list.
     */
    public FilteredQueryResultsImpl(CoralSchema coralSchema, QueryResults results, String[] select)
    {
        this.results = results;
        this.select = select;
        this.columnIndex = new int[select.length];
        this.columnAttribute = new AttributeDefinition[select.length];
        ResourceClass<?> builtins;
       	try 
		{
            builtins = coralSchema.getResourceClass("coral.Node");
		} 
       	catch (EntityDoesNotExistException e) 
		{
       		throw (IllegalStateException)
				new IllegalStateException("coral.Node class not available").initCause(e);
		}
        for(int i=0; i<select.length; i++)
        {
            nameIndex.put(select[i], new Integer(i+1));
            int p = select[i].lastIndexOf('.');
            int ci;
            String a;
            if(p < 0)
            {
                if(results.getColumnCount() != 1)
                {
                    throw new IllegalStateException("unqualified SELECT column "+
                                                    select[i]+
                                                    "in a multicolumn query");
                }
                ci = 1;
                a = select[i];
            }
            else
            {
                String c = select[i].substring(0,p);
                ci = results.getColumnIndex(c);
                a = select[i].substring(p+1);
            }
            columnIndex[i] = ci;
            ResourceClass<?> rc = results.getColumnType(ci);
            try
            {
                if(rc == null)
                {
                	columnAttribute[i] = builtins.getAttribute(a);
                }
                else
                {
                    columnAttribute[i] = rc.getAttribute(a);
                }
            }
            catch(UnknownAttributeException e)
            {
                throw (IllegalStateException)new IllegalStateException("unknown attribute").
                	initCause(e);
            }
        }   
    }

    // FilteredQueryResults interface ////////////////////////////////////////

    // iteration over results ////////////////////////////////////////////////

    /**
     * Returns an Iterator over a list of {@link FilteredQueryResults.Row} objects.
     *
     * <p>The iterator does not support <code>remove</code> operation.
     *
     * @return an Iterator over a list of {@link FilteredQueryResults.Row} objects.
     */
    public Iterator<FilteredQueryResults.Row> iterator()
    {
        final Iterator<QueryResults.Row> ri = results.iterator();
        return new Iterator<FilteredQueryResults.Row>()
            {
                public boolean hasNext()
                {
                    return ri.hasNext();
                }
                
                public FilteredQueryResults.Row next()
                {
                    QueryResults.Row rr = (QueryResults.Row)ri.next();
                    return new RowImpl(rr);
                }
                
                public void remove()
                {
                    throw new UnsupportedOperationException();
                }
            };
    }

    /**
     * Returns a list of {@link FilteredQueryResults.Row} objects.
     *
     * <p>This methods iterates over the whole result set and collects all the
     * rows in an ArrayList for subsequent random access.</p>
     *
     * @return a list of {@link FilteredQueryResults.Row} objects.
     */
    public List<FilteredQueryResults.Row> getList()
    {
        ArrayList<FilteredQueryResults.Row> temp = new ArrayList<FilteredQueryResults.Row>();
        Iterator<FilteredQueryResults.Row> i = iterator();
        while(i.hasNext())
        {
            temp.add(i.next());
        }
        return temp;
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
        return select.length;
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
        if(index < 1 || index > select.length)
        {
            throw new IndexOutOfBoundsException("index "+index+"requested "+
                                                "range 1.."+select.length);
        }
        return select[index-1];
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
     * Returns the type of attribute in the specified column.
     *
     * @param index the index of the column (1 based).
     * @return the type of the specified column.
     * @throws IndexOutOfBoundsException if the index if the index is out of
     *         1..columnCount range.
     */
    public AttributeClass<?> getColumnType(int index)
        throws IndexOutOfBoundsException
    {
        if(index < 1 || index > select.length)
        {
            throw new IndexOutOfBoundsException("index "+index+"requested "+
                                                "range 1.."+select.length);
        }
        return columnAttribute[index-1].getAttributeClass();
    }

    // row class /////////////////////////////////////////////////////////////
    
    /**
     * Represens a single query result.
     */
    public class RowImpl
        implements FilteredQueryResults.Row
    {
        // instance variables ////////////////////////////////////////////////
        
        /** The query results row. */
        private QueryResults.Row row;

        // initialization ////////////////////////////////////////////////////

        /**
         * Constructs a Row filtering proxy.
         *
         * @param row the results row to constructa a proxy for.
         */
        public RowImpl(QueryResults.Row row)
        {
            this.row = row;
        }
        
        // Row interface ////////////////////////////////////////////////////

        /**
         * Returns the selected column of the row.
         *
         * @param name the name of the column.
         * @return the value.
         * @throws IllegalArgumentException if no column by the specified name is
         *         present in the results.
         */
        public Object get(String name)
            throws IllegalArgumentException
        {
            Integer i = (Integer)nameIndex.get(name);
            if(i == null)
            {
                throw new IllegalArgumentException("no column named "+name);
            }
            else
            {
                return get(i.intValue());
            }
        }
        
        /**
         * Returns the selected column of the row.
         *
         * @param index the index of the column (1 based).
         * @return the value.
         * @throws IndexOutOfBoundsException if the index if the index is out of
         *         1..columnCount range.
         */
        public Object get(int index)
            throws IndexOutOfBoundsException
        {
            if(index < 1 || index > select.length)
            {
                throw new IndexOutOfBoundsException("index "+index+"requested "+
                                                    "range 1.."+select.length);
            }
            Resource r = row.get(columnIndex[index-1]);
            return r.get(columnAttribute[index-1]);
        }

        /**
         * Returns the value from the only column of the row.
         *
         * @return the value.
         * @throws IllegalStateException if there is more than one column in
         *         the query results.
         */
        public Object get()
            throws IllegalStateException
        {
            if(select.length > 1)
            {
                throw new IllegalStateException("more than 1 column");
            }
            return get(1);
        }

        /**
         * Returns the contents of the row as array.
         *
         * <p>The indices in the array are 0 based, on contrary to the general
         * indexing of QueryResults columns. Thus the valid indexes are 0
         * .. columnCount-1 inclusive.</p>
         * 
         * @return the contents of the row as array.
         */
        public Object[] getArray()
        {
            Object[] result = new Object[select.length];
            for(int i=1; i<=result.length; i++)
            {
                result[i-1] = get(i);
            }
            return result;
        }
    }
}
