package org.objectledge.coral.table;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
 * @version $Id: ResourceListTableModel.java,v 1.11 2008-10-21 14:44:24 rafal Exp $
 */
public class ResourceListTableModel extends ListTableModel<Resource>
{
    /** resources keyed by their id */
    private Map<String, Resource> resourcesById;

    /**
     * Creates new ResourceListTableModel instance.
     * 
     * @param array an array of Resources
     * @param locale the locale to be used by comparators.
     * @throws TableException if there is a problem creating the model.
     */
    public ResourceListTableModel(Resource[] array, Locale locale)
        throws TableException
    {
        super(array, (TableColumn<Resource>[])null);
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
    public ResourceListTableModel(List<Resource> list, Locale locale)
        throws TableException
    {
        super(list, (TableColumn<Resource>[])null);
        columns = getColumns(locale, list);
    }

    /**
     * Returns table models.
     * 
     * @param locale the locale to be used by comparators.
     * @return array of table columns.
     * @throws TableException if there is a problem crating column objects. 
     */
    protected TableColumn<Resource>[] getColumns(Locale locale, List<Resource> list)
        throws TableException
    {
        TableColumn<Resource>[] columns = new TableColumn[9];
        // add generic Resource columns

        // Subject name comparator columns
        columns[0] = new TableColumn<Resource>("creator.name", new CreatorNameComparator(locale));
        columns[1] = new TableColumn<Resource>("modifier.name", new ModifierNameComparator(locale));
        columns[2] = new TableColumn<Resource>("owner.name", new OwnerNameComparator(locale));

        // Time comparator columns
        columns[3] = new TableColumn<Resource>("creation.time", new CreationTimeComparator());
        columns[4] = new TableColumn<Resource>("modification.time", new ModificationTimeComparator());

        // Name comparator
        columns[5] = new TableColumn<Resource>("name", new NameComparator<Resource>(locale));
        // Path comparator
        columns[6] = new TableColumn<Resource>("path", new PathComparator(locale));

        // Id comparator
        columns[7] = new TableColumn<Resource>("id", new IdComparator<Resource>());
        
        // "Unsorted" comparator
        columns[8] = new TableColumn<Resource>("unsorted", new ListPositionComparator<Resource>(list));
        return columns;
    }

    /**
     * Returns the model dependent object by its id.
     *
     * @param id the id of the object
     * @return model object
     */
    public Resource getObject(String id)
    {
        if(resourcesById == null)
        {
            resourcesById = new HashMap<String, Resource>();
            for(Iterator<Resource> i = list.iterator(); i.hasNext();)
            {
                Resource res = (Resource)(i.next());
                resourcesById.put(res.getIdString(), res);
            }
        }
        return resourcesById.get(id);
    }
    
    /**
     * Returns the id of the object.
     * @param parent the id of the parent object.
     * @param child model object.
     *
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
}
