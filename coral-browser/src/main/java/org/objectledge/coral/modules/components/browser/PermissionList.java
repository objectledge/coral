package org.objectledge.coral.modules.components.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.security.Permission;
import org.objectledge.coral.session.CoralSessionFactory;
import org.objectledge.coral.table.comparator.PermissionNameComparator;
import org.objectledge.i18n.I18nContext;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.table.TableColumn;
import org.objectledge.table.TableModel;
import org.objectledge.table.TableState;
import org.objectledge.table.TableStateManager;
import org.objectledge.table.TableTool;
import org.objectledge.table.generic.ListTableModel;

/**
 * The base browse component class.
 */
public class PermissionList extends BaseBrowserComponent
{
    public PermissionList(Context context, Logger logger, CoralSessionFactory sessionFactory,
                          TableStateManager tableStateManager)
    {
        super(context, logger, sessionFactory, tableStateManager);
    }
    
    public void process(Context context) throws ProcessingException
    {
        try
        {
            I18nContext i18nContext = I18nContext.getI18nContext(context);
            TableColumn[] columns = new TableColumn[1];
            columns[0] = new TableColumn("name", new PermissionNameComparator(i18nContext.getLocale()));
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
