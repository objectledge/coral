package org.objectledge.coral.store;

/**
 * Thrown if not all obligatory attributes have defined values in a resource
 * instance. 
 * 
 * @version $Id: ValueRequiredException.java,v 1.1 2004-02-18 14:21:27 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class ValueRequiredException
    extends Exception
{
    /**
     * Constructs a new exception object with a specified detail message.
     *
     * @param msg the detail message.
     */
    public ValueRequiredException(String msg)
    {
        super(msg);
    }
}
