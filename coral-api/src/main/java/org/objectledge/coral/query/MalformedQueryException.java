package org.objectledge.coral.query;

/**
 * Thrown to indicate that the query has syntactic or sematic errors.
 *
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 * @version $Id: MalformedQueryException.java,v 1.2 2004-05-14 11:19:10 fil Exp $
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
