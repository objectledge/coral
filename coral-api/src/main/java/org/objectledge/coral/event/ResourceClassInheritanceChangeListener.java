package org.objectledge.coral.event;

import org.objectledge.coral.schema.ResourceClassInheritance;

/**
 * Is notified of resource class inherintance changes on a particular, or all 
 * resource classes.
 *
 * @see org.objectledge.event.EventWhiteboard#addListener(Class,Object,Object)
 * @see org.objectledge.event.EventWhiteboard#removeListener(Class,Object,Object)
 * @version $Id: ResourceClassInheritanceChangeListener.java,v 1.2 2005-02-08 20:34:21 rafal Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface ResourceClassInheritanceChangeListener
{
    /**
     * Called when resource class inheritance relationships change.
     *
     * @param inheritance the {@link ResourceClassInheritance}.
     * @param added <code>true</code> if the relationship was added,
     *        <code>false</code> if removed.
     */
    public void inheritanceChanged(ResourceClassInheritance inheritance, boolean added);
}
