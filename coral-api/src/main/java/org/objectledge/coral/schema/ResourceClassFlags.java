package org.objectledge.coral.schema;

import java.util.StringTokenizer;

/**
 * Contains resource class flag constants and methods for coverting them to
 * printable strings and back.
 *
 * @version $Id: ResourceClassFlags.java,v 1.1 2004-02-18 14:21:27 fil Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class ResourceClassFlags
{
    /** The <code>ABSTRACT</code> flag. 
     * Resources of that class may not be instantiated. 
     */
    public static final int ABSTRACT = 0x01;
    
    /** The <code>FINAL</code> flag. 
     * A class that may not be subclassed.
     */
    public static final int FINAL = 0x02;

    /** The <code>BUILTIN</code> flag. 
     * A class that is provided by the system. No wrappers for this class need
     * to be generated.
     */
    public static final int BUILTIN = 0x04;
    
    /** The printable names of the flags. */
    private static final String[] flagNames = {
        "ABSTRACT", "FINAL", "BUILTIN"
    };

    /**
     * Returns the names of all defined flags.
     * 
     * @return the names of all defined flags.
     */
    public String[] getFlagNames()
    {
        return flagNames;
    }

    /**
     * Converts a flag value into a printable name.
     *
     * @param flag the flag.
     */
    public static String flagName(int flag)
    {
        for(int i=0; i<flagNames.length; i++)
        {
            int f = 1<<i;
            if((flag & f) == flag)
            {
                return flagNames[i];
            }
        }
        throw new IllegalArgumentException("flag 0x"+Integer.toString(flag, 16)+" undefined");
    }

    /**
     * Converts a flag name into a value.
     *
     * @param name the name.
     */
    public static int flagValue(String name)
    {
        for(int i=0; i<flagNames.length; i++)
        {
            if(flagNames[i].equals(name))
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
     */
    public static String toString(int flags)
    {
        StringBuffer buff = new StringBuffer();
        for(int i=0; i<flagNames.length; i++)
        {
            int f = 1<<i;
            if((flags & f) == f)
            {
                buff.append(flagNames[i]);
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
