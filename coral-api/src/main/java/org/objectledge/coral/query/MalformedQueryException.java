package org.objectledge.coral.query;

/**
 * Thrown to indicate that the query has syntactic or sematic errors.
 *
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 * @version $Id: MalformedQueryException.java,v 1.3 2005-05-25 07:55:44 pablo Exp $
 */
public class MalformedQueryException
    extends Exception
{
	public static final long serialVersionUID = 0L;
	
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
