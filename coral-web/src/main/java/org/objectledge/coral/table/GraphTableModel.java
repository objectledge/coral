package org.objectledge.coral.table;

import org.jcontainer.dna.Logger;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.relation.Relation;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.store.Resource;
import org.objectledge.table.ExtendedTableModel;
import org.objectledge.table.TableColumn;
import org.objectledge.table.TableRowSet;
import org.objectledge.table.TableState;
import org.objectledge.table.generic.GenericTreeRowSet;


/**
 * A table model for Coral relations.
 *
 *  * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: GraphTableModel.java,v 1.4 2004-06-14 13:47:38 zwierzem Exp $
 */
public class GraphTableModel
    implements ExtendedTableModel
{
    /** The embeded cross reference. */
    protected Relation ref;

    /** The columns of the list. */
    protected TableColumn[] columns;

    /** logging */
    protected Logger logger;
    
    /** coral session */
    protected CoralSession coralSession;

    /**
     * Constructs a new model.
     *
     * @param ref the cross reference to build model for.
     */
    public GraphTableModel(CoralSession coralSession, Logger logger, Relation ref) 
    {
        this.coralSession = coralSession;
        this.logger = logger;
        this.ref = ref;
        this.columns = new TableColumn[0];
    }
    
    /**
     * Returns a {@link TableRowSet} object initialised by this model
     * and a given {@link TableState}.
     *
     * @param state the parent
     * @return table of children
     */
    public TableRowSet getRowSet(TableState state)
    {
        return new GenericTreeRowSet(state, this);
    }

    /**
     * Gets all children of the parent, may return empty array.
     *
     * @param parent the parent
     * @return table of children
     */
    public Object[] getChildren(Object parent)
    {
        if(parent instanceof Resource)
        {
            Resource[] resources = ref.get((Resource)parent);
            Object[] children = new Object[resources.length];
            System.arraycopy(resources, 0, children, 0, resources.length);
            return children;
        }
        else
        {
            return new Object[0];
        }
    }
    
    /**
     * Returns the model dependent object by its id.
     * 
     * TODO: Ids shold be handled differently!!
     *
     * @param id the id of the object
     * @return model object
     */
    public Object getObject(String id)
    {
        Resource resource = null;
        try
        {
            long longId = Long.parseLong(id);
            if(longId == -1)
            {
                return null;
            }
            resource = coralSession.getStore().getResource(longId);
        }
        catch(EntityDoesNotExistException e)
        {
            logger.error("Coral exception ",e);
        }
        return resource;
    }

    /**
     * Returns the id of the object.
     * @param child model object.
     *
     * TODO: Ids shold be handled differently!!
     *
     * @return the id of the object.
     */
    public String getId(Object parent, Object child)
    {
        if(child == null)
        {
            return "-1";
        }
        return ""+((Resource)child).getId();
    }

    /**
     * Returns array of column definitions. They are created on every call,
     * because they can get modified durig it's lifecycle.
     *
     * @return array of <code>TableColumn</code> objects
     */
    public TableColumn[] getColumns()
    {
        return columns;
    }
}
