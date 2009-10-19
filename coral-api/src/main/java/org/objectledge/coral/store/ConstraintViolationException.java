package org.objectledge.coral.store;

/**
 * Thrown when an error occurs while accessing the database or other physical
 * data store. 
 *
 * 
 * @version $Id: ConstraintViolationException.java,v 1.1 2004-02-18 14:21:27 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class ConstraintViolationException
    extends RuntimeException
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new exception object with a specified detail message. 
     *
     * @param msg the detail message.
     */
    public ConstraintViolationException(String msg)
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
    public ConstraintViolationException(String msg, Throwable rootCause)
    {
        super(msg, rootCause);
    }
}
