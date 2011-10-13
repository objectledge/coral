package org.objectledge.coral.entity;

/**
 * Thrown when name specified by the user maps to more than one entity.
 *
 * @version $Id: AmbigousEntityNameException.java,v 1.2 2005-05-25 07:55:40 pablo Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class AmbigousEntityNameException
    extends Exception
{
	public static final long serialVersionUID = 0L;
	
    /** 
     * Constructs a new Execption with the specified detail message.
     * 
     * @param msg the detail message.
     */
    public AmbigousEntityNameException(String msg)
    {
        super(msg);
    }
}

     
