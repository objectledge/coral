package org.objectledge.coral.security;

/**
 * Thrown to indicate various security problems.
 * 
 * @version $Id: SecurityException.java,v 1.1 2004-02-18 14:21:27 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class SecurityException
    extends Exception
{
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
