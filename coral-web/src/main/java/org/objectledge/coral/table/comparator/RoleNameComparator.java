package org.objectledge.coral.table.comparator;

import java.util.Locale;

import org.objectledge.coral.security.Role;
import org.objectledge.table.comparator.BaseStringComparator;

/**
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: RoleNameComparator.java,v 1.2 2005-02-14 17:26:32 pablo Exp $ 
 */
public class RoleNameComparator
	 extends BaseStringComparator
{
    public RoleNameComparator(Locale locale)
    {
        super(locale);
    }

    public int compare(Object o1, Object o2)
    {
        if (!((o1 instanceof Role && o2 instanceof Role)))
        {
            return 0;
        }
        Role role1 = (Role)o1;
        Role role2 = (Role)o2;
        return compareStrings(role1.getName(), role2.getName());
    }
}
