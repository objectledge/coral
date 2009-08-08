package org.objectledge.coral.schema;

import org.objectledge.coral.entity.Entity;

/**
 * Represents a concrete attribute of an resource class.
 *
 * @version $Id: AttributeDefinition.java,v 1.1 2004-02-18 14:21:27 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface AttributeDefinition
    extends Entity
{
    /**
     * Returns the class of this attribute.
     *
     * @return the class of this attribute.
     */
    public AttributeClass getAttributeClass();

    /**
     * Returns the resource class this attribute belongs to.
     *
     * @return the resource class this attribute belongs to.
     */
    public ResourceClass getDeclaringClass();
    
    /**
     * Returns the value domain constraint for the attribute.
     *
     * @return the value domain constraint for the attribute.
     */
    public String getDomain();

    /**
     * Returns the flags of this attribute.
     *
     * @return the flags of this attribute.
     */
    public int getFlags();
}
