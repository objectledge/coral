package org.objectledge.coral.modules.components;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.CoralSessionFactory;
import org.objectledge.coral.security.Permission;
import org.objectledge.coral.table.PermissionNameComparator;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.table.TableColumn;
import org.objectledge.table.TableModel;
import org.objectledge.table.TableState;
import org.objectledge.table.TableStateManager;
import org.objectledge.table.TableTool;
import org.objectledge.table.generic.ListTableModel;
import org.objectledge.web.mvc.MVCContext;

/**
 * The base browse component class.
 */
public class PermissionList extends BaseBrowserComponent
{
    public PermissionList(Logger logger, CoralSessionFactory sessionFactory,
                          TableStateManager tableStateManager)
    {
        super(logger, sessionFactory, tableStateManager);
    }
    
    public void process(Context context) throws ProcessingException
    {
        try
        {
            TableColumn[] columns = new TableColumn[1];
            MVCContext mvcContext = MVCContext.getMVCContext(context);
            columns[0] = new TableColumn("name", new PermissionNameComparator(mvcContext.getLocale()));
            TableState state = tableStateManager.getState(context, "coral:components:permission_list");
            if (state.isNew())
            {
                state.setTreeView(false);
                state.setPageSize(0);
                state.setSortColumnName("name");
            }
            Permission[] permissions = coralSession.getSecurity().getPermission();
            TableModel model = new ListTableModel(permissions, columns);
            TableTool helper = new TableTool(state, model);
            templatingContext.put("table", helper);
        }
        catch (Exception e)
        {
            throw new ProcessingException("Cannot create TableTool", e);
        }
        finally
        {
            coralSession.close();
        }
    }
}
