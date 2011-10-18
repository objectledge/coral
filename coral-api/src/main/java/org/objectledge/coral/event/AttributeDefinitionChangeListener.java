package org.objectledge.coral.event;

import org.objectledge.coral.schema.AttributeDefinition;

/**
 * Is notified of changes to a <code>AttributeDefinition</code> data.
 *
 * @see org.objectledge.event.EventWhiteboard#addListener(Class,Object,Object)
 * @see org.objectledge.event.EventWhiteboard#removeListener(Class,Object,Object)
 * @version $Id: AttributeDefinitionChangeListener.java,v 1.3 2005-02-08 20:34:21 rafal Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface AttributeDefinitionChangeListener
{
    /**
     * Called when <code>AttributeDefinition</code>'s data change.
     *
     * @param attributeDefinition the attribute that changed.
     */
    public void attributeDefinitionChanged(AttributeDefinition<?> attributeDefinition);
}
