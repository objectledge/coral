package org.objectledge.coral.table.comparator;

import java.util.Locale;

import org.objectledge.coral.security.Role;

/**
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: RoleNameComparator.java,v 1.1 2004-04-22 12:56:24 zwierzem Exp $ 
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
