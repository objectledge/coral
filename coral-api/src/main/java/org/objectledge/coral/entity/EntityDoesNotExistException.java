package org.objectledge.coral.entity;

/**
 * Thrown when the entity with the specified id does not exist in the system.
 * 
 * @version $Id: EntityDoesNotExistException.java,v 1.2 2005-05-25 07:55:40 pablo Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class EntityDoesNotExistException
    extends Exception
{
	public static final long serialVersionUID = 0L;
	
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
