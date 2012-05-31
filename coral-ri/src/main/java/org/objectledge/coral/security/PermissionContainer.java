package org.objectledge.coral.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.objectledge.coral.BackendException;
import org.objectledge.coral.CoralCore;
import org.objectledge.coral.event.CoralEventHub;
import org.objectledge.coral.event.PermissionAssignmentChangeListener;
import org.objectledge.coral.event.ResourceTreeChangeListener;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ResourceInheritance;

import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * A helper class for managing a set of permissions.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: PermissionContainer.java,v 1.11 2008-01-20 15:44:06 rafal Exp $
 */
public class PermissionContainer
    implements PermissionAssignmentChangeListener, ResourceTreeChangeListener
{
    // Instance variables ///////////////////////////////////////////////////////////////////////

    /** The component hub. */
    private CoralCore coral;

    /** The CoralEventHub. */
    private CoralEventHub coralEventHub;

    /** The role container. */
    private RoleContainer roles;

    /** Cache of permission information */
    private LoadingCache<Resource, PermissionsInfo> piCache = CacheBuilder.newBuilder().weakKeys()
        .build(CacheLoader.from(new ResourceToPermissionsInfo()));

    // Initialization ///////////////////////////////////////////////////////////////////////////

    /**
     * Constructs a permission container.
     * 
     * @param coralEventHub the CoralEventHub.
     * @param coral the Coral core component.
     * @param roles a RoleContainer.
     */
    PermissionContainer(CoralEventHub coralEventHub, CoralCore coral, RoleContainer roles)
    {
        this.coralEventHub = coralEventHub;
        this.coral = coral;
        this.roles = roles;
        roles.setPermissionContainer(this);
    }

    // Permissions //////////////////////////////////////////////////////////////////////////////

    /**
     * Return all permission the entity this permission container belongs to has on a specific
     * resource.
     * 
     * @param res the resource.
     * @return permission array.
     */
    public Permission[] getPermissions(Resource res)
    {
        List<PermissionsInfo> piList = getPermissionsInfo(res);
        List<Permission> pList = new ArrayList<Permission>();
        PermissionsInfo piRes = piList.get(0);
        for (PermissionsInfo pi : piList)
        {
            pi.addTo(pList, pi == piRes);
        }
        return pList.toArray(new Permission[pList.size()]);
    }

    /**
     * Check if the entity this container describes has a specific permission on a specific
     * resource.
     * 
     * @param res the resource.
     * @param perm the permission.
     * @return <code>true</code> if the entity has the permission.
     */
    public boolean hasPermission(Resource res, Permission perm)
    {
        List<PermissionsInfo> piList = getPermissionsInfo(res);
        PermissionsInfo piRes = piList.get(0);
        for (PermissionsInfo pi : piList)
        {
            if(pi.hasPermission(perm, pi == piRes))
            {
                return true;
            }
        }
        return false;
    }

    // PermissionAssignmentChangeListener interface /////////////////////////////////////////////

    /**
     * Called when permission assignments on a watched resource change.
     * <p>
     * Flushes cached permission information on the involved resource and all of it's child
     * resources.
     * </p>
     * 
     * @param item permission assignment information.
     * @param added was the permission added, or removed?
     */
    public void permissionsChanged(PermissionAssignment item, boolean added)
    {
        flush(item.getResource());
    }

    // ResourceTreeChangeListener interface /////////////////////////////////////////////////////

    /**
     * Called when resource tree changes.
     * <p>
     * Flushes cached permission information on the child resource in the relationship and all of it
     * child resources.
     * </p>
     * 
     * @param item resource relationship information.
     * @param added was the relationship added or removed.
     */
    public void resourceTreeChanged(ResourceInheritance item, boolean added)
    {
        flush(item.getChild());
        if(added)
        {
            coralEventHub.getGlobal().addResourceTreeChangeListener(this, item.getParent());
            coralEventHub.getGlobal().addPermissionAssignmentChangeListener(this, item.getParent());
        }
    }

    // private methods //////////////////////////////////////////////////////////////////////////

    /**
     * Returns the chain of permissions info starting from res all the way up to hierarchy root.
     * Always returns at least one element. PermissionsInfo for res is always the first element.
     */
    private List<PermissionsInfo> getPermissionsInfo(Resource res)
    {
        try
        {
            List<PermissionsInfo> piList = new ArrayList<PermissionsInfo>();
            Resource cur = res;
            while(cur != null)
            {
                piList.add(piCache.get(cur));
                cur = cur.getParent();
            }
            return piList;
        }
        catch(ExecutionException e)
        {
            throw new BackendException("unexpected exception while generating permission info", e.getCause());
        }
    }

    /**
     * Flushes permission information on all resources.
     * <p>
     * This method is called by the peer RoleContainer when role information (implications /
     * assignments) change.
     * </p>
     */
    void flush()
    {
        piCache.invalidateAll();
    }

    /**
     * Flushes permission information on a particular resource and it's children.
     * 
     * @param r the resource to flush permissions information on.
     */
    void flush(Resource r)
    {
        piCache.invalidate(r);
        // we assume than number of PermissionInfo entries is significantly smaller than number of resource's descendants
        Iterator<Resource> i = piCache.asMap().keySet().iterator();
        while(i.hasNext())
        {
            Resource res = i.next();
            if(coral.getStore().isAncestor(r, res))
            {
                i.remove();
            }
        }
    }
    
    private class ResourceToPermissionsInfo
        implements Function<Resource, PermissionsInfo>
    {
        @Override
        public PermissionsInfo apply(Resource res)
        {
            coralEventHub.getGlobal().addPermissionAssignmentChangeListener(
                PermissionContainer.this, res);
            coralEventHub.getGlobal().addResourceTreeChangeListener(PermissionContainer.this, res);
            
            return new PermissionsInfo(roles.getMatchingRoles(), coral.getRegistry()
                .getPermissionAssignments(res));
        }
    }

    private static class PermissionsInfo
    {
        private Set<Permission> inherited;

        private Set<Permission> nonInherited;

        public PermissionsInfo(Set<Role> roleSet, Set<PermissionAssignment> paSet)
        {
            for (PermissionAssignment pa : paSet)
            {
                if(roleSet.contains(pa.getRole()))
                {
                    if(pa.isInherited())
                    {
                        if(inherited == null)
                        {
                            inherited = new HashSet<Permission>();
                        }
                        inherited.add(pa.getPermission());
                    }
                    else
                    {
                        if(nonInherited == null)
                        {
                            nonInherited = new HashSet<Permission>();
                        }
                        nonInherited.add(pa.getPermission());
                    }
                }
            }
        }

        public boolean hasPermission(Permission p, boolean includeNonInherited)
        {
            return (inherited != null && inherited.contains(p))
                || (includeNonInherited && nonInherited != null && nonInherited.contains(p));
        }

        public void addTo(Collection<Permission> pCol, boolean includeNotInherited)
        {
            if(inherited != null)
            {
                pCol.addAll(inherited);
            }
            if(includeNotInherited && nonInherited != null)
            {
                pCol.addAll(nonInherited);
            }
        }
    }
}
