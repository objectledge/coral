package org.objectledge.coral.table.comparator;

import java.util.Locale;

import org.objectledge.coral.security.Role;
import org.objectledge.table.comparator.BaseStringComparator;

/**
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: RoleNameComparator.java,v 1.3 2005-02-21 14:04:29 rafal Exp $ 
 */
public class RoleNameComparator
	 extends BaseStringComparator
{
    /**
     * Creates new RoleNameComparator instance.
     * 
     * @param locale the locale to use.
     */
    public RoleNameComparator(Locale locale)
    {
        super(locale);
    }

    /**
     * {@inheritDoc} 
     */
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
