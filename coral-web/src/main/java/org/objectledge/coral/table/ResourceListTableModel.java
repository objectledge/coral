package org.objectledge.coral.table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.objectledge.coral.store.CoralStore;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.table.comparator.CreationTimeComparator;
import org.objectledge.coral.table.comparator.CreatorNameComparator;
import org.objectledge.coral.table.comparator.IdComparator;
import org.objectledge.coral.table.comparator.ModificationTimeComparator;
import org.objectledge.coral.table.comparator.ModifierNameComparator;
import org.objectledge.coral.table.comparator.NameComparator;
import org.objectledge.coral.table.comparator.OwnerNameComparator;
import org.objectledge.coral.table.comparator.PathComparator;
import org.objectledge.table.TableColumn;
import org.objectledge.table.TableException;
import org.objectledge.table.comparator.ListPositionComparator;
import org.objectledge.table.generic.ListTableModel;

/**
 * Implementation of Table Model for lists of ARL resources.
 * 
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: ResourceListTableModel.java,v 1.12 2008-10-21 15:03:56 rafal Exp $
 */
public class ResourceListTableModel<T extends Resource>
    extends ListTableModel<T>
{
    /** resources keyed by their id */
    private Map<String, T> resourcesById;

    private Map<T, T[]> childrenByParentId;

    /**
     * Creates new ResourceListTableModel instance.
     * 
     * @param array an array of Resources
     * @param locale the locale to be used by comparators.
     * @throws TableException if there is a problem creating the model.
     */
    public ResourceListTableModel(T[] array, Locale locale)
        throws TableException
    {
        super(array, (TableColumn<T>[])null);
        // list variable is intilaized by superclass constructor
        columns = getColumns(locale, list);
    }

    /**
     * Creates new ResourceListTableModel instance.
     * 
     * @param list a list of the resources.
     * @param locale the locale to be used by comparators.
     * @throws TableException if there is a problem creating the model.
     */
    public ResourceListTableModel(List<T> list, Locale locale)
        throws TableException
    {
        super((List<T>)list, (TableColumn<T>[])null);
        columns = getColumns(locale, list);
    }

    /**
     * Returns table models.
     * 
     * @param locale the locale to be used by comparators.
     * @return array of table columns.
     * @throws TableException if there is a problem crating column objects.
     */
    protected TableColumn<T>[] getColumns(Locale locale, List<T> list)
        throws TableException
    {
        @SuppressWarnings("unchecked")
        TableColumn<T>[] columns = new TableColumn[9];
        // add generic Resource columns

        // Subject name comparator columns
        columns[0] = new TableColumn<T>("creator.name", new CreatorNameComparator<T>(locale));
        columns[1] = new TableColumn<T>("modifier.name", new ModifierNameComparator<T>(locale));
        columns[2] = new TableColumn<T>("owner.name", new OwnerNameComparator<T>(locale));

        // Time comparator columns
        columns[3] = new TableColumn<T>("creation.time", new CreationTimeComparator<T>());
        columns[4] = new TableColumn<T>("modification.time", new ModificationTimeComparator<T>());

        // Name comparator
        columns[5] = new TableColumn<T>("name", new NameComparator<T>(locale));
        // Path comparator
        columns[6] = new TableColumn<T>("path", new PathComparator<T>(locale));

        // Id comparator
        columns[7] = new TableColumn<T>("id", new IdComparator<T>());

        // "Unsorted" comparator
        columns[8] = new TableColumn<T>("unsorted", new ListPositionComparator<T>(list));
        return columns;
    }

    /**
     * Returns the model dependent object by its id.
     * 
     * @param id the id of the object
     * @return model object
     */
    public T getObject(String id)
    {
        if(resourcesById == null)
        {
            resourcesById = new HashMap<String, T>();
            for(Iterator<T> i = list.iterator(); i.hasNext();)
            {
                T res = i.next();
                resourcesById.put(res.getIdString(), res);
            }
        }
        return resourcesById.get(id);
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
        return ((Resource)child).getIdString();
    }

    @Override
    @SuppressWarnings("unchecked")
    public T[] getChildren(T parent)
    {
        if(childrenByParentId == null)
        {
            childrenByParentId = new HashMap<>();
        }
        T[] children = childrenByParentId.get(parent);
        if(children == null)
        {
            children = (T[])findChildren(parent);
            childrenByParentId.put(parent, children);
        }
        return children;
    }

    private Resource[] findChildren(Resource parent)
    {
        ArrayList<T> children = new ArrayList<>();
        if(parent == null)
        {
            for(Map.Entry<String, T> entry : resourcesById.entrySet())
            {
                if(entry.getValue().getParent().getId() == CoralStore.ROOT_RESOURCE
                    || getObject(entry.getValue().getParent().getIdString()) == null)
                {
                    children.add(entry.getValue());
                }
            }
        }
        else
        {
            for(Map.Entry<String, T> entry : resourcesById.entrySet())
            {
                if(entry.getValue().getParent() != null
                    && entry.getValue().getParent().getId() == parent.getId())
                {
                    children.add(entry.getValue());
                }
            }
        }
        return children.toArray(new Resource[children.size()]);
    }
}
