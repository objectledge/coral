package org.objectledge.coral.event;

import org.objectledge.coral.schema.ResourceClassInheritance;

/**
 * Is notified of resource class inherintance changes on a particular, or all 
 * resource classes.
 *
 * @see EventService#addListener(Class,Object,Object)
 * @see EventService#removeListener(Class,Object,Object)
 * @version $Id: ResourceClassInheritanceChangeListener.java,v 1.1 2004-02-18 14:21:27 fil Exp $
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
