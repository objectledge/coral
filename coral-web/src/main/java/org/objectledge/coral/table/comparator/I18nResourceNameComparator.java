package org.objectledge.coral.table.comparator;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.objectledge.coral.i18n.CoralI18nTool;
import org.objectledge.coral.store.Resource;
import org.objectledge.i18n.I18n;

/**
 * This is a comparator for comparing localized coral resource names.
 *
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: I18nResourceNameComparator.java,v 1.3 2004-08-19 16:15:35 zwierzem Exp $
 */
public class I18nResourceNameComparator
    extends NameComparator
{
    private Map nameCache = new HashMap();
    private I18n i18n;
    private Locale locale;
    
    public I18nResourceNameComparator(I18n i18n, Locale locale)
    {
        super(locale);
        this.i18n = i18n;
        this.locale = locale;
    }
    
    public int compare(Object o1, Object o2)
    {
        String name1 = getName(o1);
        String name2 = getName(o2);
        
        return compareStrings(name1, name2);
    }
    
    private String getName(Object o)
    {
        String name = (String) nameCache.get(o);
        if(name == null)
        {
            if(o instanceof Resource)
            {
                Resource r = (Resource) o;
                name = i18n.get(locale, CoralI18nTool.getNameKey(r), r.getName());
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
