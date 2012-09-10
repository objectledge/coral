package org.objectledge.coral.query;

import org.objectledge.coral.script.parser.ASTfindResourceStatement;

/**
 * Allows the application to perform queries on data contained in the Coral.
 *
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 * @version $Id: CoralQuery.java,v 1.6 2004-12-27 02:33:32 rafal Exp $
 */
public interface CoralQuery
{
    /**
     * Executes a query.
     * 
     * @param query the query.
     * @return query results.
     * @throws MalformedQueryException if the query has syntactic or semantic errors and thus cannot
     *         be executed.
     */
    public QueryResults executeQuery(String query)
        throws MalformedQueryException;

    /**
     * Executes a query represented as RML Abstract Syntax Tree.
     * 
     * @param node query AST root.
     * @return query results.
     * @throws MalformedQueryException if the query has semantic errors and thus cannot be executed.
     */
    public QueryResults executeQuery(ASTfindResourceStatement node)
    	throws MalformedQueryException;
    
    /**
     * Prepares a query.
     * <p>
     * Query may contain a number of <em>positional parameters</em>. The parameters have form of
     * $&lt;NUMBER&gt;, with no intervening whitespace. The number must be greater or equal to 1.
     * The positional parameters can only be used on the right hand side of binary operators. When
     * the query is executed, all positional parameters must be set to non-null values. To test a
     * resource's property for being defined (i.e. not null), use the DEFINED operator.
     * </p>
     * 
     * @param query the query.
     * @return a prepared query object.
     * @throws MalformedQueryException if the query has syntactic or semantic errors and thus cannot
     *         be executed.
     */
    public PreparedQuery prepareQuery(String query)
        throws MalformedQueryException;
}
