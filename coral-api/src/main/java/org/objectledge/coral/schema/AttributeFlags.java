package org.objectledge.coral.schema;

import java.util.StringTokenizer;

/**
 * Contains attribute flag constants and methods for coverting them to
 * printable strings and back.
 *
 * @version $Id: AttributeFlags.java,v 1.3 2005-02-21 15:43:36 zwierzem Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class AttributeFlags
{
    /** The <code>REQUIRED</code> flag. 
     * An attribute that must have a definite value at all times.
     */
    public static final int REQUIRED = 0x01;
    
    /** The <code>READONLY</code> flag. 
     * An attribute that may be written to only upon creation of the
     * resource. 
     */
    public static final int READONLY = 0x02;

    /** The <code>DESCRIPTIVE</code> flag. 
     * An attribute that is readable to humans, and proviodes important
     * information. A hint for generic resource browsing tools.
     */
    public static final int DESCRIPTIVE = 0x04;

    /** The <code>BUILTIN</code> flag. 
     * An attribute like 'name', 'parent' or 'resource_class' that is handled
     * in a special way by the system. You should never use this flag when
     * defining your application data structures.
     */
    public static final int BUILTIN = 0x08;
    
    /** The <code>INDEXABLE</code> flag. 
     * An attribute that may be useful for performing comperhensive searches
     * over the information by the system. A hint for generic
     * indexing/searching tools.
     */
    public static final int INDEXABLE = 0x10;    
    
    /** The <code>CLASS_UNIQUE</code> flag. 
     * An attribute that is unique among all resources belonging to the class.
     * <b>Reserved for future use.</b>
     */
    public static final int CLASS_UNIQUE = 0x20;
    
    /** The <code>SIBLINGS_UNIQUE</code> flag. 
     * An attribute that is unique among children of any signle resource
     * belonging to the class.
     * <b>Reserved for future use.</b>
     */
    public static final int SIBLINGS_UNIQUE = 0x40;

    /** The <code>SYNTHETIC</code> flag.
     * An attribute that is not persistent, but is calculated by at runtime by
     * the Java object representation of the resource. */
    public static final int SYNTHETIC = 0x80;

    /** The printable names of the flags. */
    private static final String[] FLAG_NAMES = {
        "REQUIRED", "READONLY", "DESCRIPTIVE", "BUILTIN", 
        "INDEXABLE", "CLASS_UNIQUE", "SIBLINGS_UNIQUE", "SYNTHETIC"
    };

    /**
     * Returns the names of all defined flags.
     * 
     * @return the names of all defined flags.
     */
    public String[] getFlagNames()
    {
        return FLAG_NAMES;
    }

    /**
     * Converts a flag value into a printable name.
     *
     * @param flag the flag.
     * @return the flag name.
     */
    public static String flagName(int flag)
    {
        for(int i=0; i<FLAG_NAMES.length; i++)
        {
            int f = 1<<i;
            if((flag & f) == flag)
            {
                return FLAG_NAMES[i];
            }
        }
        throw new IllegalArgumentException("flag 0x"+Integer.toString(flag, 16)+" undefined");
    }

    /**
     * Converts a flag name into a value.
     *
     * @param name the name.
     * @return the flag value.
     */
    public static int flagValue(String name)
    {
        for(int i=0; i<FLAG_NAMES.length; i++)
        {
            if(FLAG_NAMES[i].equals(name))
            {
                return 1<<i;
            }
        }
        throw new IllegalArgumentException("flag "+name+" undefined");
    }

    /**
     * Converts a flag vector into a string.
     *
     * @param flags the flag vector.
     * @return string representation of flag vector.
     */
    public static String toString(int flags)
    {
        StringBuilder buff = new StringBuilder();
        for(int i=0; i<FLAG_NAMES.length; i++)
        {
            int f = 1<<i;
            if((flags & f) == f)
            {
                buff.append(FLAG_NAMES[i]);
                buff.append(" ");
            }
        }
        if(buff.length() > 0)
        {
            // strip extra space charcter
            buff.setLength(buff.length()-1);
        }
        return buff.toString();
    }

    /**
     * Converts a string into a flag vector.
     *
     * @param string the string.
     * @return flag vector value.
     */
    public static int parseFlags(String string)
    {
        StringTokenizer tokens = new StringTokenizer(string);
        int vector = 0;
        while(tokens.hasMoreTokens())
        {
            vector |= flagValue(tokens.nextToken());
        }
        return vector;
    }
}
