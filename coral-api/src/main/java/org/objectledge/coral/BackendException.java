package org.objectledge.coral;

/**
 * Thrown when an error occurs while accessing the database or other physical
 * data store. 
 *
 * 
 * @version $Id: BackendException.java,v 1.1 2004-02-18 14:21:27 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class BackendException
    extends RuntimeException
{
    /**
     * Constructs a new exception object with a specified detail message. 
     *
     * @param msg the detail message.
     */
    public BackendException(String msg)
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
    public BackendException(String msg, Throwable rootCause)
    {
        super(msg, rootCause);
    }
}
