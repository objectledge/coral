package org.objectledge.coral.entity;

/**
 * Thrown when name specified by the user maps to more than one entity.
 *
 * @version $Id: AmbigousEntityNameException.java,v 1.1 2004-02-18 14:21:27 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class AmbigousEntityNameException
    extends Exception
{
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

     
