package org.objectledge.coral.table.filter;

import org.objectledge.coral.store.Resource;
import org.objectledge.table.TableFilter;

/**
 * This is a filter for filtering resources upon their paths.
 *
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: PathFilter.java,v 1.1 2004-04-22 12:56:24 zwierzem Exp $
 */
public class PathFilter
    implements TableFilter
{
    protected String[] paths;

    public PathFilter(Resource root, String[] acceptedPaths)
    {
        String basePath = root.getPath();
        paths = new String[acceptedPaths.length];

        for(int i=0; i<acceptedPaths.length; i++)
        {
            String accPath = acceptedPaths[i];
            if(accPath.charAt(accPath.length()-1) == '/')
            {
                accPath = accPath.substring(0, accPath.length()-1);
            }

            if(accPath.charAt(0) == '/')
            {
                paths[i] = accPath;
            }
            else
            {
                paths[i] = basePath + '/' + accPath;
            }
        }
    }

    public boolean accept(Object object)
    {
        if(!(object instanceof Resource))
        {
            return false;
        }

        // TODO: Add sort of a dictionary for fast path comparation
        // maybe a tree of names with termination/acceptation field
        String path = ((Resource)object).getPath(); 
        for(int i=0; i<paths.length; i++)
        {
            if(path.startsWith(paths[i]))
            {
                return true;
            }
        }

        return false;
    }
}
