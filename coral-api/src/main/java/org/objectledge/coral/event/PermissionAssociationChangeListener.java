package org.objectledge.coral.event;

import org.objectledge.coral.security.PermissionAssociation;

/**
 * Is notified of permission association changes on a particular, or all
 * resourceClass / permission.
 *
 * @see EventService#addListener(Class,Object,Object)
 * @see EventService#removeListener(Class,Object,Object)
 *
 * @version $Id: PermissionAssociationChangeListener.java,v 1.1 2004-02-18 14:21:27 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public interface PermissionAssociationChangeListener
{
    /**
     * Called when permission associations on a resource class / perimission change.
     *
     * @param resource the resource.
     * @param pa the permission association.
     * @param added <code>true</code> if the permission was added,
     *        <code>false</code> if removed.
     */
    public void permissionsChanged(PermissionAssociation association, boolean added);
}
