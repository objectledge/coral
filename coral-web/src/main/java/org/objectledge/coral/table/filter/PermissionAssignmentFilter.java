package org.objectledge.coral.table.filter;

import org.objectledge.coral.security.Permission;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.Resource;
import org.objectledge.table.TableFilter;

/**
 * This is a filter for filtering resources upon subjects permission
 * assignments.
 *
 * @author <a href="mailto:damian@caltha.pl">Damian Gajda</a>
 * @version $Id: PermissionAssignmentFilter.java,v 1.2 2005-02-21 14:04:32 rafal Exp $
 */
public class PermissionAssignmentFilter
    implements TableFilter
{
    private Subject subject;
    private Permission permission;

    /**
     * Creates new PermissionAssignmentFilter instance.
     * 
     * @param subject the requested subject.
     * @param permission the requested premission.
     */
    public PermissionAssignmentFilter(Subject subject, Permission permission)
    {
        this.subject = subject;
        this.permission = permission;
    }

    /**
     * {@inheritDoc}
     */
    public boolean accept(Object object)
    {
        if(!(object instanceof Resource))
        {
            return false;
        }
        return subject.hasPermission((Resource)object, permission);
    }
}
