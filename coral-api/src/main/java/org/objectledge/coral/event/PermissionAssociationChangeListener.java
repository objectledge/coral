package org.objectledge.coral.event;

import org.objectledge.coral.security.PermissionAssociation;

/**
 * Is notified of permission association changes on a particular, or all
 * resourceClass / permission.
 *
 * @see org.objectledge.event.EventWhiteboard#addListener(Class,Object,Object)
 * @see org.objectledge.event.EventWhiteboard#removeListener(Class,Object,Object)
 *
 * @version $Id: PermissionAssociationChangeListener.java,v 1.3 2005-02-08 20:34:21 rafal Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface PermissionAssociationChangeListener
{
    /**
     * Called when permission associations on a resource class / perimission change.
     *
     * @param association the permission association.
     * @param added <code>true</code> if the permission was added,
     *        <code>false</code> if removed.
     */
    public void permissionsChanged(PermissionAssociation association, boolean added);
}
