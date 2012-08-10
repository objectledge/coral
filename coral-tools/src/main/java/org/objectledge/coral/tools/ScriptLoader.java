package org.objectledge.coral.tools;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.IOUtil;
import org.objectledge.filesystem.ClasspathFileSystemProvider;
import org.objectledge.filesystem.FileSystem;
import org.objectledge.filesystem.FileSystemProvider;

public class ScriptLoader
{
    private static FileSystem fs = new FileSystem(
        new FileSystemProvider[] { new ClasspathFileSystemProvider("classpath",
            ScriptLoader.class.getClassLoader()) }, 4069, 65536);

    public static void loadScripts(Object obj, String suffix)
    {
        try
        {
            Class<?> cl = obj.getClass();
            for(Field f : cl.getDeclaredFields())
            {
                final String dir = cl.getPackage().getName().replace('.', '/');
                final String clName = cl.getName().substring(cl.getName().lastIndexOf('.') + 1);
                final String resPrefix = clName + "$" + f.getName()
                    + (suffix != null ? "." + suffix : "");
                String res = findResource(dir, resPrefix, fs);
                if(res != null)
                {
                    Object value = null;
                    InputStream is = fs.getInputStream(dir + "/" + res);
                    if(f.getType().equals(String.class))
                    {
                        value = IOUtils.toString(is, "UTF-8");
                    }
                    if(f.getType().isArray() && f.getType().getComponentType().equals(Byte.TYPE))
                    {
                        value = IOUtil.toByteArray(is);
                    }
                    if(value != null)
                    {
                        f.setAccessible(true);
                        f.set(obj, value);
                    }
                }
            }
        }
        catch(Exception e)
        {
            throw new RuntimeException("Failed to load scripts", e);
        }
    }

    private static String findResource(String dir, String name, FileSystem fs)
        throws IOException
    {
        for(String f : fs.list(dir))
        {
            if(f.startsWith(name) && !f.endsWith("~"))
            {
                return f;
            }
        }
        return null;
    }
}
