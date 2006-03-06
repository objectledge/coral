package org.objectledge.coral.modules.components.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.security.Permission;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.table.comparator.NameComparator;
import org.objectledge.i18n.I18nContext;
import org.objectledge.parameters.Parameters;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.table.TableColumn;
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
public class PermissionList extends BaseBrowserComponent
{
    
    
    public PermissionList(Context context, Logger logger, TableStateManager tableStateManager)
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
            Permission[] permissions = coralSession.getSecurity().getPermission();
            TableModel model = new ListTableModel(permissions, columns);
            TableTool helper = new TableTool(state, null, model);
            templatingContext.put("table", helper);
        }
        catch (Exception e)
        {
            throw new ProcessingException("Cannot create TableTool", e);
        }
    }
}
