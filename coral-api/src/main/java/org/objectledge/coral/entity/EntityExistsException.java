package org.objectledge.coral.entity;

/**
 * Thrown if the new entity could not be created because the specified name is
 * not unique in the system. 
 * 
 * @version $Id: EntityExistsException.java,v 1.2 2005-05-25 07:55:40 pablo Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class EntityExistsException
    extends Exception
{
	public static final long serialVersionUID = 0L;
	
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
