package org.objectledge.coral.schema;

/**
 * Thrown by {@link org.objectledge.coral.store.Resource} objects when the application passes 
 * {@link AttributeDefinition} belonging to a wrong {@link ResourceClass} to one of
 * their methods.
 * 
 * @version $Id: UnknownAttributeException.java,v 1.2 2005-02-08 20:34:28 rafal Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class UnknownAttributeException
    extends RuntimeException
{
    /**
     * Constructs a new exception object with a specified detail message.
     *
     * @param msg the detail message.
     */
    public UnknownAttributeException(String msg)
    {
        super(msg);
    }
}
