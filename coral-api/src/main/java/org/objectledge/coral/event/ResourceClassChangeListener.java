package org.objectledge.coral.event;

import org.objectledge.coral.schema.ResourceClass;

/**
 * Is notified of changes to a <code>ResourceClass</code> data.
 *
 * @see org.objectledge.event.EventWhiteboard#addListener(Class,Object,Object)
 * @see org.objectledge.event.EventWhiteboard#removeListener(Class,Object,Object)
 * @version $Id: ResourceClassChangeListener.java,v 1.3 2005-02-08 20:34:21 rafal Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface ResourceClassChangeListener
{
    /**
     * Called when <code>ResourceClass</code>'s data change.
     *
     * @param resourceClass the resourceClass that changed.
     */
    public void resourceClassChanged(ResourceClass resourceClass);
}
