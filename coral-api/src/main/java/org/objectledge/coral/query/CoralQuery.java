package org.objectledge.coral.query;

/**
 * Allows the application to perform queries on data contained in the Coral.
 *
 * @author <a href="rkrzewsk@ngo.pl">Rafal Krzewski</a>
 * @version $Id: CoralQuery.java,v 1.2 2004-02-18 15:08:21 fil Exp $
 */
public interface CoralQuery
{
    /**
     * Executes a query.
     *
     * @param query the query.
     * @return query resuls.
     * @throws MalformedQueryException if the query has syntactic or semantic
     *         errors and thus cannot be executed.
     */
    public QueryResults executeQuery(String query)
        throws MalformedQueryException;
    
    /** 
     * Prepares a query.
     *
     * <p>Query may contain a number of <em>positional parameters</em>. The
     * parameters have form of "$"<NUMBER>, with no intervening
     * whitespace. The nuber must be greater or equal to 1. The positional
     * parameters can only be used on the right hand side of binary operators.
     * When the query is executed, all positional parameters must be set to
     * non-null values. To test a resource's property for being defined
     * (i.e. not null), use the DEFINED operator.</p>
     *
     * @param query the query.
     * @return a prepared query object.
     * @throws MalformedQueryException if the query has syntactic or semantic
     *         errors and thus cannot be executed.
     */
    public PreparedQuery prepareQuery(String query)
        throws MalformedQueryException;
}
