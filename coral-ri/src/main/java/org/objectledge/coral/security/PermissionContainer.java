package org.objectledge.coral.security;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.objectledge.coral.entity.CoralRegistry;
import org.objectledge.coral.event.EventHub;
import org.objectledge.coral.event.PermissionAssignmentChangeListener;
import org.objectledge.coral.event.ResourceTreeChangeListener;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ResourceInheritance;

/**
 * A helper class for managing a set of permissions.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: PermissionContainer.java,v 1.3 2004-02-23 10:24:55 fil Exp $
 */
public class PermissionContainer
    implements PermissionAssignmentChangeListener,
               ResourceTreeChangeListener
{
    // Instance variables ///////////////////////////////////////////////////////////////////////

    /** The event hub. */
    private EventHub eventHub;
    
    /** The CoralRegistry. */
    private CoralRegistry coralRegistry;

    /** The role container. */
    private RoleContainer roles;

    /** Resource -> Set of Permissions */
    private Map pCache = new WeakHashMap();
    
    /** Read-only threadsafe view of {@link #pCache} */
    private Map cpCache = new WeakHashMap();

    /** Respource -> Array of PermissionAssignments */
    private Map paCache = new WeakHashMap();

    /** Resource -> Set of watched child resources. */ 
    private Map childResources = new WeakHashMap();

    // Initialization ///////////////////////////////////////////////////////////////////////////

    /**
     * Constructs a permission container.
     *
     * @param eventHub the EventHub.
     * @param coralRegistry the CoralRegistry.
     * 
     * @param roles a RoleContainer.
     */
    PermissionContainer(EventHub eventHub, CoralRegistry coralRegistry, 
        RoleContainer roles)
    {
        this.eventHub = eventHub;
        this.coralRegistry = coralRegistry;
        this.roles = roles;
        roles.setPermissionContainer(this);
    }

    // Permissions //////////////////////////////////////////////////////////////////////////////

    /**
     * Return all permission the entity this permission container belongs to
     * has on a specific resource.
     *
     * @param res the resource.
     * @return permission array.
     */
    public Permission[] getPermissions(Resource res)
    {
        Set snapshot = buildPermissions(res);
        Permission[] result = new Permission[snapshot.size()];
        snapshot.toArray(result);
        return result;
    }

    /**
     * Check if the entity this container describes has a specific permission
     * on a specific resource.
     *
     * @param res the resource.
     * @param perm the permission.
     * @return <code>true</code> if the entity has the permission.
     */
    public boolean hasPermission(Resource res, Permission perm)
    {    
        return buildPermissions(res).contains(perm);
    }

    /**
     * Returl all permission assignments the entityt this permission container
     * describes has on a specific resource.
     *
     * @param res the resource.
     * @return permission assignment array.
     */
    public PermissionAssignment[] getPermissionAssignments(Resource res)
    {
        return buildPermissionAssignments(res);
    }

    // PermissionAssignmentChangeListener interface /////////////////////////////////////////////

    /**
     * Called when permission assignments on a watched resource change.
     *
     * <p>Flushes cached permission information on the involved resource and
     * all of it's child resources.</p>
     *
     * @param item permission assignment information.
     * @param added was the permission added, or removed?
     */
    public synchronized void permissionsChanged(PermissionAssignment item, boolean added)
    {
        ArrayList stack = new ArrayList();
        stack.add(item.getResource());
        while(stack.size() > 0)
        {
            Resource r = (Resource)stack.remove(stack.size()-1);
            pCache.remove(r);
            paCache.remove(r);
            Set children = (Set)childResources.get(r);
            if(children != null)
            {
                stack.addAll(children);
            }
        }
        cpCache = new WeakHashMap(pCache);
    }

    // ResourceTreeChangeListener interface /////////////////////////////////////////////////////

    /**
     * Called when resource tree changes.
     *
     * <p>Flushes cached permission information on the child resource in
     * the relationship and all of it child resources.</p>
     *
     * @param item resource relationship information.
     * @param added was the relationship added or removed.
     */
    public synchronized void resourceTreeChanged(ResourceInheritance item, boolean added)
    {
        ArrayList stack = new ArrayList();
        stack.add(item.getChild());
        while(stack.size() > 0)
        {
            Resource r = (Resource)stack.remove(stack.size()-1);
            flush(r);
            Set children = (Set)childResources.get(r);
            if(children != null)
            {
                stack.addAll(children);
            }
        }
        Set children = (Set)childResources.get(item.getParent());
        if(added)
        {
            if(children == null)
            {
                children = new HashSet();
                childResources.put(item.getParent(), children);
            }
            children.add(item.getChild());
            eventHub.getGlobal().addResourceTreeChangeListener(this, item.getParent());
            eventHub.getGlobal().addPermissionAssignmentChangeListener(this, item.getParent());
        }
        else
        {
            if(children != null)
            {
                children.remove(item.getChild());
            }
        }
    }

    // private methods //////////////////////////////////////////////////////////////////////////

    /**
     * Build a set of permissions on a specific resource.
     *
     * @param res the resource
     * @return the set of permissions
     */
    private Set buildPermissions(Resource res)
    {
        Set ps = (Set)cpCache.get(res);
        if(ps != null)
        {
            return ps;
        }
        synchronized(this)
        {
            ps = (Set)pCache.get(res);
            if(ps == null)
            {
                ps = new HashSet();
                pCache.put(res, ps);
                PermissionAssignment[] paa = buildPermissionAssignments(res);
                for(int i=0; i<paa.length; i++)
                {
                    ps.add(paa[i].getPermission());
                }
            }
            cpCache = new WeakHashMap(pCache);
            return ps;
        }
    }

    /**
     * Build a set of permission assignments on a specific resource.
     *
     * @param res the resource
     * @return the set of permissions
     */
    private synchronized PermissionAssignment[] buildPermissionAssignments(Resource res)
    {
        PermissionAssignment[] paa = (PermissionAssignment[])paCache.get(res);
        if(paa == null)
        {
            Set temp = new HashSet();
            Resource r = res;
            Resource rr = null;
            Set roleSet = roles.getMatchingRoles();
            while(r != null)
            {
                Set pas = coralRegistry.getPermissionAssignments(r);
                Iterator i = pas.iterator();
                while(i.hasNext())
                {
                    PermissionAssignment pa = (PermissionAssignment)i.next();
                    if(roleSet.contains(pa.getRole()) &&
                       (pa.isInherited() || r.equals(res)))
                    {
                        temp.add(pa);
                    }
                }
                if(!r.equals(res))
                {
                    Set children = (Set)childResources.get(r);
                    if(children == null)
                    {
                        children = new HashSet();
                        childResources.put(r, children);
                    }
                    children.add(rr);
                }
                eventHub.getGlobal().addPermissionAssignmentChangeListener(this, r);
                eventHub.getGlobal().addResourceTreeChangeListener(this, r);
                rr = r;
                r = r.getParent();
            }
            paa = new PermissionAssignment[temp.size()];
            temp.toArray(paa);
            paCache.put(res, paa);
        }
        return paa;
    }

    /**
     * Flushes permission information on all reosurces.
     *
     * <p>This method is called by the peer RoleContainer when role
     * information (implications / assignments) change.</p>
     */
    synchronized void flush()
    {
        pCache.clear();
        paCache.clear();
        cpCache = new WeakHashMap();
    }

    /**
     * Flushes permission information on a particular resource.
     *
     * @param r the resource to flush permissission information on.
     */
    synchronized void flush(Resource r)
    {
        pCache.remove(r);
        paCache.remove(r);
        cpCache = new WeakHashMap(cpCache);
    }
}
