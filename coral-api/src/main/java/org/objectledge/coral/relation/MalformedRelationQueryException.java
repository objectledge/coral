package org.objectledge.coral.relation;

/**
 * Thrown to indicate that the query has syntactic or sematic errors.
 *
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: MalformedRelationQueryException.java,v 1.1 2004-02-20 09:15:48 zwierzem Exp $
 */
public class MalformedRelationQueryException
    extends Exception
{
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
