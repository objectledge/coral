package org.objectledge.coral.security;

import org.objectledge.coral.entity.Association;
import org.objectledge.coral.schema.ResourceClass;

/**
 * Represents an association between a {@link Permission} and a {@link
 * ResourceClass}. 
 *
 * @version $Id: PermissionAssociation.java,v 1.1 2004-02-18 14:21:27 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface PermissionAssociation
    extends Association
{
    /**
     * Returns the involved {@link ResourceClass}.
     *
     * @return the involved {@link ResourceClass}.
     */
    public ResourceClass<?> getResourceClass();

    /**
     * Returns the involved {@link Permission}.
     *
     * @return the involved {@link Permission}.
     */
    public Permission getPermission();
}
