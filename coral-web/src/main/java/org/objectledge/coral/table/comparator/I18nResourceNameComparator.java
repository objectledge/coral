package org.objectledge.coral.table.comparator;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.objectledge.coral.i18n.CoralI18nHelper;
import org.objectledge.coral.store.Resource;

/**
 * This is a comparator for comparing localized coral resource names.
 *
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: I18nResourceNameComparator.java,v 1.5 2005-02-14 18:13:45 pablo Exp $
 */
public class I18nResourceNameComparator
    extends NameComparator
{
    private Map nameCache = new HashMap();
    
    protected CoralI18nHelper coralI18nHelper;
    
    protected Locale locale;
    
    public I18nResourceNameComparator(CoralI18nHelper coralI18nHelper,
        Locale locale)
    {
        super(locale);
        this.coralI18nHelper = coralI18nHelper;
        this.locale = locale;
    }
    
    public int compare(Object o1, Object o2)
    {
        String name1 = getName(o1);
        String name2 = getName(o2);
        
        return compareStrings(name1, name2);
    }
    
    protected String getName(Object o)
    {
        String name = (String) nameCache.get(o);
        if(name == null)
        {
            if(o instanceof Resource)
            {
                name = coralI18nHelper.getName(locale, (Resource) o);
            }
            else
            {
                name = "";
            }
            nameCache.put(o, name);
        }
        return name;
    }
}
