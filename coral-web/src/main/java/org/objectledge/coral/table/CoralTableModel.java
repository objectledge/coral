package org.objectledge.coral.table;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import org.jcontainer.dna.Logger;
import org.objectledge.coral.entity.EntityDoesNotExistException;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.store.Resource;
import org.objectledge.coral.table.comparator.*;
import org.objectledge.table.ExtendedTableModel;
import org.objectledge.table.TableColumn;
import org.objectledge.table.TableException;
import org.objectledge.table.TableRowSet;
import org.objectledge.table.TableState;
import org.objectledge.table.generic.GenericListRowSet;
import org.objectledge.table.generic.GenericTreeRowSet;

/**
 * Implementation of Table service based on ARL service
 *
 * @author <a href="mailto:pablo@caltha.pl">Pawel Potempski</a>
 * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
 * @version $Id: CoralTableModel.java,v 1.3 2004-04-22 12:56:23 zwierzem Exp $
 */
public class CoralTableModel implements ExtendedTableModel
{
    /** logging */
    protected Logger logger;

    /** coral session */
    protected CoralSession coralSession;

    protected HashMap comparatorByColumnName = new HashMap();

    public CoralTableModel(CoralSession coralSession, Logger logger, Locale locale)
    {
        this.logger = logger;
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
     * @return table of children
     */
    public TableRowSet getRowSet(TableState state)
    {
        if(state.getTreeView())
        {
            return new GenericTreeRowSet(state, this);
        }
        else
        {
            return new GenericListRowSet(state, this);   
        }
    }

    /**
     * Returns array of column definitions. They are created on every call,
     * because they can get modified durig it's lifecycle.
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
                throw new RuntimeException("Problem creating a column object: "+e.getMessage());
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
            logger.error("Coral Exception ",e);
        }
        return resource;
    }

    /**
     * Returns the id of the object.
     *
     * @param object model object.
     * @return the id of the object.
     */
    public String getId(Object object)
    {
        if(object == null)
        {
            return "-1";
        }
        return Long.toString(((Resource)object).getId());
    }
}
