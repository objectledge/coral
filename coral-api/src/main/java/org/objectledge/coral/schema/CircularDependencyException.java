package org.objectledge.coral.schema;

/**
 * Thrown if an operation would result in creating a circular dependency
 * chain. 
 * 
 * @version $Id: CircularDependencyException.java,v 1.1 2004-02-18 14:21:27 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class CircularDependencyException
    extends Exception
{
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
