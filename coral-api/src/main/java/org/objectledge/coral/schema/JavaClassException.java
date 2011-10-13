package org.objectledge.coral.schema;

/**
 * Thrown when the specified Java class does not exist, could not be loaded
 * because of linkage problems, or does not implement a required interface.
 * 
 * @version $Id: JavaClassException.java,v 1.2 2005-05-25 07:55:47 pablo Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class JavaClassException
    extends Exception
{
	public static final long serialVersionUID = 0L;
	
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
