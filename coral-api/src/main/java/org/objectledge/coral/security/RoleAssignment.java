package org.objectledge.coral.security;

import org.objectledge.coral.entity.Assignment;

/**
 * Reprensents an assignment of a {@link Role} to a {@link Subject}.
 *
 * @version $Id: RoleAssignment.java,v 1.1 2004-02-18 14:21:27 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface RoleAssignment
    extends Assignment
{
    /**
     * Returns the subject involved in this role assignment.
     *
     * @return the subject involved in this role assignment.
     */
    public Subject getSubject();

    /**
     * Returns the role involved this role assigment.
     *
     * @return the role involved this role assigment.
     */
    public Role getRole();

    /**
     * Returns <code>true</code> if the subject is allowed to grant the role to
     * other subjects.
     *
     * @return <code>true</code> if the subject is allowed to grant the role to
     * other subjects.
     */
    public boolean isGrantingAllowed();
}
