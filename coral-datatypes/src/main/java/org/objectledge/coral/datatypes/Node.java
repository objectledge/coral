package org.objectledge.coral.datatypes;

import org.objectledge.coral.store.Resource;

/**
 * Defines the accessor methods of <code>coral.Node</code> Coral resource class.
 *
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: Node.java,v 1.1 2004-04-22 18:00:59 zwierzem Exp $
 */
public interface Node
    extends Resource
{
    // constants /////////////////////////////////////////////////////////////

    /** The name of the Coral resource class. */    
    public static final String CLASS_NAME = "coral.Node";

    // public interface //////////////////////////////////////////////////////
 
    /**
     * Returns the value of the <code>description</code> attribute.
     *
     * @return the value of the the <code>description</code> attribute.
     */
    public String getDescription();

    /**
     * Sets the value of the <code>description</code> attribute.
     *
     * @param value the value of the <code>description</code> attribute,
     *        or <code>null</code> to remove value.
     */
    public void setDescription(String value);   
     
    // @custom methods ///////////////////////////////////////////////////////
}
