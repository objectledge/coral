package org.objectledge.coral.relation;

/**
 * Thrown to indicate that the query has syntactic or sematic errors.
 *
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: MalformedRelationQueryException.java,v 1.2 2005-05-25 07:55:55 pablo Exp $
 */
public class MalformedRelationQueryException
    extends Exception
{
	public static final long serialVersionUID = 0L;
	
    /**
     * Constructs a new exception object with a specified detail message. 
     *
     * @param msg the detail message.
     */
    public MalformedRelationQueryException(String msg)
    {
        super(msg);
    }

    /**
     * Constructs a new exception object with a specified detail message and 
     * root cause.
     *
     * @param msg the detail message.
     * @param rootCause the root cause of this exception.
     */
    public MalformedRelationQueryException(String msg, Throwable rootCause)
    {
        super(msg, rootCause);
    }
}
