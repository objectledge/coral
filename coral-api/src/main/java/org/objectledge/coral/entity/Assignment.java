package org.objectledge.coral.entity;

import java.util.Date;

import org.objectledge.coral.security.Subject;

/**
 * Base interface for {@link org.objectledge.coral.security.RoleAssignment} and 
 * {@link org.objectledge.coral.security.PermissionAssignment}.
 *
 * <p>This interface exposes the common attributes of assigments -- the subject
 * that performed the assignment and the time that the assigment was made. </p>
 *
 * @version $Id: Assignment.java,v 1.2 2005-02-08 20:34:25 rafal Exp $
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
