package org.objectledge.coral.security;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.objectledge.coral.CoralCore;
import org.objectledge.coral.event.CoralEventHub;
import org.objectledge.coral.event.PermissionAssignmentChangeListener;
import org.objectledge.coral.event.ResourceTreeChangeListener;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ResourceInheritance;
import org.objectledge.coral.store.ResourceRef;

import bak.pcj.list.LongArrayDeque;
import bak.pcj.list.LongDeque;
import bak.pcj.list.LongList;
import bak.pcj.set.LongOpenHashSet;
import bak.pcj.set.LongSet;

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
    private ConcurrentMap<ResourceRef, PermissionsInfo> piCache = new ConcurrentHashMap<ResourceRef, PermissionsInfo>();

    private final ReferenceQueue<Resource> queue = new ReferenceQueue<Resource>();

    private static final int DRAIN_LIMIT = 16;

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
        for(PermissionsInfo pi : piList)
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
        for(PermissionsInfo pi : piList)
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
        drainQueue();
        List<PermissionsInfo> piList = new ArrayList<PermissionsInfo>();
        Resource cur = res;
        LongDeque resourceChain = new LongArrayDeque();
        while(cur != null)
        {
            resourceChain.add(cur.getId());
            cur = cur.getParent();
        }
        cur = res;

        while(cur != null)
        {
            PermissionsInfo pi = piCache.get(new ResourceRef(cur.getId(), coral));
            if(pi == null)
            {
                coralEventHub.getGlobal().addPermissionAssignmentChangeListener(this, cur);
                coralEventHub.getGlobal().addResourceTreeChangeListener(this, cur);

                pi = new PermissionsInfo(roles.getMatchingRoles(), coral.getRegistry()
                    .getPermissionAssignments(cur), resourceChain);

                piCache.put(new ResourceRef(cur, coral, queue), pi);
            }
            piList.add(pi);
            cur = cur.getParent();
            resourceChain.removeFirst();
        }
        return piList;
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
        drainQueue();
        piCache.clear();
    }

    /**
     * Flushes permission information on a particular resource and it's children.
     * 
     * @param r the resource to flush permissions information on.
     */
    void flush(Resource r)
    {
        drainQueue();
        Iterator<PermissionsInfo> i = piCache.values().iterator();
        final long id = r.getId();
        while(i.hasNext())
        {
            if(i.next().dependsOn(id))
            {
                i.remove();
            }
        }
    }

    private void drainQueue()
    {
        int drainCount = DRAIN_LIMIT;
        Reference<Resource> ref;
        while(drainCount-- > 0 && (ref = (Reference<Resource>)queue.poll()) != null)
        {
            Resource r = ref.get();
            if(r != null)
            {
                piCache.remove(new ResourceRef(r.getId(), coral));
            }
        }
    }

    private static class PermissionsInfo
    {
        private Set<Permission> inherited;

        private Set<Permission> nonInherited;

        private LongSet resourceSet;

        public PermissionsInfo(Set<Role> roleSet, Set<PermissionAssignment> paSet,
            LongList resourceChain)
        {
            for(PermissionAssignment pa : paSet)
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
            this.resourceSet = new LongOpenHashSet(resourceChain.size(), 0.5);
            this.resourceSet.addAll(resourceChain);
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

        public boolean dependsOn(long resourceId)
        {
            return resourceSet.contains(resourceId);
        }

        public String toString()
        {
            StringBuilder buff = new StringBuilder();
            buff.append(resourceSet.toString());
            toString(inherited, buff);
            toString(nonInherited, buff);
            return buff.toString();
        }

        private static void toString(Set<Permission> perms, StringBuilder buff)
        {
            buff.append("[");
            if(perms != null)
            {
                Iterator<Permission> i = perms.iterator();
                while(i.hasNext())
                {
                    buff.append(i.next().getName());
                    if(i.hasNext())
                    {
                        buff.append(", ");
                    }
                }
            }
            buff.append("]");
        }
    }

    public String toString()
    {
        StringBuilder buff = new StringBuilder();
        buff.append("[");
        Iterator<Role> ri = roles.getMatchingRoles().iterator();
        while(ri.hasNext())
        {
            buff.append(ri.next().getName());
            if(ri.hasNext())
            {
                buff.append(", ");
            }
        }
        buff.append("] = {");
        Iterator<Map.Entry<ResourceRef, PermissionsInfo>> i = piCache.entrySet().iterator();
        while(i.hasNext())
        {
            Map.Entry<ResourceRef, PermissionsInfo> entry = i.next();
            buff.append(entry.getKey().getId()).append("=").append(entry.getValue().toString());
            if(i.hasNext())
            {
                buff.append(", ");
            }
        }
        buff.append("}");
        return buff.toString();
    }
}
