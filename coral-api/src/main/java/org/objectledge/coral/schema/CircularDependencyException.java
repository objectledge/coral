package org.objectledge.coral.schema;

/**
 * Thrown if an operation would result in creating a circular dependency
 * chain. 
 * 
 * @version $Id: CircularDependencyException.java,v 1.2 2005-05-25 07:55:47 pablo Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class CircularDependencyException
    extends Exception
{
	public static final long serialVersionUID = 0L;
	
    /**
     * Constructs a new exception object with a specified detail message.
     *
     * @param msg the detail message.
     */
    public CircularDependencyException(String msg)
    {
        super(msg);
    }
}
