package org.objectledge.coral.table.comparator;

import java.util.Locale;

import org.objectledge.coral.security.Permission;
import org.objectledge.table.comparator.BaseStringComparator;

/**
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: PermissionNameComparator.java,v 1.3 2005-02-21 14:04:29 rafal Exp $ 
 */
public class PermissionNameComparator
	 extends BaseStringComparator
{
    /**
     * Creates new PermissionNameComparator instance.
     * 
     * @param locale the locle to use.
     */
    public PermissionNameComparator(Locale locale)
    {
        super(locale);
    }

    /**
     * {@inheritDoc}
     */
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
