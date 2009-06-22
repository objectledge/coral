package org.objectledge.coral.store;

/**
 * Thrown upon an attempt to modify a <code>READONLY</code> attribute after
 * the resource has been created.
 * 
 * @version $Id: ModificationNotPermitedException.java,v 1.1 2004-02-18 14:21:27 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class ModificationNotPermitedException
    extends Exception
{
    /**
     * Constructs a new exception object with a specified detail message.
     *
     * @param msg the detail message.
     */
    public ModificationNotPermitedException(String msg)
    {
        super(msg);
    }
}
