package org.objectledge.coral.event;

import org.objectledge.coral.schema.ResourceClass;

/**
 * Is notified of changes to a <code>ResourceClass</code> data.
 *
 * @see EventService#addListener(Class,Object,Object)
 * @see EventService#removeListener(Class,Object,Object)
 * @version $Id: ResourceClassChangeListener.java,v 1.2 2004-02-18 15:08:21 fil Exp $
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
