package org.objectledge.coral.table;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.table.comparator.CreationTimeComparator;
import org.objectledge.coral.table.comparator.CreatorNameComparator;
import org.objectledge.coral.table.comparator.IdComparator;
import org.objectledge.coral.table.comparator.ModificationTimeComparator;
import org.objectledge.coral.table.comparator.ModifierNameComparator;
import org.objectledge.coral.table.comparator.NameComparator;
import org.objectledge.coral.table.comparator.OwnerNameComparator;
import org.objectledge.coral.table.comparator.PathComparator;
import org.objectledge.table.ExtendedTableModel;
import org.objectledge.table.TableColumn;
import org.objectledge.table.TableException;
import org.objectledge.table.TableFilter;
import org.objectledge.table.TableRowSet;
import org.objectledge.table.TableState;
import org.objectledge.table.generic.GenericListRowSet;
import org.objectledge.table.generic.GenericTreeRowSet;

/**
 * Implementation of Table model for display of Coral resource trees. 
 *
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: CoralTableModel.java,v 1.11 2005-02-21 14:04:39 rafal Exp $
 */
public class CoralTableModel implements ExtendedTableModel
{
    /** coral session. */
    protected CoralSession coralSession;

    /** comparators keyed by column name. */
    protected Map<String, Comparator> comparatorByColumnName = new HashMap<String, Comparator>();

    /**
     * Creates new CoralTableModel instance.
     * 
     * @param coralSession the coral session.
     * @param locale the locale to be used.
     */
    public CoralTableModel(CoralSession coralSession, Locale locale)
    {
        this.coralSession = coralSession;

        /**
        TODO: make it using introspectors
        // add columns for a given resource class
        if(resClass != null)
        {
            AttributeDefinition[] attrDefs = resClass.getAllAttributes();
            for(int i=0; i < attrDefs.length; i++)
            {
                // get the column name
                String attributeName = attrDefs[i].getName();

                // get the column comparator
                AttributeClass attrClass = attrDefs[i].getAttributeClass();
                //attrClass.
            }
        }
        /**/

        // add generic Resource columns

        // Subject name comparator columns
        comparatorByColumnName.put("creator.name", new CreatorNameComparator(locale));
        comparatorByColumnName.put("modifier.name", new ModifierNameComparator(locale));
        comparatorByColumnName.put("owner.name", new OwnerNameComparator(locale));

        // Time comparator columns
        comparatorByColumnName.put("creation.time", new CreationTimeComparator());
        comparatorByColumnName.put("modification.time", new ModificationTimeComparator());

        // Name comparator
        comparatorByColumnName.put("name", new NameComparator(locale));
        // Path comparator
        comparatorByColumnName.put("path", new PathComparator(locale));

        // Id comparator
        comparatorByColumnName.put("id", new IdComparator());
    }

    /**
     * Returns a {@link TableRowSet} object initialised by this model
     * and a given {@link TableState}.
     *
     * @param state the parent
     * @param filters a list of filters to be used while creating the rows set
     * @return table of children
     */
    public TableRowSet getRowSet(TableState state, TableFilter[] filters)
    {
        if(state.getTreeView())
        {
            return new GenericTreeRowSet(state, filters, this);
        }
        else
        {
            return new GenericListRowSet(state, filters, this);   
        }
    }

    /**
     * Returns array of column definitions. They defitinions are created on every call,
     * because they may be modified during it's lifecycle.
     *
     * @return array of <code>TableColumn</code> objects
     */
    public TableColumn[] getColumns()
    {
        TableColumn[] columns = new TableColumn[comparatorByColumnName.size()];
        int i=0;
        for(Iterator iter = comparatorByColumnName.keySet().iterator(); iter.hasNext(); i++)
        {
            String columnName = (String)(iter.next());
            Comparator comparator =  (Comparator)(comparatorByColumnName.get(columnName));
            try
            {
                columns[i] = new TableColumn(columnName, comparator);
            }
            catch(TableException e)
            {
                throw new RuntimeException("Problem creating a column object", e);
            }
        }
        return columns;
    }

    /**
     * Gets all children of the parent, may return empty array.
     *
     * @param parent the parent
     * @return table of children
     */
    public Object[] getChildren(Object parent)
    {
        if(parent == null)
        {
            return new Object[0];
        }
        return coralSession.getStore().getResource((Resource)parent);
    }

    /**
     * Returns the model dependent object by its id.
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
            throw new RuntimeException("Problem getting a resource object", e);
        }
        return resource;
    }

    /**
     * Returns the id of the object.
     * 
     * @param parent the id of the parent object.
     * @param child model object.
     * @return the id of the object.
     */
    public String getId(String parent, Object child)
    {
        if(child == null)
        {
            return "-1";
        }
        return ((Resource)child).getIdString();
    }
}
