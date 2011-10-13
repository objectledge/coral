package org.objectledge.coral.event;

import org.objectledge.coral.schema.AttributeDefinition;

/**
 * Is notified of resource class inherintance changes on a particular, or all 
 * resource classes.
 *
 * @see org.objectledge.event.EventWhiteboard#addListener(Class,Object,Object)
 * @see org.objectledge.event.EventWhiteboard#removeListener(Class,Object,Object)
 * @version $Id: ResourceClassAttributesChangeListener.java,v 1.2 2005-02-08 20:34:21 rafal Exp $
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
    public void attributesChanged(AttributeDefinition<?> attribute, boolean added);
}
