package org.objectledge.coral.modules.components.browser;

import org.jcontainer.dna.Logger;
import org.objectledge.context.Context;
import org.objectledge.coral.session.CoralSession;
import org.objectledge.coral.table.CoralTableModel;
import org.objectledge.i18n.I18nContext;
import org.objectledge.parameters.Parameters;
import org.objectledge.pipeline.ProcessingException;
import org.objectledge.table.TableModel;
import org.objectledge.table.TableState;
import org.objectledge.table.TableStateManager;
import org.objectledge.table.TableTool;
import org.objectledge.templating.TemplatingContext;
import org.objectledge.web.mvc.MVCContext;

/**
 * The base browse component class.
 */
public class ResourceTree
    extends BaseBrowserComponent
{
    public ResourceTree(Context context, Logger logger, TableStateManager tableStateManager)
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
            TableState state = tableStateManager.getState(context, this.getClass().getName());
            if(state.isNew())
            {
                state.setTreeView(true);
                String rootId = Long.toString(1);
                state.setRootId(rootId);
                state.setCurrentPage(0);
                state.setShowRoot(true);
                state.setExpanded(rootId);
                state.setPageSize(0);
                state.setSortColumnName("name");
            }
            TableModel model = new CoralTableModel(coralSession, i18nContext.getLocale());
            TableTool helper = new TableTool(state, null, model);
            templatingContext.put("table", helper);
        }
        catch(Exception e)
        {
            throw new ProcessingException("Cannot create TableTool", e);
        }
    }
}
