package org.objectledge.coral.entity;

/**
 * Thrown when the entity with the specified id does not exist in the system.
 * 
 * @version $Id: EntityDoesNotExistException.java,v 1.1 2004-02-18 14:21:27 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class EntityDoesNotExistException
    extends Exception
{
    /**
     * Constructs a new exception object with a specified detail message.
     *
     * @param msg the detail message.
     */
    public EntityDoesNotExistException(String msg)
    {
        super(msg);
    }
}
