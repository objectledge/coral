package org.objectledge.coral.table;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
 * @version $Id: SubjectListTableModel.java,v 1.5 2005-02-21 14:04:39 rafal Exp $
 */
public class SubjectListTableModel extends ListTableModel
{
    /** subjects keyed by their id */
    private Map<String, Subject> subjectsById;

    /**
     * Creates new SubjectListTableModel instance.
     * 
     * @param array an array of Subjects.
     * @param locale the locale to be used by comparators.
     * @throws TableException if there is a problem creating the model.
     */
    public SubjectListTableModel(Subject[] array, Locale locale)
        throws TableException
    {
        super(array, null);
        columns = getColumns(locale);
    }
        
    /**
     * Creates new SubjectListTableModel instance.
     * 
     * @param list a list of Subjects.
     * @param locale the locale to be used by comparators.
     * @throws TableException if there is a problem creating the model.
     */
    public SubjectListTableModel(List list, Locale locale)
        throws TableException
    {
        super(list, null);
        columns = getColumns(locale);
    }

    /**
     * Return model columns.
     * 
     * @param locale the locale to be used by comparators.
     * @return an array of column objects.
     * @throws TableException if there is a problem creating column objects.
     */
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
            subjectsById = new HashMap<String, Subject>();
            for(Iterator i = list.iterator(); i.hasNext();)
            {
                Subject subject = (Subject)(i.next());
                subjectsById.put(subject.getIdString(), subject);
            }
        }
        return subjectsById.get(id);
    }
    
    /**
     * Returns the id of the object.
     * 
     * @param parent the id of the parent objet.
     * @param child model object.
     * @return the id of the object.
     */
    public String getId(String parent, Object child)
    {
        if(child == null)
        {
            return "-1";
        }
        return ((Subject)child).getIdString();
    }
}
