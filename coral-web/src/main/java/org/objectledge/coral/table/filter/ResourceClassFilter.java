package org.objectledge.coral.table.filter;

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
 * @version $Id: ResourceClassFilter.java,v 1.2 2005-02-21 14:04:32 rafal Exp $
 */
public class ResourceClassFilter<R extends Resource>
    implements TableFilter<R>
{
    /** the accepted resource classes. */
    protected Set<ResourceClass<?>> acceptedResourceClasses;

    /**
     * Creates new ResourceClassFilter instance.
     * 
     * @param acceptedResourceClasses accepted resource classes.
     * @param allowInheritance should child classes be accepted as well?
     */
    public ResourceClassFilter(Set<ResourceClass<?>> acceptedResourceClasses,
        boolean allowInheritance)
    {
        this.acceptedResourceClasses = acceptedResourceClasses;
        if(allowInheritance)
        {
            Set<ResourceClass<?>> childClasses = new HashSet<>();
            for(ResourceClass<?> rc : acceptedResourceClasses)
            {
                getChildClasses(rc, childClasses);
            }
            this.acceptedResourceClasses.addAll(childClasses);
        }
    }

    /**
     * Initializes the filter.
     * 
     * @param acceptedResourceClasses the requested resource classes.
     * @param allowInheritance should child classes be accepted as well?
     */
    protected void init(ResourceClass<?>[] acceptedResourceClasses, boolean allowInheritance)
    {
        this.acceptedResourceClasses = new HashSet<>();
        for(int i=0; i<acceptedResourceClasses.length; i++)
        {
            this.acceptedResourceClasses.add(acceptedResourceClasses[i]);
        }
        

    }
    
    private void getChildClasses(ResourceClass<?> rc, Set<ResourceClass<?>> childClasses)
    {
        ResourceClass<?>[] classes = rc.getChildClasses();
        for(int i=0; i<classes.length; i++)
        {
            childClasses.add(classes[i]);
            getChildClasses(classes[i], childClasses);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean accept(R res)
    {
        return acceptedResourceClasses.contains(res.getResourceClass());
    }
}
