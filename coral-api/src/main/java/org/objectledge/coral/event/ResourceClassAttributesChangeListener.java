package org.objectledge.coral.event;

import org.objectledge.coral.schema.AttributeDefinition;

/**
 * Is notified of resource class inherintance changes on a particular, or all 
 * resource classes.
 *
 * @see EventService#addListener(Class,Object,Object)
 * @see EventService#removeListener(Class,Object,Object)
 * @version $Id: ResourceClassAttributesChangeListener.java,v 1.1 2004-02-18 14:21:27 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface ResourceClassAttributesChangeListener
{
    /**
     * Called when resource class attribute declarations change.
     *
     * @param attribute the {@link AttributeDefinition}.
     * @param added <code>true</code> if the attribute was added,
     *        <code>false</code> if removed.
     */
    public void attributesChanged(AttributeDefinition attribute, boolean added);
}
