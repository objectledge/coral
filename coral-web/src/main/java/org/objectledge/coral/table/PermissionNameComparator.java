package org.objectledge.coral.table;

import java.util.Locale;

import org.objectledge.coral.security.Permission;

/**
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: PermissionNameComparator.java,v 1.1 2004-03-23 11:44:30 pablo Exp $ 
 */
public class PermissionNameComparator
	 extends BaseStringComparator
{
    public PermissionNameComparator(Locale locale)
    {
        super(locale);
    }

    public int compare(Object o1, Object o2)
    {
        if (!((o1 instanceof Permission && o2 instanceof Permission)))
        {
            return 0;
        }
        Permission perm1 = (Permission)o1;
        Permission perm2 = (Permission)o2;
        return compareStrings(perm1.getName(), perm2.getName());
    }
}
