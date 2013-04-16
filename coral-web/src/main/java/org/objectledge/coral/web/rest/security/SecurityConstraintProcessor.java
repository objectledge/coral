package org.objectledge.coral.web.rest.security;


/**
 * Processes annotations
 * 
 * @author Marek Lewandowski
 * @param <T> type of annotation to process
 */
public interface SecurityConstraintProcessor<T>
{
    void process(T securityConstraint)
        throws SecurityViolationException;
}
