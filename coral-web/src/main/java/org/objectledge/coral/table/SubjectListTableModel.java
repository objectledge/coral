package org.objectledge.coral.table;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jcontainer.dna.Logger;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.table.comparator.IdComparator;
import org.objectledge.coral.table.comparator.NameComparator;
import org.objectledge.table.TableColumn;
import org.objectledge.table.TableException;
import org.objectledge.table.generic.ListTableModel;

/**
 * Implementation of Table Model for lists of ARL resources.
 *
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: SubjectListTableModel.java,v 1.1 2004-07-14 17:31:05 pablo Exp $
 */
public class SubjectListTableModel extends ListTableModel
{
    /** logging facility */
    protected Logger logger;
    
    /** subjects keyed by their id */
    private Map subjectsById;

    public SubjectListTableModel(Subject[] array, Locale locale, Logger logger)
        throws TableException
    {
        super(array, null);
        columns = getColumns(locale);
        this.logger = logger;
    }
        
    public SubjectListTableModel(List list, Locale locale, Logger logger)
        throws TableException
    {
        super(list, null);
        columns = getColumns(locale);
        this.logger = logger;
    }

    protected TableColumn[] getColumns(Locale locale)
        throws TableException
    {
        TableColumn[] columns = new TableColumn[2];
        // Name comparator
        columns[0] = new TableColumn("name", new NameComparator(locale));
        // Id comparator
        columns[1] = new TableColumn("id", new IdComparator());
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
        if(subjectsById == null)
        {
            subjectsById = new HashMap();
            for(Iterator i = list.iterator(); i.hasNext();)
            {
                Subject res = (Subject)(i.next());
                subjectsById.put(Long.toString(res.getId()), res);
            }
        }
        return subjectsById.get(id);
    }
    
    /**
     * Returns the id of the object.
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
        return Long.toString(((Subject)child).getId());
    }
}
