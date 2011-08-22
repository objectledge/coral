package org.objectledge.coral.security;

import org.objectledge.coral.entity.Association;

/**
 * Represents implication relationship between two roles.
 *
 * @version $Id: RoleImplication.java,v 1.1 2004-02-18 14:21:27 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface RoleImplication
    extends Association
{
    /**
     * Returns the implicating/containing role.
     *
     * @return the implicating/containing role.
     */
    public Role getSuperRole();
    
    /**
     * Returns the implied/contained role.
     *
     * @return the implied/contained role.
     */
    public Role getSubRole();
}


    
    
