package org.objectledge.coral.table.comparator;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.objectledge.coral.i18n.CoralI18nHelper;
import org.objectledge.coral.store.Resource;
import org.objectledge.table.comparator.BaseStringComparator;

/**
 * This is a comparator for comparing localized coral resource names.
 * 
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: I18nResourceNameComparator.java,v 1.7 2008-06-05 16:37:58 rafal Exp $
 */
public class I18nResourceNameComparator
    extends BaseStringComparator<Resource>
{
    private Map<Resource, String> nameCache = new HashMap<Resource, String>();

    /** the coral i18n helper. */
    protected CoralI18nHelper coralI18nHelper;

    /** the locale being used. */
    protected Locale locale;

    /**
     * Creates new I18nResourceNameComparator instance.
     * 
     * @param coralI18nHelper the coral i18n helper.
     * @param locale the locale to use.
     */
    public I18nResourceNameComparator(CoralI18nHelper coralI18nHelper, Locale locale)
    {
        super(locale);
        this.coralI18nHelper = coralI18nHelper;
        this.locale = locale;
    }

    /**
     * {@inheritDoc}
     */
    public int compare(Resource o1, Resource o2)
    {
        String name1 = getName(o1);
        String name2 = getName(o2);

        return compareStrings(name1, name2);
    }

    /**
     * Returns the name of an object.
     * 
     * @param r the object.
     * @return name of the object, or empty string if unknown.
     */
    protected String getName(Resource r)
    {
        String name = (String)nameCache.get(r);
        if(name == null)
        {
            name = coralI18nHelper.getName(locale, r);
            nameCache.put(r, name);
        }
        return name;
    }
}
