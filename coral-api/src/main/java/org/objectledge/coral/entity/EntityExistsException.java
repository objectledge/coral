package org.objectledge.coral.entity;

/**
 * Thrown if the new entity could not be created because the specified name is
 * not unique in the system. 
 * 
 * @version $Id: EntityExistsException.java,v 1.1 2004-02-18 14:21:27 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class EntityExistsException
    extends Exception
{
    /**
     * Constructs a new exception object with a specified detail message.
     *
     * @param msg the detail message.
     */
    public EntityExistsException(String msg)
    {
        super(msg);
    }
}
