package org.objectledge.coral.security;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.objectledge.coral.BackendException;
import org.objectledge.coral.CoralCore;
import org.objectledge.coral.entity.AbstractEntity;
import org.objectledge.coral.event.CoralEventHub;
import org.objectledge.coral.event.PermissionAssociationChangeListener;
import org.objectledge.coral.event.PermissionChangeListener;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.database.persistence.InputRecord;
import org.objectledge.database.persistence.OutputRecord;
import org.objectledge.database.persistence.Persistence;
import org.objectledge.database.persistence.PersistenceException;

/**
 * Each {@link ResourceClass} has an associated set of <code>Permission</code>s
 * that can be granted upon it's instances.
 *
 * @version $Id: PermissionImpl.java,v 1.12 2005-05-05 08:27:08 rafal Exp $
 * @author <a href="mailto:rkrzewsk@ngo.pl">Rafal Krzewski</a>
 */
public class PermissionImpl
    extends AbstractEntity
    implements Permission,
               PermissionAssociationChangeListener,
               PermissionChangeListener
{
    // Instance variables ///////////////////////////////////////////////////////////////////////

    /** The CoralEventHub. */
    private CoralEventHub coralEventHub;
    
    /** The component hub. */
    private CoralCore coral;

    /** Set of PermissionAssociations. */
    private Set permissionAssociations;
    
    /** Set of ResourceClasses. */
    private Set resourceClasses; 

    // initialization ///////////////////////////////////////////////////////////////////////////

    /**
     * Constructs a {@link PermissionImpl}.
     * 
     * @param persistence the Persistence subsystem.
     * @param coralEventHub the CoralEventHub.
     * @param coral the component hub.
     */
    public PermissionImpl(Persistence persistence, CoralEventHub coralEventHub, 
        CoralCore coral)
    {
        super(persistence);
        this.coralEventHub = coralEventHub;
        this.coral = coral;
    }

    /**
     * Constructs a {@link PermissionImpl}.
     *
     * @param persistence the Persistence subsystem.
     * @param coralEventHub the CoralEventHub.
     * @param coral the component hub.
     *
     * @param name the name of the permission.
     */
    public PermissionImpl(Persistence persistence,  CoralEventHub coralEventHub,
        CoralCore coral, 
        String name)
    {
        super(persistence, name);
        this.coralEventHub = coralEventHub;
        this.coral = coral;
        coralEventHub.getInbound().addPermissionChangeListener(this, this);
    }

    // Persistent interface /////////////////////////////////////////////////////////////////////

    /** The key columns */
    private static final String[] KEY_COLUMNS = { "permission_id" };    

    /**
     * Returns the name of the table this type is mapped to.
     *
     * @return the name of the table.
     */
    public String getTable()
    {
        return "coral_permission";
    }
    
    /** 
     * Returns the names of the key columns.
     *
     * @return the names of the key columns.
     */
    public String[] getKeyColumns()
    {
        return KEY_COLUMNS;
    }

    /**
     * Stores the fields of the object into the specified record.
     *
     * <p>You need to call <code>getData</code> of your superclasses if they
     * are <code>Persistent</code>.</p>
     *
     * @param record the record to store state into.
     * @throws PersistenceException if there is a problem storing field values.
     */
    public void getData(OutputRecord record)
        throws PersistenceException
    {
        super.getData(record);
    }

    /**
     * Loads the fields of the object from the specified record.
     *
     * <p>You need to call <code>setData</code> of your superclasses if they
     * are <code>Persistent</code>.</p>
     * 
     * @param record the record to read state from.
     * @throws PersistenceException if there is a problem loading field values.
     */
    public void setData(InputRecord record)
        throws PersistenceException
    {
        super.setData(record);
        coralEventHub.getInbound().addPermissionChangeListener(this, this);
    }

    // PermissionChangeListener interface ///////////////////////////////////////////////////////

    /**
     * Called when <code>Permission</code>'s data change.
     *
     * @param permission the permission that changed.
     */
    public void permissionChanged(Permission permission)
    {
        if(permission.equals(this))
        {
            try
            {
                persistence.revert(this);
            }
            catch(PersistenceException e)
            {
                throw new BackendException("failed to revert entity state", e);
            }
        }
    }

    // Permission inteface //////////////////////////////////////////////////////////////////////

    /**
     * Returns all <code>ResourceClass</code>es that this
     * <code>Permission</code> is associated with.
     *
     * @return all <code>ResourceClass</code>es that this
     * <code>Permission</code> is associated with.
     */
    public ResourceClass[] getResourceClasses()
    {
        if(resourceClasses == null)
        {
            buildResourceClassSet();
        }
        // copy on write
        Set snapshot = resourceClasses;
        ResourceClass[] rc = new ResourceClass[snapshot.size()];
        snapshot.toArray(rc);
        return rc;
    }

    /**
     * Returns <code>true</code> if this permission is assoicated with the
     * specified resource class.
     *
     * @param item the resource class.
     * @return <code>true</code> if this permission is assoicated with the
     * specified resource class.
     */
    public boolean isAssociatedWith(ResourceClass item)
    {
        if(resourceClasses == null)
        {
            buildResourceClassSet();
        }
        // copy on write
        Set snapshot = resourceClasses;
        return snapshot.contains(item);
    }
    
    /**
     * Returns all <code>PermissionAssignments</code> defined for this permission on all resources.
     * <p>
     * Use this method to acquire information about explicit permission grants on all resources.
     * Note that this operation involves database access on every invocation. 
     * </p>
     * 
     * @return all assignments of this permission.
     */
    public PermissionAssignment[] getPemrissionAssignments()
    {
        Set<PermissionAssignment> pa = coral.getRegistry().getPermissionAssigments(this);
        return pa.toArray(new PermissionAssignment[pa.size()]);
    }
    
    // PermissionAssociationChangeListener interface ////////////////////////////////////////////

    /**
     * {@inheritDoc}
     */
    public void permissionsChanged(PermissionAssociation pa, boolean added)
    {
        if(!pa.getPermission().equals(this))
        {
            return;
        }
        buildAssociationSet();
        synchronized(permissionAssociations)
        {
            if(added)
            {
                permissionAssociations.add(pa);
            }
            else
            {
                permissionAssociations.remove(pa);
            }
            buildResourceClassSet();
        }
    }           

    // private //////////////////////////////////////////////////////////////////////////////////

    /**
     * Builds the set of {@link org.objectledge.coral.security.PermissionAssociation} objects 
     * pointing to this permission. 
     *
     * <p>Upon the first invocation, {@link
     * org.objectledge.coral.entity.CoralRegistry#getPermissionAssociations(
     * org.objectledge.coral.security.Permission)} is called to obtain the initial set, and the 
     * object is registered as a listener for association changes.</p>
     */
    private void buildAssociationSet()
    {
        if(permissionAssociations == null)
        {
            permissionAssociations = coral.getRegistry().getPermissionAssociations(this);
            coralEventHub.getGlobal().addPermissionAssociationChangeListener(this, this);
        }
    }

    /**
     * Extracts a set of {@link org.objectledge.coral.schema.ResourceClass} objects out of {@link
     * org.objectledge.coral.security.PermissionAssociation} set.
     */
    private void buildResourceClassSet()
    {
        buildAssociationSet();
        synchronized(permissionAssociations)
        {
            HashSet rcs = new HashSet();
            Iterator i=permissionAssociations.iterator();
            while(i.hasNext())
            {
                rcs.add(((PermissionAssociation)i.next()).getResourceClass());
            }
            resourceClasses = rcs;
        }
    }
}
