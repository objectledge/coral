package org.objectledge.coral.table;

import org.objectledge.coral.security.Permission;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.store.Resource;
import org.objectledge.table.TableFilter;

/**
 * This is a filter for filtering resources upon subjects permission
 * assignments.
 *
 * @author <a href="mailto:damian@caltha.pl">Damian Gajda</a>
 * @version $Id: PermissionAssignmentFilter.java,v 1.1 2004-03-23 11:44:30 pablo Exp $
 */
public class PermissionAssignmentFilter
    implements TableFilter
{
    private Subject subject;
    private Permission permission;

    public PermissionAssignmentFilter(Subject subject, Permission permission)
    {
        this.subject = subject;
        this.permission = permission;
    }

    public boolean accept(Object object)
    {
        if(!(object instanceof Resource))
        {
            return false;
        }
        return subject.hasPermission((Resource)object, permission);
    }
}
