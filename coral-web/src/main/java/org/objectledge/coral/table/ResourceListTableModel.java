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
import org.objectledge.table.generic.ListTableModel;

/**
 * Implementation of Table Model for lists of ARL resources.
 *
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: ResourceListTableModel.java,v 1.8 2005-02-21 14:04:39 rafal Exp $
 */
public class ResourceListTableModel extends ListTableModel
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
        super(array, null);
        columns = getColumns(locale);
    }

    /**
     * Creates new ResourceListTableModel instance.
     * 
     * @param list a list of the resources.
     * @param locale the locale to be used by comparators.
     * @throws TableException if there is a problem creating the model.
     */
    public ResourceListTableModel(List list, Locale locale)
        throws TableException
    {
        super(list, null);
        columns = getColumns(locale);
    }

    /**
     * Returns table models.
     * 
     * @param locale the locale to be used by comparators.
     * @return array of table columns.
     * @throws TableException if there is a problem crating column objects. 
     */
    protected TableColumn[] getColumns(Locale locale)
        throws TableException
    {
        TableColumn[] columns = new TableColumn[8];
        // add generic Resource columns

        // Subject name comparator columns
        columns[0] = new TableColumn("creator.name", new CreatorNameComparator(locale));
        columns[1] = new TableColumn("modifier.name", new ModifierNameComparator(locale));
        columns[2] = new TableColumn("owner.name", new OwnerNameComparator(locale));

        // Time comparator columns
        columns[3] = new TableColumn("creation.time", new CreationTimeComparator());
        columns[4] = new TableColumn("modification.time", new ModificationTimeComparator());

        // Name comparator
        columns[5] = new TableColumn("name", new NameComparator(locale));
        // Path comparator
        columns[6] = new TableColumn("path", new PathComparator(locale));

        // Id comparator
        columns[7] = new TableColumn("id", new IdComparator());
        return columns;
    }

    /**
     * Returns the model dependent object by its id.
     *
     * @param id the id of the object
     * @return model object
     */
    public Object getObject(String id)
    {
        if(resourcesById == null)
        {
            resourcesById = new HashMap<String, Resource>();
            for(Iterator i = list.iterator(); i.hasNext();)
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
    public String getId(String parent, Object child)
    {
        if(child == null)
        {
            return "-1";
        }
        return ((Resource)child).getIdString();
    }
}
