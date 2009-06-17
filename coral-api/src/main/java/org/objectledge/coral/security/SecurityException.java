package org.objectledge.coral.security;

/**
 * Thrown to indicate various security problems.
 * 
 * @version $Id: SecurityException.java,v 1.2 2005-05-25 07:55:42 pablo Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class SecurityException
    extends Exception
{
	public static final long serialVersionUID = 0L;
	
    /**
     * Constructs a new exception object with a specified detail message.
     *
     * @param msg the detail message.
     */
    public SecurityException(String msg)
    {
        super(msg);
    }
}
