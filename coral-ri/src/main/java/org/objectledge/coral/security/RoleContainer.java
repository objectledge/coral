package org.objectledge.coral.security;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.objectledge.coral.entity.CoralRegistry;
import org.objectledge.coral.event.CoralEventHub;
import org.objectledge.coral.event.RoleImplicationChangeListener;

/**
 * A helper class for managing a set of roles.
 * 
 * @author <a href="mailto:rafal@caltha.pl">Rafal Krzewski</a>
 * @version $Id: RoleContainer.java,v 1.5 2004-02-23 11:07:08 fil Exp $
 */
public class RoleContainer
    implements RoleImplicationChangeListener
{
    // Instance variables ///////////////////////////////////////////////////////////////////////

    /** The CoralEventHub. */
    private CoralEventHub coralEventHub;
    
    /** The CoralRegistry. */
    private CoralRegistry coralRegistry;

    /** Expliclitly assigned roles. */
    private Set explicitRoles = new HashSet();

    /** Explicitly and implicitly assigned roles. */
    private Set matchingRoles;
    
    /** Direct and indirect super roles of the roles assigned explicitly. */
    private Set superRoles;
    
    /** Direct and indirect sub roles of the roles assignmed explicitly -- the
        implicitly assignmed roles. */
    private Set subRoles;

    /** peer permission container */
    private PermissionContainer permissions;
    
    // Initialization ///////////////////////////////////////////////////////////////////////////

    /**
     * Creates a new role container.
     *
     * @param coralEventHub the CoralEventHub.
     * @param coralRegistry the CoralRegistry.
     * 
     * @param data the initial roles
     * @param roles <code>true</code> if data set contains {@link Role}
     *        objects, <code>false</code> if it contains {@link
     *        RoleAssignment} objects. 
     */
    public RoleContainer(CoralEventHub coralEventHub, CoralRegistry coralRegistry, 
        Set data, boolean roles)
    {
        this.coralEventHub = coralEventHub;
        this.coralRegistry = coralRegistry;
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
    public synchronized void addRole(Role role)
    {
        coralEventHub.getGlobal().addRoleImplicationChangeListener(this, role);
        explicitRoles.add(role);
        matchingRoles = null;
        superRoles = null;
        subRoles = null;
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
    public synchronized void removeRole(Role role)
    { 
        explicitRoles.remove(role);
        matchingRoles = null;
        superRoles = null;
        subRoles = null;
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
    public Set getSuperRoles()
    {
        return buildSuperRoles();
    }

    /**
     * Returns the subroles of roles in this set.
     * 
     * @return the subroles of roles in this set.
     */
    public Set getSubRoles()
    {
        return buildSubRoles();
    }

    /**
     * Returns the roles equivalent to those in this set (subroles+self).
     * 
     * @return the roles equivalent to those in this set.
     */
    public Set getMatchingRoles()
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
     * @param implication the {@link RoleImplication}.
     * @param added <code>true</code> if the implication was added,
     *        <code>false</code> if removed.
     */
    public synchronized void roleChanged(RoleImplication implication, boolean added)
    {
        Role sup = implication.getSuperRole();
        Role sub = implication.getSubRole();
        boolean changed = false;
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
        if(permissions != null && changed)
        {
            permissions.flush();
        }
    }

    // private //////////////////////////////////////////////////////////////////////////////////

    private synchronized Set buildSuperRoles()
    {
        if(superRoles == null)
        {
            Set sup = new HashSet();
            ArrayList stack = new ArrayList();
            stack.addAll(explicitRoles);
            while(stack.size() > 0)
            {
                Role r = (Role)stack.remove(stack.size() - 1);
                if(!explicitRoles.contains(r))
                {
                    sup.add(r);
                    coralEventHub.getGlobal().addRoleImplicationChangeListener(this, r);
                }
                Set ris = coralRegistry.getRoleImplications(r);
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

    private synchronized Set buildSubRoles()
    {
        if(subRoles == null)
        {
            Set sub = new HashSet();
            ArrayList stack = new ArrayList();
            stack.addAll(explicitRoles);
            while(stack.size() > 0)
            {
                Role r = (Role)stack.remove(stack.size() - 1);
                if(!explicitRoles.contains(r))
                {
                    sub.add(r);
                    coralEventHub.getGlobal().addRoleImplicationChangeListener(this, r);
                }
                Set ris = coralRegistry.getRoleImplications(r);
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

    private synchronized Set buildMatchingRoles()
    {
        if(matchingRoles == null)
        {
            Set match = new HashSet();
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
