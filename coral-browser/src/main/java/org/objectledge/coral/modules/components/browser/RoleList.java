package org.objectledge.coral.modules.components.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.security.Role;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.table.comparator.RoleNameComparator;
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
public class RoleList
    extends BaseBrowserComponent
{
    public RoleList(Context context, Logger logger, TableStateManager tableStateManager)
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
			columns[0] = new TableColumn("name", new RoleNameComparator(i18nContext.getLocale()));
            TableState state = tableStateManager.getState(context, this.getClass().getName());
            if(state.isNew())
            {
                state.setTreeView(false);
                state.setPageSize(0);
				state.setSortColumnName("name");
            }
            Role[] roles = coralSession.getSecurity().getRole();
            TableModel model = new ListTableModel(roles, columns);
            TableTool helper = new TableTool(state, null, model);
            templatingContext.put("table", helper);
        }
        catch(TableException e)
        {
            throw new ProcessingException("Cannot create TableTool", e);
        }
    }
}
