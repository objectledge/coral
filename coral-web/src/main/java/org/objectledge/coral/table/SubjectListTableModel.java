package org.objectledge.coral.table;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.objectledge.authentication.UserManager;
import org.objectledge.coral.security.Subject;
import org.objectledge.coral.table.comparator.IdComparator;
import org.objectledge.coral.table.comparator.NameComparator;
import org.objectledge.coral.table.comparator.PersonalDataComparator;
import org.objectledge.table.TableColumn;
import org.objectledge.table.TableException;
import org.objectledge.table.generic.ListTableModel;

/**
 * Implementation of Table Model for lists of ARL resources.
 *
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: SubjectListTableModel.java,v 1.6 2007-02-27 21:40:41 rafal Exp $
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
     * @param properties the names of personal data properties to use.
     * @param userManager the user manager component, may be null if properties is empty.
     * @throws TableException if there is a problem creating the model.
     */
    public SubjectListTableModel(Subject[] array, Locale locale, String[] properties, UserManager userManager)
        throws TableException
    {
        super(array, null);
        columns = getColumns(locale, properties, userManager);
    }
        
    /**
     * Creates new SubjectListTableModel instance.
     * 
     * @param list a list of Subjects.
     * @param locale the locale to be used by comparators.
     * @param properties the names of personal data properties to use.
     * @param userManager the user manager component, may be null if properties is empty.
     * @throws TableException if there is a problem creating the model.
     */
    public SubjectListTableModel(List list, Locale locale, String[] properties, UserManager userManager)
        throws TableException
    {
        super(list, null);
        columns = getColumns(locale, properties, userManager);
    }

    /**
     * Return model columns.
     * 
     * @param locale the locale to be used by comparators.
     * @param properties TODO
     * @param userManager TODO
     * @return an array of column objects.
     * @throws TableException if there is a problem creating column objects.
     */
    protected TableColumn[] getColumns(Locale locale, String[] properties, UserManager userManager)
        throws TableException
    {
        TableColumn[] columns = new TableColumn[2 + properties.length];
        columns[0] = new TableColumn("dn", new NameComparator(locale));
        columns[1] = new TableColumn("id", new IdComparator());
        int i = 2;
        for(String property : properties)
        {
            Comparator comparator = new PersonalDataComparator(userManager, property, locale);
            columns[i++] = new TableColumn(property, comparator);
        }
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
