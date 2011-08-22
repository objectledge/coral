package org.objectledge.coral.table.filter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.objectledge.coral.store.ParentsVisitor;
import org.objectledge.coral.store.Resource;
import org.objectledge.table.TableFilter;

/**
 * Table filter that accepts only resources from predetermined set (optionally their ancestors).
 * 
 * @author rafal
 */
public class ResourceSetFilter
    implements TableFilter<Resource>
{
    private final Set<Resource> acceptedSet;
    
    /**
     * Creates a new filter instance
     * 
     * @param res A set of resources
     * @param ancestors include ancestors of the resources in the set if <code>true</code>.
     */
    public <T extends Resource> ResourceSetFilter(Collection<T> res, boolean ancestors)
    {
        if(ancestors)
        {
            acceptedSet = resourcesWithAncestors(res);
        }
        else
        {
            acceptedSet = new HashSet<Resource>(res);
        }
    }

    public boolean accept(Resource res)
    {
        return acceptedSet.contains(res);
    }

    private static <T extends Resource> Set<Resource> resourcesWithAncestors(Collection<T> res)
    {
        final Set<Resource> resourcesWithAncestors = new HashSet<Resource>();
        ParentsVisitor visitor = new ParentsVisitor()
            {
                @SuppressWarnings("unused") // accessed through reflection
                public void visit(Resource rr)
                {
                    resourcesWithAncestors.add(rr);
                }
            };
        for (Resource r : res)
        {
            visitor.traverseBreadthFirst(r);
        }
        return resourcesWithAncestors;
    }
}
