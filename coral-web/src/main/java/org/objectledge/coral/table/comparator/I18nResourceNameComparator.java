package org.objectledge.coral.table.comparator;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.objectledge.coral.i18n.CoralI18nTool;
import org.objectledge.coral.store.Resource;

/**
 * This is a comparator for comparing localized coral resource names.
 *
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: I18nResourceNameComparator.java,v 1.4 2004-08-19 16:37:09 zwierzem Exp $
 */
public class I18nResourceNameComparator
    extends NameComparator
{
    private Map nameCache = new HashMap();
    private CoralI18nTool i18nTool;
    private Locale locale;
    
    public I18nResourceNameComparator(CoralI18nTool i18nTool, Locale locale)
    {
        super(locale);
        this.i18nTool = (CoralI18nTool) i18nTool.useLocale(locale.toString());
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
                name = i18nTool.getName((Resource) o);
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
