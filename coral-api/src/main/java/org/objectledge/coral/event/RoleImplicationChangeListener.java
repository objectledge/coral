package org.objectledge.coral.event;

import org.objectledge.coral.security.RoleImplication;

/**
 * Is notified of role implication changes on a particular, or all
 * roles. 
 *
 * @see org.objectledge.event.EventWhiteboard#addListener(Class,Object,Object)
 * @see org.objectledge.event.EventWhiteboard#removeListener(Class,Object,Object)
 * @version $Id: RoleImplicationChangeListener.java,v 1.2 2005-02-08 20:34:21 rafal Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface RoleImplicationChangeListener
{
    /**
     * Called when role implications change.
     *
     * @param implication the {@link RoleImplication}.
     * @param added <code>true</code> if the implication was added,
     *        <code>false</code> if removed.
     */
    public void roleChanged(RoleImplication implication, boolean added);
}
