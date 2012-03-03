package org.objectledge.coral.event;

import org.objectledge.coral.schema.AttributeClass;

/**
 * Is notified of changes to a <code>AttributeClass</code> data.
 *
 * @see org.objectledge.event.EventWhiteboard#addListener(Class,Object,Object)
 * @see org.objectledge.event.EventWhiteboard#removeListener(Class,Object,Object)
 * @version $Id: AttributeClassChangeListener.java,v 1.3 2005-02-08 20:34:21 rafal Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface AttributeClassChangeListener
{
    /**
     * Called when <code>AttributeClass</code>'s data change.
     *
     * @param attributeClass the attributeClass that changed.
     */
    public void attributeClassChanged(AttributeClass<?> attributeClass);
}
