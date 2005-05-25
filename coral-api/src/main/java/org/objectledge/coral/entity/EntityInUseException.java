package org.objectledge.coral.entity;

/**
 * Thrown if the entity cannot be deleted because there are other entities in
 * the system that depend on it.
 * 
 * @version $Id: EntityInUseException.java,v 1.2 2005-05-25 07:55:40 pablo Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class EntityInUseException
    extends Exception
{
	public static final long serialVersionUID = 0L;
	
    /**
     * Constructs a new exception object with a specified detail message.
     *
     * @param msg the detail message.
     */
    public EntityInUseException(String msg)
    {
        super(msg);
    }
}
