package org.objectledge.coral.modules.views.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.table.ClassNameComparator;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.table.TableColumn;
import org.objectledge.table.TableModel;
import org.objectledge.table.TableState;
import org.objectledge.table.TableStateManager;
import org.objectledge.table.TableTool;
import org.objectledge.table.generic.ListTableModel;
import org.objectledge.web.mvc.MVCContext;

/**
 * The choose resource class screen.
 */
public class ChooseResourceClass extends BaseBrowserView
{
    public ChooseResourceClass(Context context, Logger logger, CoralSessionFactory sessionFactory,
                                TableStateManager tableStateManager)
    {
        super(context, logger, sessionFactory, tableStateManager);
    }

    public void process(Context context) throws ProcessingException
    {
        try
        {
            MVCContext mvcContext = MVCContext.getMVCContext(context);
            TableColumn[] columns = new TableColumn[1];
            columns[0] = new TableColumn("name", new ClassNameComparator(mvcContext.getLocale()));
            TableState state = tableStateManager.getState(context, "coral:screens:choose_resource_class");
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
        catch (Exception e)
        {
            throw new ProcessingException("Cannot create TableTool", e);
        }
    }
}
