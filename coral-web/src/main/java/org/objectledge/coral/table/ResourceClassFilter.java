package org.objectledge.coral.table;

import java.util.HashSet;
import java.util.Set;

import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.store.Resource;
import org.objectledge.table.TableFilter;

/**
 * This is a filter for filtering resources upon related resource class.
 *
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: ResourceClassFilter.java,v 1.1 2004-03-23 11:44:30 pablo Exp $
 */
public class ResourceClassFilter
    implements TableFilter
{
    protected Set acceptedResourceClasses;

    public ResourceClassFilter(ResourceClass acceptedResourceClass)
    {
        this(acceptedResourceClass, false);
    }

    public ResourceClassFilter(ResourceClass acceptedResourceClass, boolean allowInheritance)
    {
        ResourceClass[] resClasses = new ResourceClass[1];
        resClasses[0] = acceptedResourceClass;
        init(resClasses, allowInheritance);
    }
    
    public ResourceClassFilter(ResourceClass[] acceptedResourceClasses)
    {
        this(acceptedResourceClasses, false);
    }
    
    public ResourceClassFilter(ResourceClass[] acceptedResourceClasses, boolean allowInheritance)
    {
        init(acceptedResourceClasses, allowInheritance);
    }
    
    protected void init(ResourceClass[] acceptedResourceClasses, boolean allowInheritance)
    {
        this.acceptedResourceClasses = new HashSet();
        for(int i=0; i<acceptedResourceClasses.length; i++)
        {
            this.acceptedResourceClasses.add(acceptedResourceClasses[i]);
        }
        
        if(allowInheritance)
        {
            Set childClasses = new HashSet();
            for(int i=0; i<acceptedResourceClasses.length; i++)
            {
                getChildClasses(acceptedResourceClasses[i], childClasses);
            }
            this.acceptedResourceClasses.addAll(childClasses);
        }
    }
    
    private void getChildClasses(ResourceClass rc, Set childClasses)
    {
        ResourceClass[] classes = rc.getChildClasses();
        for(int i=0; i<classes.length; i++)
        {
            childClasses.add(classes[i]);
            getChildClasses(classes[i], childClasses);
        }
    }
    
    public boolean accept(Object object)
    {
        if(!(object instanceof Resource))
        {
            return false;
        }
        return acceptedResourceClasses.contains(((Resource)object).getResourceClass());
    }
}
