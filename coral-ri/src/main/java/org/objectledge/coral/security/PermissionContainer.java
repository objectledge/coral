package org.objectledge.coral.security;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.objectledge.coral.CoralCore;
import org.objectledge.coral.event.CoralEventHub;
import org.objectledge.coral.event.PermissionAssignmentChangeListener;
import org.objectledge.coral.event.ResourceTreeChangeListener;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.store.ResourceInheritance;

/**
 * A helper class for managing a set of permissions.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: PermissionContainer.java,v 1.7 2005-02-08 20:34:45 rafal Exp $
 */
public class PermissionContainer
    implements PermissionAssignmentChangeListener,
               ResourceTreeChangeListener
{
    // Instance variables ///////////////////////////////////////////////////////////////////////

    /** The component hub. */
    private CoralCore coral;

    /** The CoralEventHub. */
    private CoralEventHub coralEventHub;
    
    /** The role container. */
    private RoleContainer roles;

    /** Resource -> Set of Permissions */
    private Map pCache = new WeakHashMap();
    
    /** Respource -> Array of PermissionAssignments */
    private Map paCache = new WeakHashMap();

    /** Resource -> Set of watched child resources. */ 
    private Map childResources = new WeakHashMap();

    // Initialization ///////////////////////////////////////////////////////////////////////////

    /**
     * Constructs a permission container.
     *
     * @param coralEventHub the CoralEventHub.
     * @param coral the Coral core component.
     * @param roles a RoleContainer.
     */
    PermissionContainer(CoralEventHub coralEventHub, CoralCore coral, 
        RoleContainer roles)
    {
        this.coralEventHub = coralEventHub;
        this.coral = coral;
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
            coralEventHub.getGlobal().addResourceTreeChangeListener(this, item.getParent());
            coralEventHub.getGlobal().addPermissionAssignmentChangeListener(this, item.getParent());
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
    private synchronized Set buildPermissions(Resource res)
    {
        Set ps = (Set)pCache.get(res);
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
        return ps;
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
                Set pas = coral.getRegistry().getPermissionAssignments(r);
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
                coralEventHub.getGlobal().addPermissionAssignmentChangeListener(this, r);
                coralEventHub.getGlobal().addResourceTreeChangeListener(this, r);
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
    }
}
