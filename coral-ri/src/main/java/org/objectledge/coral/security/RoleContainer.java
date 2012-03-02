package org.objectledge.coral.security;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.objectledge.coral.CoralCore;
import org.objectledge.coral.event.CoralEventHub;
import org.objectledge.coral.event.RoleImplicationChangeListener;

/**
 * A helper class for managing a set of roles.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: RoleContainer.java,v 1.8 2005-05-20 05:36:56 pablo Exp $
 */
public class RoleContainer
    implements RoleImplicationChangeListener
{
    // Instance variables ///////////////////////////////////////////////////////////////////////

    /** The CoralEventHub. */
    private CoralEventHub coralEventHub;
    
    /** The component hub. */
    private CoralCore coral;

    /** Expliclitly assigned roles. */
    private Set<Role> explicitRoles = new HashSet<Role>();

    /** Explicitly and implicitly assigned roles. */
    private Set<Role> matchingRoles;
    
    /** Direct and indirect super roles of the roles assigned explicitly. */
    private Set<Role> superRoles;
    
    /** Direct and indirect sub roles of the roles assignmed explicitly -- the
        implicitly assignmed roles. */
    private Set<Role> subRoles;

    /** peer permission container */
    private PermissionContainer permissions;
    
    // Initialization ///////////////////////////////////////////////////////////////////////////

    /**
     * Creates a new role container.
     *
     * @param coralEventHub the even hub.
     * @param coral the component hub.
     * 
     * @param data the initial roles
     * @param roles <code>true</code> if data set contains 
     *        {@link org.objectledge.coral.security.Role} objects, <code>false</code> if it 
     *        contains {@link org.objectledge.coral.security.RoleAssignment} objects. 
     */
    public RoleContainer(CoralEventHub coralEventHub, CoralCore coral, 
        Set data, boolean roles)
    {
        this.coralEventHub = coralEventHub;
        this.coral = coral;
        if(roles)
        {
            Iterator i = data.iterator();
            while(i.hasNext())
            {
                Role role = (Role)i.next();
                explicitRoles.add(role);
                coralEventHub.getGlobal().addRoleImplicationChangeListener(this, role);
            }
        }
        else
        {
            Iterator i = data.iterator();
            while(i.hasNext())
            {
                Role role = ((RoleAssignment)i.next()).getRole();
                explicitRoles.add(role);
                coralEventHub.getGlobal().addRoleImplicationChangeListener(this, role);
            }
        }
    }

    // public interface //////////////////////////////////////////////////////////////////////////

    /**
     * Adds a role to the container.
     *
     * @param role the role to be assigned explicitly to the container.
     */
    public void addRole(Role role)
    {
        synchronized(this) 
        {
            coralEventHub.getGlobal().addRoleImplicationChangeListener(this, role);
            explicitRoles.add(role);
            matchingRoles = null;
            superRoles = null;
            subRoles = null;
        }
        if(permissions != null)
        {
            permissions.flush();
        }
    }

    /**
     * Removes a role from the container.
     *
     * @param role the role to be unassigned explicity from the container.
     */
    public void removeRole(Role role)
    { 
        synchronized(this)
        {
            explicitRoles.remove(role);
            matchingRoles = null;
            superRoles = null;
            subRoles = null;
        }
        if(permissions != null)
        {
            permissions.flush();
        }
    }

    /**
     * Returns the superroles of roles in this set.
     * 
     * @return the superroles of roles in this set.
     */
    public Set<Role> getSuperRoles()
    {
        return buildSuperRoles();
    }

    /**
     * Returns the subroles of roles in this set.
     * 
     * @return the subroles of roles in this set.
     */
    public Set<Role> getSubRoles()
    {
        return buildSubRoles();
    }

    /**
     * Returns the roles equivalent to those in this set (subroles+self).
     * 
     * @return the roles equivalent to those in this set.
     */
    public Set<Role> getMatchingRoles()
    {
        return buildMatchingRoles();
    }

    /**
     * Checks if the given role is a superrole of the roles in this set.
     * 
     * @param role the role.
     * @return <code>true</code> if the given role is a superrole of the roles in this set.
     */
    public boolean isSuperRole(Role role)
    {
        return buildSuperRoles().contains(role);
    }
    
    /**
     * Checks if the given role is a subrole of the roles in this set.
     * 
     * @param role the role.
     * @return <code>true</code> if the given role is a subrole of the roles in this set.
     */
    public boolean isSubRole(Role role)
    {
        return buildSubRoles().contains(role);
    }
    
    /**
     * Checks if the given role is equivalent to those in this set in this set.
     * 
     * @param role the role.
     * @return <code>true</code> if the given role is equivalent to those in this set in this set.
     */
    public boolean isMatchingRole(Role role)
    {
        return buildMatchingRoles().contains(role);
    }
    
    // RoleImplicationChangeListener inteface ///////////////////////////////////////////////////

    /**
     * Called when role implications change.
     *
     * @param implication the {@link org.objectledge.coral.security.RoleImplication}.
     * @param added <code>true</code> if the implication was added,
     *        <code>false</code> if removed.
     */
    public void roleChanged(RoleImplication implication, boolean added)
    {
        boolean changed = false;
        synchronized(this)
        {
            Role sup = implication.getSuperRole();
            Role sub = implication.getSubRole();
            if(explicitRoles.contains(sup) || 
                            subRoles != null && subRoles.contains(sup))
            {
                subRoles = null;
                matchingRoles = null;
                changed = true;
            }
            if(explicitRoles.contains(sub) || 
                            superRoles != null && superRoles.contains(sub))
            {
                superRoles = null;
                changed = true;
            }
        }
        if(permissions != null && changed)
        {
            permissions.flush();
        }
    }

    // private //////////////////////////////////////////////////////////////////////////////////

    private synchronized Set<Role> buildSuperRoles()
    {
        if(superRoles == null)
        {
            Set<Role> sup = new HashSet<Role>();
            ArrayList<Role> stack = new ArrayList<Role>();
            stack.addAll(explicitRoles);
            while(stack.size() > 0)
            {
                Role r = (Role)stack.remove(stack.size() - 1);
                if(!explicitRoles.contains(r))
                {
                    sup.add(r);
                    coralEventHub.getGlobal().addRoleImplicationChangeListener(this, r);
                }
                Set ris = coral.getRegistry().getRoleImplications(r);
                Iterator i = ris.iterator();
                while(i.hasNext())
                {
                    RoleImplication ri = (RoleImplication)i.next();
                    if(ri.getSubRole().equals(r))
                    {
                        stack.add(ri.getSuperRole());
                    }
                }
            }
            superRoles = sup;
            return sup;
        }
        return superRoles;
    }

    private synchronized Set<Role> buildSubRoles()
    {
        if(subRoles == null)
        {
            Set<Role> sub = new HashSet<Role>();
            ArrayList<Role> stack = new ArrayList<Role>();
            stack.addAll(explicitRoles);
            while(stack.size() > 0)
            {
                Role r = (Role)stack.remove(stack.size() - 1);
                if(!explicitRoles.contains(r))
                {
                    sub.add(r);
                    coralEventHub.getGlobal().addRoleImplicationChangeListener(this, r);
                }
                Set ris = coral.getRegistry().getRoleImplications(r);
                Iterator i = ris.iterator();
                while(i.hasNext())
                {
                    RoleImplication ri = (RoleImplication)i.next();
                    if(ri.getSuperRole().equals(r))
                    {
                        stack.add(ri.getSubRole());
                    }
                }
            }
            subRoles = sub;
            return sub;
        }
        return subRoles;
    }

    private synchronized Set<Role> buildMatchingRoles()
    {
        if(matchingRoles == null)
        {
            Set<Role> match = new HashSet<Role>();
            match.addAll(explicitRoles);
            match.addAll(buildSubRoles());
            matchingRoles = match;
            return match;
        }
        return matchingRoles;
    }

    void setPermissionContainer(PermissionContainer permissions)
    {
        this.permissions = permissions;
    }
}
