package org.objectledge.coral.schema;

import org.objectledge.coral.entity.Entity;

/**
 * Represents an attribute type.
 *
 * <p>Resources are composed of attributes.
 * 
 * @version $Id: AttributeClass.java,v 1.1 2004-02-18 14:21:27 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface AttributeClass<T>
    extends Entity
{
    /**
     * Returns the Java class that is associated with this resource attribute
     * type.
     *
     * @return the Java class that is associated with this resource attribute
     * type.
     */
    public Class<T> getJavaClass();

    /**
     * Returns the AttributeHandler implementaion that will manage the
     * attributes of that class.
     *
     * @return an <code>AttributeHandler</code> implementation.
     */
    public AttributeHandler<T> getHandler();

    /**
     * Return the name of a database table that holds the values of the
     * attributes of that type.
     *
     * @return the name of a database table that holds the values of the
     * attributes of that type.
     */
    public String getDbTable();
}
