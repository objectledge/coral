package org.objectledge.coral.table;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jcontainer.dna.Logger;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.table.comparator.*;
import org.objectledge.table.TableColumn;
import org.objectledge.table.TableException;
import org.objectledge.table.generic.ListTableModel;

/**
 * Implementation of Table Model for lists of ARL resources.
 *
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: ResourceListTableModel.java,v 1.3 2004-06-14 12:03:29 fil Exp $
 */
public class ResourceListTableModel extends ListTableModel
{
    /** logging facility */
    protected Logger logger;
    
    /** resources keyed by their id */
    private Map resourcesById;

    public ResourceListTableModel(Resource[] array, Locale locale, Logger logger)
        throws TableException
    {
        super(array, null);
        columns = getColumns(locale);
        this.logger = logger;
    }
        
    public ResourceListTableModel(List list, Locale locale, Logger logger)
        throws TableException
    {
        super(list, null);
        columns = getColumns(locale);
        this.logger = logger;
    }

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
            resourcesById = new HashMap();
            for(Iterator i = list.iterator(); i.hasNext();)
            {
                Resource res = (Resource)(i.next());
                resourcesById.put(Long.toString(res.getId()), res);
            }
        }
        return resourcesById.get(id);
    }
    
    /**
     * Returns the id of the object.
     * @param child model object.
     *
     * @return the id of the object.
     */
    public String getId(Object parent, Object child)
    {
        if(child == null)
        {
            return "-1";
        }
        return Long.toString(((Resource)child).getId());
    }
}
