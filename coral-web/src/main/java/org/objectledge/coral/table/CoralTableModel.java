package org.objectledge.coral.table;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.schema.ResourceClass;
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
 * @version $Id: CoralTableModel.java,v 1.15 2008-06-05 16:37:59 rafal Exp $
 */
public class CoralTableModel implements ExtendedTableModel<Resource>
{
    /** coral session. */
    protected CoralSession coralSession;

    /** comparators keyed by column name. */
    protected Map<String, Comparator<?>> comparatorByColumnName = new HashMap<String, Comparator<?>>();

    /**
     * reverse comparators, used for descending sort, keyed by column name. When a comparator
     * defined in comparatorByColumnName does not have a matching comparator in
     * reverseComparatorByColumnName, Collections.reverse(Comparator) is used.
     */
    protected Map<String, Comparator<?>> reverseComparatorByColumnName = new HashMap<String, Comparator<?>>();

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
        comparatorByColumnName.put("name", new NameComparator<Resource>(locale));
        // Path comparator
        comparatorByColumnName.put("path", new PathComparator(locale));

        // Id comparator
        comparatorByColumnName.put("id", new IdComparator<Resource>());
    }

    /**
     * Adds a custom column to the model.
     * 
     * @param name name of the column.
     * @param comparator the comparator for the column.
     */
    public void addColumn(String name, Comparator<Resource> comparator)
    {
        comparatorByColumnName.put(name, comparator);
    }

    /**
     * Adds an attribute value based column to the model.
     *  
     * @param resourceClass the resource class.
     * @param attributeName the attribute name.
     * @param valueComparator the comparator for attribute values.
     */
    public <V> void addColumn(ResourceClass resourceClass, String attributeName,
        Comparator<V> valueComparator)
    {
        addColumn(attributeName, AttributeTableColumn.getAttributeComparator(resourceClass,
            attributeName, valueComparator));
    }

    /**
     * Returns a {@link TableRowSet} object initialised by this model
     * and a given {@link TableState}.
     *
     * @param state the parent
     * @param filters a list of filters to be used while creating the rows set
     * @return table of children
     */
    public TableRowSet<Resource> getRowSet(TableState state, TableFilter<Resource>[] filters)
    {
        if(state.getTreeView())
        {
            return new GenericTreeRowSet<Resource>(state, filters, this);
        }
        else
        {
            return new GenericListRowSet<Resource>(state, filters, this);   
        }
    }

    /**
     * Returns array of column definitions. They defitinions are created on every call,
     * because they may be modified during it's lifecycle.
     *
     * @return array of <code>TableColumn</code> objects
     */
    public TableColumn<Resource>[] getColumns()
    {
        TableColumn<Resource>[] columns = new TableColumn[comparatorByColumnName.size()];
        int i=0;
        for(Iterator<String> iter = comparatorByColumnName.keySet().iterator(); iter.hasNext(); i++)
        {
            String columnName = (String)(iter.next());
            Comparator<Resource> comparator =  (Comparator<Resource>)(comparatorByColumnName.get(columnName));
            try
            {
                Comparator<Resource> reverseComparator = (Comparator<Resource>)(reverseComparatorByColumnName.get(columnName));
                if(reverseComparator != null)
                {
                    columns[i] = new TableColumn<Resource>(columnName, comparator, reverseComparator);
                }
                else
                {
                    columns[i] = new TableColumn<Resource>(columnName, comparator);
                }
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
    public Resource[] getChildren(Resource parent)
    {
        if(parent == null)
        {
            return new Resource[0];
        }
        return coralSession.getStore().getResource(parent);
    }

    /**
     * Returns the model dependent object by its id.
     *
     * @param id the id of the object
     * @return model object
     */
    public Resource getObject(String id)
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
    public String getId(String parent, Resource child)
    {
        if(child == null)
        {
            return "-1";
        }
        return child.getIdString();
    }
}
