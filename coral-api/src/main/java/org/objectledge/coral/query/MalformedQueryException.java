package org.objectledge.coral.query;

/**
 * Thrown to indicate that the query has syntactic or sematic errors.
 *
 * @author <a href="rkrzewsk@ngo.pl">Rafal Krzewski</a>
 * @version $Id: MalformedQueryException.java,v 1.1 2004-02-18 14:21:27 fil Exp $
 */
public class MalformedQueryException
    extends Exception
{
    /**
     * Constructs a new exception object with a specified detail message. 
     *
     * @param msg the detail message.
     */
    public MalformedQueryException(String msg)
    {
        super(msg);
    }

    /**
     * Constructs a new exception object with a specified detail message and 
     * root cause.
     *
     * @param msg the detail message.
     * @param rootCause the root cause of this exception.
     */
    public MalformedQueryException(String msg, Throwable rootCause)
    {
        super(msg, rootCause);
    }
}
