package org.objectledge.coral.schema;

/**
 * Thrown when the specified Java class does not exist, could not be loaded
 * because of linkage problems, or does not implement a required interface.
 * 
 * @version $Id: JavaClassException.java,v 1.1 2004-02-18 14:21:27 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class JavaClassException
    extends Exception
{
    /**
     * Constructs a new exception object with a specified detail message.
     *
     * @param msg the detail message.
     */
    public JavaClassException(String msg)
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
    public JavaClassException(String msg, Throwable rootCause)
    {
        super(msg, rootCause);
    }
}
