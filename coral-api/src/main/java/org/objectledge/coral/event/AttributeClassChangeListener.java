package org.objectledge.coral.event;

import org.objectledge.coral.schema.AttributeClass;

/**
 * Is notified of changes to a <code>AttributeClass</code> data.
 *
 * @see EventService#addListener(Class,Object,Object)
 * @see EventService#removeListener(Class,Object,Object)
 * @version $Id: AttributeClassChangeListener.java,v 1.2 2004-02-18 15:08:21 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface AttributeClassChangeListener
{
    /**
     * Called when <code>AttributeClass</code>'s data change.
     *
     * @param attributeClass the attributeClass that changed.
     */
    public void attributeClassChanged(AttributeClass attributeClass);
}
