package org.objectledge.coral.table.comparator;

import java.util.Locale;

import org.objectledge.coral.i18n.CoralI18nTool;
import org.objectledge.coral.store.Resource;
import org.objectledge.i18n.I18n;

/**
 * This is a comparator for comparing localized coral resource names.
 *
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: I18nResourceNameComparator.java,v 1.1 2004-07-16 14:32:29 zwierzem Exp $
 */
public class I18nResourceNameComparator
    extends BaseStringComparator
{
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
        if(!((o1 instanceof Resource && o2 instanceof Resource )))
        {
            return 0;
        }

        Resource r1 = (Resource)o1;
        Resource r2 = (Resource)o2;
        
        return compareStrings(
            i18n.get(locale, CoralI18nTool.getNameKey(r1), r1.getName()),
            i18n.get(locale, CoralI18nTool.getNameKey(r2), r2.getName()));
    }
}
