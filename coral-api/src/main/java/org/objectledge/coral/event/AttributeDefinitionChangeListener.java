package org.objectledge.coral.event;

import org.objectledge.coral.schema.AttributeDefinition;

/**
 * Is notified of changes to a <code>AttributeDefinition</code> data.
 *
 * @see EventService#addListener(Class,Object,Object)
 * @see EventService#removeListener(Class,Object,Object)
 * @version $Id: AttributeDefinitionChangeListener.java,v 1.1 2004-02-18 14:21:27 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface AttributeDefinitionChangeListener
{
    /**
     * Called when <code>AttributeDefinition</code>'s data change.
     *
     * @param AttributeDefinition the attribute that changed.
     */
    public void attributeDefinitionChanged(AttributeDefinition attributeDefinition);
}
