package org.objectledge.coral.modules.components.browser;

import java.util.ArrayList;
import java.util.List;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.schema.ResourceClass;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.table.comparator.NameComparator;
import org.objectledge.i18n.I18nContext;
import org.objectledge.parameters.Parameters;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.table.TableColumn;
import org.objectledge.table.TableException;
import org.objectledge.table.TableModel;
import org.objectledge.table.TableState;
import org.objectledge.table.TableStateManager;
import org.objectledge.table.TableTool;
import org.objectledge.table.generic.ListTableModel;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.web.mvc.MVCContext;

/**
 * The base browse component class.
 */
public class ResourceClassList extends BaseBrowserComponent
{
    public ResourceClassList(Context context, Logger logger, TableStateManager tableStateManager)
    {
        super(context, logger, tableStateManager);
    }
    
    public void process(Parameters parameters, TemplatingContext templatingContext, 
        MVCContext mvcContext, CoralSession coralSession)
        throws ProcessingException
    {
        try
        {
            I18nContext i18nContext = I18nContext.getI18nContext(context);
            TableColumn[] columns = new TableColumn[1];
            columns[0] = new TableColumn("name", new NameComparator(i18nContext.getLocale()));
            TableState state = tableStateManager.getState(context, this.getClass().getName());
            if (state.isNew())
            {
                state.setTreeView(false);
                state.setPageSize(0);
                state.setSortColumnName("name");
            }
            List<ResourceClass<?>> classes = new ArrayList<ResourceClass<?>>(coralSession
                            .getSchema().getAllResourceClasses().unmodifiableSet());
            TableModel<ResourceClass<?>> model = new ListTableModel<ResourceClass<?>>(classes, columns);
            TableTool<ResourceClass<?>> helper = new TableTool<ResourceClass<?>>(state, null, model);
            templatingContext.put("table", helper);
        }
        catch (TableException e)
        {
            throw new ProcessingException("Cannot create TableTool", e);
        }
    }
}
