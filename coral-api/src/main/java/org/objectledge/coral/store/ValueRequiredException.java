package org.objectledge.coral.store;

/**
 * Thrown if not all obligatory attributes have defined values in a resource
 * instance. 
 * 
 * @version $Id: ValueRequiredException.java,v 1.2 2005-05-25 07:56:00 pablo Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class ValueRequiredException
    extends Exception
{
	public static final long serialVersionUID = 0L;
	
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
