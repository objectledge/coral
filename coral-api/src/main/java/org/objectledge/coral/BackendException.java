package org.objectledge.coral;

/**
 * Thrown when an error occurs while accessing the database or other physical
 * data store. 
 *
 * 
 * @version $Id: BackendException.java,v 1.2 2005-05-25 07:55:38 pablo Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class BackendException
    extends RuntimeException
{
	public static final long serialVersionUID = 0L;	
	
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
