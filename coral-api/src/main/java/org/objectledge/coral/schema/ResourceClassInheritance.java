package org.objectledge.coral.schema;

import org.objectledge.coral.entity.Association;

/**
 * Represents resource class inheritance relationship.
 *
 * @version $Id: ResourceClassInheritance.java,v 1.1 2004-02-18 14:21:27 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface ResourceClassInheritance
    extends Association
{
    /**
     * Returns the parent class in this relationship.
     *
     * @return the parent class in this relationship.
     */
    public ResourceClass getParent();
    
    /**
     * Returns the child class in this relationship.
     *
     * @return the child class in this relationship.
     */
    public ResourceClass getChild();
}
