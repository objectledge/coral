package org.objectledge.coral.entity;

import java.util.Date;

import org.objectledge.coral.security.Subject;

/**
 * Base interface for {@link RoleAssignment} and {@link PermissionAssignment}.
 *
 * <p>This interface exposes the common attributes of assigments -- the subject
 * that performed the assignment and the time that the assigment was made. </p>
 *
 * @version $Id: Assignment.java,v 1.1 2004-02-18 14:21:27 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface Assignment
    extends Association
{
    /**
     * Returns the {@link Subject} that created this assignment.
     *
     * @return the {@link Subject} that created this assignment.
     */
    public Subject getGrantedBy();

    /**
     * Returns the time the assignement was created.
     *
     * @return the time the assignement was created.
     */
    public Date getGrantTime();    
}
