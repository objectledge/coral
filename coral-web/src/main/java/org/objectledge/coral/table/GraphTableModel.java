package org.objectledge.coral.table;

import org.jcontainer.dna.Logger;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.relation.Relation;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.store.Resource;
import org.objectledge.table.ExtendedTableModel;
import org.objectledge.table.TableColumn;
import org.objectledge.table.TableException;
import org.objectledge.table.TableFilter;
import org.objectledge.table.TableRowSet;
import org.objectledge.table.TableState;
import org.objectledge.table.generic.GenericTreeRowSet;


/**
 * A table model for Coral relations.
 *
 *  * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * @version $Id: GraphTableModel.java,v 1.8 2005-02-21 14:04:39 rafal Exp $
 */
public class GraphTableModel<T extends Resource>
    implements ExtendedTableModel<T>
{
    /** The embeded cross reference. */
    protected Relation ref;

    /** The columns of the list. */
    protected TableColumn<T>[] columns;

    /** logging */
    protected Logger logger;
    
    /** coral session */
    protected CoralSession coralSession;

    /**
     * Constructs a new model.
     *
     * @param coralSession the coralSession.
     * @param logger the logger to use.
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
     * {@inheritDoc}
     */
    public TableRowSet<T> getRowSet(TableState state, TableFilter<T>[] filters)
    {
        return new GenericTreeRowSet<T>(state, filters, this);
    }

    /**
     * Gets all children of the parent, may return empty array.
     *
     * @param parent the parent
     * @return table of children
     */
    public Resource[] getChildren(Resource parent)
    {
        if(parent instanceof Resource)
        {
            Resource[] resources = ref.get((Resource)parent);
            Resource[] children = new Resource[resources.length];
            System.arraycopy(resources, 0, children, 0, resources.length);
            return children;
        }
        else
        {
            return new Resource[0];
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
    public T getObject(String id)
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
        return (T)resource;
    }

    /**
     * Returns the id of the object.
     * 
     * TODO: Ids should be handled differently!!
     *
     * @param parent the id of the parent object.
     * @param child model object.
     * @return the id of the object.
     */
    public String getId(String parent, T child)
    {
        if(child == null)
        {
            return "-1";
        }
        return ((Resource)child).getIdString();
    }

    /**
     * Returns array of column definitions. They are created on every call,
     * because they can get modified durig it's lifecycle.
     *
     * @return array of <code>TableColumn</code> objects
     */
    public TableColumn<T>[] getColumns()
    {
        return columns;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TableColumn<T> getColumn(String name)
        throws TableException
    {        
        return null;
    }
}
