package org.objectledge.coral.modules.components;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.CoralSessionFactory;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.table.ClassNameComparator;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.table.TableColumn;
import org.objectledge.table.TableException;
import org.objectledge.table.TableModel;
import org.objectledge.table.TableState;
import org.objectledge.table.TableStateManager;
import org.objectledge.table.TableTool;
import org.objectledge.table.generic.ListTableModel;
import org.objectledge.web.mvc.MVCContext;


/**
 * The base browse component class.
 */
public class ResourceClassList extends BaseBrowserComponent
{
    public ResourceClassList(Logger logger, CoralSessionFactory sessionFactory, TableStateManager tableStateManager)
    {
        super(logger, sessionFactory, tableStateManager);
    }

    public void process(Context context) throws ProcessingException
    {
        try
        {
            MVCContext mvcContext = MVCContext.getMVCContext(context);
            TableColumn[] columns = new TableColumn[1];
            columns[0] = new TableColumn("name", new ClassNameComparator(mvcContext.getLocale()));
            TableState state = tableStateManager.getState(context, "arlbrowse:components:resource_class_list");
            if (state.isNew())
            {
                state.setTreeView(false);
                state.setPageSize(0);
                state.setSortColumnName("name");
            }
            ResourceClass[] classes = coralSession.getSchema().getResourceClass();
            TableModel model = new ListTableModel(classes, columns);
            TableTool helper = new TableTool(state, model);
            templatingContext.put("table", helper);
        }
        catch (TableException e)
        {
            throw new ProcessingException("Cannot create TableTool", e);
        }
        finally
        {
            coralSession.close();
        }
    }
}
